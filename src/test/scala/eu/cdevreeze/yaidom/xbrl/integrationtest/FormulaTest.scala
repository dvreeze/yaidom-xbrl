/*
 * Copyright 2011 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.yaidom.xbrl.integrationtest

import java.io.File

import scala.BigInt
import scala.Vector
import scala.collection.immutable
import scala.math.abs

import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.Suite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.xbrl.aspects.AspectQueryApi
import eu.cdevreeze.yaidom.bridge.DefaultDocawareBridgeElem
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.docaware
import eu.cdevreeze.yaidom.parse.DocumentParserUsingSax
import eu.cdevreeze.yaidom.queryapi.HasENameApi.withLocalName
import eu.cdevreeze.yaidom.simple.Document
import eu.cdevreeze.yaidom.xbrl.ItemFact
import eu.cdevreeze.yaidom.xbrl.XbrlInstanceDocument

/**
 * Formula test, using the formula conformance suite test data. This test emulates the formulas in Scala code,
 * instead of processing the formulas themselves. The idea is that the formulas are represented (in an ad-hoc
 * manner, admittedly) in Scala code, with a much better signal-to-noise ratio.
 *
 * One of the shortcuts applied is that the Scala "formulas" do not apply any aspects, but match on contextRefs
 * and (if numeric) unitRefs. An interesting question is: what assumptions have to be made in order for this
 * simplified matching to be correct?
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class FormulaTest extends Suite {

  private val docParser = DocumentParserUsingSax.newInstance

  /**
   * Tests the 0019 Value Assertion, written in ad-hoc Scala code.
   *
   * This is example 7.1 in http://www.xbrl.org/wgn/xbrl-formula-overview/pwd-2011-12-21/xbrl-formula-overview-wgn-pwd-2011-12-21.html.
   */
  @Test def testValueAssertion(): Unit = {
    val xbrlInstanceDoc =
      getXbrlInstanceDoc(new File(classOf[FormulaTest].getResource("/conformance-formula/examples/0019 Value Assertion/instance.xml").toURI))

    // Missing taxonomy model!
    val aspectQueryApi = new AspectQueryApi(xbrlInstanceDoc.xbrlInstance, null)
    import aspectQueryApi._

    val tns = "http://xbrl.org/formula/conformance/example"
    val netIncomesEName = EName(tns, "NetIncomes")
    val grossIncomesEName = EName(tns, "GrossIncomes")

    val results: immutable.IndexedSeq[Boolean] =
      for {
        netIncomes <- xbrlInstanceDoc.xbrlInstance.allTopLevelNumericItemsByEName.getOrElse(netIncomesEName, Vector())
        grossIncomes <- xbrlInstanceDoc.xbrlInstance.allTopLevelNumericItemsByEName.getOrElse(grossIncomesEName, Vector())
        if matchOnLocation(netIncomes, grossIncomes) &&
          matchOnEntityIdentifier(netIncomes, grossIncomes) &&
          matchOnPeriod(netIncomes, grossIncomes) &&
          matchOnNonXdtSegmentContent(netIncomes, grossIncomes) &&
          matchOnNonXdtScenarioContent(netIncomes, grossIncomes) &&
          matchOnSegmentExplicitDimensions(netIncomes, grossIncomes) &&
          matchOnScenarioExplicitDimensions(netIncomes, grossIncomes) &&
          matchOnUnit(netIncomes, grossIncomes)
      } yield {
        valueAssertionIteration(netIncomes, grossIncomes)
      }

    assertResult(2) {
      results.size
    }
    assertResult(1) {
      results.filter(Set(true)).size
    }
  }

  /**
   * Tests the 0015 Movement Pattern, written in ad-hoc Scala code.
   *
   * This is example 7.2 in http://www.xbrl.org/wgn/xbrl-formula-overview/pwd-2011-12-21/xbrl-formula-overview-wgn-pwd-2011-12-21.html.
   */
  @Test def testMovementPattern(): Unit = {
    val xbrlInstanceDoc =
      getXbrlInstanceDoc(new File(classOf[FormulaTest].getResource("/conformance-formula/examples/0015 Movement Pattern/instance-value-assertion.xml").toURI))

    val tns = "http://xbrl.org/formula/conformance/example"
    val balanceEName = EName(tns, "balance")
    val changesEName = EName(tns, "changes")

    // Emulating instantDuration filters. Per loop iteration, there are 2 different balance variables involved.

    def matchOnPeriodStart(startBalance: ItemFact, changes: ItemFact): Boolean = {
      val startBalanceContext = xbrlInstanceDoc.xbrlInstance.allContextsById(startBalance.contextRef)
      val changesContext = xbrlInstanceDoc.xbrlInstance.allContextsById(changes.contextRef)

      require(startBalanceContext.period.isInstant)
      require(changesContext.period.isFiniteDuration)

      val instant = new LocalDate(startBalanceContext.period.getChildElem(withLocalName("instant")).text)
      val startDate = new LocalDate(changesContext.period.getChildElem(withLocalName("startDate")).text)

      instant.plusDays(1) == startDate
    }

    def matchOnPeriodEnd(endBalance: ItemFact, changes: ItemFact): Boolean = {
      val endBalanceContext = xbrlInstanceDoc.xbrlInstance.allContextsById(endBalance.contextRef)
      val changesContext = xbrlInstanceDoc.xbrlInstance.allContextsById(changes.contextRef)

      require(endBalanceContext.period.isInstant)
      require(changesContext.period.isFiniteDuration)

      val instant = new LocalDate(endBalanceContext.period.getChildElem(withLocalName("instant")).text)
      val endDate = new LocalDate(changesContext.period.getChildElem(withLocalName("endDate")).text)

      instant == endDate
    }

    val results: immutable.IndexedSeq[Boolean] =
      for {
        startBalance <- xbrlInstanceDoc.xbrlInstance.allTopLevelNumericItemsByEName.getOrElse(balanceEName, Vector())
        changes <- xbrlInstanceDoc.xbrlInstance.allTopLevelNumericItemsByEName.getOrElse(changesEName, Vector())
        if matchOnPeriodStart(startBalance, changes)
        endBalance <- xbrlInstanceDoc.xbrlInstance.allTopLevelNumericItemsByEName.getOrElse(balanceEName, Vector())
        if matchOnPeriodEnd(endBalance, changes)
      } yield {
        movementPatternIteration(startBalance, changes, endBalance)
      }

    assertResult(3) {
      results.size
    }
    assertResult(2) {
      results.filter(Set(true)).size
    }
  }

  private def getXbrlInstanceDoc(file: File): XbrlInstanceDocument = {
    val doc: Document = docParser.parse(file).withUriOption(Some(file.toURI))

    val xbrlInstanceDoc: XbrlInstanceDocument =
      new XbrlInstanceDocument(
        DefaultDocawareBridgeElem.wrap(docaware.Elem(doc.uriOption.get, doc.documentElement)))
    xbrlInstanceDoc
  }

  private def valueAssertionIteration(netIncomes: ItemFact, grossIncomes: ItemFact): Boolean = {
    val tns = "http://xbrl.org/formula/conformance/example"
    assert(netIncomes.resolvedName == EName(tns, "NetIncomes"))
    assert(grossIncomes.resolvedName == EName(tns, "GrossIncomes"))

    BigInt(netIncomes.text) <= BigInt(grossIncomes.text)
  }

  private def movementPatternIteration(startBalance: ItemFact, changes: ItemFact, endBalance: ItemFact): Boolean = {
    val tns = "http://xbrl.org/formula/conformance/example"
    assert(startBalance.resolvedName == EName(tns, "balance"))
    assert(changes.resolvedName == EName(tns, "changes"))
    assert(endBalance.resolvedName == EName(tns, "balance"))

    abs(startBalance.text.toDouble + changes.text.toDouble - endBalance.text.toDouble) < 1.toDouble
  }
}

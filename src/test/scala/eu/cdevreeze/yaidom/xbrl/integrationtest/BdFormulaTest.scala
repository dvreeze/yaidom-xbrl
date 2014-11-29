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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI

import scala.BigDecimal
import scala.collection.immutable

import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.Suite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.yaidom.bridge.DefaultDocawareBridgeElem
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.docaware
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingSax
import eu.cdevreeze.yaidom.print.DocumentPrinterUsingSax
import eu.cdevreeze.yaidom.queryapi.HasENameApi.withEName
import eu.cdevreeze.yaidom.simple
import eu.cdevreeze.yaidom.simple.Document
import eu.cdevreeze.yaidom.xbrl.ItemFact
import eu.cdevreeze.yaidom.xbrl.XbrlInstanceDocument
import eu.cdevreeze.yaidom.xbrl.saxon
import eu.cdevreeze.yaidom.xbrl.saxon.BridgeElemTakingSaxonElem
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.om.DocumentInfo
import net.sf.saxon.s9api.Processor

/**
 * BD formula test.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class BdFormulaTest extends Suite {

  private val pathToParentDir: java.io.File =
    (new java.io.File(classOf[BdFormulaTest].getResource("IHZ2013-01.xbrl").toURI)).getParentFile

  private val clazz = classOf[BdFormulaTest]

  /**
   * Tests the equivalent of formula Saldo_fiscale_winstberekening__volgens_vermogensvergelijking__Regel_3_173.xml.
   * It uses an indexed element.
   */
  @Test def testXbrlProcessingUsingIndexedElem(): Unit = {
    val docParser = DocumentParserUsingSax.newInstance

    val parentDir = new File(pathToParentDir.getPath)

    val doc: Document =
      docParser.parse(new File(parentDir, "IHZ2013-01.xbrl"))

    val editedElem = adaptElem(doc.documentElement)

    val xbrlInstanceDoc: XbrlInstanceDocument =
      new XbrlInstanceDocument(
        DefaultDocawareBridgeElem.wrap(docaware.Elem(doc.uriOption.getOrElse(new URI("")), editedElem)))

    testXbrlProcessing(xbrlInstanceDoc)
  }

  /**
   * Tests the equivalent of formula Saldo_fiscale_winstberekening__volgens_vermogensvergelijking__Regel_3_173.xml.
   * It uses a Saxon element.
   */
  @Test def testXbrlProcessingUsingSaxonElem(): Unit = {
    val docParser = DocumentParserUsingSax.newInstance

    val parentDir = new File(pathToParentDir.getPath)
    val f = new File(parentDir, "IHZ2013-01.xbrl")

    val doc: Document =
      docParser.parse(f)

    val editedElem = adaptElem(doc.documentElement)

    val docPrinter = DocumentPrinterUsingSax.newInstance

    val bos = new ByteArrayOutputStream
    docPrinter.print(editedElem, scala.io.Codec.UTF8.toString, bos)
    val bytes = bos.toByteArray

    val processor = new Processor(false)
    val docBuilder = processor.newDocumentBuilder()

    val is = new ByteArrayInputStream(bytes)

    val node = docBuilder.build(new StreamSource(is))

    val xbrlInstanceDoc: XbrlInstanceDocument =
      new XbrlInstanceDocument(
        BridgeElemTakingSaxonElem.wrap(new saxon.DomDocument(node.getUnderlyingNode.asInstanceOf[DocumentInfo]).documentElement))

    testXbrlProcessing(xbrlInstanceDoc)
  }

  private def testXbrlProcessing(xbrlInstanceDoc: XbrlInstanceDocument): Unit = {
    require {
      xbrlInstanceDoc.xbrlInstance.allTopLevelItems.size >= 20
    }

    require(xbrlInstanceDoc.xbrlInstance.findAllElemsOrSelf.map(_.scope).toSet.size == 1)

    val scope = xbrlInstanceDoc.xbrlInstance.scope
    import scope._

    // The "value assertion" itself

    val saldoVolgensVermogensVergelijkingFacts: immutable.IndexedSeq[ItemFact] = {
      val factsFilteredOnName =
        xbrlInstanceDoc.xbrlInstance.filterItems(withEName(QName("bd-bedr:BalanceProfitComparisonMethod").res))

      factsFilteredOnName filter { fact =>
        val context = xbrlInstanceDoc.xbrlInstance.allContextsById(fact.contextRef)
        context.filterElems(withEName(QName("xbrldi:explicitMember").res)) exists { elem =>
          (elem.attributeAsResolvedQName(EName("dimension")) == QName("bd-dim-dim:PartyDimension").res) &&
            (elem.textAsResolvedQName == QName("bd-dim-dom:Declarant").res)
        }
      }
    }

    val saldoFacts: immutable.IndexedSeq[ItemFact] = {
      val factsFilteredOnName =
        xbrlInstanceDoc.xbrlInstance.filterItems(withEName(QName("bd-bedr:BalanceProfitCalculationForTaxPurposesFiscal").res))

      factsFilteredOnName filter { fact =>
        val context = xbrlInstanceDoc.xbrlInstance.allContextsById(fact.contextRef)
        context.filterElems(withEName(QName("xbrldi:explicitMember").res)) exists { elem =>
          (elem.attributeAsResolvedQName(EName("dimension")) == QName("bd-dim-dim:PartyDimension").res) &&
            (elem.textAsResolvedQName == QName("bd-dim-dom:Declarant").res)
        }
      }
    }

    val varSetEvals =
      for {
        saldoVolgensVermogensVergelijking <- saldoVolgensVermogensVergelijkingFacts
        saldo <- saldoFacts
        // Very naive approximation of implicit filtering (matching on uncovered aspects)
        if saldoVolgensVermogensVergelijking.contextRef == saldo.contextRef
      } yield {
        // The value assertion test
        BigDecimal(saldoVolgensVermogensVergelijking.text.trim) == BigDecimal(saldo.text.trim)
      }

    assertResult(1) {
      varSetEvals.size
    }

    assertResult(true) {
      varSetEvals forall (_ == true)
    }
  }

  private def adaptElem(elem: simple.Elem): simple.Elem = {
    // Edit the document, updating bd-bedr:BalanceProfitCalculationForTaxPurposesFiscal with contextRef c1
    val paths = indexed.Elem(elem).filterElems(_.qname == QName("bd-bedr:BalanceProfitCalculationForTaxPurposesFiscal")).map(_.path)
    val editedElem = elem.updatedAtPaths(paths.toSet) {
      case (elem, path) =>
        elem.plusAttribute(QName("contextRef"), "c1")
    }
    editedElem
  }

  // TODO Totaal_vermogensverschil_Regel_1_203.xml
  // TODO Belastbare_winst_Regel_2_31.xml
  // TODO Identificatienummer_Regel_1_1723.xml
}

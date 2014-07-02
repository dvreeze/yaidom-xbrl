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

package eu.cdevreeze.yaidom
package xbrl
package integrationtest

import java.net.URI
import java.util.Properties
import java.io.File
import javax.xml.parsers._
import scala.collection.immutable
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import org.junit.{ Test, Before, Ignore }
import org.junit.runner.RunWith
import org.scalatest.{ Suite, BeforeAndAfterAll }
import org.scalatest.junit.JUnitRunner
import eu.cdevreeze.yaidom.ElemApi._
import eu.cdevreeze.yaidom.xbrl.xbrli.yaidomimpl.defaultimpl.XbrlInstanceDocument

/**
 * NL-FRIS validation test. It shows how yaidom's extensibility and yaidom-XBRL can help in precise and clear validation
 * of rules against XBRL instances.
 *
 * This code can also be used as basis for a first blog on yaidom-XBRL.
 *
 * Note that in this example, we only look at XBRL instances, without looking at the corresponding taxonomies, That, of course,
 * is a major simplification.
 *
 * The blog could first refer to preceding blogs about yaidom, next it could explain the basics of XBRL (instances), then it
 * could go on to build a yaidom wrapper for XBRL instances, and then it could show simple NL-FRIS and KVK-FRIS validation checks
 * using the yaidom-XBRL wrapper class for XBRL instances. Some queries could easily be written without the "XBRL instance"
 * wrapper, and some queries really profit from the wrapper. The wrapper can always be unwrapped to descend to the raw XML
 * level, of course. Much of the query API remains the same when doing so.
 *
 * Encourage the reader to play with Scala and yaidom in the REPL.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class NlFrisTest extends Suite {

  private val pathToParentDir: java.io.File =
    (new java.io.File(classOf[NlFrisTest].getResource("kvk-rpt-grote-rechtspersoon-geconsolideerd-model-b-e-indirect-2013.xbrl").toURI)).getParentFile

  private val clazz = classOf[NlFrisTest]

  /**
   * The code in this test can be copied to the content in the first article on yaidom XBRL processing.
   */
  @Test def testXbrlProcessing(): Unit = {
    // Using a yaidom DocumentParser that used SAX internally
    val docParser = parse.DocumentParserUsingSax.newInstance

    // Replace the following path!
    val parentDir = new File(pathToParentDir.getPath)

    val doc: Document =
      docParser.parse(new File(parentDir, "kvk-rpt-grote-rechtspersoon-geconsolideerd-model-b-e-indirect-2013.xbrl"))

    val xbrlInstanceDoc: XbrlInstanceDocument = new XbrlInstanceDocument(indexed.Document(doc))

    import ElemApi._

    require {
      xbrlInstanceDoc.xbrlInstance.allTopLevelItems.size >= 20
    }

    val bw2iNs =
      xbrlInstanceDoc.xbrlInstance.toElem.scope.prefixNamespaceMap("bw2-i")

    val entityFacts = xbrlInstanceDoc.xbrlInstance.filterFacts(withEName(EName(bw2iNs, "EntityName")))
    require(entityFacts.forall(e => !e.isTopLevel))

    val entityFactsInTuples =
      xbrlInstanceDoc.xbrlInstance.allTopLevelTuples.flatMap(e => e.filterFacts(withEName(EName(bw2iNs, "EntityName"))))

    assertResult(entityFacts) {
      entityFactsInTuples
    }

    // NL-FRIS 8.0 checks.

    assertResult(true) {
      hasLanguageInRootElem(xbrlInstanceDoc)
    }

    assertResult(true) {
      elementNamesOnlyUseReservedPrefixes(xbrlInstanceDoc)
    }

    assertResult(true) {
      hasNoCData(xbrlInstanceDoc)
    }

    assertResult(true) {
      hasNoXsiNilIsTrue(xbrlInstanceDoc)
    }

    assertResult(true) {
      hasAtMostOneSchemaRef(xbrlInstanceDoc)
    }

    assertResult(true) {
      hasNoLinkbaseRef(xbrlInstanceDoc)
    }

    assertResult(true) {
      hasNoUnusedContexts(xbrlInstanceDoc)
    }

    assertResult(true) {
      hasNoStartEndDateOverlaps(xbrlInstanceDoc)
    }

    assertResult(true) {
      hasNoPeriodWithTime(xbrlInstanceDoc)
    }

    assertResult(true) {
      hasNoPeriodForever(xbrlInstanceDoc)
    }

    assertResult(true) {
      hasNoFootnotes(xbrlInstanceDoc)
    }
  }

  /** Checks NL-FRIS 8.0, rule 2.1.1. */
  private def hasLanguageInRootElem(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    xbrlInstanceDoc.xbrlInstance.attributeOption(XmlLangEName).isDefined
  }

  /** Checks NL-FRIS 8.0, rule 2.1.2. */
  private def elementNamesOnlyUseReservedPrefixes(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    import scala.collection.JavaConverters._

    val props = new Properties
    props.load(clazz.getResourceAsStream("reserved-namespaces-and-prefixes.properties"))
    val namespacePrefixMap: Map[String, String] = props.asScala.toMap

    elementNamesOnlyUseReservedPrefixes(xbrlInstanceDoc, namespacePrefixMap)
  }

  /** Checks NL-FRIS 8.0, rule 2.1.2. */
  private def elementNamesOnlyUseReservedPrefixes(xbrlInstanceDoc: XbrlInstanceDocument, namespacePrefixMap: Map[String, String]): Boolean = {
    xbrlInstanceDoc.xbrlInstance.findAllElemsOrSelf forall { e =>
      val expectedPrefixOption = e.resolvedName.namespaceUriOption.flatMap(ns => namespacePrefixMap.get(ns))

      (expectedPrefixOption.isEmpty) || (e.toElem.qname.prefixOption == expectedPrefixOption)
    }
  }

  /** Checks NL-FRIS 8.0, rule 2.1.4. */
  private def hasNoCData(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    xbrlInstanceDoc.xbrlInstance.findAllElemsOrSelf.forall(e => !e.toElem.textChildren.exists(_.isCData))
  }

  /** Checks NL-FRIS 8.0, rule 2.1.5. */
  private def hasNoXsiNilIsTrue(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    xbrlInstanceDoc.xbrlInstance.findAllElemsOrSelf.forall(e => e.attributeOption(XsiNilEName) != Some("true"))
  }

  /** Checks NL-FRIS 8.0, rule 2.2.1. */
  private def hasAtMostOneSchemaRef(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    xbrlInstanceDoc.xbrlInstance.findAllSchemaRefs.size <= 1
  }

  /** Checks NL-FRIS 8.0, rule 2.2.1. */
  private def hasNoLinkbaseRef(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    xbrlInstanceDoc.xbrlInstance.findAllLinkbaseRefs.isEmpty
  }

  /** Checks NL-FRIS 8.0, rule 2.4.1. */
  private def hasNoUnusedContexts(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    val usedContextIds = xbrlInstanceDoc.xbrlInstance.findAllItems.map(_.contextRef).toSet

    val allContextIds = xbrlInstanceDoc.xbrlInstance.allContextsById.keySet

    val unusedContextIds = allContextIds diff usedContextIds
    unusedContextIds.isEmpty
  }

  /** Checks NL-FRIS 8.0, rule 2.5.1. */
  private def hasNoStartEndDateOverlaps(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    val dateFormatter = ISODateTimeFormat.date()

    val startDatesByContextId: Map[String, LocalDate] =
      xbrlInstanceDoc.xbrlInstance.allContextsById filter { case (id, ctx) => ctx.period.isFiniteDuration } mapValues { ctx =>
        val s = ctx.period.getChildElem(XbrliStartDateEName).wrappedElem.text
        dateFormatter.parseLocalDate(s)
      }

    val endDatesByContextId: Map[String, LocalDate] =
      xbrlInstanceDoc.xbrlInstance.allContextsById filter { case (id, ctx) => ctx.period.isFiniteDuration } mapValues { ctx =>
        val s = ctx.period.getChildElem(XbrliEndDateEName).wrappedElem.text
        dateFormatter.parseLocalDate(s)
      }

    val contextIdsByEndDates: Map[LocalDate, immutable.IndexedSeq[String]] =
      endDatesByContextId.toVector.groupBy(_._2) mapValues { case idDateSeq => idDateSeq.map(_._1) }

    val offendingStartDatesByContextId: Map[String, LocalDate] =
      startDatesByContextId filter {
        case (id, startDate) =>
          val idsWithSameEndDate = contextIdsByEndDates.getOrElse(startDate, Vector())
          !(idsWithSameEndDate.toSet - id).isEmpty
      }

    offendingStartDatesByContextId.isEmpty
  }

  /** Checks NL-FRIS 8.0, rule 2.5.2. */
  private def hasNoPeriodWithTime(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    xbrlInstanceDoc.xbrlInstance.allContexts.filter(e => !e.period.isForever) forall {
      case e: xbrli.XbrliContext if e.period.isInstant =>
        val instant = e.period.getChildElem(XbrliInstantEName).wrappedElem.text
        !instant.contains("T")
      case e: xbrli.XbrliContext if e.period.isFiniteDuration =>
        val startDate = e.period.getChildElem(XbrliStartDateEName).wrappedElem.text
        val endDate = e.period.getChildElem(XbrliEndDateEName).wrappedElem.text
        !startDate.contains("T") && !endDate.contains("T")
      case _ =>
        true
    }
  }

  /** Checks NL-FRIS 8.0, rule 2.5.3. */
  private def hasNoPeriodForever(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    xbrlInstanceDoc.xbrlInstance.allContexts.filter(_.period.isForever).isEmpty
  }

  /** Checks NL-FRIS 8.0, rule 2.9. */
  private def hasNoFootnotes(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    xbrlInstanceDoc.xbrlInstance.findAllFootnoteLinks.isEmpty
  }
}

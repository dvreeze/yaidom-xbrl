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
import java.net.URI
import java.util.Properties

import scala.Vector
import scala.collection.JavaConverters.propertiesAsScalaMapConverter
import scala.collection.immutable

import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.Suite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.yaidom.bridge.DefaultDocawareBridgeElem
import eu.cdevreeze.yaidom.docaware
import eu.cdevreeze.yaidom.parse.DocumentParserUsingSax
import eu.cdevreeze.yaidom.queryapi.HasENameApi.ToHasElemApi
import eu.cdevreeze.yaidom.queryapi.HasENameApi.withEName
import eu.cdevreeze.yaidom.simple.Document
import eu.cdevreeze.yaidom.xbrl.XbrlInstanceDocument
import eu.cdevreeze.yaidom.xbrl.XbrliContext
import eu.cdevreeze.yaidom.xbrl.XbrliEndDateEName
import eu.cdevreeze.yaidom.xbrl.XbrliInstantEName
import eu.cdevreeze.yaidom.xbrl.XbrliStartDateEName
import eu.cdevreeze.yaidom.xbrl.XmlLangEName
import eu.cdevreeze.yaidom.xbrl.XsiNilEName
import eu.cdevreeze.yaidom.xbrl.saxon
import eu.cdevreeze.yaidom.xbrl.saxon.BridgeElemTakingSaxonElem
import net.sf.saxon.om.DocumentInfo
import net.sf.saxon.s9api.Processor

/**
 * NL-FRIS validation test. It shows how yaidom's extensibility and yaidom-XBRL can help in precise and clear validation
 * of rules against XBRL instances.
 *
 * Note that in this example, we only look at XBRL instances, without looking at the corresponding taxonomies, That, of course,
 * is a major simplification.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class NlFrisTest extends Suite {

  private val pathToParentDir: java.io.File =
    (new java.io.File(classOf[NlFrisTest].getResource("kvk-rpt-grote-rechtspersoon-geconsolideerd-model-b-e-indirect-2013.xbrl").toURI)).getParentFile

  private val clazz = classOf[NlFrisTest]

  @Test def testXbrlProcessingUsingIndexedElem(): Unit = {
    val docParser = DocumentParserUsingSax.newInstance

    val parentDir = new File(pathToParentDir.getPath)

    val doc: Document =
      docParser.parse(new File(parentDir, "kvk-rpt-grote-rechtspersoon-geconsolideerd-model-b-e-indirect-2013.xbrl"))

    val xbrlInstanceDoc: XbrlInstanceDocument =
      new XbrlInstanceDocument(
        DefaultDocawareBridgeElem.wrap(docaware.Elem(doc.uriOption.getOrElse(new URI("")), doc.documentElement)))

    testXbrlProcessing(xbrlInstanceDoc)
  }

  @Test def testXbrlProcessingUsingSaxonElem(): Unit = {
    val processor = new Processor(false)
    val docBuilder = processor.newDocumentBuilder()

    val parentDir = new File(pathToParentDir.getPath)
    val f = new File(parentDir, "kvk-rpt-grote-rechtspersoon-geconsolideerd-model-b-e-indirect-2013.xbrl")

    val node = docBuilder.build(f)

    val xbrlInstanceDoc: XbrlInstanceDocument =
      new XbrlInstanceDocument(
        BridgeElemTakingSaxonElem.wrap(new saxon.DomDocument(node.getUnderlyingNode.asInstanceOf[DocumentInfo]).documentElement))

    testXbrlProcessing(xbrlInstanceDoc)
  }

  private def testXbrlProcessing(xbrlInstanceDoc: XbrlInstanceDocument): Unit = {
    require {
      xbrlInstanceDoc.xbrlInstance.allTopLevelItems.size >= 20
    }

    val bw2iNs =
      xbrlInstanceDoc.xbrlInstance.scope.prefixNamespaceMap("bw2-i")

    val entityFacts = xbrlInstanceDoc.xbrlInstance.filterFacts(withEName(bw2iNs, "EntityName"))
    require(entityFacts.forall(e => !e.isTopLevel))

    val entityFactsInTuples =
      xbrlInstanceDoc.xbrlInstance.allTopLevelTuples.flatMap(e => e.filterFacts(withEName(bw2iNs, "EntityName")))

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

      (expectedPrefixOption.isEmpty) || (e.qname.prefixOption == expectedPrefixOption)
    }
  }

  /** Checks NL-FRIS 8.0, rule 2.1.4. */
  private def hasNoCData(xbrlInstanceDoc: XbrlInstanceDocument): Boolean = {
    // We can always convert to a simple.Elem, if needed (but it can be expensive)
    xbrlInstanceDoc.xbrlInstance.findAllElemsOrSelf.forall(e => !e.bridgeElem.toElem.textChildren.exists(_.isCData))
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
        val s = ctx.period.getChildElem(XbrliStartDateEName).text
        dateFormatter.parseLocalDate(s)
      }

    val endDatesByContextId: Map[String, LocalDate] =
      xbrlInstanceDoc.xbrlInstance.allContextsById filter { case (id, ctx) => ctx.period.isFiniteDuration } mapValues { ctx =>
        val s = ctx.period.getChildElem(XbrliEndDateEName).text
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
      case e: XbrliContext if e.period.isInstant =>
        val instant = e.period.getChildElem(XbrliInstantEName).text
        !instant.contains("T")
      case e: XbrliContext if e.period.isFiniteDuration =>
        val startDate = e.period.getChildElem(XbrliStartDateEName).text
        val endDate = e.period.getChildElem(XbrliEndDateEName).text
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

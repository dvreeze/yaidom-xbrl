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

package eu.cdevreeze.xbrl.taxomodel

import java.io.File
import java.io.InputStream
import java.net.URI

import scala.Vector
import scala.collection.immutable
import scala.reflect.classTag

import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.Suite
import org.scalatest.junit.JUnitRunner

import TaxonomyQueryApi.ToTaxonomyQueryApi
import eu.cdevreeze.xbrl.taxo.Taxonomy
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.docaware
import eu.cdevreeze.yaidom.parse.DocumentParser
import eu.cdevreeze.yaidom.parse.DocumentParserUsingSax
import eu.cdevreeze.yaidom.print.DocumentPrinterUsingDom
import eu.cdevreeze.yaidom.simple.Document

/**
 * Large TaxonomyModel parsing and querying test.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class LargeTaxonomyModelTest extends Suite {

  private val clazz = classOf[LargeTaxonomyModelTest]

  @Test def testParseDts(): Unit = {
    val defaultEntrypointUri = "http://www.nltaxonomie.nl/9.0/report/cbs/entrypoints/stat-handel/cbs-rpt-maandenquete-detailhandel-2015.xsd"
    val entrypointUri = new URI(System.getProperty("taxomodel.entrypoint", defaultEntrypointUri))

    val docParser = getDocumentParser
    val docPrinter = DocumentPrinterUsingDom.newInstance

    def getDoc(uri: URI): docaware.Document = {
      val doc = docParser.parse(uri)
      docaware.Document(doc.uriOption.get, doc)
    }

    def skipUris(uri: URI): Boolean = {
      uri.toString.contains("www.xbrl.org/") && Set("/dtr/", "/lrr/").exists(s => uri.toString.contains(s))
    }

    val taxoDocs = Taxonomy.findDts(Set(entrypointUri), skipUris _)(getDoc _)
    val taxo = new Taxonomy(taxoDocs)

    assertResult(true) {
      taxo.docs.size >= 10
    }
    assertResult(true) {
      taxo.linkbases.size >= 10
    }

    val taxoModelBuilder = new TaxonomyModelBuilder(taxo)

    val taxoModel = taxoModelBuilder.convertToTaxonomyModel

    assertResult(true) {
      taxoModel.findAllDefinitionLinks.size >= 2
    }

    if (System.getProperty("taxomodel.debug", "false").toBoolean) {
      val taxoModelXmlString = docPrinter.print(Document(taxoModel.simpleElem))
      println(taxoModelXmlString)
    }
  }

  @Test def testQueryCbsTaxonomyModel(): Unit = {
    val docParser = DocumentParserUsingSax.newInstance

    val taxoModelFile = new File(classOf[LargeTaxonomyModelTest].getResource("/cbs-rpt-investeringsstatistiek-klein-2014.xml").toURI)
    val doc = docParser.parse(taxoModelFile.toURI)

    implicit val taxoModel = TaxonomyModel.build(doc.documentElement)
    import TaxonomyQueryApi._

    assertResult(true) {
      taxoModel.findAllDefinitionLinks.size >= 10
    }
    assertResult(true) {
      taxoModel.findAllElemsOfType(classTag[Arc]).flatMap(_.attributeAsResolvedQNameOption(EName(YatmNs, "from"))).toSet.
        contains(EName("http://www.nltaxonomie.nl/9.0/basis/cbs/items/cbs-bedr-items", "AccommodationCostsBuildingTaxes"))
    }

    val cbsBedrANs = "http://www.nltaxonomie.nl/9.0/report/cbs/abstracts/cbs-bedr-abstracts"
    val cbsBedrItemsNs = "http://www.nltaxonomie.nl/9.0/basis/cbs/items/cbs-bedr-items"

    val scope = Scope.from(
      "cbs-bedr-a" -> cbsBedrANs,
      "cbs-bedr-items" -> cbsBedrItemsNs)

    // Finding P-links ending with concrete concepts

    def findConcretePLinkChildren(concept: EName, elr: String): immutable.IndexedSeq[EName] = {
      val chains = concept.findOutgoingParentChildArcChains(elr)
      val concepts = chains.map(_.targetConcept).distinct

      val elemDecls = concepts.flatMap(_.asOptionalGlobalElementDeclaration)
      elemDecls.filter(e => !e.isAbstract).map(_.targetEName)
    }

    val reportElr = "urn:cbs:linkrole:fs-cbs-investments-small"

    val islpt = EName(cbsBedrANs, "InvestmentStatisticLimitedPlaceholderTitle")

    val islptLeaves = findConcretePLinkChildren(islpt, reportElr)

    assertResult(true) {
      Set(
        EName(cbsBedrItemsNs, "InvestmentsTerrains"),
        EName(cbsBedrItemsNs, "InvestmentsIndustrialBuildings"),
        EName(cbsBedrItemsNs, "InvestmentsHousesNotForSale"),
        EName(cbsBedrItemsNs, "InvestmentsCivilEngineering")).subsetOf(islptLeaves.toSet)
    }

    // Finding P-links ending with given concepts

    def findUltimatePLinkParents(concept: EName, elr: String): immutable.IndexedSeq[EName] = {
      val chains = concept.findIncomingParentChildArcChains(elr)
      chains.map(_.sourceConcept).distinct
    }

    assertResult(true) {
      val parents = findUltimatePLinkParents(EName(cbsBedrItemsNs, "InvestmentsIndustrialBuildings"), reportElr)

      Set(islpt).subsetOf(parents.toSet)
    }

    assertResult(true) {
      val parents = findUltimatePLinkParents(EName(cbsBedrItemsNs, "InvestmentsCivilEngineering"), reportElr)

      Set(islpt).subsetOf(parents.toSet)
    }

    // Finding concept labels

    val nlLabels =
      islpt.filterOutgoingArcs(classTag[LabelArc])(arc => arc.getLabel.langOption == Some("nl")) map { arc =>
        arc.getLabel
      }

    assertResult(true) {
      nlLabels.map(_.text).contains("Investeringsstatistiek voor kleine bedrijven [titel]")
    }
    assertResult(true) {
      islpt.asGlobalElementDeclaration.substitutionGroupOption ==
        Some(EName("{http://www.nltaxonomie.nl/2011/xbrl/xbrl-syntax-extension}presentationItem"))
    }
  }

  @Test def testQueryCbsDimensions(): Unit = {
    val docParser = DocumentParserUsingSax.newInstance

    val taxoModelFile = new File(classOf[LargeTaxonomyModelTest].getResource("/cbs-rpt-investeringsstatistiek-klein-2014.xml").toURI)
    val doc = docParser.parse(taxoModelFile.toURI)

    implicit val taxoModel = TaxonomyModel.build(doc.documentElement)
    import TaxonomyQueryApi._

    val cbsBedrItemsNs = "http://www.nltaxonomie.nl/9.0/basis/cbs/items/cbs-bedr-items"

    val hasHypercubes =
      taxoModel.findAllDefinitionLinks.flatMap(_.findAllDefinitionArcs).filter(_.isHasHypercube)
    val elrs = hasHypercubes.map(_.linkRole).toSet

    assertResult(3) {
      hasHypercubes.size
    }
    assertResult(Set(
      "urn:cbs:linkrole:adimensional-table",
      "urn:cbs:linkrole:nature-of-investment-table",
      "urn:cbs:linkrole:nature-of-investment-software-table")) {
      elrs
    }

    assertResult(true) {
      hasHypercubes forall { hasHypercube =>
        !hasHypercube.sourceConcept.filterOutgoingArcs(classTag[DefinitionArc])(arc => arc.isDomainMember && arc.linkRole == hasHypercube.linkRole).isEmpty
      }
    }

    // Perform queries for a specific has-hypercube linkrole, at the has-hypercube inheritance side ("the left-hand side")

    val elr = "urn:cbs:linkrole:nature-of-investment-table"

    val hasHypercube = hasHypercubes.find(_.linkRole == elr).get

    val primary = EName("http://www.nltaxonomie.nl/9.0/report/cbs/lineitems/cbs-primary-domains", "NatureOfInvestmentLineItems")

    assertResult(primary) {
      hasHypercube.sourceConcept
    }

    val domMemChains =
      primary.findOutgoingArcChains(classTag[DefinitionArc]) { arc =>
        arc.isDomainMember && arc.linkRole == hasHypercube.linkRole
      } {
        case (chain, arc) =>
          ArcChain.areConsecutiveDimensionalArcs(chain.arcs.last, arc) && !chain.append(arc).hasCycle
      }

    val concepts = domMemChains.map(_.targetConcept)

    assertResult(true) {
      Set(
        EName(cbsBedrItemsNs, "InvestmentsCivilEngineering"),
        EName(cbsBedrItemsNs, "InvestmentsIndustrialBuildings"),
        EName(cbsBedrItemsNs, "InvestmentsHousesNotForSale"),
        EName(cbsBedrItemsNs, "InvestmentsTransportEquipmentOnTrack")).subsetOf(concepts.toSet)
    }

    def findInheritanceChains(concept: EName, elr: String): immutable.IndexedSeq[ArcChain[DefinitionArc]] = {
      val chains =
        concept.findIncomingArcChains(classTag[DefinitionArc]) { arc =>
          arc.isDomainMember
        } {
          case (chain, arc) =>
            arc.isDomainMember &&
              ArcChain.areConsecutiveDimensionalArcs(arc, chain.arcs.head) && !chain.prepend(arc).hasCycle
        }
      chains.filter(_.arcs.head.linkRole == elr)
    }

    def findPrimariesInheritedBy(concept: EName, elr: String): immutable.IndexedSeq[EName] = {
      val chains = findInheritanceChains(concept, elr)
      chains.map(_.sourceConcept).distinct
    }

    val primaries = concepts.flatMap(concept => findPrimariesInheritedBy(concept, elr))

    assertResult(Set(primary)) {
      primaries.toSet
    }

    // Perform queries for a specific has-hypercube linkrole, at the dimensional tree side ("the right-hand side")

    val dimChains = findDimensionalChains(hasHypercube)

    assertResult(true) {
      dimChains.forall(_.arcs.head == hasHypercube)
    }

    assertResult(true) {
      dimChains forall {
        case ch =>
          ch.arcs exists {
            case arc: DefinitionArc =>
              arc.isHypercubeDimension &&
                arc.linkRole == elr &&
                arc.sourceConcept == EName("http://www.nltaxonomie.nl/9.0/report/cbs/tables/cbs-tables", "ValidationTable") &&
                arc.targetConcept == EName("http://www.nltaxonomie.nl/9.0/domein/cbs/axes/cbs-axes", "NewUsedSelfProducedAxis")
          }
      }
    }

    assertResult(true) {
      dimChains forall {
        case ch =>
          ch.arcs exists {
            case arc: DefinitionArc =>
              arc.isDimensionDomain &&
                arc.linkRole == "urn:cbs:linkrole:new-used-selfproduced-axis" &&
                arc.sourceConcept == EName("http://www.nltaxonomie.nl/9.0/domein/cbs/axes/cbs-axes", "NewUsedSelfProducedAxis") &&
                arc.targetConcept == EName("http://www.nltaxonomie.nl/9.0/basis/cbs/domains/cbs-domains-natureofinvestment", "NewUsedSelfProducedDomain")
          }
      }
    }

    assertResult(true) {
      dimChains forall {
        case ch =>
          ch.arcs exists {
            case arc: DefinitionArc =>
              arc.isDomainMember &&
                arc.linkRole == "urn:cbs:linkrole:natureofinvestment-domain" &&
                arc.sourceConcept == EName("http://www.nltaxonomie.nl/9.0/basis/cbs/domains/cbs-domains-natureofinvestment", "NewUsedSelfProducedDomain")
          }
      }
    }

    assertResult(Set(
      "NewMember",
      "UsedMember",
      "SelfProducedMember",
      "TotalAssetsInvestmentsMember").map(s => EName("http://www.nltaxonomie.nl/9.0/basis/cbs/domains/cbs-domains-natureofinvestment", s))) {
      dimChains.map(_.targetConcept).toSet
    }

    // Perform queries from concrete concepts via has-hypercubes to the members

    def findInheritedDimensionalChains(inheritingConcept: EName): immutable.IndexedSeq[ArcChain[DefinitionArc]] = {
      val result =
        findInheritanceChains(inheritingConcept, elr) flatMap { ch =>
          val hasHypercubes =
            ch.sourceConcept.filterOutgoingArcs(classTag[DefinitionArc])(arc => arc.isHasHypercube && arc.linkRole == elr)
          hasHypercubes.flatMap(hasHypercube => findDimensionalChains(hasHypercube)).distinct
        }
      result.distinct
    }

    val foundDimChains = findInheritedDimensionalChains(concepts(0))

    assertResult(dimChains.map(_.targetConcept).toSet) {
      foundDimChains.map(_.targetConcept).toSet
    }

    assertResult(true) {
      concepts forall { concept =>
        findInheritedDimensionalChains(concept).map(_.arcs.map(_.targetConcept)).toSet ==
          foundDimChains.map(_.arcs.map(_.targetConcept)).toSet
      }
    }

    assertResult(dimChains.map(_.arcs.map(_.targetConcept)).toSet) {
      foundDimChains.map(_.arcs.map(_.targetConcept)).toSet
    }
  }

  @Test def testQueryBdDimensions(): Unit = {
    val docParser = DocumentParserUsingSax.newInstance

    val taxoModelFile = new File(classOf[LargeTaxonomyModelTest].getResource("/bd-rpt-ihz-aangifte-2014.xml").toURI)
    val doc = docParser.parse(taxoModelFile.toURI)

    implicit val taxoModel = TaxonomyModel.build(doc.documentElement)
    import TaxonomyQueryApi._

    val hasHypercubes =
      taxoModel.findAllDefinitionLinks.flatMap(_.findAllDefinitionArcs).filter(_.isHasHypercube)
    val elrs = hasHypercubes.map(_.linkRole).toSet

    assertResult(true) {
      elrs.size >= 10
    }

    assertResult(Set(EName("{http://www.nltaxonomie.nl/2013/xbrl/sbr-dimensional-concepts}ValidationLineItems"))) {
      hasHypercubes.map(_.sourceConcept).toSet
    }

    assertResult(Set(EName("{http://www.nltaxonomie.nl/2013/xbrl/sbr-dimensional-concepts}ValidationTable"))) {
      hasHypercubes.map(_.targetConcept).toSet
    }
  }

  private def findDimensionalChains(hasHypercube: DefinitionArc)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[ArcChain[DefinitionArc]] = {
    import TaxonomyQueryApi._

    val chains =
      hasHypercube.sourceConcept.findOutgoingArcChains(classTag[DefinitionArc]) { arc =>
        arc == hasHypercube
      } {
        case (chain, arc) =>
          ArcChain.areConsecutiveDimensionalArcs(chain.arcs.last, arc) && !chain.append(arc).hasCycle
      }
    chains
  }

  private def findFiles(root: File, fileFilter: File => Boolean): Vector[File] = {
    // Recursive calls
    root.listFiles.toVector flatMap {
      case d: File if d.isDirectory => findFiles(d, fileFilter)
      case f: File if f.isFile && fileFilter(f) => Vector(f)
      case _ => Vector()
    }
  }

  private def fileUriToHttpUri(fileUri: URI): URI = {
    require(fileUri.getScheme == "file", s"Not a 'file' URI: $fileUri")
    require(fileUri.getPath.contains("/nltaxo/"), s"Missing 'nltaxo' directory in $fileUri")
    val idx = fileUri.getPath.indexOf("/nltaxo/") + "/nltaxo/".length
    val path = fileUri.getPath.substring(idx)
    new URI(s"http://${path}")
  }

  private def httpUriToFileUri(httpUri: URI): URI = {
    require(httpUri.getScheme == "http")
    val rootDir = (new File(clazz.getResource("/nltaxo").toURI))
    val hostDir = new File(rootDir, httpUri.getHost)
    val path = new File(hostDir, httpUri.getPath)
    path.toURI
  }

  private def getDocumentParser: DocumentParser = {
    val wrappedDocParser = DocumentParserUsingSax.newInstance

    new DocumentParser() {
      def parse(is: InputStream): Document = ???

      def parse(f: File): Document = ???

      def parse(uri: URI): Document = {
        val localUri = if (uri.getScheme == "http") httpUriToFileUri(uri) else uri

        val doc = wrappedDocParser.parse(localUri)

        doc.withUriOption(Some(fileUriToHttpUri(localUri)))
      }
    }
  }
}

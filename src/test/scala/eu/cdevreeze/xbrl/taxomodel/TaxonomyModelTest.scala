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

import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.Suite
import org.scalatest.junit.JUnitRunner

import QueryableTaxonomyModel.ToQueryableTaxonomyModel
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.parse.DocumentParserUsingSax
import eu.cdevreeze.yaidom.simple.Document

/**
 * TaxonomyModel parsing test.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class TaxonomyModelTest extends Suite {

  private val clazz = classOf[TaxonomyModelTest]

  @Test def testParseTaxonomyModel(): Unit = {
    val docParser = DocumentParserUsingSax.newInstance

    val taxoFileUri = clazz.getResource("/sample-taxonomy.xml").toURI

    val doc: Document =
      docParser.parse(taxoFileUri)

    val taxoModel: TaxonomyModel =
      TaxonomyModel.build(doc.documentElement)

    assertResult(true) {
      val roles = taxoModel.findAllDefinitionLinks.map(_.linkRole).toSet
      Set("urn:cbs:linkrole:new-used-selfproduced-axis", "urn:cbs:linkrole:natureofinvestment-domain").subsetOf(roles)
    }

    val elemDecls = taxoModel.findAllSchemas.flatMap(_.findAllGlobalElementDeclarations)

    assertResult(true) {
      elemDecls.size >= 10
    }

    assertResult(true) {
      val ename = EName("http://www.nltaxonomie.nl/9.0/basis/cbs/items/cbs-bedr-items", "TurnoverNetOtherAbroad")
      taxoModel.findAllSchemas.flatMap(_.findGlobalElementDeclarationByEName(ename)).headOption.isDefined
    }

    val elemDeclENames = taxoModel.findAllSchemas.flatMap(_.findAllGlobalElementDeclarations).map(_.targetEName).distinct

    assertResult(true) {
      elemDeclENames.flatMap(_.namespaceUriOption).toSet.size >= 6
    }
  }

  @Test def testQueryPLinks(): Unit = {
    val docParser = DocumentParserUsingSax.newInstance

    val taxoFileUri = clazz.getResource("/bd-rpt-vpb-aangifte-2014.xml").toURI

    val doc: Document =
      docParser.parse(taxoFileUri)

    val scope = doc.documentElement.scope
    import scope._

    import QueryableTaxonomyModel._

    val taxoModel: QueryableTaxonomyModel =
      TaxonomyModel.build(doc.documentElement).queryable

    val elr = "urn:bd:linkrole:bd-lr-pre_aandeelhouders"

    val startConcept = QName("bd-abstr:ShareholdersTitle").res

    val parentChildChains = taxoModel.findOutgoingParentChildArcChains(startConcept, elr)

    assertResult(Set(QName("bd-bedr-tuple:ShareholderSpecification").res)) {
      parentChildChains.flatMap(_.arcs.drop(1).headOption.map(_.sourceConcept)).toSet
    }

    parentChildChains.foreach(e => println(e.arcs.map(_.targetConcept.localPart)))
  }
}

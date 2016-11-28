/*
 * Copyright 2011-2017 Chris de Vreeze
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

package eu.cdevreeze.xbrl.taxonomy.model.relationship

import scala.Vector
import scala.math.BigDecimal.int2bigDecimal

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope

/**
 * Relationship creation test case.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class CreateRelationshipTest extends FunSuite {

  test("testCreatePresentationRelationships") {
    val elr = "urn:kvk:linkrole:consolidated-statement-of-comprehensive-income"
    val parentChildRelationshipBuilder = PresentationRelationship.parentChildRelationshipBuilder(elr)

    val scope = Scope.from(
      "kvk-abstr" -> "http://www.nltaxonomie.nl/nt11/kvk/20161214/presentation/kvk-abstracts",
      "venj-bw2-i" -> "http://www.nltaxonomie.nl/nt11/venj/20161214/dictionary/venj-bw2-data",
      "rj-i" -> "http://www.nltaxonomie.nl/nt11/rj/20161214/dictionary/rj-data")

    import scope._

    val parentChildRelationships = Vector(
      parentChildRelationshipBuilder.build(
        QName("kvk-abstr:StatementOfComprehensiveIncomeTitle").res,
        QName("venj-bw2-i:NetResultAfterTax").res,
        RelationshipAttributes.Empty.withOrder(1)),
      parentChildRelationshipBuilder.build(
        QName("kvk-abstr:StatementOfComprehensiveIncomeTitle").res,
        QName("rj-i:ComprehensiveIncomeOther").res,
        RelationshipAttributes.Empty.withOrder(2)),
      parentChildRelationshipBuilder.build(
        QName("kvk-abstr:StatementOfComprehensiveIncomeTitle").res,
        QName("venj-bw2-i:ComprehensiveIncome").res,
        RelationshipAttributes.Empty.withOrder(3)),
      parentChildRelationshipBuilder.build(
        QName("kvk-abstr:StatementOfComprehensiveIncomeTitle").res,
        QName("rj-i:ComprehensiveIncomeOtherIncomeExpenses").res,
        RelationshipAttributes.Empty.withOrder(4)),
      parentChildRelationshipBuilder.build(
        QName("rj-i:ComprehensiveIncomeOther").res,
        QName("rj-i:RevaluationPropertyPlantEquipment").res,
        RelationshipAttributes.Empty.withOrder(1)),
      parentChildRelationshipBuilder.build(
        QName("rj-i:ComprehensiveIncomeOther").res,
        QName("rj-i:ChangesValueFinancialAssets").res,
        RelationshipAttributes.Empty.withOrder(2)),
      parentChildRelationshipBuilder.build(
        QName("rj-i:ComprehensiveIncomeOther").res,
        QName("rj-i:ExchangeDifferencesForeignParticipatingInterests").res,
        RelationshipAttributes.Empty.withOrder(3)),
      parentChildRelationshipBuilder.build(
        QName("rj-i:ComprehensiveIncomeOther").res,
        QName("rj-i:RealisedRevaluationChargedToEquity").res,
        RelationshipAttributes.Empty.withOrder(4)),
      parentChildRelationshipBuilder.build(
        QName("rj-i:ComprehensiveIncomeOther").res,
        QName("rj-i:AdjustmentsRecycleItems").res,
        RelationshipAttributes.Empty.withOrder(5)),
      parentChildRelationshipBuilder.build(
        QName("rj-i:ComprehensiveIncomeOther").res,
        QName("rj-i:ImpactTaxesComprehensiveIncomeOther").res,
        RelationshipAttributes.Empty.withOrder(6)))

    assertResult(Set(PresentationRelationship.ParentChildArcRole)) {
      parentChildRelationships.map(_.arcRole).toSet
    }

    assertResult(Set(
      QName("kvk-abstr:StatementOfComprehensiveIncomeTitle").res,
      QName("rj-i:ComprehensiveIncomeOther").res)) {

      parentChildRelationships.map(_.source).toSet
    }

    assertResult(Set(QName("rj-i:ComprehensiveIncomeOther").res)) {
      parentChildRelationships.map(_.source).toSet.intersect(parentChildRelationships.map(_.target).toSet)
    }

    val expectedRelationship =
      PresentationRelationship(
        elr,
        PresentationRelationship.ParentChildArcRole,
        QName("kvk-abstr:StatementOfComprehensiveIncomeTitle").res,
        QName("rj-i:ComprehensiveIncomeOther").res,
        RelationshipAttributes.Empty.withOrder(2))

    assertResult(Set(expectedRelationship)) {
      parentChildRelationships.filter(_.target == QName("rj-i:ComprehensiveIncomeOther").res).toSet
    }
  }

  test("testCreateConceptLabelRelationships") {
    val conceptLabelRelationshipBuilder = ConceptLabelRelationship.defaultRelationshipBuilder
    val conceptLabelBuilder = StandardLabel.terseLabelBuilderForLanguage("nl")

    val scope = Scope.from(
      "kvk-abstr" -> "http://www.nltaxonomie.nl/nt11/kvk/20161214/presentation/kvk-abstracts")

    import scope._

    val conceptLabelRelationships = Vector(
      conceptLabelRelationshipBuilder.build(
        QName("kvk-abstr:ConsolidatedBalanceSheetTitle").res,
        conceptLabelBuilder.build(
          "Geconsolideerde balans"),
        RelationshipAttributes.Empty),
      conceptLabelRelationshipBuilder.build(
        QName("kvk-abstr:ConsolidatedFinancialStatementTitle").res,
        conceptLabelBuilder.build(
          "Geconsolideerde jaarrekening"),
        RelationshipAttributes.Empty),
      conceptLabelRelationshipBuilder.build(
        QName("kvk-abstr:ConsolidatedGeneralNotesTitle").res,
        conceptLabelBuilder.build(
          "Algemene toelichting"),
        RelationshipAttributes.Empty),
      conceptLabelRelationshipBuilder.build(
        QName("kvk-abstr:ConsolidatedIncomeStatementTitle").res,
        conceptLabelBuilder.build(
          "Geconsolideerde winst- en verliesrekening"),
        RelationshipAttributes.Empty),
      conceptLabelRelationshipBuilder.build(
        QName("kvk-abstr:ConsolidatedNotesTitle").res,
        conceptLabelBuilder.build(
          "Geconsolideerde toelichting"),
        RelationshipAttributes.Empty),
      conceptLabelRelationshipBuilder.build(
        QName("kvk-abstr:FinancialStatementTitle").res,
        conceptLabelBuilder.build(
          "Jaarrekening"),
        RelationshipAttributes.Empty),
      conceptLabelRelationshipBuilder.build(
        QName("kvk-abstr:GeneralNotesTitle").res,
        conceptLabelBuilder.build(
          "Algemene toelichting"),
        RelationshipAttributes.Empty))

    assertResult(Set(ConceptLabelRelationship.DefaultArcRole)) {
      conceptLabelRelationships.map(_.arcRole).toSet
    }

    assertResult(Set(
      QName("kvk-abstr:ConsolidatedBalanceSheetTitle").res,
      QName("kvk-abstr:ConsolidatedFinancialStatementTitle").res,
      QName("kvk-abstr:ConsolidatedGeneralNotesTitle").res,
      QName("kvk-abstr:ConsolidatedIncomeStatementTitle").res,
      QName("kvk-abstr:ConsolidatedNotesTitle").res,
      QName("kvk-abstr:FinancialStatementTitle").res,
      QName("kvk-abstr:GeneralNotesTitle").res)) {

      conceptLabelRelationships.map(_.source).toSet
    }

    val expectedRelationship =
      ConceptLabelRelationship(
        Relationship.DefaultLinkRole,
        ConceptLabelRelationship.DefaultArcRole,
        QName("kvk-abstr:FinancialStatementTitle").res,
        StandardLabel(
          StandardLabel.ResourceRoleTerseLabel,
          "nl",
          "Jaarrekening",
          ResourceAttributes.Empty),
        RelationshipAttributes.Empty)

    assertResult(Set(expectedRelationship)) {
      conceptLabelRelationships.filter(_.source == QName("kvk-abstr:FinancialStatementTitle").res).toSet
    }
  }
}

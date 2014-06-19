/*
 * Copyright 2014 Chris de Vreeze
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
package xbrli
package yaidomimpl

import scala.collection.immutable
import ElemApi._

/**
 * Yaidom-backed XML element inside XBRL instance (or the entire XBRL instance itself).
 *
 * No (val or var) fields have been used in this trait and its sub-traits, to prevent any initialization order problems.
 *
 * @author Chris de Vreeze
 */
trait XbrliElem extends xbrli.XbrliElem with ElemLike[XbrliElem] with HasText {

  def toElem: Elem

  def findAllChildElems: immutable.IndexedSeq[XbrliElem]

  final def resolvedName: EName = wrappedElem.resolvedName

  final def resolvedAttributes: immutable.Iterable[(EName, String)] = wrappedElem.resolvedAttributes

  final def text: String = wrappedElem.text

  def getTextAsEName: EName
}

/**
 * Yaidom-backed XBRL instance.
 *
 * @author Chris de Vreeze
 */
trait XbrlInstance extends XbrliElem with xbrli.XbrlInstance {

  final def findAllContexts: immutable.IndexedSeq[XbrliContext] = {
    filterChildElems(XbrliContextEName) collect { case e: XbrliContext => e }
  }

  final def findAllContextsById: Map[String, XbrliContext] = {
    findAllContexts.groupBy(_.id) mapValues (contexts => contexts.head)
  }

  final def findAllUnits: immutable.IndexedSeq[XbrliUnit] = {
    filterChildElems(XbrliUnitEName) collect { case e: XbrliUnit => e }
  }

  final def findAllUnitsById: Map[String, XbrliUnit] = {
    findAllUnits.groupBy(_.id) mapValues (units => units.head)
  }

  final def findAllTopLevelFacts: immutable.IndexedSeq[Fact] = {
    val childFacts =
      filterChildElems(e => !Set(Option(LinkNs), Option(XbrliNs)).contains(e.resolvedName.namespaceUriOption))
    childFacts collect { case e: Fact => e }
  }

  final def findAllTopLevelItems: immutable.IndexedSeq[ItemFact] = {
    findAllTopLevelFacts collect { case e: ItemFact => e }
  }

  final def findAllTopLevelTuples: immutable.IndexedSeq[TupleFact] = {
    findAllTopLevelFacts collect { case e: TupleFact => e }
  }

  final def findAllFacts: immutable.IndexedSeq[Fact] = {
    findAllTopLevelFacts flatMap (e => e.findAllElemsOrSelf) collect { case e: Fact => e }
  }

  final def findAllItems: immutable.IndexedSeq[ItemFact] = {
    findAllFacts collect { case e: ItemFact => e }
  }

  final def findAllTuples: immutable.IndexedSeq[TupleFact] = {
    findAllFacts collect { case e: TupleFact => e }
  }

  final def findAllTopLevelFactsByEName: Map[EName, immutable.IndexedSeq[Fact]] = {
    findAllTopLevelFacts groupBy (e => e.resolvedName)
  }

  final def findAllTopLevelItemsByEName: Map[EName, immutable.IndexedSeq[ItemFact]] = {
    findAllTopLevelItems groupBy (e => e.resolvedName)
  }

  final def findAllTopLevelTuplesByEName: Map[EName, immutable.IndexedSeq[TupleFact]] = {
    findAllTopLevelTuples groupBy (e => e.resolvedName)
  }

  final def filterFacts(p: xbrli.Fact => Boolean): immutable.IndexedSeq[Fact] = {
    findAllFacts filter (e => p(e))
  }

  final def filterItems(p: xbrli.ItemFact => Boolean): immutable.IndexedSeq[ItemFact] = {
    findAllItems filter (e => p(e))
  }

  final def filterTuples(p: xbrli.TupleFact => Boolean): immutable.IndexedSeq[TupleFact] = {
    findAllTuples filter (e => p(e))
  }

  final def filterTopLevelFacts(p: xbrli.Fact => Boolean): immutable.IndexedSeq[Fact] = {
    findAllTopLevelFacts filter (e => p(e))
  }

  final def filterTopLevelItems(p: xbrli.ItemFact => Boolean): immutable.IndexedSeq[ItemFact] = {
    findAllTopLevelItems filter (e => p(e))
  }

  final def filterTopLevelTuples(p: xbrli.TupleFact => Boolean): immutable.IndexedSeq[TupleFact] = {
    findAllTopLevelTuples filter (e => p(e))
  }

  final def findAllSchemaRefs: immutable.IndexedSeq[SchemaRef] = {
    filterChildElems(LinkSchemaRefEName) collect { case e: SchemaRef => e }
  }

  final def findAllLinkbaseRefs: immutable.IndexedSeq[LinkbaseRef] = {
    filterChildElems(LinkLinkbaseRefEName) collect { case e: LinkbaseRef => e }
  }

  final def findAllRoleRefs: immutable.IndexedSeq[RoleRef] = {
    filterChildElems(LinkRoleRefEName) collect { case e: RoleRef => e }
  }

  final def findAllArcroleRefs: immutable.IndexedSeq[ArcroleRef] = {
    filterChildElems(LinkArcroleRefEName) collect { case e: ArcroleRef => e }
  }

  final def findAllFootnoteLinks: immutable.IndexedSeq[FootnoteLink] = {
    filterChildElems(LinkFootnoteLinkEName) collect { case e: FootnoteLink => e }
  }
}

trait SchemaRef extends XbrliElem with xbrli.SchemaRef

trait LinkbaseRef extends XbrliElem with xbrli.LinkbaseRef

trait RoleRef extends XbrliElem with xbrli.RoleRef

trait ArcroleRef extends XbrliElem with xbrli.ArcroleRef

trait XbrliContext extends XbrliElem with xbrli.XbrliContext {

  final def id: String = attribute(IdEName)

  final def entity: Entity = {
    getChildElem(XbrliEntityEName).asInstanceOf[Entity]
  }

  final def period: Period = {
    getChildElem(XbrliPeriodEName).asInstanceOf[Period]
  }

  final def scenarioOption: Option[Scenario] = {
    findChildElem(XbrliScenarioEName) collect { case e: Scenario => e }
  }
}

trait XbrliUnit extends XbrliElem with xbrli.XbrliUnit {

  final def id: String = attribute(IdEName)

  final def measures: immutable.IndexedSeq[EName] = {
    filterChildElems(XbrliMeasureEName) map (e => e.getTextAsEName)
  }

  final def divide: Divide = {
    getChildElem(XbrliDivideEName).asInstanceOf[Divide]
  }
}

trait Fact extends XbrliElem with xbrli.Fact

trait ItemFact extends Fact with xbrli.ItemFact {

  final def contextRef: String = attribute(ContextRefEName)
}

trait NonNumericItemFact extends ItemFact with xbrli.NonNumericItemFact

trait NumericItemFact extends ItemFact with xbrli.NumericItemFact {

  final def unitRef: String = attribute(UnitRefEName)
}

trait NonFractionNumericItemFact extends NumericItemFact with xbrli.NonFractionNumericItemFact {

  final def precisionOption: Option[String] = attributeOption(PrecisionEName)

  final def decimalsOption: Option[String] = attributeOption(DecimalsEName)
}

trait FractionItemFact extends NumericItemFact with xbrli.FractionItemFact {

  final def numerator: BigDecimal = {
    val s = getChildElem(XbrliNumeratorEName).text
    BigDecimal(s)
  }

  final def denominator: BigDecimal = {
    val s = getChildElem(XbrliDenominatorEName).text
    BigDecimal(s)
  }
}

trait TupleFact extends Fact with xbrli.TupleFact {

  final def findAllChildFacts: immutable.IndexedSeq[Fact] = {
    findAllChildElems collect { case e: Fact => e }
  }

  final def findAllFacts: immutable.IndexedSeq[Fact] = {
    findAllElems collect { case e: Fact => e }
  }

  final def filterChildFacts(p: xbrli.Fact => Boolean): immutable.IndexedSeq[Fact] = {
    findAllChildFacts filter (e => p(e))
  }

  final def filterFacts(p: xbrli.Fact => Boolean): immutable.IndexedSeq[Fact] = {
    findAllFacts filter (e => p(e))
  }
}

trait FootnoteLink extends XbrliElem with xbrli.FootnoteLink

trait Entity extends XbrliElem with xbrli.Entity {

  final def identifier: Identifier = {
    getChildElem(XbrliIdentifierEName).asInstanceOf[Identifier]
  }

  final def segmentOption: Option[Segment] = {
    findChildElem(XbrliSegmentEName) collect { case e: Segment => e }
  }
}

trait Period extends XbrliElem with xbrli.Period {

  final def isInstant: Boolean = {
    findChildElem(XbrliInstantEName).isDefined
  }

  final def isFiniteDuration: Boolean = {
    findChildElem(XbrliStartDateEName).isDefined
  }

  final def isForever: Boolean = {
    findChildElem(XbrliForeverEName).isDefined
  }
}

trait Scenario extends XbrliElem with xbrli.Scenario

trait Segment extends XbrliElem with xbrli.Segment

trait Identifier extends XbrliElem with xbrli.Identifier

trait Divide extends XbrliElem with xbrli.Divide {

  final def numerator: immutable.IndexedSeq[EName] = {
    val unitNumerator = getChildElem(XbrliUnitNumeratorEName)
    val result = unitNumerator.filterChildElems(XbrliMeasureEName).map(e => e.getTextAsEName)
    result
  }

  final def denominator: immutable.IndexedSeq[EName] = {
    val unitDenominator = getChildElem(XbrliUnitDenominatorEName)
    val result = unitDenominator.filterChildElems(XbrliMeasureEName).map(e => e.getTextAsEName)
    result
  }
}

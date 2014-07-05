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

import scala.collection.immutable

import ElemApi.withEName
import eu.cdevreeze.yaidom.EName
import eu.cdevreeze.yaidom.ElemLike
import eu.cdevreeze.yaidom.HasText
import eu.cdevreeze.yaidom.Path

/**
 * XML element inside XBRL instance (or the entire XBRL instance itself). This API is immutable if the wrapped element is
 * immutable, which should be the case.
 *
 * Implementation notes: This trait and its sub-traits form a type hierarchy. That would not go well together with type parameters
 * for underlying DOM trees. To prevent these type parameters, non-generic class DomElem is used for the wrapped DOM elements.
 *
 * Also note that the package-private constructor contains redundant data, in order to speed up (yaidom-based) querying.
 *
 * @author Chris de Vreeze
 */

trait XbrliModule extends DomElemTypeModule {

  sealed class XbrliElem private[xbrli] (
    val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends ElemLike[XbrliElem] with HasText {

    require(childElems.map(_.wrappedElem.elem) == wrappedElem.elem.findAllChildElems)

    /**
     * Very fast implementation of findAllChildElems, for fast querying
     */
    final def findAllChildElems: immutable.IndexedSeq[XbrliElem] = childElems

    final def resolvedName: EName = wrappedElem.elem.resolvedName

    final def resolvedAttributes: immutable.Iterable[(EName, String)] = wrappedElem.elem.resolvedAttributes

    final def text: String = wrappedElem.elem.text

    final def getTextAsEName: EName = wrappedElem.textAsResolvedQName
  }

  /**
   * XBRL instance.
   *
   * It does not check validity of the XBRL instance. Neither does it know about the DTS describing the XBRL instance.
   * It does, however, contain the entrypoint URI(s) to the DTS.
   *
   * Without any knowledge about the DTS, this class only recognizes (item and tuple) facts by looking at the
   * structure of the element and its ancestry. Attribute @contextRef is only allowed for item facts, and tuple facts can be
   * recognized by looking at the "path" of the element.
   *
   * @author Chris de Vreeze
   */
  final class XbrlInstance private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == XbrliXbrlEName, s"Expected EName $XbrliXbrlEName but found $resolvedName")

    val allContexts: immutable.IndexedSeq[XbrliContext] = findAllContexts

    val allContextsById: Map[String, XbrliContext] = findAllContextsById

    val allUnits: immutable.IndexedSeq[XbrliUnit] = findAllUnits

    val allUnitsById: Map[String, XbrliUnit] = findAllUnitsById

    val allTopLevelFacts: immutable.IndexedSeq[Fact] = findAllTopLevelFacts

    val allTopLevelItems: immutable.IndexedSeq[ItemFact] = findAllTopLevelItems

    val allTopLevelTuples: immutable.IndexedSeq[TupleFact] = findAllTopLevelTuples

    val allTopLevelFactsByEName: Map[EName, immutable.IndexedSeq[Fact]] =
      findAllTopLevelFactsByEName

    val allTopLevelItemsByEName: Map[EName, immutable.IndexedSeq[ItemFact]] =
      findAllTopLevelItemsByEName

    val allTopLevelTuplesByEName: Map[EName, immutable.IndexedSeq[TupleFact]] =
      findAllTopLevelTuplesByEName

    def findAllFacts: immutable.IndexedSeq[Fact] = {
      findAllTopLevelFacts flatMap (e => e.findAllElemsOrSelf) collect { case e: Fact => e }
    }

    def findAllItems: immutable.IndexedSeq[ItemFact] = {
      findAllFacts collect { case e: ItemFact => e }
    }

    def findAllTuples: immutable.IndexedSeq[TupleFact] = {
      findAllFacts collect { case e: TupleFact => e }
    }

    def filterFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
      findAllFacts filter (e => p(e))
    }

    def filterItems(p: ItemFact => Boolean): immutable.IndexedSeq[ItemFact] = {
      findAllItems filter (e => p(e))
    }

    def filterTuples(p: TupleFact => Boolean): immutable.IndexedSeq[TupleFact] = {
      findAllTuples filter (e => p(e))
    }

    def filterTopLevelFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
      findAllTopLevelFacts filter (e => p(e))
    }

    def filterTopLevelItems(p: ItemFact => Boolean): immutable.IndexedSeq[ItemFact] = {
      findAllTopLevelItems filter (e => p(e))
    }

    def filterTopLevelTuples(p: TupleFact => Boolean): immutable.IndexedSeq[TupleFact] = {
      findAllTopLevelTuples filter (e => p(e))
    }

    def findAllSchemaRefs: immutable.IndexedSeq[SchemaRef] = {
      filterChildElems(LinkSchemaRefEName) collect { case e: SchemaRef => e }
    }

    def findAllLinkbaseRefs: immutable.IndexedSeq[LinkbaseRef] = {
      filterChildElems(LinkLinkbaseRefEName) collect { case e: LinkbaseRef => e }
    }

    def findAllRoleRefs: immutable.IndexedSeq[RoleRef] = {
      filterChildElems(LinkRoleRefEName) collect { case e: RoleRef => e }
    }

    def findAllArcroleRefs: immutable.IndexedSeq[ArcroleRef] = {
      filterChildElems(LinkArcroleRefEName) collect { case e: ArcroleRef => e }
    }

    def findAllFootnoteLinks: immutable.IndexedSeq[FootnoteLink] = {
      filterChildElems(LinkFootnoteLinkEName) collect { case e: FootnoteLink => e }
    }

    private def findAllContexts: immutable.IndexedSeq[XbrliContext] = {
      filterChildElems(XbrliContextEName) collect { case e: XbrliContext => e }
    }

    private def findAllContextsById: Map[String, XbrliContext] = {
      findAllContexts.groupBy(_.id) mapValues (contexts => contexts.head)
    }

    private def findAllUnits: immutable.IndexedSeq[XbrliUnit] = {
      filterChildElems(XbrliUnitEName) collect { case e: XbrliUnit => e }
    }

    private def findAllUnitsById: Map[String, XbrliUnit] = {
      findAllUnits.groupBy(_.id) mapValues (units => units.head)
    }

    private def findAllTopLevelFacts: immutable.IndexedSeq[Fact] = {
      val childFacts =
        filterChildElems(e => !Set(Option(LinkNs), Option(XbrliNs)).contains(e.resolvedName.namespaceUriOption))
      childFacts collect { case e: Fact => e }
    }

    private def findAllTopLevelItems: immutable.IndexedSeq[ItemFact] = {
      findAllTopLevelFacts collect { case e: ItemFact => e }
    }

    private def findAllTopLevelTuples: immutable.IndexedSeq[TupleFact] = {
      findAllTopLevelFacts collect { case e: TupleFact => e }
    }

    private def findAllTopLevelFactsByEName: Map[EName, immutable.IndexedSeq[Fact]] = {
      findAllTopLevelFacts groupBy (e => e.resolvedName)
    }

    private def findAllTopLevelItemsByEName: Map[EName, immutable.IndexedSeq[ItemFact]] = {
      findAllTopLevelItems groupBy (e => e.resolvedName)
    }

    private def findAllTopLevelTuplesByEName: Map[EName, immutable.IndexedSeq[TupleFact]] = {
      findAllTopLevelTuples groupBy (e => e.resolvedName)
    }
  }

  /**
   * SchemaRef in an XBRL instance
   *
   * @author Chris de Vreeze
   */
  final class SchemaRef private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == LinkSchemaRefEName, s"Expected EName $LinkSchemaRefEName but found $resolvedName")
  }

  /**
   * LinkbaseRef in an XBRL instance
   *
   * @author Chris de Vreeze
   */
  final class LinkbaseRef private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == LinkLinkbaseRefEName, s"Expected EName $LinkLinkbaseRefEName but found $resolvedName")
  }

  /**
   * RoleRef in an XBRL instance
   *
   * @author Chris de Vreeze
   */
  final class RoleRef private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == LinkRoleRefEName, s"Expected EName $LinkRoleRefEName but found $resolvedName")
  }

  /**
   * ArcroleRef in an XBRL instance
   *
   * @author Chris de Vreeze
   */
  final class ArcroleRef private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == LinkArcroleRefEName, s"Expected EName $LinkArcroleRefEName but found $resolvedName")
  }

  /**
   * Context in an XBRL instance
   *
   * @author Chris de Vreeze
   */
  final class XbrliContext private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == XbrliContextEName, s"Expected EName $XbrliContextEName but found $resolvedName")

    def id: String = attribute(IdEName)

    def entity: Entity = {
      getChildElem(XbrliEntityEName).asInstanceOf[Entity]
    }

    def period: Period = {
      getChildElem(XbrliPeriodEName).asInstanceOf[Period]
    }

    def scenarioOption: Option[Scenario] = {
      findChildElem(XbrliScenarioEName) collect { case e: Scenario => e }
    }
  }

  /**
   * Unit in an XBRL instance
   *
   * @author Chris de Vreeze
   */
  final class XbrliUnit private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == XbrliUnitEName, s"Expected EName $XbrliUnitEName but found $resolvedName")

    def id: String = attribute(IdEName)

    def measures: immutable.IndexedSeq[EName] = {
      filterChildElems(XbrliMeasureEName) map (e => e.getTextAsEName)
    }

    def divide: Divide = {
      getChildElem(XbrliDivideEName).asInstanceOf[Divide]
    }
  }

  /**
   * Item or tuple fact in an XBRL instance, either top-level or nested
   *
   * @author Chris de Vreeze
   */
  abstract class Fact private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    def isTopLevel: Boolean = wrappedElem.path.entries.size == 1
  }

  /**
   * Item fact in an XBRL instance, either top-level or nested
   *
   * @author Chris de Vreeze
   */
  abstract class ItemFact private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(wrappedElem, childElems) {

    require(attributeOption(ContextRefEName).isDefined, s"Expected attribute $ContextRefEName")

    def contextRef: String = attribute(ContextRefEName)
  }

  /**
   * Non-numeric item fact in an XBRL instance, either top-level or nested
   *
   * @author Chris de Vreeze
   */
  final class NonNumericItemFact private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends ItemFact(wrappedElem, childElems) {
  }

  /**
   * Numeric item fact in an XBRL instance, either top-level or nested
   *
   * @author Chris de Vreeze
   */
  abstract class NumericItemFact private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends ItemFact(wrappedElem, childElems) {

    require(attributeOption(UnitRefEName).isDefined, s"Expected attribute $UnitRefEName")

    def unitRef: String = attribute(UnitRefEName)
  }

  /**
   * Non-fraction numeric item fact in an XBRL instance, either top-level or nested
   *
   * @author Chris de Vreeze
   */
  final class NonFractionNumericItemFact private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(wrappedElem, childElems) {

    def precisionOption: Option[String] = attributeOption(PrecisionEName)

    def decimalsOption: Option[String] = attributeOption(DecimalsEName)
  }

  /**
   * Fraction item fact in an XBRL instance, either top-level or nested
   *
   * @author Chris de Vreeze
   */
  final class FractionItemFact private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(wrappedElem, childElems) {

    require(findAllChildElems.map(_.resolvedName).toSet == Set(XbrliNumeratorEName, XbrliDenominatorEName))

    def numerator: BigDecimal = {
      val s = getChildElem(XbrliNumeratorEName).text
      BigDecimal(s)
    }

    def denominator: BigDecimal = {
      val s = getChildElem(XbrliDenominatorEName).text
      BigDecimal(s)
    }
  }

  /**
   * Tuple fact in an XBRL instance, either top-level or nested
   *
   * @author Chris de Vreeze
   */
  final class TupleFact private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(wrappedElem, childElems) {

    def findAllChildFacts: immutable.IndexedSeq[Fact] = {
      findAllChildElems collect { case e: Fact => e }
    }

    def findAllFacts: immutable.IndexedSeq[Fact] = {
      findAllElems collect { case e: Fact => e }
    }

    def filterChildFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
      findAllChildFacts filter (e => p(e))
    }

    def filterFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
      findAllFacts filter (e => p(e))
    }
  }

  /**
   * FootnoteLink in an XBRL instance
   *
   * @author Chris de Vreeze
   */
  final class FootnoteLink private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == LinkFootnoteLinkEName, s"Expected EName $LinkFootnoteLinkEName but found $resolvedName")
  }

  /**
   * Entity in an XBRL instance context
   *
   * @author Chris de Vreeze
   */
  final class Entity private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == XbrliEntityEName, s"Expected EName $XbrliEntityEName but found $resolvedName")

    def identifier: Identifier = {
      getChildElem(XbrliIdentifierEName).asInstanceOf[Identifier]
    }

    def segmentOption: Option[Segment] = {
      findChildElem(XbrliSegmentEName) collect { case e: Segment => e }
    }
  }

  /**
   * Period in an XBRL instance context
   *
   * TODO sub-traits
   *
   * @author Chris de Vreeze
   */
  final class Period private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == XbrliPeriodEName, s"Expected EName $XbrliPeriodEName but found $resolvedName")

    def isInstant: Boolean = {
      findChildElem(XbrliInstantEName).isDefined
    }

    def isFiniteDuration: Boolean = {
      findChildElem(XbrliStartDateEName).isDefined
    }

    def isForever: Boolean = {
      findChildElem(XbrliForeverEName).isDefined
    }
  }

  /**
   * Scenario in an XBRL instance context
   *
   * @author Chris de Vreeze
   */
  final class Scenario private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == XbrliScenarioEName, s"Expected EName $XbrliScenarioEName but found $resolvedName")
  }

  /**
   * Segment in an XBRL instance context entity
   *
   * @author Chris de Vreeze
   */
  final class Segment private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == XbrliSegmentEName, s"Expected EName $XbrliSegmentEName but found $resolvedName")
  }

  /**
   * Identifier in an XBRL instance context entity
   *
   * @author Chris de Vreeze
   */
  final class Identifier private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == XbrliIdentifierEName, s"Expected EName $XbrliIdentifierEName but found $resolvedName")
  }

  /**
   * Divide in an XBRL instance unit
   *
   * @author Chris de Vreeze
   */
  final class Divide private[xbrli] (
    override val wrappedElem: DomElem,
    childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

    require(resolvedName == XbrliDivideEName, s"Expected EName $XbrliDivideEName but found $resolvedName")

    def numerator: immutable.IndexedSeq[EName] = {
      val unitNumerator = getChildElem(XbrliUnitNumeratorEName)
      val result = unitNumerator.filterChildElems(XbrliMeasureEName).map(e => e.getTextAsEName)
      result
    }

    def denominator: immutable.IndexedSeq[EName] = {
      val unitDenominator = getChildElem(XbrliUnitDenominatorEName)
      val result = unitDenominator.filterChildElems(XbrliMeasureEName).map(e => e.getTextAsEName)
      result
    }
  }

  object XbrliElem {

    /**
     * Expensive method to create an XbrliElem tree
     */
    def apply(elem: DomElem): XbrliElem = {
      // Recursive calls
      val childElems = elem.findAllChildElems.map(e => apply(e))
      apply(elem, childElems)
    }

    private[xbrli] def apply(elem: DomElem, childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = elem.elem.resolvedName match {
      case XbrliXbrlEName => new XbrlInstance(elem, childElems)
      case LinkSchemaRefEName => new SchemaRef(elem, childElems)
      case LinkLinkbaseRefEName => new LinkbaseRef(elem, childElems)
      case LinkRoleRefEName => new RoleRef(elem, childElems)
      case LinkArcroleRefEName => new ArcroleRef(elem, childElems)
      case XbrliContextEName => new XbrliContext(elem, childElems)
      case XbrliUnitEName => new XbrliUnit(elem, childElems)
      case LinkFootnoteLinkEName => new FootnoteLink(elem, childElems)
      case XbrliEntityEName => new Entity(elem, childElems)
      case XbrliPeriodEName => new Period(elem, childElems)
      case XbrliScenarioEName => new Scenario(elem, childElems)
      case XbrliSegmentEName => new Segment(elem, childElems)
      case XbrliIdentifierEName => new Identifier(elem, childElems)
      case XbrliDivideEName => new Divide(elem, childElems)
      case _ if Fact.accepts(elem) => Fact(elem, childElems)
      case _ => new XbrliElem(elem, childElems)
    }
  }

  object Fact {

    def accepts(elem: DomElem): Boolean = ItemFact.accepts(elem) || TupleFact.accepts(elem)

    private[xbrli] def apply(elem: DomElem, childElems: immutable.IndexedSeq[XbrliElem]): Fact =
      if (ItemFact.accepts(elem)) ItemFact(elem, childElems) else TupleFact(elem, childElems)

    def isFactPath(path: Path): Boolean = {
      !path.isRoot &&
        !Set(Option(LinkNs), Option(XbrliNs)).contains(path.firstEntry.elementName.namespaceUriOption)
    }
  }

  object ItemFact {

    def accepts(elem: DomElem): Boolean = {
      Fact.isFactPath(elem.path) &&
        elem.elem.attributeOption(ContextRefEName).isDefined
    }

    private[xbrli] def apply(elem: DomElem, childElems: immutable.IndexedSeq[XbrliElem]): ItemFact = {
      require(Fact.isFactPath(elem.path))
      require(elem.elem.attributeOption(ContextRefEName).isDefined)

      val unitRefOption = elem.elem.attributeOption(UnitRefEName)

      if (unitRefOption.isEmpty) new NonNumericItemFact(elem, childElems)
      else {
        if (elem.elem.findChildElem(withEName(XbrliNumeratorEName)).isDefined) new FractionItemFact(elem, childElems)
        else new NonFractionNumericItemFact(elem, childElems)
      }
    }
  }

  object TupleFact {

    def accepts(elem: DomElem): Boolean = {
      Fact.isFactPath(elem.path) &&
        elem.elem.attributeOption(ContextRefEName).isEmpty
    }

    private[xbrli] def apply(elem: DomElem, childElems: immutable.IndexedSeq[XbrliElem]): TupleFact = {
      require(Fact.isFactPath(elem.path))
      require(elem.elem.attributeOption(ContextRefEName).isEmpty)

      new TupleFact(elem, childElems)
    }
  }

}


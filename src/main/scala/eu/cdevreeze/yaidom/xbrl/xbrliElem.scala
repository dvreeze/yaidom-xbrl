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

package eu.cdevreeze.yaidom.xbrl

import scala.BigDecimal
import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.ElemApi.anyElem
import eu.cdevreeze.yaidom.queryapi.HasENameApi.ToHasElemApi
import eu.cdevreeze.yaidom.queryapi.HasENameApi.withEName
import eu.cdevreeze.yaidom.queryapi.IsNavigable
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike

/**
 * XML element inside XBRL instance (or the entire XBRL instance itself). This API is immutable if the wrapped element is
 * immutable, which should be the case.
 *
 * The `SubtypeAwareElemApi` API is offered.
 *
 * Also note that the package-private constructor contains redundant data, in order to speed up (yaidom-based) querying.
 *
 * TODO Mind xsi:nil.
 *
 * @author Chris de Vreeze
 */
sealed class XbrliElem private[xbrl] (
  val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends ScopedElemLike[XbrliElem] with IsNavigable[XbrliElem] with SubtypeAwareElemLike[XbrliElem] {

  // require(childElems.map(_.bridgeElem.backingElem) == bridgeElem.findAllChildElems.map(_.backingElem))

  /**
   * Very fast implementation of findAllChildElems, for fast querying
   */
  final def findAllChildElems: immutable.IndexedSeq[XbrliElem] = childElems

  final def resolvedName: EName = bridgeElem.resolvedName

  final def resolvedAttributes: immutable.Iterable[(EName, String)] = bridgeElem.resolvedAttributes

  final def qname: QName = bridgeElem.qname

  final def attributes: immutable.Iterable[(QName, String)] = bridgeElem.attributes

  final def scope: Scope = bridgeElem.scope

  final def text: String = bridgeElem.text

  final def findChildElemByPathEntry(entry: Path.Entry): Option[XbrliElem] =
    childElems.find(e => e.localName == entry.elementName.localPart && e.bridgeElem.path.lastEntry == entry)

  override def equals(other: Any): Boolean = other match {
    case e: XbrliElem => bridgeElem.backingElem == e.bridgeElem.backingElem
    case _ => false
  }

  override def hashCode: Int = bridgeElem.backingElem.hashCode
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
final class XbrlInstance private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == XbrliXbrlEName, s"Expected EName $XbrliXbrlEName but found $resolvedName")

  val allContexts: immutable.IndexedSeq[XbrliContext] =
    findAllChildElemsOfType(classTag[XbrliContext])

  val allContextsById: Map[String, XbrliContext] =
    allContexts.groupBy(_.id) mapValues (_.head)

  val allUnits: immutable.IndexedSeq[XbrliUnit] =
    findAllChildElemsOfType(classTag[XbrliUnit])

  val allUnitsById: Map[String, XbrliUnit] =
    allUnits.groupBy(_.id) mapValues (_.head)

  val allTopLevelFacts: immutable.IndexedSeq[Fact] =
    findAllChildElemsOfType(classTag[Fact])

  val allTopLevelItems: immutable.IndexedSeq[ItemFact] =
    findAllChildElemsOfType(classTag[ItemFact])

  val allTopLevelTuples: immutable.IndexedSeq[TupleFact] =
    findAllChildElemsOfType(classTag[TupleFact])

  val allTopLevelFactsByEName: Map[EName, immutable.IndexedSeq[Fact]] =
    allTopLevelFacts groupBy (_.resolvedName)

  val allTopLevelItemsByEName: Map[EName, immutable.IndexedSeq[ItemFact]] =
    allTopLevelItems groupBy (_.resolvedName)

  val allTopLevelTuplesByEName: Map[EName, immutable.IndexedSeq[TupleFact]] =
    allTopLevelTuples groupBy (_.resolvedName)

  def filterTopLevelFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
    filterChildElemsOfType(classTag[Fact])(p)
  }

  def filterTopLevelItems(p: ItemFact => Boolean): immutable.IndexedSeq[ItemFact] = {
    filterChildElemsOfType(classTag[ItemFact])(p)
  }

  def filterTopLevelTuples(p: TupleFact => Boolean): immutable.IndexedSeq[TupleFact] = {
    filterChildElemsOfType(classTag[TupleFact])(p)
  }

  def findAllFacts: immutable.IndexedSeq[Fact] = {
    findAllElemsOfType(classTag[Fact])
  }

  def findAllItems: immutable.IndexedSeq[ItemFact] = {
    findAllElemsOfType(classTag[ItemFact])
  }

  def findAllTuples: immutable.IndexedSeq[TupleFact] = {
    findAllElemsOfType(classTag[TupleFact])
  }

  def filterFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
    filterElemsOfType(classTag[Fact])(p)
  }

  def filterItems(p: ItemFact => Boolean): immutable.IndexedSeq[ItemFact] = {
    filterElemsOfType(classTag[ItemFact])(p)
  }

  def filterTuples(p: TupleFact => Boolean): immutable.IndexedSeq[TupleFact] = {
    filterElemsOfType(classTag[TupleFact])(p)
  }

  def findAllSchemaRefs: immutable.IndexedSeq[SchemaRef] = {
    findAllChildElemsOfType(classTag[SchemaRef])
  }

  def findAllLinkbaseRefs: immutable.IndexedSeq[LinkbaseRef] = {
    findAllChildElemsOfType(classTag[LinkbaseRef])
  }

  def findAllRoleRefs: immutable.IndexedSeq[RoleRef] = {
    findAllChildElemsOfType(classTag[RoleRef])
  }

  def findAllArcroleRefs: immutable.IndexedSeq[ArcroleRef] = {
    findAllChildElemsOfType(classTag[ArcroleRef])
  }

  def findAllFootnoteLinks: immutable.IndexedSeq[FootnoteLink] = {
    findAllChildElemsOfType(classTag[FootnoteLink])
  }
}

/**
 * SchemaRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class SchemaRef private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == LinkSchemaRefEName, s"Expected EName $LinkSchemaRefEName but found $resolvedName")
}

/**
 * LinkbaseRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class LinkbaseRef private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == LinkLinkbaseRefEName, s"Expected EName $LinkLinkbaseRefEName but found $resolvedName")
}

/**
 * RoleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class RoleRef private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == LinkRoleRefEName, s"Expected EName $LinkRoleRefEName but found $resolvedName")
}

/**
 * ArcroleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class ArcroleRef private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == LinkArcroleRefEName, s"Expected EName $LinkArcroleRefEName but found $resolvedName")
}

/**
 * Context in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class XbrliContext private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == XbrliContextEName, s"Expected EName $XbrliContextEName but found $resolvedName")

  def id: String = attribute(IdEName)

  def entity: Entity = {
    getChildElemOfType(classTag[Entity])(anyElem)
  }

  def period: Period = {
    getChildElemOfType(classTag[Period])(anyElem)
  }

  def scenarioOption: Option[Scenario] = {
    findChildElemOfType(classTag[Scenario])(anyElem)
  }
}

/**
 * Unit in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class XbrliUnit private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == XbrliUnitEName, s"Expected EName $XbrliUnitEName but found $resolvedName")

  def id: String = attribute(IdEName)

  def measures: immutable.IndexedSeq[EName] = {
    filterChildElems(XbrliMeasureEName) map (e => e.textAsResolvedQName)
  }

  def divide: Divide = {
    getChildElemOfType(classTag[Divide])(anyElem)
  }
}

/**
 * Item or tuple fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
abstract class Fact private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  def isTopLevel: Boolean = bridgeElem.path.entries.size == 1
}

/**
 * Item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
abstract class ItemFact private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(bridgeElem, childElems) {

  require(attributeOption(ContextRefEName).isDefined, s"Expected attribute $ContextRefEName")

  def contextRef: String = attribute(ContextRefEName)
}

/**
 * Non-numeric item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
final class NonNumericItemFact private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends ItemFact(bridgeElem, childElems) {
}

/**
 * Numeric item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
abstract class NumericItemFact private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends ItemFact(bridgeElem, childElems) {

  require(attributeOption(UnitRefEName).isDefined, s"Expected attribute $UnitRefEName")

  def unitRef: String = attribute(UnitRefEName)
}

/**
 * Non-fraction numeric item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
final class NonFractionNumericItemFact private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(bridgeElem, childElems) {

  def precisionOption: Option[String] = attributeOption(PrecisionEName)

  def decimalsOption: Option[String] = attributeOption(DecimalsEName)
}

/**
 * Fraction item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
final class FractionItemFact private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(bridgeElem, childElems) {

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
final class TupleFact private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(bridgeElem, childElems) {

  def findAllChildFacts: immutable.IndexedSeq[Fact] = {
    findAllChildElemsOfType(classTag[Fact])
  }

  def findAllFacts: immutable.IndexedSeq[Fact] = {
    findAllElemsOfType(classTag[Fact])
  }

  def filterChildFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
    filterChildElemsOfType(classTag[Fact])(p)
  }

  def filterFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
    filterElemsOfType(classTag[Fact])(p)
  }
}

/**
 * FootnoteLink in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class FootnoteLink private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == LinkFootnoteLinkEName, s"Expected EName $LinkFootnoteLinkEName but found $resolvedName")
}

/**
 * Entity in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class Entity private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == XbrliEntityEName, s"Expected EName $XbrliEntityEName but found $resolvedName")

  def identifier: Identifier = {
    getChildElemOfType(classTag[Identifier])(anyElem)
  }

  def segmentOption: Option[Segment] = {
    findChildElemOfType(classTag[Segment])(anyElem)
  }
}

/**
 * Period in an XBRL instance context
 *
 * TODO sub-traits
 *
 * @author Chris de Vreeze
 */
final class Period private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

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
final class Scenario private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == XbrliScenarioEName, s"Expected EName $XbrliScenarioEName but found $resolvedName")
}

/**
 * Segment in an XBRL instance context entity
 *
 * @author Chris de Vreeze
 */
final class Segment private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == XbrliSegmentEName, s"Expected EName $XbrliSegmentEName but found $resolvedName")
}

/**
 * Identifier in an XBRL instance context entity
 *
 * @author Chris de Vreeze
 */
final class Identifier private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == XbrliIdentifierEName, s"Expected EName $XbrliIdentifierEName but found $resolvedName")
}

/**
 * Divide in an XBRL instance unit
 *
 * @author Chris de Vreeze
 */
final class Divide private[xbrl] (
  override val bridgeElem: BridgeElem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(bridgeElem, childElems) {

  require(resolvedName == XbrliDivideEName, s"Expected EName $XbrliDivideEName but found $resolvedName")

  def numerator: immutable.IndexedSeq[EName] = {
    val unitNumerator = getChildElem(XbrliUnitNumeratorEName)
    val result = unitNumerator.filterChildElems(XbrliMeasureEName).map(e => e.textAsResolvedQName)
    result
  }

  def denominator: immutable.IndexedSeq[EName] = {
    val unitDenominator = getChildElem(XbrliUnitDenominatorEName)
    val result = unitDenominator.filterChildElems(XbrliMeasureEName).map(e => e.textAsResolvedQName)
    result
  }
}

object XbrliElem {

  /**
   * Expensive method to create an XbrliElem tree
   */
  def apply(elem: BridgeElem): XbrliElem = {
    // Recursive calls
    val childElems = elem.findAllChildElems.map(e => apply(e))
    apply(elem, childElems)
  }

  private[xbrl] def apply(elem: BridgeElem, childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = elem.resolvedName match {
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

  def accepts(elem: BridgeElem): Boolean = ItemFact.accepts(elem) || TupleFact.accepts(elem)

  private[xbrl] def apply(elem: BridgeElem, childElems: immutable.IndexedSeq[XbrliElem]): Fact =
    if (ItemFact.accepts(elem)) ItemFact(elem, childElems) else TupleFact(elem, childElems)

  def isFactPath(path: Path): Boolean = {
    !path.isRoot &&
      !Set(Option(LinkNs), Option(XbrliNs)).contains(path.firstEntry.elementName.namespaceUriOption)
  }
}

object ItemFact {

  def accepts(elem: BridgeElem): Boolean = {
    Fact.isFactPath(elem.path) &&
      elem.backingElem.attributeOption(ContextRefEName).isDefined
  }

  private[xbrl] def apply(elem: BridgeElem, childElems: immutable.IndexedSeq[XbrliElem]): ItemFact = {
    require(Fact.isFactPath(elem.path))
    require(elem.backingElem.attributeOption(ContextRefEName).isDefined)

    val unitRefOption = elem.backingElem.attributeOption(UnitRefEName)

    if (unitRefOption.isEmpty) new NonNumericItemFact(elem, childElems)
    else {
      if (elem.backingElem.findChildElem(withEName(XbrliNumeratorEName)).isDefined) new FractionItemFact(elem, childElems)
      else new NonFractionNumericItemFact(elem, childElems)
    }
  }
}

object TupleFact {

  def accepts(elem: BridgeElem): Boolean = {
    Fact.isFactPath(elem.path) &&
      elem.backingElem.attributeOption(ContextRefEName).isEmpty
  }

  private[xbrl] def apply(elem: BridgeElem, childElems: immutable.IndexedSeq[XbrliElem]): TupleFact = {
    require(Fact.isFactPath(elem.path))
    require(elem.backingElem.attributeOption(ContextRefEName).isEmpty)

    new TupleFact(elem, childElems)
  }
}

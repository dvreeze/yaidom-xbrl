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
package defaultimpl

import scala.collection.immutable
import ElemApi._

/**
 * Yaidom indexed-Elem-backed XML element inside XBRL instance (or the entire XBRL instance itself).
 *
 * @author Chris de Vreeze
 */
sealed class XbrliElem private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends yaidomimpl.XbrliElem {

  require(childElems.map(_.wrappedElem) == wrappedElem.findAllChildElems)
  require(wrappedElem.rootElem.resolvedName == XbrliXbrlEName)

  final type E = indexed.Elem

  final def toElem: Elem = wrappedElem.elem

  /**
   * Very fast implementation of findAllChildElems, for fast querying
   */
  final def findAllChildElems: immutable.IndexedSeq[XbrliElem] = childElems

  final def getTextAsEName: EName = wrappedElem.elem.textAsResolvedQName
}

/**
 * Yaidom indexed-Elem-backed XBRL instance.
 *
 * @author Chris de Vreeze
 */
final class XbrlInstance private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.XbrlInstance {

  require(resolvedName == XbrliXbrlEName, s"Expected EName $XbrliXbrlEName but found $resolvedName")

  val allContexts: immutable.IndexedSeq[XbrliContext] = findAllContexts collect { case e: XbrliContext => e }

  val allContextsById: Map[String, XbrliContext] = findAllContextsById collect { case (id, e: XbrliContext) => (id, e) }

  val allUnits: immutable.IndexedSeq[XbrliUnit] = findAllUnits collect { case e: XbrliUnit => e }

  val allUnitsById: Map[String, XbrliUnit] = findAllUnitsById collect { case (id, e: XbrliUnit) => (id, e) }

  val allTopLevelFacts: immutable.IndexedSeq[Fact] = findAllTopLevelFacts collect { case e: Fact => e }

  val allTopLevelItems: immutable.IndexedSeq[ItemFact] = findAllTopLevelItems collect { case e: ItemFact => e }

  val allTopLevelTuples: immutable.IndexedSeq[TupleFact] = findAllTopLevelTuples collect { case e: TupleFact => e }

  val allTopLevelFactsByEName: Map[EName, immutable.IndexedSeq[Fact]] =
    findAllTopLevelFactsByEName collect { case (ename, xs: immutable.IndexedSeq[Fact]) => (ename, xs) }

  val allTopLevelItemsByEName: Map[EName, immutable.IndexedSeq[ItemFact]] =
    findAllTopLevelItemsByEName collect { case (ename, xs: immutable.IndexedSeq[ItemFact]) => (ename, xs) }

  val allTopLevelTuplesByEName: Map[EName, immutable.IndexedSeq[TupleFact]] =
    findAllTopLevelTuplesByEName collect { case (ename, xs: immutable.IndexedSeq[TupleFact]) => (ename, xs) }
}

final class SchemaRef private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.SchemaRef {

  require(resolvedName == LinkSchemaRefEName, s"Expected EName $LinkSchemaRefEName but found $resolvedName")
}

final class LinkbaseRef private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.LinkbaseRef {

  require(resolvedName == LinkLinkbaseRefEName, s"Expected EName $LinkLinkbaseRefEName but found $resolvedName")
}

final class RoleRef private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.RoleRef {

  require(resolvedName == LinkRoleRefEName, s"Expected EName $LinkRoleRefEName but found $resolvedName")
}

final class ArcroleRef private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.ArcroleRef {

  require(resolvedName == LinkArcroleRefEName, s"Expected EName $LinkArcroleRefEName but found $resolvedName")
}

final class XbrliContext private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.XbrliContext {

  require(resolvedName == XbrliContextEName, s"Expected EName $XbrliContextEName but found $resolvedName")
}

final class XbrliUnit private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.XbrliUnit {

  require(resolvedName == XbrliUnitEName, s"Expected EName $XbrliUnitEName but found $resolvedName")
}

abstract class Fact private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.Fact {

  def isTopLevel: Boolean = wrappedElem.path.entries.size == 1
}

abstract class ItemFact private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(wrappedElem, childElems) with yaidomimpl.ItemFact {

  require(attributeOption(ContextRefEName).isDefined, s"Expected attribute $ContextRefEName")
}

final class NonNumericItemFact private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends ItemFact(wrappedElem, childElems) with yaidomimpl.NonNumericItemFact

abstract class NumericItemFact private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends ItemFact(wrappedElem, childElems) with yaidomimpl.NumericItemFact {

  require(attributeOption(UnitRefEName).isDefined, s"Expected attribute $UnitRefEName")
}

final class NonFractionNumericItemFact private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(wrappedElem, childElems) with yaidomimpl.NonFractionNumericItemFact

final class FractionItemFact private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(wrappedElem, childElems) with yaidomimpl.FractionItemFact {

  require(findAllChildElems.map(_.resolvedName).toSet == Set(XbrliNumeratorEName, XbrliDenominatorEName))
}

final class TupleFact private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(wrappedElem, childElems) with yaidomimpl.TupleFact

final class FootnoteLink private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.FootnoteLink {

  require(resolvedName == LinkFootnoteLinkEName, s"Expected EName $LinkFootnoteLinkEName but found $resolvedName")
}

final class Entity private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.Entity {

  require(resolvedName == XbrliEntityEName, s"Expected EName $XbrliEntityEName but found $resolvedName")
}

final class Period private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.Period {

  require(resolvedName == XbrliPeriodEName, s"Expected EName $XbrliPeriodEName but found $resolvedName")
}

final class Scenario private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.Scenario {

  require(resolvedName == XbrliScenarioEName, s"Expected EName $XbrliScenarioEName but found $resolvedName")
}

final class Segment private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.Segment {

  require(resolvedName == XbrliSegmentEName, s"Expected EName $XbrliSegmentEName but found $resolvedName")
}

final class Identifier private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.Identifier {

  require(resolvedName == XbrliIdentifierEName, s"Expected EName $XbrliIdentifierEName but found $resolvedName")
}

final class Divide private[defaultimpl] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) with yaidomimpl.Divide {

  require(resolvedName == XbrliDivideEName, s"Expected EName $XbrliDivideEName but found $resolvedName")
}

object XbrliElem {

  /**
   * Expensive method to create an XbrliElem tree
   */
  def apply(elem: indexed.Elem): XbrliElem = {
    // Recursive calls
    val childElems = elem.findAllChildElems.map(e => apply(e))
    apply(elem, childElems)
  }

  private[defaultimpl] def apply(elem: indexed.Elem, childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = elem.resolvedName match {
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

  def accepts(elem: indexed.Elem): Boolean = ItemFact.accepts(elem) || TupleFact.accepts(elem)

  private[defaultimpl] def apply(elem: indexed.Elem, childElems: immutable.IndexedSeq[XbrliElem]): Fact =
    if (ItemFact.accepts(elem)) ItemFact(elem, childElems) else TupleFact(elem, childElems)

  def isFactPath(path: Path): Boolean = {
    !path.isRoot &&
      !Set(Option(LinkNs), Option(XbrliNs)).contains(path.firstEntry.elementName.namespaceUriOption)
  }
}

object ItemFact {

  def accepts(elem: indexed.Elem): Boolean = {
    elem.rootElem.resolvedName == XbrliXbrlEName &&
      Fact.isFactPath(elem.path) &&
      elem.attributeOption(ContextRefEName).isDefined
  }

  private[defaultimpl] def apply(elem: indexed.Elem, childElems: immutable.IndexedSeq[XbrliElem]): ItemFact = {
    require(elem.rootElem.resolvedName == XbrliXbrlEName)
    require(Fact.isFactPath(elem.path))
    require(elem.attributeOption(ContextRefEName).isDefined)

    val unitRefOption = elem.attributeOption(UnitRefEName)

    if (unitRefOption.isEmpty) new NonNumericItemFact(elem, childElems)
    else {
      if (elem.findChildElem(withEName(XbrliNumeratorEName)).isDefined) new FractionItemFact(elem, childElems)
      else new NonFractionNumericItemFact(elem, childElems)
    }
  }
}

object TupleFact {

  def accepts(elem: indexed.Elem): Boolean = {
    elem.rootElem.resolvedName == XbrliXbrlEName &&
      Fact.isFactPath(elem.path) &&
      elem.attributeOption(ContextRefEName).isEmpty
  }

  private[defaultimpl] def apply(elem: indexed.Elem, childElems: immutable.IndexedSeq[XbrliElem]): TupleFact = {
    require(elem.rootElem.resolvedName == XbrliXbrlEName)
    require(Fact.isFactPath(elem.path))
    require(elem.attributeOption(ContextRefEName).isEmpty)

    new TupleFact(elem, childElems)
  }
}

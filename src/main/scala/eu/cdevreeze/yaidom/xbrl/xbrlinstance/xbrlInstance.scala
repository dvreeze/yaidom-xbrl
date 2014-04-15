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
package xbrl.xbrlinstance

import scala.collection.immutable
import ElemApi.withEName

/**
 * Immutable XML element inside XBRL instance (or the entire XBRL instance itself), offering the ElemApi query API itself.
 *
 * @author Chris de Vreeze
 */
sealed abstract class XbrliElem private[xbrlinstance] (
  val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends ElemLike[XbrliElem] {

  assert(childElems.map(_.wrappedElem) == wrappedElem.findAllChildElems)
  assert(wrappedElem.rootElem.resolvedName == XbrliXbrlEName)

  final override def findAllChildElems: immutable.IndexedSeq[XbrliElem] = childElems

  final override def resolvedName: EName = wrappedElem.resolvedName

  final override def resolvedAttributes: immutable.IndexedSeq[(EName, String)] = wrappedElem.resolvedAttributes
}

/**
 * Immutable XBRL instance. Expensive to create, because of the cached contexts, units and facts.
 *
 * This class does not check validity of the XBRL instance. Neither does it know about the DTS describing the XBRL instance.
 * It does, however, contain the entrypoint URI(s) to the DTS.
 *
 * Without any knowledge about the DTS, this XBRL instance class only recognizes (item and tuple) facts by looking at the
 * structure of the element and its ancestry. Attribute @contextRef is only allowed for item facts, and tuple facts can be
 * recognized by looking at the "path" of the element.
 *
 * @author Chris de Vreeze
 */
final class XbrlInstance private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  import XbrliElem._

  assert(wrappedElem.resolvedName == XbrliXbrlEName)

  val contextsById: Map[String, XbrliContext] = {
    val contextsGrouped =
      filterChildElems(withEName(XbrliContextEName)).groupBy(_.attribute(IdEName))
    require(contextsGrouped.values.forall(_.size == 1), s"All context @id attributes must be unique inside the XBRL instance")

    contextsGrouped.mapValues(e => e.head.asInstanceOf[XbrliContext])
  }

  def contexts: immutable.IndexedSeq[XbrliContext] = contextsById.values.toVector

  val unitsById: Map[String, XbrliUnit] = {
    val unitsGrouped =
      filterChildElems(withEName(XbrliUnitEName)).groupBy(_.attribute(IdEName))
    require(unitsGrouped.values.forall(_.size == 1), s"All unit @id attributes must be unique inside the XBRL instance")

    unitsGrouped.mapValues(e => e.head.asInstanceOf[XbrliUnit])
  }

  def units: immutable.IndexedSeq[XbrliUnit] = unitsById.values.toVector

  val factsByEName: Map[EName, immutable.IndexedSeq[Fact]] = {
    val result = filterElems(e => canBeFact(e.wrappedElem)) collect { case e: Fact => e }
    result.groupBy(_.resolvedName)
  }

  def facts: immutable.IndexedSeq[Fact] = factsByEName.values.toVector.flatten

  def items: immutable.IndexedSeq[ItemFact] =
    facts collect { case e: ItemFact => e }

  def tuples: immutable.IndexedSeq[TupleFact] =
    facts collect { case e: TupleFact => e }

  def topLevelFacts: immutable.IndexedSeq[Fact] =
    facts filter { e => e.wrappedElem.path.entries.size == 1 }

  def topLevelItems: immutable.IndexedSeq[ItemFact] =
    items filter { e => e.wrappedElem.path.entries.size == 1 }

  def topLevelTuples: immutable.IndexedSeq[TupleFact] =
    tuples filter { e => e.wrappedElem.path.entries.size == 1 }

  def filterFacts(ename: EName): immutable.IndexedSeq[Fact] =
    factsByEName.getOrElse(ename, Vector())

  def filterItems(ename: EName): immutable.IndexedSeq[ItemFact] =
    filterFacts(ename) collect { case e: ItemFact => e }

  def filterTuples(ename: EName): immutable.IndexedSeq[TupleFact] =
    filterFacts(ename) collect { case e: TupleFact => e }

  def filterTopLevelFacts(ename: EName): immutable.IndexedSeq[Fact] =
    filterFacts(ename) filter (_.isTopLevel)

  def filterTopLevelItems(ename: EName): immutable.IndexedSeq[ItemFact] =
    filterItems(ename) filter (_.isTopLevel)

  def filterTopLevelTuples(ename: EName): immutable.IndexedSeq[TupleFact] =
    filterTuples(ename) filter (_.isTopLevel)

  def schemaRefs: immutable.IndexedSeq[SchemaRef] =
    filterChildElems(withEName(LinkSchemaRefEName)) collect { case e: SchemaRef => e }

  def linkbaseRefs: immutable.IndexedSeq[LinkbaseRef] =
    filterChildElems(withEName(LinkLinkbaseRefEName)) collect { case e: LinkbaseRef => e }

  def roleRefs: immutable.IndexedSeq[RoleRef] =
    filterChildElems(withEName(LinkRoleRefEName)) collect { case e: RoleRef => e }

  def arcroleRefs: immutable.IndexedSeq[ArcroleRef] =
    filterChildElems(withEName(LinkArcroleRefEName)) collect { case e: ArcroleRef => e }

  def footnoteLinks: immutable.IndexedSeq[FootnoteLink] =
    filterChildElems(withEName(LinkFootnoteLinkEName)) collect { case e: FootnoteLink => e }
}

/**
 * SchemaRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class SchemaRef private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == LinkSchemaRefEName)
}

/**
 * LinkbaseRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class LinkbaseRef private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == LinkLinkbaseRefEName)
}

/**
 * RoleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class RoleRef private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == LinkRoleRefEName)
}

/**
 * ArcroleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class ArcroleRef private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == LinkArcroleRefEName)
}

/**
 * Context in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class XbrliContext private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == XbrliContextEName)
  require(wrappedElem.attributeOption(IdEName).isDefined, s"An xbrli:context must have attribute @id")

  def entity: XbrliEntity = getChildElem(XbrliEntityEName).asInstanceOf[XbrliEntity]

  def period: XbrliPeriod = getChildElem(XbrliPeriodEName).asInstanceOf[XbrliPeriod]

  def scenarioOption: Option[XbrliScenario] =
    findChildElem(XbrliScenarioEName) collect { case e: XbrliScenario => e }
}

/**
 * Unit in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class XbrliUnit private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == XbrliUnitEName)
  require(wrappedElem.attributeOption(IdEName).isDefined, s"An xbrli:unit must have attribute @id")
}

/**
 * Item or tuple fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
abstract class Fact private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  import XbrliElem._

  assert(canBeFact(wrappedElem))

  final def isTopLevel: Boolean = wrappedElem.path.entries.size == 1
}

/**
 * Item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
final class ItemFact private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(wrappedElem, childElems) {

  assert(attributeOption(ContextRefEName).isDefined)

  def contextRef: String = attribute(ContextRefEName)
}

/**
 * Tuple fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
final class TupleFact private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(wrappedElem, childElems) {

  import XbrliElem._

  assert(!attributeOption(ContextRefEName).isDefined)

  def facts: immutable.IndexedSeq[Fact] =
    filterElems(e => canBeFact(e.wrappedElem)) collect { case e: Fact => e }

  def childFacts: immutable.IndexedSeq[Fact] =
    filterChildElems(e => canBeFact(e.wrappedElem)) collect { case e: Fact => e }

  def filterFacts(ename: EName): immutable.IndexedSeq[Fact] =
    facts filter (_.resolvedName == ename)

  def filterChildFacts(ename: EName): immutable.IndexedSeq[Fact] =
    childFacts filter (_.resolvedName == ename)
}

/**
 * FootnoteLink in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class FootnoteLink private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == LinkFootnoteLinkEName)
}

/**
 * Entity in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class XbrliEntity private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == XbrliEntityEName)
}

/**
 * Period in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class XbrliPeriod private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == XbrliPeriodEName)

  def isInstant: Boolean = findChildElem(XbrliInstantEName).isDefined

  def isFiniteDuration: Boolean =
    findChildElem(XbrliStartDateEName).isDefined && findChildElem(XbrliEndDateEName).isDefined

  def isForever: Boolean = findChildElem(XbrliForeverEName).isDefined
}

/**
 * Scenario in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class XbrliScenario private[xbrlinstance] (
  override val wrappedElem: indexed.Elem,
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(wrappedElem, childElems) {

  assert(wrappedElem.resolvedName == XbrliScenarioEName)
}

object XbrliElem {

  /**
   * Returns true if the element can be a top-level fact, without consulting the taxonomy, but just by looking at the
   * structure of the XBRL instance itself. Only the Path of the element is taken into account.
   */
  def canBeTopLevelFact(wrappedElem: indexed.Elem): Boolean = {
    require(
      wrappedElem.rootElem.resolvedName == XbrliXbrlEName,
      s"The root element must be $XbrliXbrlEName but found ${wrappedElem.rootElem.resolvedName} instead")

    canBeFact(wrappedElem) && (wrappedElem.path.entries.size == 1)
  }

  /**
   * Returns true if the element can be a fact, without consulting the taxonomy, but just by looking at the
   * structure of the XBRL instance itself. Only the Path of the element is taken into account.
   */
  def canBeFact(wrappedElem: indexed.Elem): Boolean = {
    require(
      wrappedElem.rootElem.resolvedName == XbrliXbrlEName,
      s"The root element must be $XbrliXbrlEName but found ${wrappedElem.rootElem.resolvedName} instead")

    val path = wrappedElem.path

    (path.entries.size >= 1) && {
      val topLevelAncestorPath = path.findAncestorOrSelfPath(_.entries.size == 1).get
      val namespaceUriOption = topLevelAncestorPath.elementNameOption.get.namespaceUriOption

      (namespaceUriOption != Some(LinkNs)) && (namespaceUriOption != Some(XbrliNs))
    }
  }

  /**
   * Expensive recursive factory method for XbrliElem instances.
   */
  def apply(wrappedElem: indexed.Elem): XbrliElem = {
    require(
      wrappedElem.rootElem.resolvedName == XbrliXbrlEName,
      s"The root element must be $XbrliXbrlEName but found ${wrappedElem.rootElem.resolvedName} instead")

    // Recursive calls
    val childElems = wrappedElem.findAllChildElems.map(e => apply(e))

    wrappedElem.resolvedName match {
      case XbrliXbrlEName => new XbrlInstance(wrappedElem, childElems)
      case LinkSchemaRefEName => new SchemaRef(wrappedElem, childElems)
      case LinkLinkbaseRefEName => new LinkbaseRef(wrappedElem, childElems)
      case LinkRoleRefEName => new RoleRef(wrappedElem, childElems)
      case LinkArcroleRefEName => new ArcroleRef(wrappedElem, childElems)
      case XbrliContextEName => new XbrliContext(wrappedElem, childElems)
      case XbrliUnitEName => new XbrliUnit(wrappedElem, childElems)
      case LinkFootnoteLinkEName => new FootnoteLink(wrappedElem, childElems)
      case XbrliEntityEName => new XbrliEntity(wrappedElem, childElems)
      case XbrliPeriodEName => new XbrliPeriod(wrappedElem, childElems)
      case XbrliScenarioEName => new XbrliScenario(wrappedElem, childElems)
      case _ => wrappedElem match {
        case e if canBeFact(e) =>
          if (e.attributeOption(ContextRefEName).isDefined) new ItemFact(e, childElems)
          else new TupleFact(e, childElems)
        case _ => new XbrliElem(wrappedElem, childElems) {}
      }
    }
  }
}

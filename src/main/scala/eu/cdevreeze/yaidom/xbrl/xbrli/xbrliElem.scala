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
package xbrl.xbrli

import scala.collection.immutable

/**
 * XML element inside XBRL instance (or the entire XBRL instance itself). Typical implementations of this API are entirely
 * immutable.
 *
 * @author Chris de Vreeze
 */
trait XbrliElem {

  /**
   * The underlying DOM tree type, offering the ElemApi query API itself
   */
  type E <: ElemApi[E] with HasText

  def wrappedElem: E
}

/**
 * XBRL instance.
 *
 * Implementations typically do not check validity of the XBRL instance. Neither do they know about the DTS describing the XBRL instance.
 * They do, however, contain the entrypoint URI(s) to the DTS.
 *
 * Without any knowledge about the DTS, implementations only recognize (item and tuple) facts by looking at the
 * structure of the element and its ancestry. Attribute @contextRef is only allowed for item facts, and tuple facts can be
 * recognized by looking at the "path" of the element.
 *
 * @author Chris de Vreeze
 */
trait XbrlInstance extends XbrliElem {

  def findAllContexts: immutable.IndexedSeq[XbrliContext]

  def findAllContextsById: Map[String, XbrliContext]

  def findAllUnits: immutable.IndexedSeq[XbrliUnit]

  def findAllUnitsById: Map[String, XbrliUnit]

  def findAllFacts: immutable.IndexedSeq[Fact]

  def findAllItems: immutable.IndexedSeq[ItemFact]

  def findAllTuples: immutable.IndexedSeq[TupleFact]

  def findAllTopLevelFacts: immutable.IndexedSeq[Fact]

  def findAllTopLevelItems: immutable.IndexedSeq[ItemFact]

  def findAllTopLevelTuples: immutable.IndexedSeq[TupleFact]

  def findAllTopLevelFactsByEName: Map[EName, immutable.IndexedSeq[Fact]]

  def findAllTopLevelItemsByEName: Map[EName, immutable.IndexedSeq[ItemFact]]

  def findAllTopLevelTuplesByEName: Map[EName, immutable.IndexedSeq[TupleFact]]

  def filterFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact]

  def filterItems(p: ItemFact => Boolean): immutable.IndexedSeq[ItemFact]

  def filterTuples(p: TupleFact => Boolean): immutable.IndexedSeq[TupleFact]

  def filterTopLevelFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact]

  def filterTopLevelItems(p: ItemFact => Boolean): immutable.IndexedSeq[ItemFact]

  def filterTopLevelTuples(p: TupleFact => Boolean): immutable.IndexedSeq[TupleFact]

  def findAllSchemaRefs: immutable.IndexedSeq[SchemaRef]

  def findAllLinkbaseRefs: immutable.IndexedSeq[LinkbaseRef]

  def findAllRoleRefs: immutable.IndexedSeq[RoleRef]

  def findAllArcroleRefs: immutable.IndexedSeq[ArcroleRef]

  def findAllFootnoteLinks: immutable.IndexedSeq[FootnoteLink]
}

/**
 * SchemaRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
trait SchemaRef extends XbrliElem

/**
 * LinkbaseRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
trait LinkbaseRef extends XbrliElem

/**
 * RoleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
trait RoleRef extends XbrliElem

/**
 * ArcroleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
trait ArcroleRef extends XbrliElem

/**
 * Context in an XBRL instance
 *
 * @author Chris de Vreeze
 */
trait XbrliContext extends XbrliElem {

  def id: String

  def entity: Entity

  def period: Period

  def scenarioOption: Option[Scenario]
}

/**
 * Unit in an XBRL instance
 *
 * @author Chris de Vreeze
 */
trait XbrliUnit extends XbrliElem {

  def id: String

  def measures: immutable.IndexedSeq[EName]

  def divide: Divide
}

/**
 * Item or tuple fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
trait Fact extends XbrliElem {

  def isTopLevel: Boolean
}

/**
 * Item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
trait ItemFact extends Fact {

  def contextRef: String
}

/**
 * Non-numeric item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
trait NonNumericItemFact extends ItemFact

/**
 * Numeric item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
trait NumericItemFact extends ItemFact {

  def unitRef: String
}

/**
 * Non-fraction numeric item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
trait NonFractionNumericItemFact extends NumericItemFact {

  def precisionOption: Option[String]

  def decimalsOption: Option[String]
}

/**
 * Fraction item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
trait FractionItemFact extends NumericItemFact {

  def numerator: BigDecimal

  def denominator: BigDecimal
}

/**
 * Tuple fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
trait TupleFact extends Fact {

  def findAllChildFacts: immutable.IndexedSeq[Fact]

  def findAllFacts: immutable.IndexedSeq[Fact]

  def filterChildFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact]

  def filterFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact]
}

/**
 * FootnoteLink in an XBRL instance
 *
 * @author Chris de Vreeze
 */
trait FootnoteLink extends XbrliElem

/**
 * Entity in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
trait Entity extends XbrliElem {

  def identifier: Identifier

  def segmentOption: Option[Segment]
}

/**
 * Period in an XBRL instance context
 *
 * TODO sub-traits
 *
 * @author Chris de Vreeze
 */
trait Period extends XbrliElem {

  def isInstant: Boolean

  def isFiniteDuration: Boolean

  def isForever: Boolean
}

/**
 * Scenario in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
trait Scenario extends XbrliElem

/**
 * Segment in an XBRL instance context entity
 *
 * @author Chris de Vreeze
 */
trait Segment extends XbrliElem

/**
 * Identifier in an XBRL instance context entity
 *
 * @author Chris de Vreeze
 */
trait Identifier extends XbrliElem

/**
 * Divide in an XBRL instance unit
 *
 * @author Chris de Vreeze
 */
trait Divide extends XbrliElem {

  def numerator: immutable.IndexedSeq[EName]

  def denominator: immutable.IndexedSeq[EName]
}

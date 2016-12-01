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

package eu.cdevreeze.tqa.model.taxonomydom

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.collection.immutable

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.XmlFragmentKey.XmlFragmentKeyAware
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import eu.cdevreeze.yaidom.queryapi.Nodes
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike

/**
 * Any taxonomy XML element, for taxonomies without XLink locators, XLink simple links, schema import hrefs and schema
 * includes. These classes are reasonably lenient when instantiating them (although schema validity helps), but query
 * methods may fail if the taxonomy XML is not schema-valid (against the schemas for this higher level taxonomy model).
 *
 * @author Chris de Vreeze
 */
sealed abstract class TaxonomyElem private[taxonomydom] (
  val backingElem: BackingElemApi,
  val childElems: immutable.IndexedSeq[TaxonomyElem]) extends AnyTaxonomyElem with Nodes.Elem with ScopedElemLike with SubtypeAwareElemLike {

  type ThisElem = TaxonomyElem

  assert(childElems.map(_.backingElem) == backingElem.findAllChildElems, msg("Corrupt element!"))

  // Implementations of abstract query API methods, and overridden equals and hashCode methods

  final def thisElem: ThisElem = this

  /**
   * Returns all child elements, and returns them extremely fast. This is important for fast querying, at the
   * expense of more expensive recursive creation.
   */
  final def findAllChildElems: immutable.IndexedSeq[TaxonomyElem] = childElems

  final def resolvedName: EName = backingElem.resolvedName

  final def resolvedAttributes: immutable.IndexedSeq[(EName, String)] = backingElem.resolvedAttributes.toIndexedSeq

  final def text: String = backingElem.text

  final def qname: QName = backingElem.qname

  final def attributes: immutable.IndexedSeq[(QName, String)] = backingElem.attributes.toIndexedSeq

  final def scope: Scope = backingElem.scope

  final override def equals(obj: Any): Boolean = obj match {
    case other: TaxonomyElem =>
      (other.backingElem == this.backingElem)
    case _ => false
  }

  final override def hashCode: Int = backingElem.hashCode

  // Other public methods

  final def key: XmlFragmentKey = backingElem.key

  // Internal functions

  protected final def msg(s: String): String = s"${s} (${key})"
}

// Root elements, like linkbase or schema root elements.

sealed trait TaxonomyRootElem extends TaxonomyElem

// XLink elements in taxonomies.

sealed trait TaxonomyXLinkElem extends TaxonomyElem

sealed trait TaxonomyExtendedLinkElem extends TaxonomyXLinkElem

sealed trait TaxonomyXLinkArcElem extends TaxonomyXLinkElem

sealed trait TaxonomyXLinkResourceElem extends TaxonomyXLinkElem

// No XLink simple links and locators in this model!

// Schema content (restricted) or linkbase content (in the adapted linkbase model).

sealed trait XsdElem extends TaxonomyElem

sealed trait LinkElem extends TaxonomyElem

// The class inheritance hierarchy, under TaxonomyElem. First the root elements.

final class XsdRootElem private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem with TaxonomyRootElem

final class LinkbaseRootElem private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with TaxonomyRootElem

// The remaining classes. First for schema content, then for linkbase content.

sealed trait Particle extends XsdElem

// Element declarations or references.

sealed trait ElementDeclarationOrReference extends XsdElem

sealed trait ElementDeclaration extends ElementDeclarationOrReference

final class GlobalElementDeclaration private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclaration

final class LocalElementDeclaration private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclaration with Particle

final class ElementReference private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclarationOrReference

// Attribute declarations or references.

sealed trait AttributeDeclarationOrReference extends XsdElem

sealed trait AttributeDeclaration extends AttributeDeclarationOrReference

final class GlobalAttributeDeclaration private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration

final class LocalAttributeDeclaration private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration

final class AttributeReference private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclarationOrReference

// Type definitions.

sealed trait TypeDefinition extends XsdElem

sealed trait NamedTypeDefinition extends TypeDefinition

sealed trait AnonymousTypeDefinition extends TypeDefinition

sealed trait SimpleTypeDefinition extends TypeDefinition

sealed trait ComplexTypeDefinition extends TypeDefinition

final class NamedSimpleTypeDefinition private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with NamedTypeDefinition with SimpleTypeDefinition

final class AnonymousSimpleTypeDefinition private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AnonymousTypeDefinition with SimpleTypeDefinition

final class NamedComplexTypeDefinition private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with NamedTypeDefinition with ComplexTypeDefinition

final class AnonymousComplexTypeDefinition private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AnonymousTypeDefinition with ComplexTypeDefinition

// Attribute groups.

sealed trait AttributeGroupDefinitionOrReference extends XsdElem

final class AttributeGroupDefinition private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeGroupDefinitionOrReference

final class AttributeGroupReference private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeGroupDefinitionOrReference

// Model groups.

sealed trait ModelGroupDefinitionOrReference extends XsdElem

final class ModelGroupDefinition private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroupDefinitionOrReference

final class ModelGroupReference private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroupDefinitionOrReference

// Ignoring identity constraints, notations, model groups, wildcards, complex/simple content.

final class Annotation private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

final class Appinfo private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

final class Import private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

// No includes, and certainly no redefines.

final class OtherXsdElem private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

// TODO Linkbase content.

final class OtherLinkbaseElem private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem

final class OtherElem private[taxonomydom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems)


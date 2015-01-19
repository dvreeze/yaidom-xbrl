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

package eu.cdevreeze.xbrl.taxomodel

import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.ElemApi.anyElem
import eu.cdevreeze.yaidom.queryapi.IsNavigable
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike
import eu.cdevreeze.yaidom.simple
import TaxonomyElem._

/**
 * Read-only YATM taxonomy element, backed by a simple Elem (so without context).
 *
 * Having no ancestry context, the taxonomy elements must be sufficiently stand-alone to be of any use.
 * For example, relationships know the link role of the containing link, and global element declarations know
 * the target namespace, even if the ancestry context is unknown. Indeed, YATM has been designed with this in mind.
 * The advantage of using simple Elems as backing elements is composability.
 *
 * @author Chris de Vreeze
 */
sealed abstract class TaxonomyElem private[taxomodel] (
  val simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends ScopedElemLike[TaxonomyElem] with IsNavigable[TaxonomyElem] with SubtypeAwareElemLike[TaxonomyElem] {

  require(childElems.map(_.simpleElem) == simpleElem.findAllChildElems)

  /**
   * Very fast implementation of findAllChildElems, for fast querying
   */
  final def findAllChildElems: immutable.IndexedSeq[TaxonomyElem] = childElems

  final def resolvedName: EName = simpleElem.resolvedName

  final def resolvedAttributes: immutable.Iterable[(EName, String)] = simpleElem.resolvedAttributes

  final def qname: QName = simpleElem.qname

  final def attributes: immutable.Iterable[(QName, String)] = simpleElem.attributes

  final def scope: Scope = simpleElem.scope

  final def text: String = simpleElem.text

  final def findChildElemByPathEntry(entry: Path.Entry): Option[TaxonomyElem] = {
    val filteredChildElems = childElems.toStream filter { e => e.resolvedName == entry.elementName }

    val childElemOption = filteredChildElems.drop(entry.index).headOption
    assert(childElemOption.forall(_.resolvedName == entry.elementName))
    childElemOption
  }

  final override def equals(other: Any): Boolean = other match {
    case e: TaxonomyElem => simpleElem == e.simpleElem
    case _ => false
  }

  final override def hashCode: Int = simpleElem.hashCode
}

// Taxonomy model

final class TaxonomyModel private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmTaxonomyEName)

  def findAllLabelLinks: immutable.IndexedSeq[LabelLink] =
    findAllChildElemsOfType(classTag[LabelLink])

  def findAllReferenceLinks: immutable.IndexedSeq[ReferenceLink] =
    findAllChildElemsOfType(classTag[ReferenceLink])

  def findAllDefinitionLinks: immutable.IndexedSeq[DefinitionLink] =
    findAllChildElemsOfType(classTag[DefinitionLink])

  def findAllSchemas: immutable.IndexedSeq[Schema] =
    findAllChildElemsOfType(classTag[Schema])
}

// Links

abstract class Link private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(simpleElem, childElems) {

  require(attributeOption(YatmLinkRoleEName).isDefined, s"Element ${resolvedName} must have a ${YatmLinkRoleEName} attribute")
  require(findAllRelationships.forall(_.linkRole == linkRole), s"All relationshils in ${resolvedName} must have linkrole ${linkRole}")

  final def linkRole: String = attribute(YatmLinkRoleEName)

  final def findAllRelationships: immutable.IndexedSeq[Relationship] =
    findAllChildElemsOfType(classTag[Relationship])
}

final class LabelLink private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends Link(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmLabelLinkEName)

  def findAllConceptLabels: immutable.IndexedSeq[ConceptLabel] =
    findAllChildElemsOfType(classTag[ConceptLabel])
}

final class ReferenceLink private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends Link(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmReferenceLinkEName)

  def findAllConceptReferences: immutable.IndexedSeq[ConceptReference] =
    findAllChildElemsOfType(classTag[ConceptReference])
}

final class DefinitionLink private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends Link(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmDefinitionLinkEName)

  def findAllDefinitionArcs: immutable.IndexedSeq[DefinitionArc] =
    findAllChildElemsOfType(classTag[DefinitionArc])
}

// Relationships

abstract class Relationship private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(simpleElem, childElems) {

  require(attributeOption(YatmLinkRoleEName).isDefined, s"Element ${resolvedName} must have a ${YatmLinkRoleEName} attribute")

  final def linkRole: String = attribute(YatmLinkRoleEName)
}

abstract class StandardRelationship private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends Relationship(simpleElem, childElems) {

  require(attributeAsResolvedQNameOption(YatmFromEName).isDefined, s"Element ${resolvedName} must have a ${YatmFromEName} attribute")

  final def sourceConcept: EName = attributeAsResolvedQName(YatmFromEName)
}

abstract class InterConceptRelationship private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardRelationship(simpleElem, childElems) {

  require(attributeAsResolvedQNameOption(YatmToEName).isDefined, s"Element ${resolvedName} must have a ${YatmToEName} attribute")

  final def targetConcept: EName = attributeAsResolvedQName(YatmToEName)
}

abstract class ConceptResourceRelationship private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardRelationship(simpleElem, childElems) {
}

final class ConceptLabel private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends ConceptResourceRelationship(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmConceptLabelEName)
  require(filterChildElemsOfType(classTag[Label])(anyElem).size == 1, s"Element ${resolvedName} must have precisely 1 ${YatmLabelEName} child element")

  def getLabel: Label = getChildElemOfType(classTag[Label])(anyElem)
}

final class ConceptReference private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends ConceptResourceRelationship(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmConceptReferenceEName)
}

final class DefinitionArc private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends InterConceptRelationship(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmDefinitionArcEName)
}

// Resources

abstract class Resource private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(simpleElem, childElems) {
}

final class Label private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends Resource(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmLabelEName)
}

// Role types

final class RoleTypes private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmRoleTypesEName)

  def findAllRoleTypes: immutable.IndexedSeq[RoleType] =
    findAllChildElemsOfType(classTag[RoleType])
}

final class RoleType private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmRoleTypeEName)
}

final class Definition private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmDefinitionEName)
}

final class UsedOn private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmUsedOnEName)
}

// Schema elements

abstract class SchemaElem private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(simpleElem, childElems) {
}

final class Schema private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends SchemaElem(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmXsSchemaEName)

  def findAllGlobalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration] =
    findAllChildElemsOfType(classTag[GlobalElementDeclaration])

  def findAllNamedComplexTypeDefinitions: immutable.IndexedSeq[NamedComplexTypeDefinition] =
    findAllChildElemsOfType(classTag[NamedComplexTypeDefinition])

  def findAllNamedSimpleTypeDefinitions: immutable.IndexedSeq[NamedSimpleTypeDefinition] =
    findAllChildElemsOfType(classTag[NamedSimpleTypeDefinition])

  def findGlobalElementDeclarationByEName(ename: EName): Option[GlobalElementDeclaration] =
    findChildElemOfType(classTag[GlobalElementDeclaration])(e => e.targetEName == ename)

  def findNamedTypeDefinitionByEName(ename: EName): Option[NamedTypeDefinition] =
    findChildElemOfType(classTag[NamedTypeDefinition])(e => e.targetEName == ename)
}

final class GlobalElementDeclaration private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends SchemaElem(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmXsElementEName)
  require(attributeAsResolvedQNameOption(QNameEName).isDefined, s"Element ${resolvedName} must have a ${QNameEName} attribute")

  def targetEName: EName = attributeAsResolvedQName(QNameEName)
}

abstract class NamedTypeDefinition private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends SchemaElem(simpleElem, childElems) {

  require(attributeAsResolvedQNameOption(QNameEName).isDefined, s"Element ${resolvedName} must have a ${QNameEName} attribute")

  final def targetEName: EName = attributeAsResolvedQName(QNameEName)
}

final class NamedComplexTypeDefinition private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends NamedTypeDefinition(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmXsComplexTypeEName)
}

final class NamedSimpleTypeDefinition private[taxomodel] (
  simpleElem: simple.Elem,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends NamedTypeDefinition(simpleElem, childElems) {

  require(simpleElem.resolvedName == YatmXsSimpleTypeEName)
}

// Companion objects with factory methods

object TaxonomyElem {

  /**
   * Expensive method to create a TaxonomyElem tree
   */
  def build(simpleElem: simple.Elem): TaxonomyElem = {
    // Recursive calls
    val childElems = simpleElem.findAllChildElems.map(e => build(e))
    build(simpleElem, childElems)
  }

  private[taxomodel] def build(simpleElem: simple.Elem, childElems: immutable.IndexedSeq[TaxonomyElem]): TaxonomyElem = simpleElem.resolvedName match {
    case YatmTaxonomyEName => new TaxonomyModel(simpleElem, childElems)
    case YatmLabelLinkEName => new LabelLink(simpleElem, childElems)
    case YatmReferenceLinkEName => new ReferenceLink(simpleElem, childElems)
    case YatmDefinitionLinkEName => new DefinitionLink(simpleElem, childElems)
    case YatmConceptLabelEName => new ConceptLabel(simpleElem, childElems)
    case YatmConceptReferenceEName => new ConceptReference(simpleElem, childElems)
    case YatmDefinitionArcEName => new DefinitionArc(simpleElem, childElems)
    case YatmLabelEName => new Label(simpleElem, childElems)
    case YatmRoleTypesEName => new RoleTypes(simpleElem, childElems)
    case YatmRoleTypeEName => new RoleType(simpleElem, childElems)
    case YatmDefinitionEName => new Definition(simpleElem, childElems)
    case YatmUsedOnEName => new UsedOn(simpleElem, childElems)
    case EName(Some(YatmXsNs), _) => SchemaElem.build(simpleElem, childElems)
    case _ => new TaxonomyElem(simpleElem, childElems) {}
  }
}

object TaxonomyModel {

  /**
   * Expensive method to create a TaxonomyModel tree
   */
  def build(simpleElem: simple.Elem): TaxonomyModel = {
    // Recursive calls
    val childElems = simpleElem.findAllChildElems.map(e => TaxonomyElem.build(e))
    new TaxonomyModel(simpleElem, childElems)
  }
}

object SchemaElem {

  private[taxomodel] def build(simpleElem: simple.Elem, childElems: immutable.IndexedSeq[TaxonomyElem]): SchemaElem = simpleElem.resolvedName match {
    case YatmXsSchemaEName => new Schema(simpleElem, childElems)
    case YatmXsElementEName => new GlobalElementDeclaration(simpleElem, childElems)
    case YatmXsComplexTypeEName => new NamedComplexTypeDefinition(simpleElem, childElems)
    case YatmXsSimpleTypeEName => new NamedSimpleTypeDefinition(simpleElem, childElems)
    case EName(Some(YatmXsNs), _) => new SchemaElem(simpleElem, childElems) {}
    case _ => assert(false); ???
  }
}

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

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.xbrl.ENames._

/**
 * Standard or non-standard relationship. Relationships are like arcs, but with resolved locators and therefore
 * at a higher level of abstraction than triples of XLink arcs and locators/resources.
 *
 * @author Chris de Vreeze
 */
sealed trait Relationship extends AnyRelationship {

  final def order: BigDecimal = {
    attributes.order
  }

  final def use: RelationshipAttributes.Use = {
    attributes.use
  }

  final def priority: Int = {
    attributes.priority
  }

  def relationshipKey: RelationshipKey
}

sealed trait StandardRelationship extends Relationship {

  type SourceType = EName

  override def relationshipKey: StandardRelationshipKey
}

final class NonStandardRelationship(
  val baseSetKey: BaseSetKey,
  val source: AnyRef,
  val target: AnyRef,
  val attributes: RelationshipAttributes) extends Relationship {

  type SourceType = AnyRef

  type TargetType = AnyRef

  def extendedLinkEName: EName = baseSetKey.extendedLinkName

  def extendedLinkRole: String = baseSetKey.extendedLinkRole

  def arcEName: EName = baseSetKey.arcName

  def arcRole: String = baseSetKey.arcRole

  def relationshipKey: NonStandardRelationshipKey = {
    // TODO Is this correct? Source and target are also the corresponding keys?
    NonStandardRelationshipKey(baseSetKey, attributes.nonExemptAttributes, source, target)
  }
}

sealed abstract class InterConceptRelationship(
  val extendedLinkRole: String,
  val arcRole: String,
  val source: EName,
  val target: EName,
  val attributes: RelationshipAttributes) extends StandardRelationship {

  type TargetType = EName

  final def relationshipKey: InterConceptRelationshipKey = {
    InterConceptRelationshipKey(baseSetKey, attributes.nonExemptAttributes, source, target)
  }
}

sealed abstract class ConceptResourceRelationship(
  val extendedLinkRole: String,
  val arcRole: String,
  val source: EName,
  val attributes: RelationshipAttributes) extends StandardRelationship {

  type TargetType <: StandardResource

  def target: TargetType

  final def relationshipKey: ConceptResourceRelationshipKey = {
    ConceptResourceRelationshipKey(baseSetKey, attributes.nonExemptAttributes, source, target)
  }
}

final case class DefinitionRelationship(
  override val extendedLinkRole: String,
  override val arcRole: String,
  override val source: EName,
  override val target: EName,
  override val attributes: RelationshipAttributes) extends InterConceptRelationship(extendedLinkRole, arcRole, source, target, attributes) {

  def baseSetKey: BaseSetKey = {
    BaseSetKey(LinkDefinitionLinkEName, extendedLinkRole, LinkDefinitionArcEName, arcRole)
  }

  def isDimensional: Boolean = {
    arcRole.startsWith("http://xbrl.org/int/dim/arcrole/")
  }
}

final case class PresentationRelationship(
  override val extendedLinkRole: String,
  override val arcRole: String,
  override val source: EName,
  override val target: EName,
  override val attributes: RelationshipAttributes) extends InterConceptRelationship(extendedLinkRole, arcRole, source, target, attributes) {

  def baseSetKey: BaseSetKey = {
    BaseSetKey(LinkPresentationLinkEName, extendedLinkRole, LinkPresentationArcEName, arcRole)
  }

  def isParentChildRelationship: Boolean = {
    arcRole == PresentationRelationship.ParentChildArcRole
  }
}

final case class CalculationRelationship(
  override val extendedLinkRole: String,
  override val arcRole: String,
  override val source: EName,
  override val target: EName,
  override val attributes: RelationshipAttributes) extends InterConceptRelationship(extendedLinkRole, arcRole, source, target, attributes) {

  def baseSetKey: BaseSetKey = {
    BaseSetKey(LinkCalculationLinkEName, extendedLinkRole, LinkCalculationArcEName, arcRole)
  }
}

final case class ConceptLabelRelationship(
  override val extendedLinkRole: String,
  override val arcRole: String,
  override val source: EName,
  override val target: StandardLabel,
  override val attributes: RelationshipAttributes) extends ConceptResourceRelationship(extendedLinkRole, arcRole, source, attributes) {

  type TargetType = StandardLabel

  def baseSetKey: BaseSetKey = {
    BaseSetKey(LinkLabelLinkEName, extendedLinkRole, LinkLabelArcEName, arcRole)
  }
}

final case class ConceptReferenceRelationship(
  override val extendedLinkRole: String,
  override val arcRole: String,
  override val source: EName,
  override val target: StandardReference,
  override val attributes: RelationshipAttributes) extends ConceptResourceRelationship(extendedLinkRole, arcRole, source, attributes) {

  type TargetType = StandardReference

  def baseSetKey: BaseSetKey = {
    BaseSetKey(LinkReferenceLinkEName, extendedLinkRole, LinkReferenceArcEName, arcRole)
  }
}

object Relationship {

  /**
   * Builder of relationships of the given type. The builder holds an extended link role and arc role, and passes
   * those each time a relationship is created from the builder. In other words, the builder creates relationships
   * within one base set.
   */
  trait Builder {

    type RelationshipType

    type SourceType

    type TargetType

    def extendedLinkRole: String

    def arcRole: String

    def build(source: SourceType, target: TargetType, attributes: RelationshipAttributes): RelationshipType
  }

  val DefaultLinkRole = "http://www.xbrl.org/2003/role/link"
}

object DefinitionRelationship {

  final case class Builder(val extendedLinkRole: String, val arcRole: String) extends Relationship.Builder {

    type RelationshipType = DefinitionRelationship

    type SourceType = EName

    type TargetType = EName

    def build(source: EName, target: EName, attributes: RelationshipAttributes): DefinitionRelationship = {
      DefinitionRelationship(extendedLinkRole, arcRole, source, target, attributes)
    }
  }

  def generalSpecialRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, GeneralSpecialArcRole)
  }

  def essenceAliasRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, EssenceAliasArcRole)
  }

  def similarTuplesRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, SimilarTuplesArcRole)
  }

  def requiresElementRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, RequiresElementArcRole)
  }

  // Builders for dimensional arcroles. See the arc roles below.

  def allHasHypercubeRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, AllHasHypercubeArcRole)
  }

  def notAllHasHypercubeRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, NotAllHasHypercubeArcRole)
  }

  def hypercubeDimensionRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, HypercubeDimensionArcRole)
  }

  def dimensionDomainRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, DimensionDomainArcRole)
  }

  def domainMemberRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, DomainMemberArcRole)
  }

  def dimensionDefaultRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, DimensionDefaultArcRole)
  }

  val GeneralSpecialArcRole = "http://www.xbrl.org/2003/arcrole/general-special"
  val EssenceAliasArcRole = "http://www.xbrl.org/2003/arcrole/essence-alias"
  val SimilarTuplesArcRole = "http://www.xbrl.org/2003/arcrole/similar-tuples"
  val RequiresElementArcRole = "http://www.xbrl.org/2003/arcrole/requires-element"

  // Dimensional arcroles. We mention them here, although in this package they are just other definition relationship
  // arc roles.

  val AllHasHypercubeArcRole = "http://xbrl.org/int/dim/arcrole/all"
  val NotAllHasHypercubeArcRole = "http://xbrl.org/int/dim/arcrole/notAll"
  val HypercubeDimensionArcRole = "http://xbrl.org/int/dim/arcrole/hypercube-dimension"
  val DimensionDomainArcRole = "http://xbrl.org/int/dim/arcrole/dimension-domain"
  val DomainMemberArcRole = "http://xbrl.org/int/dim/arcrole/domain-member"
  val DimensionDefaultArcRole = "http://xbrl.org/int/dim/arcrole/dimension-default"
}

object PresentationRelationship {

  final case class Builder(val extendedLinkRole: String, val arcRole: String) extends Relationship.Builder {

    type RelationshipType = PresentationRelationship

    type SourceType = EName

    type TargetType = EName

    def build(source: EName, target: EName, attributes: RelationshipAttributes): PresentationRelationship = {
      PresentationRelationship(extendedLinkRole, arcRole, source, target, attributes)
    }
  }

  def parentChildRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, ParentChildArcRole)
  }

  val ParentChildArcRole = "http://www.xbrl.org/2003/arcrole/parent-child"
}

object CalculationRelationship {

  final case class Builder(val extendedLinkRole: String, val arcRole: String) extends Relationship.Builder {

    type RelationshipType = CalculationRelationship

    type SourceType = EName

    type TargetType = EName

    def build(source: EName, target: EName, attributes: RelationshipAttributes): CalculationRelationship = {
      CalculationRelationship(extendedLinkRole, arcRole, source, target, attributes)
    }
  }

  def summationItemRelationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, SummationItemArcRole)
  }

  val SummationItemArcRole = "http://www.xbrl.org/2003/arcrole/summation-item"
}

object ConceptLabelRelationship {

  final case class Builder(val extendedLinkRole: String, val arcRole: String) extends Relationship.Builder {

    type RelationshipType = ConceptLabelRelationship

    type SourceType = EName

    type TargetType = StandardLabel

    def build(source: EName, target: StandardLabel, attributes: RelationshipAttributes): ConceptLabelRelationship = {
      ConceptLabelRelationship(extendedLinkRole, arcRole, source, target, attributes)
    }
  }

  def relationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, DefaultArcRole)
  }

  def defaultRelationshipBuilder: Builder = {
    relationshipBuilder(Relationship.DefaultLinkRole)
  }

  val DefaultArcRole = "http://www.xbrl.org/2003/arcrole/concept-label"
}

object ConceptReferenceRelationship {

  final case class Builder(val extendedLinkRole: String, val arcRole: String) extends Relationship.Builder {

    type RelationshipType = ConceptReferenceRelationship

    type SourceType = EName

    type TargetType = StandardReference

    def build(source: EName, target: StandardReference, attributes: RelationshipAttributes): ConceptReferenceRelationship = {
      ConceptReferenceRelationship(extendedLinkRole, arcRole, source, target, attributes)
    }
  }

  def relationshipBuilder(extendedLinkRole: String): Builder = {
    Builder(extendedLinkRole, DefaultArcRole)
  }

  def defaultRelationshipBuilder: Builder = {
    relationshipBuilder(Relationship.DefaultLinkRole)
  }

  val DefaultArcRole = "http://www.xbrl.org/2003/arcrole/concept-reference"
}

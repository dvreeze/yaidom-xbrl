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
    ConceptResourceRelationshipKey(baseSetKey, attributes.nonExemptAttributes, source, target.localKey)
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
    arcRole == "http://www.xbrl.org/2003/arcrole/parent-child"
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

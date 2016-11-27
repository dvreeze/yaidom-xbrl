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

package eu.cdevreeze.xbrl.taxonomy.model.dimrelationship

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.xbrl.ENames._
import eu.cdevreeze.xbrl.taxonomy.model.relationship.AnyRelationship
import eu.cdevreeze.xbrl.taxonomy.model.relationship.RelationshipAttributes
import eu.cdevreeze.xbrl.taxonomy.model.relationship.BaseSetKey
import eu.cdevreeze.xbrl.taxonomy.model.relationship.DefinitionRelationship

/**
 * Any dimensional relationship.
 *
 * @author Chris de Vreeze
 */
sealed abstract class DimensionalRelationship(
  val extendedLinkRole: String,
  val source: EName,
  val target: EName,
  val attributes: RelationshipAttributes) extends AnyRelationship {

  type SourceType = EName

  type TargetType = EName

  def arcRole: String

  // TODO Effective target role
  // TODO Consecutive relationships

  final def toDefinitionRelationship: DefinitionRelationship = {
    DefinitionRelationship(extendedLinkRole, arcRole, source, target, attributes)
  }

  final def baseSetKey: BaseSetKey = {
    BaseSetKey(LinkDefinitionLinkEName, extendedLinkRole, LinkDefinitionArcEName, arcRole)
  }

  final def order: BigDecimal = {
    attributes.order
  }

  final def use: RelationshipAttributes.Use = {
    attributes.use
  }

  final def priority: Int = {
    attributes.priority
  }
}

sealed abstract class HasHypercubeRelationship(
  extendedLinkRole: String,
  source: EName,
  target: EName,
  attributes: RelationshipAttributes) extends DimensionalRelationship(extendedLinkRole, source, target, attributes) {

  final def primary: EName = source

  final def hypercube: EName = target
}

final case class AllHasHypercubeRelationship(
  override val extendedLinkRole: String,
  override val source: EName,
  override val target: EName,
  override val attributes: RelationshipAttributes) extends HasHypercubeRelationship(extendedLinkRole, source, target, attributes) {

  def arcRole: String = "http://xbrl.org/int/dim/arcrole/all"
}

final case class NotAllHasHypercubeRelationship(
  override val extendedLinkRole: String,
  override val source: EName,
  override val target: EName,
  override val attributes: RelationshipAttributes) extends HasHypercubeRelationship(extendedLinkRole, source, target, attributes) {

  def arcRole: String = "http://xbrl.org/int/dim/arcrole/notAll"
}

final case class HypercubeDimensionRelationship(
  override val extendedLinkRole: String,
  override val source: EName,
  override val target: EName,
  override val attributes: RelationshipAttributes) extends DimensionalRelationship(extendedLinkRole, source, target, attributes) {

  def arcRole: String = "http://xbrl.org/int/dim/arcrole/hypercube-dimension"

  def hypercube: EName = source

  def dimension: EName = target
}

final case class DimensionDomainRelationship(
  override val extendedLinkRole: String,
  override val source: EName,
  override val target: EName,
  override val attributes: RelationshipAttributes) extends DimensionalRelationship(extendedLinkRole, source, target, attributes) {

  def arcRole: String = "http://xbrl.org/int/dim/arcrole/dimension-domain"

  def dimension: EName = source

  def domain: EName = target
}

final case class DomainMemberRelationship(
  override val extendedLinkRole: String,
  override val source: EName,
  override val target: EName,
  override val attributes: RelationshipAttributes) extends DimensionalRelationship(extendedLinkRole, source, target, attributes) {

  def arcRole: String = "http://xbrl.org/int/dim/arcrole/domain-member"

  def domain: EName = source

  def member: EName = target
}

final case class DimensionDefaultRelationship(
  override val extendedLinkRole: String,
  override val source: EName,
  override val target: EName,
  override val attributes: RelationshipAttributes) extends DimensionalRelationship(extendedLinkRole, source, target, attributes) {

  def arcRole: String = "http://xbrl.org/int/dim/arcrole/dimension-default"

  def dimension: EName = source

  def default: EName = target
}

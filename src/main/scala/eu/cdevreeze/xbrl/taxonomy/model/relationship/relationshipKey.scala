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

/**
 * Key of a relationship within a base set, for resolving prohibition and overriding.
 *
 * @author Chris de Vreeze
 */
sealed trait RelationshipKey {

  type SourceKey

  type TargetKey

  def baseSetKey: BaseSetKey

  def nonExemptAttributes: RelationshipAttributes

  def sourceKey: SourceKey

  def targetKey: TargetKey
}

sealed trait StandardRelationshipKey extends RelationshipKey

final case class NonStandardRelationshipKey(
  val baseSetKey: BaseSetKey,
  val nonExemptAttributes: RelationshipAttributes,
  val sourceKey: AnyRef,
  val targetKey: AnyRef) extends RelationshipKey {

  type SourceKey = AnyRef

  type TargetKey = AnyRef
}

final case class InterConceptRelationshipKey(
  val baseSetKey: BaseSetKey,
  val nonExemptAttributes: RelationshipAttributes,
  val sourceKey: EName,
  val targetKey: EName) extends StandardRelationshipKey {

  type SourceKey = EName

  type TargetKey = EName
}

final case class ConceptResourceRelationshipKey(
  val baseSetKey: BaseSetKey,
  val nonExemptAttributes: RelationshipAttributes,
  val sourceKey: EName,
  val targetKey: String) extends StandardRelationshipKey {

  type SourceKey = EName

  type TargetKey = String
}

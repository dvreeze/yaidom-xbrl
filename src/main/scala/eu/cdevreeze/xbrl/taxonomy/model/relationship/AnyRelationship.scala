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
 * Relationship at any abstraction level. For example, all relationships in this package and the ones in the
 * dimrelationship package all extend this relationship super-type.
 *
 * @author Chris de Vreeze
 */
trait AnyRelationship {

  type SourceType

  type TargetType

  def extendedLinkRole: String

  def arcRole: String

  def source: SourceType

  def target: TargetType

  /**
   * The relationship attributes. They include order, use and priority. They do not contain XLink type, arcrole, from and to.
   */
  def attributes: RelationshipAttributes

  def order: BigDecimal

  def use: RelationshipAttributes.Use

  def priority: Int

  def baseSetKey: BaseSetKey
}

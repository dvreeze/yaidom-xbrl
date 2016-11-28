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
import eu.cdevreeze.xbrl.Namespaces._
import eu.cdevreeze.xbrl.ENames._

/**
 * Attributes of a relationship. XLink attributes are not allowed. When creating an instance of this class,
 * mind default and fixed attributes in the schema; they must explicitly be added. Also mind the correct
 * type of the attribute values.
 *
 * Relationship attributes do not include extendedLinkRole or arcRole. They always do include order, use and priority
 * attributes, however.
 *
 * This class is carefully designed for equality.
 *
 * @author Chris de Vreeze
 */
final case class RelationshipAttributes private (val attrMap: Map[EName, AttributeValue]) {
  require(
    attrMap.keySet.forall(_.namespaceUriOption.getOrElse("") != XLinkNamespace),
    s"XLink attributes not allowed, but got attributes $attrMap")

  assert(attrMap.contains(OrderEName), s"No order attribute in attributes $attrMap")
  assert(
    attrMap(OrderEName).isInstanceOf[NumberAttributeValue],
    s"No numeric order attribute in attributes $attrMap")

  assert(attrMap.contains(UseEName), s"No use attribute in attributes $attrMap")
  assert(attrMap.contains(PriorityEName), s"No priority attribute in attributes $attrMap")

  def order: BigDecimal = attrMap(OrderEName).asInstanceOf[NumberAttributeValue].value

  def use: RelationshipAttributes.Use = {
    RelationshipAttributes.Use.fromString(attrMap(UseEName).asInstanceOf[StringAttributeValue].value)
  }

  def priority: Int = {
    attrMap(PriorityEName).asInstanceOf[NumberAttributeValue].value.toInt
  }

  /**
   * Returns the non-exempt attributes. Note that the order attribute is always present.
   * Also note that the (limited) type-awareness helps in equality comparisons for non-exempt attributes.
   */
  def nonExemptAttributes: Map[EName, AttributeValue] = {
    attrMap.filterKeys(attrName => attrName != UseEName && attrName != PriorityEName)
  }

  def withOrder(order: BigDecimal): RelationshipAttributes = {
    RelationshipAttributes(this.attrMap + (OrderEName -> NumberAttributeValue(order)))
  }

  def withUse(use: RelationshipAttributes.Use): RelationshipAttributes = {
    RelationshipAttributes(this.attrMap + (UseEName -> StringAttributeValue(use.toString)))
  }

  def withPriority(priority: Int): RelationshipAttributes = {
    RelationshipAttributes(this.attrMap + (PriorityEName -> NumberAttributeValue(priority)))
  }

  def plusAttribute(attrName: EName, attrValue: AttributeValue): RelationshipAttributes = {
    RelationshipAttributes(this.attrMap + (attrName -> attrValue))
  }
}

object RelationshipAttributes {

  def from(attrMap: Map[EName, AttributeValue]): RelationshipAttributes = {
    RelationshipAttributes(
      attrMap.updated(OrderEName, attrMap.getOrElse(OrderEName, NumberAttributeValue(BigDecimal(1)))).
        updated(UseEName, attrMap.getOrElse(UseEName, StringAttributeValue(UseOptional.toString))).
        updated(PriorityEName, attrMap.getOrElse(PriorityEName, NumberAttributeValue(BigDecimal(0)))))
  }

  val Empty: RelationshipAttributes = RelationshipAttributes.from(Map())

  sealed trait Use
  object UseOptional extends Use { override def toString: String = "optional" }
  object UseProhibited extends Use { override def toString: String = "prohibited" }

  object Use {

    def fromString(s: String): Use = s match {
      case "optional"   => UseOptional
      case "prohibited" => UseProhibited
      case _            => sys.error(s"Not a valid 'use': $s")
    }
  }
}

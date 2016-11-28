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
 * Attributes of a resource (label/reference). XLink attributes are not allowed. When creating an instance of this class,
 * mind default and fixed attributes in the schema; they must explicitly be added. Also mind the correct
 * type of the attribute values.
 *
 * Resource attributes do not include resourceRole, nor do they include the xml:lang attribute.
 *
 * This class is carefully designed for equality.
 *
 * @author Chris de Vreeze
 */
final case class ResourceAttributes private (val attrMap: Map[EName, AttributeValue]) {
  require(
    attrMap.keySet.forall(_.namespaceUriOption.getOrElse("") != XLinkNamespace),
    s"XLink attributes not allowed, but got attributes $attrMap")

  assert(!attrMap.contains(XmlLangEName), s"No xml:lang attribute allowed in attributes $attrMap")

  def plusAttribute(attrName: EName, attrValue: AttributeValue): ResourceAttributes = {
    ResourceAttributes(this.attrMap + (attrName -> attrValue))
  }
}

object ResourceAttributes {

  def from(attrMap: Map[EName, AttributeValue]): ResourceAttributes = {
    ResourceAttributes(attrMap)
  }

  val Empty: ResourceAttributes = ResourceAttributes.from(Map())
}

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
 * Attribute value, with some typing information. All attribute values can be compared for equality.
 *
 * @author Chris de Vreeze
 */
sealed trait AttributeValue {

  type ValueType

  def value: ValueType
}

final case class StringAttributeValue(val value: String) extends AttributeValue {

  type ValueType = String
}

final case class NumberAttributeValue(val value: BigDecimal) extends AttributeValue {

  type ValueType = BigDecimal
}

final case class BooleanAttributeValue(val value: Boolean) extends AttributeValue {

  type ValueType = Boolean
}

final case class ENameAttributeValue(val value: EName) extends AttributeValue {

  type ValueType = EName
}

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
 * Standard label of reference in a standard label or reference linkbase.
 *
 * @author Chris de Vreeze
 */
sealed trait StandardResource {

  def resourceRole: String

  /**
   * Key of the resource in the parent link, like an XLink label.
   */
  def localKey: String

  def languageCode: String

  def resourceText: String

  def otherAttributes: Map[EName, AttributeValue]
}

final case class StandardLabel(
  val localKey: String,
  val resourceRole: String,
  val languageCode: String,
  val resourceText: String,
  val otherAttributes: Map[EName, AttributeValue]) extends StandardResource

final case class StandardReference(
  val localKey: String,
  val resourceRole: String,
  val languageCode: String,
  val resourceText: String,
  val otherAttributes: Map[EName, AttributeValue]) extends StandardResource

object StandardLabel {

  final case class Builder(val resourceRole: String, val languageCode: String) {

    def build(localKey: String, resourceText: String, otherAttributes: Map[EName, AttributeValue]): StandardLabel = {
      StandardLabel(localKey, resourceRole, languageCode, resourceText, otherAttributes)
    }

    def build(localKey: String, resourceText: String): StandardLabel = {
      build(localKey, resourceText, Map())
    }
  }

  def defaultLabelBuilderForLanguage(languageCode: String): Builder = {
    Builder(ResourceRoleLabel, languageCode)
  }

  def terseLabelBuilderForLanguage(languageCode: String): Builder = {
    Builder(ResourceRoleTerseLabel, languageCode)
  }

  def verboseLabelBuilderForLanguage(languageCode: String): Builder = {
    Builder(ResourceRoleVerboseLabel, languageCode)
  }

  val ResourceRoleLabel = "http://www.xbrl.org/2003/role/label"
  val ResourceRoleTerseLabel = "http://www.xbrl.org/2003/role/terseLabel"
  val ResourceRoleVerboseLabel = "http://www.xbrl.org/2003/role/verboseLabel"
}

object StandardReference {

  final case class Builder(val resourceRole: String, val languageCode: String) {

    def build(localKey: String, resourceText: String, otherAttributes: Map[EName, AttributeValue]): StandardReference = {
      StandardReference(localKey, resourceRole, languageCode, resourceText, otherAttributes)
    }

    def build(localKey: String, resourceText: String): StandardReference = {
      build(localKey, resourceText, Map())
    }
  }

  def defaultReferenceBuilderForLanguage(languageCode: String): Builder = {
    Builder(ResourceRoleReference, languageCode)
  }

  def definitionReferenceBuilderForLanguage(languageCode: String): Builder = {
    Builder(ResourceRoleDefinitionRef, languageCode)
  }

  val ResourceRoleReference = "http://www.xbrl.org/2003/role/reference"
  val ResourceRoleDefinitionRef = "http://www.xbrl.org/2003/role/definitionRef"
}

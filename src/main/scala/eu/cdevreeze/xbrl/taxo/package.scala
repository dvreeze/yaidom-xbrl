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

package eu.cdevreeze.xbrl

import eu.cdevreeze.yaidom.core.EName

/**
 * Syntactic taxonomy model, wrapping original taxonomy documents.
 *
 * @author Chris de Vreeze
 */
package object taxo {

  val XsNs = "http://www.w3.org/2001/XMLSchema"
  val XbrldtNs = "http://xbrl.org/2005/xbrldt"
  val LinkNs = "http://www.xbrl.org/2003/linkbase"
  val XLinkNs = "http://www.w3.org/1999/xlink"

  val XsSchemaEName = EName(XsNs, "schema")
  val XsElementEName = EName(XsNs, "element")
  val XsSimpleTypeEName = EName(XsNs, "simpleType")
  val XsComplexTypeEName = EName(XsNs, "complexType")
  val XsImportEName = EName(XsNs, "import")
  val XsIncludeEName = EName(XsNs, "include")

  val XbrldtTargetRoleEName = EName(XbrldtNs, "targetRole")

  val LinkLinkbaseRefEName = EName(LinkNs, "linkbaseRef")
  val LinkSchemaRefEName = EName(LinkNs, "schemaRef")
  val LinkRoleRefEName = EName(LinkNs, "roleRef")
  val LinkArcroleRefEName = EName(LinkNs, "arcroleRef")
  val LinkLocEName = EName(LinkNs, "loc")

  val XLinkHrefEName = EName(XLinkNs, "href")

  val TargetNamespaceEName = EName("targetNamespace")
  val NameEName = EName("name")
  val IdEName = EName("id")
  val SubstitutionGroupEName = EName("substitutionGroup")
  val SchemaLocationEName = EName("schemaLocation")
  val RefEName = EName("ref")
  val TypeEName = EName("type")
  val BaseEName = EName("base")
}

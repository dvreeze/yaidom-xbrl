package eu.cdevreeze.xbrl

import eu.cdevreeze.yaidom.core.EName

/**
 * Syntactic taxonomy model, wrapping original taxonomy documents.
 */
package object taxo {

  val XsNs = "http://www.w3.org/2001/XMLSchema"

  val XsSchemaEName = EName(XsNs, "schema")
  val XsElementEName = EName(XsNs, "element")
  val XsSimpleTypeEName = EName(XsNs, "simpleType")
  val XsComplexTypeEName = EName(XsNs, "complexType")

  val TargetNamespaceEName = EName("targetNamespace")
  val NameEName = EName("name")
  val IdEName = EName("id")
}

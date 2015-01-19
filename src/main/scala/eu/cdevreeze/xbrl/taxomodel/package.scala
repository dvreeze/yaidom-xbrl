package eu.cdevreeze.xbrl

import eu.cdevreeze.yaidom.core.EName

/**
 * The YATM taxonomy model, which abstracts away XLink, and makes relationships from and between concepts explicit.
 * These YATM taxonomy models are readable, concise, and composable.
 *
 * The direct connection to the original taxonomy documents is lost, and as a result URIs in the original documents
 * make no sense in this model. Indeed, xs:import, xs:include and XLink locators do not occur in YATM.
 */
package object taxomodel {

  val YatmNs = "https://github.com/dvreeze/yaidom-xbrl/taxonomy"
  val YatmXsNs = "https://github.com/dvreeze/yaidom-xbrl/xmlschema"

  val YatmTaxonomyEName = EName(YatmNs, "taxonomy")

  val YatmLabelLinkEName = EName(YatmNs, "labelLink")
  val YatmReferenceLinkEName = EName(YatmNs, "referenceLink")
  val YatmDefinitionLinkEName = EName(YatmNs, "definitionLink")

  val YatmConceptLabelEName = EName(YatmNs, "conceptLabel")
  val YatmConceptReferenceEName = EName(YatmNs, "conceptReference")

  val YatmDefinitionArcEName = EName(YatmNs, "definitionArc")

  val YatmLabelEName = EName(YatmNs, "label")

  val YatmRoleTypesEName = EName(YatmNs, "roleTypes")
  val YatmRoleTypeEName = EName(YatmNs, "roleType")
  val YatmDefinitionEName = EName(YatmNs, "definition")
  val YatmUsedOnEName = EName(YatmNs, "usedOn")

  val YatmLinkRoleEName = EName(YatmNs, "linkrole")
  val YatmRoleEName = EName(YatmNs, "role")
  val YatmArcroleEName = EName(YatmNs, "arcrole")
  val YatmFromEName = EName(YatmNs, "from")
  val YatmToEName = EName(YatmNs, "to")

  val YatmXsSchemaEName = EName(YatmXsNs, "schema")
  val YatmXsElementEName = EName(YatmXsNs, "element")
  val YatmXsComplexTypeEName = EName(YatmXsNs, "complexType")
  val YatmXsSimpleTypeEName = EName(YatmXsNs, "simpleType")

  val QNameEName = EName("qname")
}

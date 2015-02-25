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

import scala.collection.immutable

import eu.cdevreeze.xbrl.taxomodel.ArcChain
import eu.cdevreeze.xbrl.taxomodel.DimensionalArc
import eu.cdevreeze.yaidom.core.EName

/**
 * The YATM taxonomy model, which abstracts away XLink, and makes relationships from and between concepts explicit.
 * These YATM taxonomy models are readable, concise, and composable.
 *
 * The direct connection to the original taxonomy documents is lost, and as a result URIs in the original documents
 * make no sense in this model. Indeed, xs:import, xs:include and XLink locators do not occur in YATM.
 *
 * This package also contains a query API on top of the taxonomy model, for easy navigation within a taxonomy.
 *
 * @author Chris de Vreeze
 */
package object taxomodel {

  /**
   * Dimensional arc chains, starting with a has-hypercube, by ELR.
   */
  type DimChainsByElr = Map[String, immutable.IndexedSeq[ArcChain[DimensionalArc]]]

  /**
   * Dimensional arc chains by ELR, by inheriting concept.
   */
  type DimChainsByElrByInheritingConcept = Map[EName, DimChainsByElr]

  val YatmNs = "https://github.com/dvreeze/yaidom-xbrl/taxonomy"
  val YatmXsNs = "https://github.com/dvreeze/yaidom-xbrl/xmlschema"

  val YatmTaxonomyEName = EName(YatmNs, "taxonomy")

  val YatmLabelLinkEName = EName(YatmNs, "labelLink")
  val YatmReferenceLinkEName = EName(YatmNs, "referenceLink")
  val YatmDefinitionLinkEName = EName(YatmNs, "definitionLink")
  val YatmPresentationLinkEName = EName(YatmNs, "presentationLink")
  val YatmCalculationLinkEName = EName(YatmNs, "calculationLink")

  val YatmLabelArcEName = EName(YatmNs, "labelArc")
  val YatmReferenceArcEName = EName(YatmNs, "referenceArc")
  val YatmDefinitionArcEName = EName(YatmNs, "definitionArc")
  val YatmPresentationArcEName = EName(YatmNs, "presentationArc")
  val YatmCalculationArcEName = EName(YatmNs, "calculationArc")

  val YatmLabelEName = EName(YatmNs, "label")
  val YatmReferenceEName = EName(YatmNs, "reference")

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
  val OrderEName = EName("order")
  val AbstractEName = EName("abstract")
}

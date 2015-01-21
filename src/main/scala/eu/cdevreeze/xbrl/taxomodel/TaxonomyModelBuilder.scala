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

package eu.cdevreeze.xbrl.taxomodel

import scala.collection.immutable

import eu.cdevreeze.xbrl.taxo.Taxonomy
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.docaware
import eu.cdevreeze.yaidom.queryapi.HasENameApi.withEName
import eu.cdevreeze.yaidom.simple
import eu.cdevreeze.yaidom.simple.Node
import eu.cdevreeze.yaidom.utils.NamespaceUtils
import eu.cdevreeze.yaidom.xlink.link
import eu.cdevreeze.xbrl.taxo
import eu.cdevreeze.xbrl.taxo.SchemaElem.ToGlobalElementDeclaration
import eu.cdevreeze.xbrl.taxo.SchemaElem.ToNamedSimpleTypeDefinition
import eu.cdevreeze.xbrl.taxo.SchemaElem.ToNamedComplexTypeDefinition

/**
 * Builder of a TaxonomyModel and its parts, given an input Taxonomy.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyModelBuilder(val taxonomy: Taxonomy) {

  import Node._

  def convertToTaxonomyModel: TaxonomyModel = {
    val scope = Scope.from("t" -> YatmNs)

    // Not complete yet. For example, missing generic links and role/arcrole types.

    val resultElem =
      emptyElem(QName("t:taxonomy"), scope) withChildren {
        val links =
          taxonomy.definitionLinks.map(lnk => convertToDefinitionLink(lnk)) ++
            taxonomy.presentationLinks.map(lnk => convertToPresentationLink(lnk)) ++
            taxonomy.calculationLinks.map(lnk => convertToCalculationLink(lnk)) ++
            taxonomy.labelLinks.map(lnk => convertToLabelLink(lnk)) ++
            taxonomy.referenceLinks.map(lnk => convertToReferenceLink(lnk))

        val schema = convertToSchema(taxonomy.schemaDocs.map(_.documentElement))

        (links :+ schema).map(_.simpleElem)
      }

    def removeSomeNamespaces(elem: simple.Elem): simple.Elem =
      elem.transformElemsOrSelf(e => e.copy(scope = e.scope -- Set("xlink", "link", "xsi")))

    val editedResultElem =
      NamespaceUtils.pushUpPrefixedNamespaces(removeSomeNamespaces(resultElem)).prettify(2)

    TaxonomyModel.build(editedResultElem)
  }

  def convertToLabelLink(labelLink: link.LabelLink): LabelLink = {
    val elr = labelLink.role
    val orgLabelArcs = labelLink.labelArcs

    // Does not work for prohibition/override
    val labelArcs =
      for {
        arc <- orgLabelArcs
        fromLoc <- labelLink.labeledLocators.getOrElse(arc.from, Vector())
        toRes <- labelLink.labeledResources.getOrElse(arc.to, Vector())
      } yield {
        convertToLabelArc(arc, fromLoc, toRes.asInstanceOf[link.LabelResource])
      }

    val scope = Scope.from("t" -> YatmNs)

    val resultElem =
      emptyElem(QName("t:labelLink"), Vector(QName("t:linkrole") -> elr), scope).
        withChildren(labelArcs.map(_.simpleElem))

    TaxonomyElem.build(NamespaceUtils.pushUpPrefixedNamespaces(resultElem)).asInstanceOf[LabelLink]
  }

  def convertToReferenceLink(referenceLink: link.ReferenceLink): ReferenceLink = {
    val elr = referenceLink.role
    val orgReferenceArcs = referenceLink.referenceArcs

    // Does not work for prohibition/override
    val referenceArcs =
      for {
        arc <- orgReferenceArcs
        fromLoc <- referenceLink.labeledLocators.getOrElse(arc.from, Vector())
        toRes <- referenceLink.labeledResources.getOrElse(arc.to, Vector())
      } yield {
        convertToReferenceArc(arc, fromLoc, toRes.asInstanceOf[link.ReferenceResource])
      }

    val scope = Scope.from("t" -> YatmNs)

    val resultElem =
      emptyElem(QName("t:referenceLink"), Vector(QName("t:linkrole") -> elr), scope).
        withChildren(referenceArcs.map(_.simpleElem))

    TaxonomyElem.build(NamespaceUtils.pushUpPrefixedNamespaces(resultElem)).asInstanceOf[ReferenceLink]
  }

  def convertToDefinitionLink(definitionLink: link.DefinitionLink): DefinitionLink = {
    val elr = definitionLink.role
    val orgDefinitionArcs = definitionLink.definitionArcs

    // Does not work for prohibition/override
    val definitionArcs =
      for {
        arc <- orgDefinitionArcs
        fromLoc <- definitionLink.labeledLocators.getOrElse(arc.from, Vector())
        toLoc <- definitionLink.labeledLocators.getOrElse(arc.to, Vector())
      } yield {
        convertToDefinitionArc(arc, fromLoc, toLoc)
      }

    val scope = Scope.from("t" -> YatmNs)

    val resultElem =
      emptyElem(QName("t:definitionLink"), Vector(QName("t:linkrole") -> elr), scope).
        withChildren(definitionArcs.sortBy(_.order).map(_.simpleElem))

    TaxonomyElem.build(NamespaceUtils.pushUpPrefixedNamespaces(resultElem)).asInstanceOf[DefinitionLink]
  }

  def convertToPresentationLink(presentationLink: link.PresentationLink): PresentationLink = {
    val elr = presentationLink.role
    val orgPresentationArcs = presentationLink.presentationArcs

    // Does not work for prohibition/override
    val presentationArcs =
      for {
        arc <- orgPresentationArcs
        fromLoc <- presentationLink.labeledLocators.getOrElse(arc.from, Vector())
        toLoc <- presentationLink.labeledLocators.getOrElse(arc.to, Vector())
      } yield {
        convertToPresentationArc(arc, fromLoc, toLoc)
      }

    val scope = Scope.from("t" -> YatmNs)

    val resultElem =
      emptyElem(QName("t:presentationLink"), Vector(QName("t:linkrole") -> elr), scope).
        withChildren(presentationArcs.sortBy(_.order).map(_.simpleElem))

    TaxonomyElem.build(NamespaceUtils.pushUpPrefixedNamespaces(resultElem)).asInstanceOf[PresentationLink]
  }

  def convertToCalculationLink(calculationLink: link.CalculationLink): CalculationLink = {
    val elr = calculationLink.role
    val orgCalculationArcs = calculationLink.calculationArcs

    // Does not work for prohibition/override
    val calculationArcs =
      for {
        arc <- orgCalculationArcs
        fromLoc <- calculationLink.labeledLocators.getOrElse(arc.from, Vector())
        toLoc <- calculationLink.labeledLocators.getOrElse(arc.to, Vector())
      } yield {
        convertToCalculationArc(arc, fromLoc, toLoc)
      }

    val scope = Scope.from("t" -> YatmNs)

    val resultElem =
      emptyElem(QName("t:calculationLink"), Vector(QName("t:linkrole") -> elr), scope).
        withChildren(calculationArcs.sortBy(_.order).map(_.simpleElem))

    TaxonomyElem.build(NamespaceUtils.pushUpPrefixedNamespaces(resultElem)).asInstanceOf[CalculationLink]
  }

  def convertToLabelArc(arc: link.Arc, fromLoc: link.Locator, toRes: link.LabelResource): LabelArc = {
    val globalElemDecl =
      taxonomy.findGlobalElementDeclaration(fromLoc).getOrElse(sys.error(s"Could not find ${fromLoc.href} (document ${fromLoc.bridgeElem.docUri})"))

    val fromConceptEName = globalElemDecl.targetEName
    val fromConceptQName = globalElemDecl.preferredTargetQName
    assert(globalElemDecl.wrappedElem.scope.resolveQNameOption(fromConceptQName) == Some(fromConceptEName))

    val scope =
      Scope.from("t" -> YatmNs) ++ globalElemDecl.scopeNeededForPreferredTargetQName

    val resultElem =
      emptyElem(
        QName("t:labelArc"),
        Vector(
          QName("t:linkrole") -> arc.elr,
          QName("t:arcrole") -> arc.arcrole,
          QName("t:from") -> fromConceptQName.toString) ++ filterNonIdNoNamespaceAttributes(arc),
        scope) plusChild {
          textElem(
            QName("t:label"),
            filterNonIdNoNamespaceAttributes(toRes),
            scope,
            toRes.text).
            plusAttributeOption(QName("t:role"), toRes.roleOption).
            plusAttributeOption(QName("xml:lang"), toRes.langOption)
        }

    TaxonomyElem.build(resultElem).asInstanceOf[LabelArc]
  }

  def convertToReferenceArc(arc: link.Arc, fromLoc: link.Locator, toRes: link.ReferenceResource): ReferenceArc = {
    val globalElemDecl =
      taxonomy.findGlobalElementDeclaration(fromLoc).getOrElse(sys.error(s"Could not find ${fromLoc.href} (document ${fromLoc.bridgeElem.docUri})"))

    val fromConceptEName = globalElemDecl.targetEName
    val fromConceptQName = globalElemDecl.preferredTargetQName
    assert(globalElemDecl.wrappedElem.scope.resolveQNameOption(fromConceptQName) == Some(fromConceptEName))

    val refElems = toRes.findAllChildElems.map(_.bridgeElem.toElem)
    val refScope =
      refElems.foldLeft(Scope.Empty) {
        case (sc, e) =>
          sc ++ (e.scope filter { case (pref, ns) => Some(ns) == e.resolvedName.namespaceUriOption })
      }

    val scope =
      refScope ++ Scope.from("t" -> YatmNs) ++ globalElemDecl.scopeNeededForPreferredTargetQName

    val resultElem =
      emptyElem(
        QName("t:referenceArc"),
        Vector(
          QName("t:linkrole") -> arc.elr,
          QName("t:arcrole") -> arc.arcrole,
          QName("t:from") -> fromConceptQName.toString) ++ filterNonIdNoNamespaceAttributes(arc),
        scope) plusChild {
          textElem(
            QName("t:reference"),
            filterNonIdNoNamespaceAttributes(toRes),
            scope,
            toRes.text).
            plusAttributeOption(QName("t:role"), toRes.roleOption).withChildren(refElems.map(e => e.copy(scope = scope)))
        }

    TaxonomyElem.build(resultElem).asInstanceOf[ReferenceArc]
  }

  def convertToDefinitionArc(arc: link.Arc, fromLoc: link.Locator, toLoc: link.Locator): DefinitionArc = {
    convertToInterConceptArc(arc, fromLoc, toLoc, QName("t:definitionArc")).asInstanceOf[DefinitionArc]
  }

  def convertToPresentationArc(arc: link.Arc, fromLoc: link.Locator, toLoc: link.Locator): PresentationArc = {
    convertToInterConceptArc(arc, fromLoc, toLoc, QName("t:presentationArc")).asInstanceOf[PresentationArc]
  }

  def convertToCalculationArc(arc: link.Arc, fromLoc: link.Locator, toLoc: link.Locator): CalculationArc = {
    convertToInterConceptArc(arc, fromLoc, toLoc, QName("t:calculationArc")).asInstanceOf[CalculationArc]
  }

  def convertToSchema(schemaElems: immutable.IndexedSeq[docaware.Elem]): Schema = {
    val childElems = schemaElems.map(e => convertToSchema(e)).flatMap(_.findAllChildElems).map(_.simpleElem)

    val resultElem =
      emptyElem(
        QName("txs:schema"),
        Vector(QName("elementFormDefault") -> "qualified"),
        Scope.from("txs" -> YatmXsNs)).withChildren(childElems)

    TaxonomyElem.build(NamespaceUtils.pushUpPrefixedNamespaces(resultElem)).asInstanceOf[Schema]
  }

  def convertToSchema(schemaElem: docaware.Elem): Schema = {
    val elemDecls =
      schemaElem.filterChildElems(withEName(taxo.XsElementEName)).map(e => convertToGlobalElementDeclaration(e.toGlobalElementDeclaration))

    val simpleTypeDefs =
      schemaElem.filterChildElems(withEName(taxo.XsSimpleTypeEName)).map(e => convertToNamedSimpleTypeDefinition(e.toNamedSimpleTypeDefinition))

    val complexTypeDefs =
      schemaElem.filterChildElems(withEName(taxo.XsComplexTypeEName)).map(e => convertToNamedComplexTypeDefinition(e.toNamedComplexTypeDefinition))

    val childElems = (elemDecls ++ simpleTypeDefs ++ complexTypeDefs).map(_.simpleElem)

    val resultElem =
      emptyElem(
        QName("txs:schema"),
        Vector(QName("elementFormDefault") -> "qualified"),
        Scope.from("txs" -> YatmXsNs)).withChildren(childElems)

    TaxonomyElem.build(NamespaceUtils.pushUpPrefixedNamespaces(resultElem)).asInstanceOf[Schema]
  }

  def convertToGlobalElementDeclaration(orgElemDecl: taxo.GlobalElementDeclaration): GlobalElementDeclaration = {
    val resultElem = convertToGlobalSchemaComponent(orgElemDecl)

    TaxonomyElem.build(resultElem).asInstanceOf[GlobalElementDeclaration]
  }

  def convertToNamedSimpleTypeDefinition(orgTypeDef: taxo.NamedSimpleTypeDefinition): NamedSimpleTypeDefinition = {
    val resultElem = convertToGlobalSchemaComponent(orgTypeDef)

    TaxonomyElem.build(resultElem).asInstanceOf[NamedSimpleTypeDefinition]
  }

  def convertToNamedComplexTypeDefinition(orgTypeDef: taxo.NamedComplexTypeDefinition): NamedComplexTypeDefinition = {
    val resultElem = convertToGlobalSchemaComponent(orgTypeDef)

    TaxonomyElem.build(resultElem).asInstanceOf[NamedComplexTypeDefinition]
  }

  private def convertToGlobalSchemaComponent(orgElem: taxo.GlobalSchemaComponent): simple.Elem = {
    val scope =
      orgElem.wrappedElem.scope ++ Scope.from("txs" -> YatmXsNs) ++ orgElem.scopeNeededForPreferredTargetQName

    val targetQName = orgElem.preferredTargetQName
    val attrs =
      (QName("qname") -> targetQName.toString) +: (orgElem.wrappedElem.attributes.filterNot(_._1 == QName("name")))

    val resultElem =
      orgElem.wrappedElem.elem.copy(qname = QName("txs", orgElem.wrappedElem.localName), attributes = attrs, scope = scope)
    resultElem
  }

  private def convertToInterConceptArc(arc: link.Arc, fromLoc: link.Locator, toLoc: link.Locator, arcQName: QName): InterConceptArc = {
    val fromGlobalElemDecl =
      taxonomy.findGlobalElementDeclaration(fromLoc).getOrElse(sys.error(s"Could not find ${fromLoc.href} (document ${fromLoc.bridgeElem.docUri})"))

    val toGlobalElemDecl =
      taxonomy.findGlobalElementDeclaration(toLoc).getOrElse(sys.error(s"Could not find ${toLoc.href} (document ${toLoc.bridgeElem.docUri})"))

    val fromConceptEName = fromGlobalElemDecl.targetEName
    val fromConceptQName = fromGlobalElemDecl.preferredTargetQName
    assert(fromGlobalElemDecl.wrappedElem.scope.resolveQNameOption(fromConceptQName) == Some(fromConceptEName))

    val toConceptEName = toGlobalElemDecl.targetEName
    val toConceptQName = toGlobalElemDecl.preferredTargetQName
    assert(toGlobalElemDecl.wrappedElem.scope.resolveQNameOption(toConceptQName) == Some(toConceptEName))

    val scope =
      Scope.from("t" -> YatmNs) ++
        fromGlobalElemDecl.scopeNeededForPreferredTargetQName ++
        toGlobalElemDecl.scopeNeededForPreferredTargetQName

    val resultElem =
      emptyElem(
        arcQName,
        Vector(
          QName("t:linkrole") -> arc.elr,
          QName("t:arcrole") -> arc.arcrole,
          QName("t:from") -> fromConceptQName.toString,
          QName("t:to") -> toConceptQName.toString) ++ filterNonIdNoNamespaceAttributes(arc),
        scope)

    TaxonomyElem.build(resultElem).asInstanceOf[InterConceptArc]
  }

  private def filterNonIdNoNamespaceAttributes(xlink: link.XLink): Vector[(QName, String)] = {
    xlink.attributes.toVector filter {
      case (qname, value) =>
        qname.prefixOption.isEmpty && (qname.localPart != "id")
    }
  }
}

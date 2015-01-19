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
import eu.cdevreeze.yaidom.simple.Node
import eu.cdevreeze.yaidom.utils.NamespaceUtils
import eu.cdevreeze.yaidom.xlink.link

/**
 * Builder of a TaxonomyModel and its parts, given an input Taxonomy.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyModelBuilder(val taxo: Taxonomy) {

  import Node._

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
      taxo.findGlobalElementDeclaration(fromLoc).getOrElse(sys.error(s"Could not find ${fromLoc.href}"))

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
      taxo.findGlobalElementDeclaration(fromLoc).getOrElse(sys.error(s"Could not find ${fromLoc.href}"))

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

  private def convertToInterConceptArc(arc: link.Arc, fromLoc: link.Locator, toLoc: link.Locator, arcQName: QName): InterConceptArc = {
    val fromGlobalElemDecl =
      taxo.findGlobalElementDeclaration(fromLoc).getOrElse(sys.error(s"Could not find ${fromLoc.href}"))

    val toGlobalElemDecl =
      taxo.findGlobalElementDeclaration(toLoc).getOrElse(sys.error(s"Could not find ${toLoc.href}"))

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

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

import scala.Vector

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
    val labelArcs = labelLink.labelArcs

    // Does not work for prohibition/override
    val conceptLabels =
      for {
        arc <- labelArcs
        fromLoc <- labelLink.labeledLocators.getOrElse(arc.from, Vector())
        toRes <- labelLink.labeledResources.getOrElse(arc.to, Vector())
      } yield {
        convertToConceptLabel(arc, fromLoc, toRes.asInstanceOf[link.LabelResource])
      }

    val scope = Scope.from("t" -> YatmNs)

    val resultElem =
      emptyElem(QName("t:labelLink"), Vector(QName("t:linkrole") -> elr), scope).
        withChildren(conceptLabels.map(_.simpleElem))

    TaxonomyElem.build(NamespaceUtils.pushUpPrefixedNamespaces(resultElem)).asInstanceOf[LabelLink]
  }

  def convertToConceptLabel(arc: link.Arc, fromLoc: link.Locator, toRes: link.LabelResource): ConceptLabel = {
    val globalElemDecl =
      taxo.findGlobalElementDeclaration(fromLoc).getOrElse(sys.error(s"Could not find ${fromLoc.href}"))

    val fromConceptEName = globalElemDecl.targetEName
    val fromConceptQName = globalElemDecl.preferredTargetQName
    assert(globalElemDecl.wrappedElem.scope.resolveQNameOption(fromConceptQName) == Some(fromConceptEName))

    val scope = Scope.from("t" -> YatmNs) ++ globalElemDecl.wrappedElem.scope.filterKeys(fromConceptQName.prefixOption.toSet)

    val resultElem =
      emptyElem(
        QName("t:conceptLabel"),
        Vector(
          QName("t:linkrole") -> arc.elr,
          QName("t:arcrole") -> arc.arcrole,
          QName("t:from") -> fromConceptQName.toString) ++ arc.attributes.toVector.filter(kv => kv._1 == ""),
        scope) plusChild {
          textElem(
            QName("t:label"),
            toRes.attributes.toVector.filter(kv => kv._1 == ""),
            scope,
            toRes.text).
            plusAttributeOption(QName("t:role"), toRes.roleOption).
            plusAttributeOption(QName("xml:lang"), toRes.langOption)
        }

    TaxonomyElem.build(resultElem).asInstanceOf[ConceptLabel]
  }
}

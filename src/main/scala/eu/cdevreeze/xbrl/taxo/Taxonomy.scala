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

package eu.cdevreeze.xbrl.taxo

import java.net.URI
import scala.collection.immutable
import eu.cdevreeze.yaidom.docaware
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.xlink.link.Linkbase
import eu.cdevreeze.yaidom.xlink.link.Locator
import eu.cdevreeze.yaidom.xlink.link.LabelLink
import eu.cdevreeze.yaidom.xlink.link.ReferenceLink
import eu.cdevreeze.yaidom.xlink.link.PresentationLink
import eu.cdevreeze.yaidom.xlink.link.DefinitionLink
import eu.cdevreeze.yaidom.xlink.link.CalculationLink
import eu.cdevreeze.yaidom.queryapi.HasENameApi.withEName
import eu.cdevreeze.yaidom.xlink.link.LinkLinkbaseEName
import eu.cdevreeze.yaidom.bridge.DefaultDocawareBridgeElem
import SchemaElem.ToGlobalElementDeclaration
import SchemaElem.ToNamedTypeDefinition

/**
 * Read-only taxonomy. Expensive to create.
 *
 * @author Chris de Vreeze
 */
final class Taxonomy(val docs: immutable.IndexedSeq[docaware.Document]) {

  val schemaDocs: immutable.IndexedSeq[docaware.Document] =
    docs.filter(doc => doc.documentElement.resolvedName == XsSchemaEName)

  /**
   * All linkbases, including embedded ones.
   */
  val linkbases: immutable.IndexedSeq[Linkbase] = {
    docs.flatMap(doc => doc.documentElement.filterElemsOrSelf(withEName(LinkLinkbaseEName))) map { elem =>
      Linkbase(DefaultDocawareBridgeElem.wrap(elem))
    }
  }

  def labelLinks: immutable.IndexedSeq[LabelLink] = linkbases.flatMap(_.labelLinks)

  def referenceLinks: immutable.IndexedSeq[ReferenceLink] = linkbases.flatMap(_.referenceLinks)

  def presentationLinks: immutable.IndexedSeq[PresentationLink] = linkbases.flatMap(_.presentationLinks)

  def definitionLinks: immutable.IndexedSeq[DefinitionLink] = linkbases.flatMap(_.definitionLinks)

  def calculationLinks: immutable.IndexedSeq[CalculationLink] = linkbases.flatMap(_.calculationLinks)

  val globalElementDeclarationsByUri: Map[URI, GlobalElementDeclaration] = {
    schemaDocs.flatMap(_.documentElement.filterChildElems(withEName(XsElementEName))).map(_.toGlobalElementDeclaration) filter { elemDecl =>
      elemDecl.ownUriOption.isDefined
    } groupBy { elemDecl =>
      elemDecl.ownUriOption.get
    } mapValues { elemDecls =>
      // Taking the first one, and ignoring the others, if any, which would mean the schema were invalid!
      elemDecls.head
    }
  }

  val globalElementDeclarationsByEName: Map[EName, GlobalElementDeclaration] = {
    schemaDocs.flatMap(_.documentElement.filterChildElems(withEName(XsElementEName))).map(_.toGlobalElementDeclaration) groupBy { elemDecl =>
      elemDecl.targetEName
    } mapValues { elemDecls =>
      // Taking the first one, and ignoring the others, if any, which would mean the schema were invalid!
      elemDecls.head
    }
  }

  val namedTypeDefinitionsByEName: Map[EName, NamedTypeDefinition] = {
    schemaDocs.flatMap(_.documentElement.filterChildElems(e => Set(XsSimpleTypeEName, XsComplexTypeEName).contains(e.resolvedName))).map(_.toNamedTypeDefinition) groupBy { typeDef =>
      typeDef.targetEName
    } mapValues { typeDefs =>
      // Taking the first one, and ignoring the others, if any, which would mean the schema were invalid!
      typeDefs.head
    }
  }

  /**
   * Finds global element declaration given a locator. Only locators with shorthand pointers are supported.
   */
  def findGlobalElementDeclaration(locator: Locator): Option[GlobalElementDeclaration] = {
    val uri = locator.bridgeElem.baseUri.resolve(locator.href)

    globalElementDeclarationsByUri.get(uri)
  }
}

object Taxonomy {

  private val knownHosts = Set("www.w3.org", "www.xbrl.org", "xbrl.org")

  def skipXbrlAndW3cUris(uri: URI): Boolean = {
    if (uri.getScheme.startsWith("http")) {
      knownHosts.contains(uri.getHost)
    } else {
      val uriString = uri.toString
      knownHosts.exists(h => uriString.contains(s"/${h}/"))
    }
  }

  /**
   * Finds the documents in the DTS with the given entrypoints.
   */
  def findDts(entrypointUris: Set[URI], skipUris: URI => Boolean)(docFactory: URI => docaware.Document): immutable.IndexedSeq[docaware.Document] = {
    findDts(entrypointUris, skipUris, entrypointUris.map(u => (u, docFactory(u))).toMap)(docFactory)
  }

  private def findDts(
    entrypointUris: Set[URI],
    skipUris: URI => Boolean,
    foundSoFar: Map[URI, docaware.Document])(docFactory: URI => docaware.Document): immutable.IndexedSeq[docaware.Document] = {

    require(entrypointUris.subsetOf(foundSoFar.keySet))

    val entrypoints = entrypointUris.toVector.map(u => docFactory(u))
    val foundUris = entrypoints.flatMap(d => findDtsUris(d)(docFactory)).toSet
    val newlyFoundUris = foundUris.diff(foundSoFar.keySet).filterNot(skipUris)

    if (newlyFoundUris.isEmpty) {
      foundSoFar.values.toVector.sortBy(_.uri.toString)
    } else {
      val newlyFoundDocs = newlyFoundUris.toVector.map(uri => docFactory(uri))

      // Recursive call
      findDts(newlyFoundDocs.map(_.uri).toSet, skipUris, foundSoFar ++ (newlyFoundDocs.map(d => (d.uri, d)).toMap))(docFactory)
    }
  }

  private def findDtsUris(doc: docaware.Document)(docFactory: URI => docaware.Document): immutable.IndexedSeq[URI] = {
    val imports =
      doc.documentElement.filterElems(withEName(XsImportEName)).map(e => e.baseUri.resolve(e.attribute(SchemaLocationEName)))
    val includes =
      doc.documentElement.filterElems(withEName(XsIncludeEName)).map(e => e.baseUri.resolve(e.attribute(SchemaLocationEName)))

    val xlinkENames = Set(LinkLocEName, LinkRoleRefEName, LinkArcroleRefEName, LinkLinkbaseRefEName)
    val xlinkElems = doc.documentElement.filterElemsOrSelf(e => xlinkENames.contains(e.resolvedName))
    val xlinkHrefs = xlinkElems.map(e => e.baseUri.resolve(e.attribute(XLinkHrefEName)))

    val refs = (imports ++ includes ++ xlinkHrefs).map(u => removeFragment(u)).distinct
    refs
  }

  private def removeFragment(uri: URI): URI = {
    new URI(uri.getScheme, uri.getSchemeSpecificPart, null)
  }
}

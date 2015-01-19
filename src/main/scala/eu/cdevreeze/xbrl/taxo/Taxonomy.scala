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

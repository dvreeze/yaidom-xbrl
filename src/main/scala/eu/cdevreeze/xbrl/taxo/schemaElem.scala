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

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.docaware

/**
 * Schema element, such as a global element declaration.
 *
 * @author Chris de Vreeze
 */
sealed trait SchemaElem {

  def wrappedElem: docaware.Elem
}

abstract class GlobalSchemaComponent(val wrappedElem: docaware.Elem) extends SchemaElem {
  require(wrappedElem.path.entries.size == 1)
  require(wrappedElem.attributeOption(NameEName).isDefined)

  final def targetNamespaceOption: Option[String] = wrappedElem.rootElem.attributeOption(TargetNamespaceEName)

  final def targetEName: EName =
    EName(targetNamespaceOption, wrappedElem.attribute(NameEName))

  final def preferredTargetQName: QName = {
    val scope = wrappedElem.scope filter { case (pref, ns) => targetNamespaceOption.toSet.contains(ns) }
    val adaptedScope = if (scope.keySet == Set("")) scope else scope.withoutDefaultNamespace

    val prefixOption = adaptedScope.keySet.headOption
    QName(prefixOption, wrappedElem.attribute(NameEName))
  }

  final def scopeNeededForPreferredTargetQName: Scope = {
    wrappedElem.scope filter {
      case (pref, ns) =>
        (pref == preferredTargetQName.prefixOption.getOrElse("")) && (Some(ns) == targetNamespaceOption)
    }
  }
}

final class GlobalElementDeclaration(wrappedElem: docaware.Elem) extends GlobalSchemaComponent(wrappedElem) {
  require(wrappedElem.resolvedName == XsElementEName)
  require(wrappedElem.path.entries.size == 1)
  require(wrappedElem.attributeOption(NameEName).isDefined)

  def ownUriOption: Option[URI] =
    wrappedElem.attributeOption(IdEName).
      map(id => new URI(wrappedElem.baseUri.getScheme, wrappedElem.baseUri.getSchemeSpecificPart, id))
}

abstract class NamedTypeDefinition(wrappedElem: docaware.Elem) extends GlobalSchemaComponent(wrappedElem) {
}

final class NamedSimpleTypeDefinition(wrappedElem: docaware.Elem) extends NamedTypeDefinition(wrappedElem) {
  require(wrappedElem.resolvedName == XsSimpleTypeEName)
  require(wrappedElem.path.entries.size == 1)
  require(wrappedElem.attributeOption(NameEName).isDefined)
}

final class NamedComplexTypeDefinition(wrappedElem: docaware.Elem) extends NamedTypeDefinition(wrappedElem) {
  require(wrappedElem.resolvedName == XsComplexTypeEName)
  require(wrappedElem.path.entries.size == 1)
  require(wrappedElem.attributeOption(NameEName).isDefined)
}

object SchemaElem {

  implicit class ToGlobalElementDeclaration(val wrappedElem: docaware.Elem) extends AnyVal {

    def toGlobalElementDeclaration: GlobalElementDeclaration = new GlobalElementDeclaration(wrappedElem)
  }

  implicit class ToNamedTypeDefinition(val wrappedElem: docaware.Elem) extends AnyVal {

    def toNamedTypeDefinition: NamedTypeDefinition =
      if (wrappedElem.resolvedName == XsSimpleTypeEName) new NamedSimpleTypeDefinition(wrappedElem)
      else new NamedComplexTypeDefinition(wrappedElem)
  }

  implicit class ToNamedSimpleTypeDefinition(val wrappedElem: docaware.Elem) extends AnyVal {

    def toNamedSimpleTypeDefinition: NamedSimpleTypeDefinition =
      new NamedSimpleTypeDefinition(wrappedElem)
  }

  implicit class ToNamedComplexTypeDefinition(val wrappedElem: docaware.Elem) extends AnyVal {

    def toNamedComplexTypeDefinition: NamedComplexTypeDefinition =
      new NamedComplexTypeDefinition(wrappedElem)
  }
}

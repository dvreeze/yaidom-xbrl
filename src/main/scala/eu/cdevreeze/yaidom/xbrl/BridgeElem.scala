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

package eu.cdevreeze.yaidom.xbrl

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.IsNavigableApi
import eu.cdevreeze.yaidom.queryapi.ScopedElemApi

/**
 * Bridge element that enables the `ScopedElemLike with IsNavigableLike` API (and more) on the classes delegating to this bridge element.
 *
 * It offers pluggable DOM-like element implementations, without any "type gymnastics" and without paying any
 * "cake pattern tax".
 *
 * Note that in yaidom, generics have been used extensively for composable pieces of the query API, in order to
 * assemble these into concrete element implementations. Here we use abstract types, in order to make concrete element
 * implementations pluggable as "XML back-ends". The goal is different, and so is the mechanism (abstract types
 * instead of type parameters).
 *
 * This is a purely abstract universal trait, allowing for allocation-free value objects.
 *
 * @author Chris de Vreeze
 */
trait BridgeElem extends Any {

  /**
   * The backing element type, for example `docaware.Elem`
   */
  type BackingElem <: ScopedElemApi[BackingElem] with IsNavigableApi[BackingElem]

  /**
   * The type of this bridge element itself
   */
  type SelfType <: BridgeElem

  /**
   * The unwrapped backing element type, for example `simple.Elem`
   */
  type UnwrappedBackingElem <: ScopedElemApi[UnwrappedBackingElem] with IsNavigableApi[UnwrappedBackingElem]

  def backingElem: BackingElem

  // Needed for the ScopedElemLike API

  def findAllChildElems: immutable.IndexedSeq[SelfType]

  def resolvedName: EName

  def resolvedAttributes: immutable.Iterable[(EName, String)]

  def qname: QName

  def attributes: immutable.Iterable[(QName, String)]

  def scope: Scope

  def text: String

  // Needed for the IsNavigable API

  def findChildElemByPathEntry(entry: Path.Entry): Option[SelfType]

  // Extra methods

  def toElem: eu.cdevreeze.yaidom.simple.Elem

  def rootElem: UnwrappedBackingElem

  def path: Path

  def unwrappedBackingElem: UnwrappedBackingElem
}

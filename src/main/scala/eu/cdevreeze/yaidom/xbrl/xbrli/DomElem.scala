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

package eu.cdevreeze.yaidom
package xbrl.xbrli

import scala.collection.immutable

trait DomElemTypeModule {

  type E <: ElemApi[E] with HasText

  type DomElem <: DomElemApi
  
  def wrap(e: E): DomElem

  /**
   * "DOM element", offering the API of the wrapped DOM element needed by XBRL instances.
   *
   * @author Chris de Vreeze
   */
  trait DomElemApi extends Any {

    def findAllChildElems: immutable.IndexedSeq[DomElem]

    def elem: E

    def path: Path

    def scope: Scope

    def textAsResolvedQName: EName

    def toElem: Elem
  }

}

object DomElemTypeModule {
  trait IndexedElemModule extends DomElemTypeModule {
    type E = indexed.Elem
    type DomElem = IndexedDomElem
    
    def wrap(e: indexed.Elem) = new IndexedDomElem(e)
    
    class IndexedDomElem(val elem: indexed.Elem) extends DomElemApi {
      
      def findAllChildElems: immutable.IndexedSeq[IndexedDomElem] = {
        elem.findAllChildElems.map(e => new IndexedDomElem(e))
      }

      def path: Path = elem.path

      def scope: Scope = elem.elem.scope

      def textAsResolvedQName: EName = elem.elem.textAsResolvedQName

      def toElem: Elem = elem.elem
    }

  }
}


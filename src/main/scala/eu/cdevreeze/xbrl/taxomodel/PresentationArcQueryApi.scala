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
import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.yaidom.core.EName

/**
 * Presentation arc query API, for finding presentation arc chains.
 *
 * @author Chris de Vreeze
 */
trait PresentationArcQueryApi extends ArcQueryApi with AbstractPresentationArcQueryApi {

  final def findOutgoingParentChildArcChains(concept: EName, elr: String): immutable.IndexedSeq[ArcChain[PresentationArc]] = {
    def hasCorrectElr(arc: PresentationArc): Boolean = arc.linkRole == elr

    def hasCorrectElrAndNoCycles(ch: ArcChain[PresentationArc], arc: PresentationArc): Boolean = {
      hasCorrectElr(arc) && !ch.append(arc).hasCycle
    }

    findOutgoingArcChains(concept, classTag[PresentationArc])(hasCorrectElr)(hasCorrectElrAndNoCycles)
  }

  final def findIncomingParentChildArcChains(concept: EName, elr: String): immutable.IndexedSeq[ArcChain[PresentationArc]] = {
    def hasCorrectElr(arc: PresentationArc): Boolean = arc.linkRole == elr

    def hasCorrectElrAndNoCycles(arc: PresentationArc, ch: ArcChain[PresentationArc]): Boolean = {
      hasCorrectElr(arc) && !ch.prepend(arc).hasCycle
    }

    findIncomingArcChains(concept, classTag[PresentationArc])(hasCorrectElr)(hasCorrectElrAndNoCycles)
  }
}

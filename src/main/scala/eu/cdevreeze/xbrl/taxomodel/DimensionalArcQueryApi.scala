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
import scala.reflect.classTag

import eu.cdevreeze.yaidom.core.EName

/**
 * Dimensional arc query API, for finding dimensional arc chains.
 *
 * @author Chris de Vreeze
 */
trait DimensionalArcQueryApi extends ArcQueryApi with AbstractDimensionalArcQueryApi {

  final def findOutgoingDimensionalArcChains(concept: EName, elr: String): immutable.IndexedSeq[ArcChain[DimensionalArc]] = {
    def isMatchingHasHypercube(arc: DimensionalArc): Boolean = arc match {
      case hh: HasHypercubeArc if hh.sourceConcept == concept && hh.linkRole == elr => true
      case arc: DimensionalArc => false
    }

    def follows(ch: ArcChain[DimensionalArc], arc: DimensionalArc): Boolean = {
      ArcChain.areConsecutiveDimensionalArcs(ch.arcs.last, arc) && !ch.append(arc).hasCycle
    }

    findOutgoingArcChains(concept, classTag[DimensionalArc])(isMatchingHasHypercube)(follows)
  }

  final def findOutgoingDomainMemberArcChains(concept: EName, elr: String): immutable.IndexedSeq[ArcChain[DomainMemberArc]] = {
    def isMatchingDomainMember(arc: DomainMemberArc): Boolean =
      (arc.sourceConcept == concept) && (arc.linkRole == elr)

    def follows(ch: ArcChain[DomainMemberArc], arc: DomainMemberArc): Boolean = {
      ArcChain.areConsecutiveDimensionalArcs(ch.arcs.last, arc) && !ch.append(arc).hasCycle
    }

    findOutgoingArcChains(concept, classTag[DomainMemberArc])(isMatchingDomainMember)(follows)
  }

  final def findIncomingDomainMemberArcChains(concept: EName): immutable.IndexedSeq[ArcChain[DomainMemberArc]] = {
    def isMatchingDomainMember(arc: DomainMemberArc): Boolean =
      (arc.targetConcept == concept)

    def precedes(arc: DomainMemberArc, ch: ArcChain[DomainMemberArc]): Boolean = {
      ArcChain.areConsecutiveDimensionalArcs(arc, ch.arcs.head) && !ch.prepend(arc).hasCycle
    }

    findIncomingArcChains(concept, classTag[DomainMemberArc])(isMatchingDomainMember)(precedes)
  }

  final def findInheritedDimensionalArcChains(concept: EName): immutable.IndexedSeq[ArcChain[DimensionalArc]] = {
    val incomingChains = findIncomingDomainMemberArcChains(concept)

    incomingChains flatMap {
      case ch =>
        val startConcept = ch.arcs.head.sourceConcept
        val elr = ch.arcs.head.linkRole

        findOutgoingDimensionalArcChains(startConcept, elr)
    }
  }
}

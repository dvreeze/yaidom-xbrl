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

import eu.cdevreeze.yaidom.core.EName

/**
 * Inter-concept arc chain. Subsequent arcs in the chain must match in target and source concept, respectively.
 * Each arc chain has at least one arc.
 *
 * @author Chris de Vreeze
 */
final class ArcChain[A <: InterConceptArc](val arcs: immutable.IndexedSeq[A]) {
  require(arcs.size >= 1)
  require(arcs.sliding(2).filter(_.size == 2).forall(pair => ArcChain.haveMatchingConcepts(pair(0), pair(1))))

  def sourceConcept: EName = arcs.head.sourceConcept

  def targetConcept: EName = arcs.last.targetConcept

  def hasCycle: Boolean = {
    val concepts = arcs.map(_.sourceConcept) :+ arcs.last.targetConcept
    concepts.distinct.size < concepts.size
  }

  def append(arc: A): ArcChain[A] = {
    require(canAppend(arc))
    new ArcChain(arcs :+ arc)
  }

  def prepend(arc: A): ArcChain[A] = {
    require(canPrepend(arc))
    new ArcChain(arc +: arcs)
  }

  def canAppend(arc: A): Boolean = {
    this.targetConcept == arc.sourceConcept
  }

  def canPrepend(arc: A): Boolean = {
    this.sourceConcept == arc.targetConcept
  }
}

object ArcChain {

  def from[A <: InterConceptArc](arcs: A*): ArcChain[A] = new ArcChain(arcs.toVector)

  def haveMatchingConcepts[A <: InterConceptArc](arc1: A, arc2: A): Boolean = {
    arc1.targetConcept == arc2.sourceConcept
  }

  def haveMatchingConceptsAndSameElr[A <: InterConceptArc](arc1: A, arc2: A): Boolean = {
    haveMatchingConcepts(arc1, arc2) && (arc1.linkRole == arc2.linkRole)
  }

  def areConsecutiveDimensionalArcs[A <: InterConceptArc](arc1: A, arc2: A): Boolean = {
    haveMatchingConcepts(arc1, arc2) && {
      (arc1, arc2) match {
        case (arc1: DefinitionArc, arc2: DefinitionArc) if arc1.isHasHypercube && arc2.isHypercubeDimension =>
          arc1.effectiveTargetRole == arc2.linkRole
        case (arc1: DefinitionArc, arc2: DefinitionArc) if arc1.isHypercubeDimension && arc2.isDimensionDomain =>
          arc1.effectiveTargetRole == arc2.linkRole
        case (arc1: DefinitionArc, arc2: DefinitionArc) if arc1.isDimensionDomain && arc2.isDomainMember =>
          arc1.effectiveTargetRole == arc2.linkRole
        case (arc1: DefinitionArc, arc2: DefinitionArc) if arc1.isDomainMember && arc2.isDomainMember =>
          arc1.effectiveTargetRole == arc2.linkRole
        case (arc1, arc2) => false
      }
    }
  }
}

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

import eu.cdevreeze.yaidom.core.EName

/**
 * Taxonomy arc query API, for navigating from concepts to concepts or resources.
 *
 * @author Chris de Vreeze
 */
trait ArcQueryApi extends AbstractArcQueryApi {

  /**
   * Finds the standard arcs grouped by source concept. This must be a very fast method, in order for the query
   * API to be fast.
   */
  def standardArcsBySource: Map[EName, immutable.IndexedSeq[StandardArc]]

  /**
   * Finds the inter-concept arcs grouped by target concept. This must be a very fast method, in order for the query
   * API to be fast.
   */
  def interConceptArcsByTarget: Map[EName, immutable.IndexedSeq[InterConceptArc]]

  final def findOutgoingArcs[A <: StandardArc](concept: EName, arcType: ClassTag[A]): immutable.IndexedSeq[A] = {
    filterOutgoingArcs(concept, arcType)(_ => true)
  }

  final def findIncomingArcs[A <: InterConceptArc](concept: EName, arcType: ClassTag[A]): immutable.IndexedSeq[A] = {
    filterIncomingArcs(concept, arcType)(_ => true)
  }

  final def filterOutgoingArcs[A <: StandardArc](concept: EName, arcType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {
    implicit val arcClassTag = arcType
    standardArcsBySource.getOrElse(concept, Vector()) collect { case arc: A if p(arc) => arc }
  }

  final def filterIncomingArcs[A <: InterConceptArc](concept: EName, arcType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {
    implicit val arcClassTag = arcType
    interConceptArcsByTarget.getOrElse(concept, Vector()) collect { case arc: A if p(arc) => arc }
  }

  final def findOutgoingArcChains[A <: InterConceptArc](concept: EName, arcType: ClassTag[A])(first: A => Boolean)(succ: (ArcChain[A], A) => Boolean): immutable.IndexedSeq[ArcChain[A]] = {
    val nextArcs = filterOutgoingArcs(concept, arcType)(first)

    val arcChains = nextArcs.flatMap(arc => findAllLongestArcChainsStartingWith(ArcChain.from(arc), arcType)(succ))
    arcChains
  }

  final def findIncomingArcChains[A <: InterConceptArc](concept: EName, arcType: ClassTag[A])(last: A => Boolean)(pred: (ArcChain[A], A) => Boolean): immutable.IndexedSeq[ArcChain[A]] = {
    val prevArcs = filterIncomingArcs(concept, arcType)(last)

    val arcChains = prevArcs.flatMap(arc => findAllLongestArcChainsEndingWith(ArcChain.from(arc), arcType)(pred))
    arcChains
  }

  // Private implementation methods

  private def findAllLongestArcChainsStartingWith[A <: InterConceptArc](chain: ArcChain[A], arcType: ClassTag[A])(p: (ArcChain[A], A) => Boolean): immutable.IndexedSeq[ArcChain[A]] = {
    val concept = chain.targetConcept
    val nextArcs = filterOutgoingArcs(concept, arcType)(arc => p(chain, arc))

    if (nextArcs.isEmpty) {
      Vector(chain)
    } else {
      val arcChains = nextArcs flatMap { arc =>
        assert(chain.canAppend(arc))
        val nextChain = chain.append(arc)

        // Recursive calls
        val nextArcChains = findAllLongestArcChainsStartingWith(nextChain, arcType)(p)
        nextArcChains
      }
      arcChains
    }
  }

  private def findAllLongestArcChainsEndingWith[A <: InterConceptArc](chain: ArcChain[A], arcType: ClassTag[A])(p: (ArcChain[A], A) => Boolean): immutable.IndexedSeq[ArcChain[A]] = {
    val concept = chain.sourceConcept
    val prevArcs = filterIncomingArcs(concept, arcType)(arc => p(chain, arc))

    if (prevArcs.isEmpty) {
      Vector(chain)
    } else {
      val arcChains = prevArcs flatMap { arc =>
        assert(chain.canPrepend(arc))
        val prevChain = chain.prepend(arc)

        // Recursive calls
        val prevArcChains = findAllLongestArcChainsEndingWith(prevChain, arcType)(p)
        prevArcChains
      }
      arcChains
    }
  }
}

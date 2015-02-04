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
import scala.reflect.ClassTag

import eu.cdevreeze.yaidom.core.EName

/**
 * Purely abstract taxonomy arc query API, for finding arcs and chains of arcs. This is a very general arc
 * query API. The arc chain query methods are typically not used directly in application code, but help in
 * implementing methods for querying presentation trees, dimensional trees, etc.
 *
 * @author Chris de Vreeze
 */
trait AbstractArcQueryApi {

  /**
   * Filters the standard arcs of the given arc type obeying the given arc predicate.
   */
  def filterStandardArcs[A <: StandardArc](arcType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all arcs of the given arc type having the source concept passed as first parameter.
   *
   * This method must be equivalent to:
   * {{{
   * filterOutgoingArcs[A](concept, arcType)(_ => true)
   * }}}
   */
  def findOutgoingArcs[A <: StandardArc](concept: EName, arcType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Finds all arcs of the given arc type having the target concept passed as first parameter.
   *
   * This method must be equivalent to:
   * {{{
   * filterIncomingArcs[A](concept, arcType)(_ => true)
   * }}}
   */
  def findIncomingArcs[A <: InterConceptArc](concept: EName, arcType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters the arcs of the given arc type that obey the given arc filter, having the source concept passed as
   * first parameter.
   */
  def filterOutgoingArcs[A <: StandardArc](concept: EName, arcType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Filters the arcs of the given arc type that obey the given arc filter, having the target concept passed as
   * first parameter.
   */
  def filterIncomingArcs[A <: InterConceptArc](concept: EName, arcType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all longest arc chains with arcs of the given arc type, having the first parameter as source concept,
   * such that predicate `first` holds for the first arc in each such chain, and `succ` holds for each intermediate
   * "prefix" arc chain result and the following arc.
   *
   * This is a very general method, used to implement methods such as `findOutgoingParentChildArcChains`.
   *
   * Typically this method should not be used in application code if a more specific method is available.
   * When using this method, keep in mind that a combinatorial explosion is possible. Even non-termination is
   * possible, if there are arc cycles, and `succ` does not return false on cycle detection.
   */
  def findOutgoingArcChains[A <: InterConceptArc](concept: EName, arcType: ClassTag[A])(first: A => Boolean)(succ: (ArcChain[A], A) => Boolean): immutable.IndexedSeq[ArcChain[A]]

  /**
   * Finds all longest arc chains with arcs of the given arc type, having the first parameter as target concept,
   * such that predicate `last` holds for the last arc in each such chain, and `pred` holds for each intermediate
   * "suffix" arc chain result and the preceding arc.
   *
   * This is a very general method, used to implement methods such as `findIncomingParentChildArcChains`.
   *
   * Typically this method should not be used in application code if a more specific method is available.
   * When using this method, keep in mind that a combinatorial explosion is possible. Even non-termination is
   * possible, if there are arc cycles, and `pred` does not return false on cycle detection.
   */
  def findIncomingArcChains[A <: InterConceptArc](concept: EName, arcType: ClassTag[A])(last: A => Boolean)(pred: (A, ArcChain[A]) => Boolean): immutable.IndexedSeq[ArcChain[A]]
}

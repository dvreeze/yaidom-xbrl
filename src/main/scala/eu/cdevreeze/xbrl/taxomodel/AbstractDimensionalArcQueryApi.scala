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
 * Purely abstract dimensional arc query API, for finding dimensional arc chains.
 *
 * @author Chris de Vreeze
 */
trait AbstractDimensionalArcQueryApi extends AbstractArcQueryApi {

  /**
   * Returns all longest outgoing dimensional arc chains of has-hypercube arcs having the given concept and ELR.
   * In other words, returns the dimensional "right-hand sides", starting with matching has-hypercube arcs.
   * On cycle detection, the arc chain is returned instead of being extended any further.
   */
  def findOutgoingDimensionalArcChains(concept: EName, elr: String): immutable.IndexedSeq[ArcChain[DimensionalArc]]

  /**
   * Returns all longest outgoing domain member arc chains starting with the given concept and ELR.
   * Typically this method is used to return dimensional "left-hand sides", for has-hypercube inheritance.
   * On cycle detection, the arc chain is returned instead of being extended any further.
   */
  def findOutgoingDomainMemberArcChains(concept: EName, elr: String): immutable.IndexedSeq[ArcChain[DomainMemberArc]]

  /**
   * Returns all longest incoming domain member arc chains ending with the given concept.
   * Typically this method is used to return dimensional "left-hand sides", for has-hypercube inheritance.
   * On cycle detection, the arc chain is returned instead of being extended (to the left) any further.
   */
  def findIncomingDomainMemberArcChains(concept: EName): immutable.IndexedSeq[ArcChain[DomainMemberArc]]

  /**
   * Returns the inherited dimensional arc chains, starting with has-hypercubes, inherited by the given concept.
   * The result is returned as a Map from ELRs to collections of arc chains.
   *
   * That is, combines `findIncomingDomainMemberArcChains` and `findOutgoingDimensionalArcChains`, via
   * matching has-hypercubes, and returns the result grouped by ELR.
   */
  def findInheritedDimensionalArcChainsGroupedByElr(concept: EName): DimChainsByElr

  /**
   * Returns per concept and has-hypercube ELR the inherited dimensional arc chains, starting with has-hypercubes,
   * inherited by that concept and using that has-hypercube ELR.
   *
   * The method returns a Map from concept ENames to ELRs to "dimensional trees". This is a "bulk version" of method
   * `findInheritedDimensionalArcChains`, except that the implementation is more efficient.
   */
  def findAllInheritedDimensionalArcChainsGroupedByConceptAndElr(): DimChainsByElrByInheritingConcept
}

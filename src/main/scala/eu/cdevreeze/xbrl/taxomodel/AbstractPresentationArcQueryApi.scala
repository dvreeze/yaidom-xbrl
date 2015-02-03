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
 * Purely abstract presentation arc query API, for finding presentation arc chains.
 *
 * @author Chris de Vreeze
 */
trait AbstractPresentationArcQueryApi extends AbstractArcQueryApi {

  /**
   * Returns all longest parent-child arc chains of the given extended link role, with the first parameter as source concept.
   * On cycle detection, the arc chain is returned instead of being extended any further.
   */
  def findOutgoingParentChildArcChains(concept: EName, elr: String): immutable.IndexedSeq[ArcChain[PresentationArc]]

  /**
   * Returns all longest parent-child arc chains of the given extended link role, with the first parameter as target concept.
   * On cycle detection, the arc chain is returned instead of being extended any further.
   */
  def findIncomingParentChildArcChains(concept: EName, elr: String): immutable.IndexedSeq[ArcChain[PresentationArc]]
}

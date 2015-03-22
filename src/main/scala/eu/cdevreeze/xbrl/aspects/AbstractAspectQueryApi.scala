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

package eu.cdevreeze.xbrl.aspects

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path
import eu.cdevreeze.yaidom.xbrl.Fact
import eu.cdevreeze.yaidom.xbrl.Identifier
import eu.cdevreeze.yaidom.xbrl.ItemFact
import eu.cdevreeze.yaidom.xbrl.NumericItemFact
import eu.cdevreeze.yaidom.xbrl.Period
import eu.cdevreeze.yaidom.xbrl.Scenario
import eu.cdevreeze.yaidom.xbrl.Segment
import eu.cdevreeze.yaidom.xbrl.XbrliElem
import eu.cdevreeze.yaidom.xbrl.XbrliUnit

/**
 * Purely abstract aspect query API, for finding and matching on aspects.
 *
 * @author Chris de Vreeze
 */
trait AbstractAspectQueryApi {

  // Aspects.
  // TODO Typed dimensions

  def locationAspect(fact: Fact): Path

  def conceptAspect(fact: Fact): EName

  def entityIdentifierAspect(itemFact: ItemFact): Identifier

  def periodAspect(itemFact: ItemFact): Period

  def completeSegmentAspectOption(itemFact: ItemFact): Option[Segment]

  def completeScenarioAspectOption(itemFact: ItemFact): Option[Scenario]

  def nonXdtSegmentAspectOption(itemFact: ItemFact): immutable.IndexedSeq[XbrliElem]

  def nonXdtScenarioAspectOption(itemFact: ItemFact): immutable.IndexedSeq[XbrliElem]

  def segmentExplicitDimensionAspects(itemFact: ItemFact): Map[EName, EName]

  def scenarioExplicitDimensionAspects(itemFact: ItemFact): Map[EName, EName]

  def unitAspect(numericItemFact: NumericItemFact): XbrliUnit

  // Aspect matching

  def matchOnLocation(fact1: Fact, fact2: Fact): Boolean

  def matchOnConcept(fact1: Fact, fact2: Fact): Boolean

  def matchOnEntityIdentifier(fact1: Fact, fact2: Fact): Boolean

  def matchOnPeriod(fact1: Fact, fact2: Fact): Boolean

  def matchOnCompleteSegment(fact1: Fact, fact2: Fact): Boolean

  def matchOnCompleteScenario(fact1: Fact, fact2: Fact): Boolean

  def matchOnNonXdtSegmentContent(fact1: Fact, fact2: Fact): Boolean

  def matchOnNonXdtScenarioContent(fact1: Fact, fact2: Fact): Boolean

  def matchOnSegmentExplicitDimensions(fact1: Fact, fact2: Fact): Boolean

  def matchOnScenarioExplicitDimensions(fact1: Fact, fact2: Fact): Boolean

  def matchOnUnit(fact1: Fact, fact2: Fact): Boolean
}

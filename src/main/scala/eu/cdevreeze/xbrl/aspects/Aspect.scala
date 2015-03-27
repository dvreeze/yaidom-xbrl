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

import eu.cdevreeze.yaidom.core.EName

/**
 * Aspect.
 *
 * @author Chris de Vreeze
 */
sealed trait Aspect

// Dimensional aspect model

object ConceptAspect extends Aspect

object LocationAspect extends Aspect

object EntityIdentifierAspect extends Aspect

object PeriodAspect extends Aspect

object NonXdtSegmentAspect extends Aspect

object NonXdtScenarioAspect extends Aspect

final case class DimensionAspect(val dimension: EName) extends Aspect

object UnitAspect extends Aspect

// Non-dimensional aspect model

object CompleteSegmentAspect extends Aspect

object CompleteScenarioAspect extends Aspect

object Aspect {

  def allDimensionalModelAspects(dimensions: Set[EName]): Set[Aspect] = {
    val nonDimAspects =
      Set(ConceptAspect, LocationAspect, EntityIdentifierAspect, PeriodAspect, NonXdtSegmentAspect, NonXdtScenarioAspect, UnitAspect)

    val dimAspects: Set[Aspect] = dimensions.map(dim => DimensionAspect(dim))

    nonDimAspects.union(dimAspects)
  }

  val allNonDimensionalModelAspects: Set[Aspect] = {
    Set(ConceptAspect, LocationAspect, EntityIdentifierAspect, PeriodAspect, CompleteSegmentAspect, CompleteScenarioAspect, UnitAspect)
  }
}

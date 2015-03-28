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

import scala.Vector
import scala.collection.immutable

import eu.cdevreeze.xbrl.taxomodel.TaxonomyModel
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path
import eu.cdevreeze.yaidom.resolved
import eu.cdevreeze.yaidom.xbrl.Fact
import eu.cdevreeze.yaidom.xbrl.Identifier
import eu.cdevreeze.yaidom.xbrl.ItemFact
import eu.cdevreeze.yaidom.xbrl.NumericItemFact
import eu.cdevreeze.yaidom.xbrl.Period
import eu.cdevreeze.yaidom.xbrl.Scenario
import eu.cdevreeze.yaidom.xbrl.Segment
import eu.cdevreeze.yaidom.xbrl.XbrlInstance
import eu.cdevreeze.yaidom.xbrl.XbrliElem
import eu.cdevreeze.yaidom.xbrl.XbrliUnit

/**
 * Concrete aspect query API implementation, for finding and matching on aspects.
 *
 * @author Chris de Vreeze
 */
final class AspectQueryApi(val xbrlInstance: XbrlInstance, val taxoModel: TaxonomyModel) extends AbstractAspectQueryApi {

  // Aspects.
  // TODO Typed dimensions

  def locationAspect(fact: Fact): Path = {
    fact.bridgeElem.path.parentPathOption.getOrElse(sys.error(s"Facts must have a parent path"))
  }

  def conceptAspect(fact: Fact): EName = fact.resolvedName

  def entityIdentifierAspect(itemFact: ItemFact): Identifier = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    context.entity.identifier
  }

  def periodAspect(itemFact: ItemFact): Period = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    context.period
  }

  def nonXdtSegmentAspectOption(itemFact: ItemFact): immutable.IndexedSeq[XbrliElem] = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    context.entity.segmentOption.map(e => e.nonXdtContent).getOrElse(Vector())
  }

  def nonXdtScenarioAspectOption(itemFact: ItemFact): immutable.IndexedSeq[XbrliElem] = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    context.scenarioOption.map(e => e.nonXdtContent).getOrElse(Vector())
  }

  def unitAspect(numericItemFact: NumericItemFact): XbrliUnit = {
    xbrlInstance.allUnitsById.getOrElse(numericItemFact.unitRef, sys.error("Missing unit with ID ${numericItemFact.unitRef}"))
  }

  def findExplicitDimensions(itemFact: ItemFact): Set[EName] = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    val dims =
      (context.entity.segmentOption.map(e => e.explicitDimensions).getOrElse(Map())) ++
        (context.scenarioOption.map(e => e.explicitDimensions).getOrElse(Map()))
    dims.keySet
  }

  def findExplicitDimensions(itemFactEName: EName): Set[EName] = {
    xbrlInstance.findAllItems.flatMap(e => findExplicitDimensions(e)).toSet
  }

  def explicitDimensionAspectOption(dimension: EName, itemFact: ItemFact): Option[EName] = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    (segmentExplicitDimensionAspects(itemFact) ++ scenarioExplicitDimensionAspects(itemFact)).get(dimension)
  }

  // Aspects in non-dimensional aspect model

  def completeSegmentAspectOption(itemFact: ItemFact): Option[Segment] = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    context.entity.segmentOption
  }

  def completeScenarioAspectOption(itemFact: ItemFact): Option[Scenario] = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    context.scenarioOption
  }

  // Aspect matching

  def matchOnAspect(aspect: Aspect, fact1: Fact, fact2: Fact): Boolean = aspect match {
    case ConceptAspect => matchOnConcept(fact1, fact2)
    case LocationAspect => matchOnLocation(fact1, fact2)
    case EntityIdentifierAspect => matchOnEntityIdentifier(fact1, fact2)
    case PeriodAspect => matchOnPeriod(fact1, fact2)
    case NonXdtSegmentAspect => matchOnNonXdtSegmentContent(fact1, fact2)
    case NonXdtScenarioAspect => matchOnNonXdtScenarioContent(fact1, fact2)
    case CompleteSegmentAspect => matchOnCompleteSegment(fact1, fact2)
    case CompleteScenarioAspect => matchOnCompleteScenario(fact1, fact2)
    case UnitAspect => matchOnUnit(fact1, fact2)
    case DimensionAspect(dim) => matchOnDimension(dim, fact1, fact2)
  }

  def matchOnAspects(aspects: Set[Aspect], fact1: Fact, fact2: Fact): Boolean = {
    // TODO Sort aspects
    val aspectSeq = aspects.toSeq

    aspectSeq.foldLeft(true) {
      case (acc, aspect) =>
        acc && matchOnAspect(aspect, fact1, fact2)
    }
  }

  // Private methods

  // Dimensional aspect model aspect matching

  private def matchOnLocation(fact1: Fact, fact2: Fact): Boolean = {
    locationAspect(fact1) == locationAspect(fact2)
  }

  private def matchOnConcept(fact1: Fact, fact2: Fact): Boolean = {
    conceptAspect(fact1) == conceptAspect(fact2)
  }

  private def matchOnEntityIdentifier(fact1: Fact, fact2: Fact): Boolean = (fact1, fact2) match {
    case (item1: ItemFact, item2: ItemFact) if (item1.contextRef == item2.contextRef) => true
    case (item1: ItemFact, item2: ItemFact) =>
      // Lacks type-safety
      entityIdentifierAspect(item1).asResolvedElem == entityIdentifierAspect(item2).asResolvedElem
    case (item1: ItemFact, _) => false
    case (_, item2: ItemFact) => false
    case _ => true
  }

  private def matchOnPeriod(fact1: Fact, fact2: Fact): Boolean = (fact1, fact2) match {
    case (item1: ItemFact, item2: ItemFact) if (item1.contextRef == item2.contextRef) => true
    case (item1: ItemFact, item2: ItemFact) =>
      // Lacks type-safety
      periodAspect(item1).asResolvedElem == periodAspect(item2).asResolvedElem
    case (item1: ItemFact, _) => false
    case (_, item2: ItemFact) => false
    case _ => true
  }

  private def matchOnNonXdtSegmentContent(fact1: Fact, fact2: Fact): Boolean = (fact1, fact2) match {
    case (item1: ItemFact, item2: ItemFact) if (item1.contextRef == item2.contextRef) => true
    case (item1: ItemFact, item2: ItemFact) =>
      // Lacks type-safety
      nonXdtSegmentAspectOption(item1).map(e => resolved.Elem(e.bridgeElem.toElem)) ==
        nonXdtSegmentAspectOption(item2).map(e => resolved.Elem(e.bridgeElem.toElem))
    case (item1: ItemFact, _) => false
    case (_, item2: ItemFact) => false
    case _ => true
  }

  private def matchOnNonXdtScenarioContent(fact1: Fact, fact2: Fact): Boolean = (fact1, fact2) match {
    case (item1: ItemFact, item2: ItemFact) if (item1.contextRef == item2.contextRef) => true
    case (item1: ItemFact, item2: ItemFact) =>
      // Lacks type-safety
      nonXdtScenarioAspectOption(item1).map(e => resolved.Elem(e.bridgeElem.toElem)) ==
        nonXdtScenarioAspectOption(item2).map(e => resolved.Elem(e.bridgeElem.toElem))
    case (item1: ItemFact, _) => false
    case (_, item2: ItemFact) => false
    case _ => true
  }

  private def matchOnUnit(fact1: Fact, fact2: Fact): Boolean = (fact1, fact2) match {
    case (item1: NumericItemFact, item2: NumericItemFact) if (item1.unitRef == item2.unitRef) => true
    case (item1: NumericItemFact, item2: NumericItemFact) if unitAspect(item1).divideOption.isDefined && unitAspect(item2).divideOption.isDefined =>
      unitAspect(item1).divideOption.get.numerator.toSet == unitAspect(item2).divideOption.get.numerator.toSet &&
        unitAspect(item1).divideOption.get.denominator.toSet == unitAspect(item2).divideOption.get.denominator.toSet
    case (item1: NumericItemFact, item2: NumericItemFact) if unitAspect(item1).divideOption.isEmpty && unitAspect(item2).divideOption.isEmpty =>
      unitAspect(item1).measures.toSet == unitAspect(item2).measures.toSet
    case (item1: NumericItemFact, item2: NumericItemFact) => false
    case (item1: NumericItemFact, _) => false
    case (_, item2: NumericItemFact) => false
    case _ => true
  }

  private def matchOnDimension(dimension: EName, fact1: Fact, fact2: Fact): Boolean = {
    // TODO Typed dimension
    (fact1, fact2) match {
      case (item1: ItemFact, item2: ItemFact) if (item1.contextRef == item2.contextRef) => true
      case (item1: ItemFact, item2: ItemFact) =>
        explicitDimensionAspectOption(dimension, item1) == explicitDimensionAspectOption(dimension, item2)
      case (item1: ItemFact, _) => false
      case (_, item2: ItemFact) => false
      case _ => true
    }
  }

  // Non-dimensional aspect model aspect matching

  private def matchOnCompleteSegment(fact1: Fact, fact2: Fact): Boolean = (fact1, fact2) match {
    case (item1: ItemFact, item2: ItemFact) if (item1.contextRef == item2.contextRef) => true
    case (item1: ItemFact, item2: ItemFact) =>
      // Lacks type-safety
      completeSegmentAspectOption(item1).map(_.asResolvedElem) ==
        completeSegmentAspectOption(item2).map(_.asResolvedElem)
    case (item1: ItemFact, _) => false
    case (_, item2: ItemFact) => false
    case _ => true
  }

  private def matchOnCompleteScenario(fact1: Fact, fact2: Fact): Boolean = (fact1, fact2) match {
    case (item1: ItemFact, item2: ItemFact) if (item1.contextRef == item2.contextRef) => true
    case (item1: ItemFact, item2: ItemFact) =>
      // Lacks type-safety
      completeScenarioAspectOption(item1).map(_.asResolvedElem) ==
        completeScenarioAspectOption(item2).map(_.asResolvedElem)
    case (item1: ItemFact, _) => false
    case (_, item2: ItemFact) => false
    case _ => true
  }

  // Other private methods

  private def segmentExplicitDimensionAspects(itemFact: ItemFact): Map[EName, EName] = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    context.entity.segmentOption.map(e => e.explicitDimensions).getOrElse(Map())
  }

  private def scenarioExplicitDimensionAspects(itemFact: ItemFact): Map[EName, EName] = {
    val context =
      xbrlInstance.allContextsById.getOrElse(itemFact.contextRef, sys.error(s"Missing context with ID ${itemFact.contextRef}"))
    context.scenarioOption.map(e => e.explicitDimensions).getOrElse(Map())
  }
}

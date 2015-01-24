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
 * Taxonomy query API, for navigating from concepts to concepts or resources. Most functions below have very fast
 * implementations, backed by appropriate Maps in the underlying taxonomy model.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyQueryApi extends Any {

  def concept: EName

  /**
   * Finds all arcs of the given arc type with this source concept.
   */
  final def findOutgoingArcs[A <: StandardArc](arcType: ClassTag[A])(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    filterOutgoingArcs(arcType)(_ => true)(taxonomy)
  }

  /**
   * Finds all arcs of the given arc type with this target concept.
   */
  final def findIncomingArcs[A <: InterConceptArc](arcType: ClassTag[A])(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    filterIncomingArcs(arcType)(_ => true)(taxonomy)
  }

  /**
   * Filters the arcs of the given arc type with this source concept that obey the given arc filter.
   */
  final def filterOutgoingArcs[A <: StandardArc](arcType: ClassTag[A])(p: A => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    filterOutgoingArcs(concept, arcType)(p)(taxonomy)
  }

  /**
   * Filters the arcs of the given arc type with this target concept that obey the given arc filter.
   */
  final def filterIncomingArcs[A <: InterConceptArc](arcType: ClassTag[A])(p: A => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    filterIncomingArcs(concept, arcType)(p)(taxonomy)
  }

  /**
   * Converts this concept to its global element declaration, if any, wrapped in an Option.
   */
  final def asOptionalGlobalElementDeclaration(implicit taxonomy: TaxonomyModel): Option[GlobalElementDeclaration] = {
    taxonomy.findAllSchemas.flatMap(_.findGlobalElementDeclarationByEName(concept)).headOption
  }

  /**
   * Returns the equivalent of `asOptionalGlobalElementDeclaration(taxonomy).get`.
   */
  final def asGlobalElementDeclaration(implicit taxonomy: TaxonomyModel): GlobalElementDeclaration = {
    asOptionalGlobalElementDeclaration(taxonomy).
      getOrElse(sys.error(s"Could not find global element declaration $concept"))
  }

  /**
   * Finds all longest arc chains with arcs of the given arc type, having this concept as source concept,
   * such that predicate `first` holds for the first arc in each such chain, and `succ` holds for each intermediate
   * "prefix" arc chain result and the following arc.
   *
   * This is a very general method, used to implement methods such as `findOutgoingParentChildArcChains`.
   *
   * Typically this method should not be used in application code if a more specific method is available.
   * When using this method, keep in mind that a combinatorial explosion is possible. Even non-termination is
   * possible, if there are arc cycles, and `succ` does not return false on cycle detection.
   */
  final def findOutgoingArcChains[A <: InterConceptArc](arcType: ClassTag[A])(first: A => Boolean)(succ: (ArcChain[A], A) => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[ArcChain[A]] = {
    findOutgoingArcChains(concept, arcType)(first)(succ)(taxonomy)
  }

  /**
   * Finds all longest arc chains with arcs of the given arc type, having this concept as target concept,
   * such that predicate `last` holds for the last arc in each such chain, and `pred` holds for each intermediate
   * "suffix" arc chain result and the preceding arc.
   *
   * This is a very general method, used to implement methods such as `findIncomingParentChildArcChains`.
   *
   * Typically this method should not be used in application code if a more specific method is available.
   * When using this method, keep in mind that a combinatorial explosion is possible. Even non-termination is
   * possible, if there are arc cycles, and `pred` does not return false on cycle detection.
   */
  final def findIncomingArcChains[A <: InterConceptArc](arcType: ClassTag[A])(last: A => Boolean)(pred: (ArcChain[A], A) => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[ArcChain[A]] = {
    findIncomingArcChains(concept, arcType)(last)(pred)(taxonomy)
  }

  /**
   * Returns all longest parent-child arc chains of the given extended link role, with this concept as source concept.
   * On cycle detection, the arc chain is returned instead of being extended any further.
   */
  final def findOutgoingParentChildArcChains(elr: String)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[ArcChain[PresentationArc]] = {
    def hasCorrectElr(arc: PresentationArc): Boolean = arc.linkRole == elr

    def hasCorrectElrAndNoCycles(ch: ArcChain[PresentationArc], arc: PresentationArc): Boolean = {
      hasCorrectElr(arc) && !ch.append(arc).hasCycle
    }

    findOutgoingArcChains(classTag[PresentationArc])(hasCorrectElr)(hasCorrectElrAndNoCycles)(taxonomy)
  }

  /**
   * Returns all longest parent-child arc chains of the given extended link role, with this concept as target concept.
   * On cycle detection, the arc chain is returned instead of being extended any further.
   */
  final def findIncomingParentChildArcChains(elr: String)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[ArcChain[PresentationArc]] = {
    def hasCorrectElr(arc: PresentationArc): Boolean = arc.linkRole == elr

    def hasCorrectElrAndNoCycles(ch: ArcChain[PresentationArc], arc: PresentationArc): Boolean = {
      hasCorrectElr(arc) && !ch.prepend(arc).hasCycle
    }

    findIncomingArcChains(classTag[PresentationArc])(hasCorrectElr)(hasCorrectElrAndNoCycles)(taxonomy)
  }

  private def filterOutgoingArcs[A <: StandardArc](thisConcept: EName, arcType: ClassTag[A])(p: A => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    implicit val arcClassTag = arcType
    taxonomy.standardArcsBySource.getOrElse(thisConcept, Vector()) collect { case arc: A if p(arc) => arc }
  }

  private def filterIncomingArcs[A <: InterConceptArc](thisConcept: EName, arcType: ClassTag[A])(p: A => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    implicit val arcClassTag = arcType
    taxonomy.interConceptArcsByTarget.getOrElse(thisConcept, Vector()) collect { case arc: A if p(arc) => arc }
  }

  private def findOutgoingArcChains[A <: InterConceptArc](thisConcept: EName, arcType: ClassTag[A])(first: A => Boolean)(succ: (ArcChain[A], A) => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[ArcChain[A]] = {
    val nextArcs = filterOutgoingArcs(thisConcept, arcType)(first)(taxonomy)

    val arcChains = nextArcs.flatMap(arc => findAllLongestArcChainsStartingWith(ArcChain.from(arc), arcType)(succ)(taxonomy))
    arcChains
  }

  private def findAllLongestArcChainsStartingWith[A <: InterConceptArc](thisChain: ArcChain[A], arcType: ClassTag[A])(p: (ArcChain[A], A) => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[ArcChain[A]] = {
    val concept = thisChain.targetConcept
    val nextArcs = filterOutgoingArcs(concept, arcType)(arc => p(thisChain, arc))(taxonomy)

    if (nextArcs.isEmpty) {
      Vector(thisChain)
    } else {
      val arcChains = nextArcs flatMap { arc =>
        assert(thisChain.canAppend(arc))
        val nextChain = thisChain.append(arc)

        // Recursive calls
        val nextArcChains = findAllLongestArcChainsStartingWith(nextChain, arcType)(p)(taxonomy)
        nextArcChains
      }
      arcChains
    }
  }

  private def findIncomingArcChains[A <: InterConceptArc](thisConcept: EName, arcType: ClassTag[A])(last: A => Boolean)(pred: (ArcChain[A], A) => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[ArcChain[A]] = {
    val prevArcs = filterIncomingArcs(thisConcept, arcType)(last)(taxonomy)

    val arcChains = prevArcs.flatMap(arc => findAllLongestArcChainsEndingWith(ArcChain.from(arc), arcType)(pred)(taxonomy))
    arcChains
  }

  private def findAllLongestArcChainsEndingWith[A <: InterConceptArc](thisChain: ArcChain[A], arcType: ClassTag[A])(p: (ArcChain[A], A) => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[ArcChain[A]] = {
    val concept = thisChain.sourceConcept
    val prevArcs = filterIncomingArcs(concept, arcType)(arc => p(thisChain, arc))(taxonomy)

    if (prevArcs.isEmpty) {
      Vector(thisChain)
    } else {
      val arcChains = prevArcs flatMap { arc =>
        assert(thisChain.canPrepend(arc))
        val prevChain = thisChain.prepend(arc)

        // Recursive calls
        val prevArcChains = findAllLongestArcChainsEndingWith(prevChain, arcType)(p)(taxonomy)
        prevArcChains
      }
      arcChains
    }
  }
}

/**
 * The TaxonomyQueryApi companion object, offering an implicit conversion from (concept) ENames to the TaxonomyQueryApi.
 */
object TaxonomyQueryApi {

  implicit class ToTaxonomyQueryApi(override val concept: EName) extends AnyVal with TaxonomyQueryApi {
  }
}

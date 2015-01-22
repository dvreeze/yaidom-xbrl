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
import scala.reflect.ClassTag
import eu.cdevreeze.yaidom.core.EName

/**
 * Taxonomy query API, for navigating from concepts to concepts or resources. Most functions below have very fast
 * implementations, backed by appropriate Maps in the underlying taxonomy model.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyQueryApi extends Any {

  def concept: EName

  final def findOutgoingArcs[A <: StandardArc](arcType: ClassTag[A])(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    filterOutgoingArcs(arcType)(_ => true)(taxonomy)
  }

  final def findIncomingArcs[A <: InterConceptArc](arcType: ClassTag[A])(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    filterIncomingArcs(arcType)(_ => true)(taxonomy)
  }

  final def filterOutgoingArcs[A <: StandardArc](arcType: ClassTag[A])(p: A => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    implicit val arcClassTag = arcType
    taxonomy.standardArcsBySource.getOrElse(concept, Vector()) collect { case arc: A if p(arc) => arc }
  }

  final def filterIncomingArcs[A <: InterConceptArc](arcType: ClassTag[A])(p: A => Boolean)(implicit taxonomy: TaxonomyModel): immutable.IndexedSeq[A] = {
    implicit val arcClassTag = arcType
    taxonomy.interConceptArcsByTarget.getOrElse(concept, Vector()) collect { case arc: A if p(arc) => arc }
  }

  final def asOptionalGlobalElementDeclaration(implicit taxonomy: TaxonomyModel): Option[GlobalElementDeclaration] = {
    taxonomy.findAllSchemas.flatMap(_.findGlobalElementDeclarationByEName(concept)).headOption
  }

  final def asGlobalElementDeclaration(implicit taxonomy: TaxonomyModel): GlobalElementDeclaration = {
    asOptionalGlobalElementDeclaration(taxonomy).
      getOrElse(sys.error(s"Could not find global element declaration $concept"))
  }
}

/**
 * The TaxonomyQueryApi companion object, offering an implicit conversion from (concept) ENames to the TaxonomyQueryApi.
 */
object TaxonomyQueryApi {

  implicit class ToTaxonomyQueryApi(override val concept: EName) extends AnyVal with TaxonomyQueryApi {
  }
}

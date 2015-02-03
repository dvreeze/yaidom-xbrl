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
 * Queryable taxonomy model, mixing in arc (chain) query API traits.
 *
 * @author Chris de Vreeze
 */
final class QueryableTaxonomyModel(val model: TaxonomyModel) extends ArcQueryApi with PresentationArcQueryApi {

  val standardArcsBySource: Map[EName, immutable.IndexedSeq[StandardArc]] =
    model.standardArcsBySource

  val interConceptArcsByTarget: Map[EName, immutable.IndexedSeq[InterConceptArc]] =
    model.interConceptArcsByTarget
}

object QueryableTaxonomyModel {

  final implicit class ToQueryableTaxonomyModel(val model: TaxonomyModel) {

    def queryable: QueryableTaxonomyModel = new QueryableTaxonomyModel(model)
  }
}

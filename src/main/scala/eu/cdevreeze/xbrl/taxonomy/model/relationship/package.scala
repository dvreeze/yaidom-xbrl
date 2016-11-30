/*
 * Copyright 2011-2017 Chris de Vreeze
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

package eu.cdevreeze.xbrl.taxonomy.model

/**
 * Standard or non-standard relationships in XBRL taxonomies. Relationships are like arcs, but with resolved
 * source and target elements. In the case of standard relationships, the source is the expanded name of a concept.
 * In the case of standard inter-concept relationships, the target is also the expanded name of a concept.
 *
 * Relationships are modeled as case classes. These case classes make relationships easy to create. Moreover, relationships
 * are easy to reason about, because the source and target are resolved. They are already useful without any other
 * (taxonomy) context.
 *
 * Being case classes, relationships have meaningful equality defined.
 *
 * Relationships contain all the data needed for grouping them into base sets and networks of relationships.
 * In particular, the extended link role of the parent extended link is kept in the relationship, and the element
 * name of the parent extended link is always known from the relationship class name.
 *
 * Relationships do not keep any reference to the linkbase file from which they potentially originate. They also
 * do not know in which DTS they reside.
 *
 * TODO What about the different equalities defined in XBRL?
 *
 * The XML representation of these relationship linkbases uses XLink, but no XLink locators, replacing them with
 * XLink resources that contain the concept names as QNames.
 *
 * @author Chris de Vreeze
 */
package object relationship

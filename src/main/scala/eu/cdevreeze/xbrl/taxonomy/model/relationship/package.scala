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
 * Relationships do not contain any XLink attributes. Given that extended link roles and arc roles (and resource
 * roles in resources) are kept anyway, other XLink attributes such as the XLink type and href are not relevant for
 * relationships. They are relevant at the syntactic level, where we have arcs, locators and resources, where the
 * locators are "unresolved" and may point to elements in other files.
 *
 * TODO What about the different equalities defined in XBRL?
 *
 * TODO What about an XML representation for relationships?
 *
 * To come up with syntactic linkbases from a collection of relationships, mind the following:
 * <ul>
 * <li>We need a mapping from relationships to file path names.</li>
 * <li>We also need an "invertible Scope" to generate QNames from ENames.</li>
 * <li>We know the element name (implicitly) and extended link role (explicitly) of the parent extended link element.</li>
 * <li>The arc role and other attributes of the underlying arc are known. Locators and resources can also be constructed
 * and the corresponding XLink attributes can be created (using heuristics to generate XLink labels).</li>
 * <li>Given heuristics to make absolute URIs relative, we can create the locators with their XLink href attributes.</li>
 * <li>How do we prevent duplicate labels and references in label and reference linkbases?</li>
 * <li>Mind DTS discovery. A post-processing step may be needed to make DTS discovery work.</li>
 * </ul>
 *
 * @author Chris de Vreeze
 */
package object relationship

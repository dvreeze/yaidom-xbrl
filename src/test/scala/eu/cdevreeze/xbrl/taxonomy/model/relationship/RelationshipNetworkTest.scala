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

package eu.cdevreeze.xbrl.taxonomy.model.relationship

import scala.Vector
import scala.math.BigDecimal.int2bigDecimal

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.xbrl.ENames._

/**
 * Relationship network test case.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class RelationshipNetworkTest extends FunSuite {

  test("testRelationshipEquivalence") {
    // See XBRL-CONF-2014-12-10/Common/200-linkbase/210-relationshipEquivalence.xml, V-1

    val elr = Relationship.DefaultLinkRole
    val summationItemRelationshipBuilder = CalculationRelationship.summationItemRelationshipBuilder(elr)

    val scope = Scope.from(
      "t" -> "http://mycompany.com/xbrl/taxonomy")

    import scope._

    // TODO Equivalents of XLink attributes show, actuate and title, which must be treated as exempt!

    val summationItemRelationships = Vector(
      summationItemRelationshipBuilder.build(
        QName("t:fixedAssets").res,
        QName("t:changeInRetainedEarnings").res,
        RelationshipAttributes.Empty.withOrder(BigDecimal(2)).withPriority(0).withUse(RelationshipAttributes.UseOptional).
          plusAttribute(EName("weight"), NumberAttributeValue(1))),
      summationItemRelationshipBuilder.build(
        QName("t:fixedAssets").res,
        QName("t:changeInRetainedEarnings").res,
        RelationshipAttributes.Empty.withOrder(2).withPriority(1).withUse(RelationshipAttributes.UseProhibited).
          plusAttribute(EName("weight"), NumberAttributeValue(1))))

    val expectedRelationshipKey =
      InterConceptRelationshipKey(
        BaseSetKey(LinkCalculationLinkEName, elr, LinkCalculationArcEName, summationItemRelationshipBuilder.arcRole),
        Map(OrderEName -> NumberAttributeValue(BigDecimal(2)), EName("weight") -> NumberAttributeValue(1)),
        QName("t:fixedAssets").res,
        QName("t:changeInRetainedEarnings").res)

    assertResult(Set(expectedRelationshipKey)) {
      summationItemRelationships.map(_.relationshipKey).toSet
    }
  }
}

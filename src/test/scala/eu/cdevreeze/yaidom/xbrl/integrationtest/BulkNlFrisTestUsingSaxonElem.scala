/*
 * Copyright 2011 Chris de Vreeze
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

package eu.cdevreeze.yaidom.xbrl.integrationtest

import java.io.File

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.yaidom.xbrl.saxon.BridgeElemTakingSaxonElem
import eu.cdevreeze.yaidom.xbrl.XbrlInstanceDocument
import eu.cdevreeze.yaidom.xbrl.saxon
import net.sf.saxon.om.DocumentInfo
import net.sf.saxon.s9api.Processor

/**
 * See NlFrisTest, but this time validating in parallel and in bulk, using Saxon elements.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class BulkNlFrisTestUsingSaxonElem extends AbstractBulkNlFrisTest {

  private val processor = new Processor(false)

  protected def getXbrlInstanceDocument(): XbrlInstanceDocument = {
    val docBuilder = processor.newDocumentBuilder()

    val parentDir = new File(pathToParentDir.getPath)
    val f = new File(parentDir, "kvk-rpt-grote-rechtspersoon-geconsolideerd-model-b-e-indirect-2013.xbrl")

    val node = docBuilder.build(f)

    val xbrlInstanceDoc: XbrlInstanceDocument =
      new XbrlInstanceDocument(
        Some(f.toURI),
        BridgeElemTakingSaxonElem.wrap(new saxon.DomDocument(node.getUnderlyingNode.asInstanceOf[DocumentInfo]).documentElement))

    xbrlInstanceDoc
  }
}

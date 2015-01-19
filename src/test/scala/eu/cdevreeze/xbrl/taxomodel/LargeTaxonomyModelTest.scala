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

package eu.cdevreeze.xbrl.taxomodel

import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.Suite
import org.scalatest.junit.JUnitRunner
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.docaware
import eu.cdevreeze.yaidom.parse.DocumentParserUsingSax
import eu.cdevreeze.yaidom.simple.Document
import java.net.URI
import java.io.File
import eu.cdevreeze.yaidom.parse.DocumentParser
import java.io.InputStream
import eu.cdevreeze.xbrl.taxo.Taxonomy
import eu.cdevreeze.yaidom.print.DocumentPrinterUsingDom

/**
 * Large TaxonomyModel parsing test.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class LargeTaxonomyModelTest extends Suite {

  private val clazz = classOf[LargeTaxonomyModelTest]

  @Test def testParseTaxonomyModel(): Unit = {
    def isXml(file: File): Boolean = Set(".xml", ".xsd").exists(s => file.toString.endsWith(s))
    val xmlFiles = findFiles(new File(clazz.getResource("/nltaxo").toURI), isXml)

    val docParser = getDocumentParser
    val docPrinter = DocumentPrinterUsingDom.newInstance

    val taxoDocs =
      xmlFiles.map(f => docParser.parse(f.toURI)).map(doc => docaware.Document(doc.uriOption.get, doc))
    val taxo = new Taxonomy(taxoDocs)

    val taxoModelBuilder = new TaxonomyModelBuilder(taxo)

    val firstLabelLink = {
      val linkbase = taxo.linkbases.find(_.bridgeElem.docUri.toString.contains("cbs-bedr-items-lab-en.xml")).get
      val orgLabelLink = linkbase.labelLinks.head

      taxoModelBuilder.convertToLabelLink(orgLabelLink)
    }

    val xmlString = docPrinter.print(firstLabelLink.simpleElem.prettify(2))
    println(xmlString)

    assertResult(true) {
      taxo.docs.size >= 40
    }
    assertResult(true) {
      taxo.linkbases.size >= 30
    }
    assertResult(true) {
      taxo.schemaDocs.exists(doc => doc.uri == new URI("http://www.nltaxonomie.nl/9.0/basis/cbs/items/cbs-bedr-items.xsd"))
    }
  }

  private def findFiles(root: File, fileFilter: File => Boolean): Vector[File] = {
    // Recursive calls
    root.listFiles.toVector flatMap {
      case d: File if d.isDirectory => findFiles(d, fileFilter)
      case f: File if f.isFile && fileFilter(f) => Vector(f)
      case _ => Vector()
    }
  }

  private def fileUriToHttpUri(fileUri: URI): URI = {
    require(fileUri.getScheme == "file" && fileUri.getPath.contains("www.nltaxonomie.nl"))
    new URI("http://" + (fileUri.toString.substring(fileUri.toString.indexOf("www.nltaxonomie.nl"))))
  }

  private def httpUriToFileUri(httpUri: URI): URI = {
    require(httpUri.getScheme == "http")
    new URI("file:///" + httpUri.getHost + httpUri.getPath)
  }

  private def getDocumentParser: DocumentParser = {
    val wrappedDocParser = DocumentParserUsingSax.newInstance

    new DocumentParser() {
      def parse(is: InputStream): Document = ???

      def parse(f: File): Document = ???

      def parse(uri: URI): Document = {
        val localUri = if (uri.getScheme == "http") httpUriToFileUri(uri) else uri

        val doc = wrappedDocParser.parse(localUri)

        doc.withUriOption(Some(fileUriToHttpUri(localUri)))
      }
    }
  }
}

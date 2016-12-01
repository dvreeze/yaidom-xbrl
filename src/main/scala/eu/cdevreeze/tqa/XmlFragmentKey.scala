package eu.cdevreeze.tqa

import java.net.URI

import eu.cdevreeze.yaidom.core.Path
import eu.cdevreeze.yaidom.queryapi.BackingElemApi

/**
 * A unique identifier of an XML fragment in a Taxonomy. It is made up by
 * the document URI and the Path within that document.
 */
final case class XmlFragmentKey(val docUri: URI, val path: Path)

object XmlFragmentKey {

  implicit class XmlFragmentKeyAware(val backingElem: BackingElemApi) {

    def key: XmlFragmentKey =
      XmlFragmentKey(backingElem.docUri, backingElem.path)
  }
}

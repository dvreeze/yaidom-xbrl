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

package eu.cdevreeze.yaidom.xbrl

import java.net.URI
import eu.cdevreeze.yaidom.bridge.IndexedBridgeElem

/**
 * Immutable XBRL instance document. Its main value in addition to its document element is its optional URI.
 * Expensive to create, because of the cached XBRL instance element.
 *
 * @author Chris de Vreeze
 */
final class XbrlInstanceDocument(val uriOption: Option[URI], val bridgeElem: IndexedBridgeElem) {

  /** The document element, as XbrlInstance */
  val xbrlInstance: XbrlInstance =
    XbrliElem(bridgeElem).asInstanceOf[XbrlInstance]
}

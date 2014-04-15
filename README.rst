===========
Yaidom-XBRL
===========

Yaidom-XBRL is XBRL(-like) support based on a small foundation of yaidom. XML Schema, XPath (2.0) and XLink are not part of this
foundation. In other words, yaidom-XBRL is an attempt to reconstruct much of XBRL without the XML stack that it is officially based on.
Hence, yaidom-XBRL tries to simplify "XBRL" a lot.

For example, the idea of typing, let alone static typing of XML has been abandoned. Validation of XML against a schema has been replaced
by validations as yaidom queries. (Of course this is very ambitious. Think about the difference between lexical space and value space, and
about the degrees of freedom in content models of complex types with complex context.)

XLink has been removed as well, and replaced by more readable XML. For example, parent-child links in presentation links become oneliner
XML elements with @parent and @child attributes containing QNames of concepts.

XPath, like XML Schema, has been replaced by yaidom queries. XBRL formulas are also written as yaidom queries.

Of course, all of this is not yet the case, but the idea is that it will become reality.

XBRL instances
==============

XBRL instances in yaidom-xbrl are "XBRL" views on immutable "indexed elements".

Links and arcs
==============

Yaidom-XBRL replaces linkbases with XLink by concise "linkbases" without XLink. Two kinds of arcs are supported:

* Locator-locator arcs
* Locator-resource arcs

These arcs are represented as XML elements that contain the locators and resources, if any, as attributes and child
elements. Examples of locator-locator arcs are the representations of parent-child relationships in presentation linkbases,
for example. Examples of locator-resource arcs are the representations of label and reference arcs, for example. In the latter
representations, references and labels shared by multiple elements are repeated per yaidom-XBRL arc.

Yaidom-XBRL arcs are "indexed elements" with a root element containing an ELR (extended link role), and where the arc element
is the child of that root element. Resources become children of the arc element, and locators are represented as arc element
attributes.

The locator attributes, if referring to element declarations, have a QName as value (so need no traversal to another document
to get semantics). Of course the QName must be resolvable using the in-scope namespaces.

The element name of the arc is determined by a mapping from pairs of original element name and arcrole to yaidom-XBRL element names.

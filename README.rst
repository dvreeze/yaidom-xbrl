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


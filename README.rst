===========
Yaidom-XBRL
===========

Yaidom-XBRL is an attempt at representing XBRL taxonomies and instances at a somewhat higher level of abstraction.
It tries to do so in a mathematically sound manner, retaining important semantics such as networks of relationships and cycle detection.
This should be realized not only in code, but also in higher level XML formats for linkbases, etc.
The hope is that much XBRL processing can thus at least become more light-weight than it currently is.

Of course the lower level syntactic taxonomy and instance XML formats cannot be totally ignored, but the idea is
to make them less important and move them more to the "periphery", for instance when publishing taxonomies.

XBRL instances should be modeled after the XBRL OIM, based on aspects rather than lower level syntax. The higher level
taxonomy model is described in one of the following sections.


Goals
=====

This experiment hopefully leads to the following, for example:

* Easy to create taxonomy and instance (test) data
* Easy to read and understand taxonomy and instance data
* Mathematical precision in the model, for example when resolving prohibition and overriding, or when detecting cycles
* Easy to develop taxonomies, without the pain of cross-references among files
* Faster taxonomy processing with a low memory footprint
* Especially much faster XBRL instance validation (core and dimensional)
* Easy to generate from original taxonomy and instance files
* Given several heuristics, relatively easy to use as input to generate taxonomy and instance files
* Easy to merge taxonomies during taxonomy development
* Easy to store taxonomy versions in a database
* A decreased need for syntactic checks on taxonomies (think NTA)

Not all XBRL corner cases need to be supported. For example, the following may not be supported:

* Chameleon schemas. Not even schemas without target namespace are supported, unless only appinfo sections are used.
* Embedded linkbases within schema data.
* Schema includes. Schema imports (without the schemaLocation) are supported, however.
* Default and fixed attributes in schemas.


CTM
===

The higher level XBRL taxonomy model is called CTM (concise taxonomy model). It tries to do away with URIs
(not URIs as in namespace URIs or role type URIs, but hrefs and schemaLocations), replacing them with "business keys"
such as concept expanded names and role types.

This should solve or at least evade the following issues:

* Tight coupling between linkbases and files referenced from locators in those linkbases. This also makes it hard to understand relationships in isolation.
* Tight coupling between taxonomy files because of their role in DTS discovery. That's unfortunate: taxonomy files not only carry semantics, but also contribute to DTS discovery.

It is this mix of roles that makes the tight coupling among taxonomy files even worse than it otherwise would have been.
DTSes can instead be descibed by summing up the files, or by "smart joins" of CTM content.

How can we get rid of URI locations in taxonomy files? The first attempt used generic linkbases, in which XLink locators
were replaced by XLink resources containing "business keys". The advantage would be that XBRL tools would understand
them to be XBRL generic linkbases. Alas, this attemp was not good enough. Generic linkbases require XLink simple links for
custom role types and arcrole types, which defeats the purpose of getting rid of URI locations.

A more drastic solution was needed. It seemed necessary to "redesign XLink" itself.


CXLink
======

CXLink (concise XLink) is the concise "XLink" underlying CTM. Despite the name it is not (a subset of) XLink.

CXLink must meet the following requirements:

* It must replace XLink locators containing hrefs by CXLink resources containing "business keys" such as concept expanded names.
* It must be more concise than XLink, and easier to understand in isolation.
* It must offer its notions of extended links, arcs and resources, and support the (semantic) role and arcrole attributes.
* It must support a CTM in which networks of relationships and cycles can be expressed. Prohibition/overriding resolution must be possible on CTM taxonomies.
* It should be more consistent with references in schema documents (based on expanded names) than XLink locators are.

CXLink meets these requirements as follows:

* CXLink offers extended links, arcs and resources, through its type attribute. It also offers "imports", as alternative to simple links without hrefs.
* CXLink also offers the (semantic) role and arcrole attributes.
* Arcs contain source and target as child nodes, with CXLink attribute position (values "from" or "to") pointing to source or target resource of the arc.
* Hence, arcs can be understood in isolation, except for the (element name and) role of the parent extended link element.
* Arc source and target child elements must be in a "CXLink resource substitution group". CTM builds on that with elements such as "clink:concept" containing "clink:qname" attributes.
* Alternatively to arc child elements, source and/or target can be elsewhere in the extended link, using from/to and label attributes, like for XLink.
* This should not alter the semantics of the extended link in any way.
* In any case, for each arc the source and target can and must be specified precisely once.

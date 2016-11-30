===========
Yaidom-XBRL
===========

This is an attempt at representing XBRL (taxonomies and instances) at a slightly higher level of abstraction.
That is, syntactic cruft like XLink locators and URI references disappear, but the data content is still the same in that
for example prohibition and override still work. Mathematical precision is strived for.

Taxonomies and instances are mostly represented by Scala case classes. For taxonomy schema content, this is a challenge.

DTSes are described by summing up content instead of discovery. Files are unknown in the taxonomy model, so generating
files from taxonomies in this model requires the use of some heuristics. Note that in original taxonomy files, each one
plays 2 roles: carrying semantics, and supporting DTS discovery. This mix makes taxonomy development hard.

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

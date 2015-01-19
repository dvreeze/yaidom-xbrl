===========
Yaidom-XBRL
===========

First of all, this is an XBRL taxonomy model and API, called YATM (yet another taxonomy model).

The XBRL taxonomy model is syntactic, but at the abstraction level of relationships instead of arcs. Note that one
arc can represent multiple relationships, and that a relationship corresponds to an arc, and only one source and
target concept (instead of all source and target concepts, if there are multiple ones). The model should be one or
several readable XML files, in which XLink is absent but concept QNames are used instead (which resolve to expanded
names using the in-scope namespaces).

By using a separate taxonomy model (serializable as XML) instead of the original taxonomy files, we lose ease of
updates to the taxonomy. Preferably, the taxonomy model has an in-memory representation that can be generated from
the original taxonomy files without serializing it.

Relationship prohibition and overriding must be possible on a given taxonomy model, resulting in an "effective" taxonomy
model, without requiring any further context. Cycle detection and equalities such as S-equality should preferably also
be possible using this taxonomy model alone. Foremost, however, the taxonomy model should make it easy to query for
concepts and their relationships. Types, substitution groups and their hierarchies should also be represented.

The taxonomy model API is an in-memory representation of the taxonomy model, wrapping the taxonomy model DOM trees.

An additional taxonomy query API, on top of the taxonomy model API, makes it possible to easily navigate from concepts
to other concepts and "resources".

The taxonomy model knows about standard relationships, but also about dimensional relationships and generic relationships.
Preferably the model can be instantiated with or without dimensional knowledge, in order to make the model useful
during stages of XBRL taxonomy validation. The model must also know about role types, arcrole types etc.
How can we automatically support new kinds of generic links?

Be careful with URIs (such as in hrefs) and id attributes in the original taxonomy. They may be meaningless or incorrect
in the taxonomy model.

The taxonomy model and its generation must be advanced enough to handle corner cases such as embedded linkbases,
the use of XML Base, XPointer in an XBRL context, chameleon schemas, and default/fixed attributes.
(Fortunately, for taxonomies xsi:nil does not apply.)

The serialized taxonomy model should be readable and concise. Hence the use of qualified names for concepts, whose
namespaces must be in scope.

Besides a taxonomy model and API, there is also an XBRL instance model and API.

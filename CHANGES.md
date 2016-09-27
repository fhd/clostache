2.0.0 (2016)
============
* Rename to cljstache
* Add support for Clojure 1.7 - 1.9
* Remove support for Clojure < 1.7
* Add support for ClojureScript 1.8 - 1.9

1.5.0 (2014-05-07)
==================
* Handle path whitespace consistently.

1.4.0 (2014-05-05)
==================
* Support variables containing templates.
* Support all seqable data structures.
* Make the data parameter optional.
* Allow lambda sections to render text.

1.3.1 (2012-11-20)
==================
* Fixed rendering of nested partials.
* Moved development dependencies to the dev profile.

1.3.0 (2012-04-05)
==================
* Move from Maven to Leiningen 2.
* Eliminated reflection warnings.
* Added `(parser/render-resource)`.

1.2.0 (2012-03-30)
==================
* Updated to Clojure 1.3.0.

1.1.0 (2012-03-27)
==================
* Added support for lambdas.

1.0.0 (2012-01-28)
==================
* Made Clostache compliant with the Mustache spec.
* Added support for dotted variable names.
* Added support for implicit iterators.
* Added support for partials.
* Added support for set delimiters.

0.6.0 (2011-10-28)
==================
* Fixed rendering issues with dollar signs and backslashes.
* Made missing and nil variables render as empty strings.

0.5.0 (2011-09-28)
==================
* Changed the Maven groupId to de.ubercode.clostache.
* Added support for single value sections.
* Added support for sequences as lists.

0.4.1 (2010-12-21)
==================
* Added support for repeated sections.

0.4.0 (2010-11-20)
==================
* Made HTML escaping identical to mustache.rb.
* Added inverted sections.

0.3.0 (2010-10-24)
==================
* Changed base namespace to `clostache`.

0.2.0 (2010-10-20)
==================
* Added comment tags.
* Made it possible for tags to contain whitespace (e.g. `{{ name }}`).
* Added support for alternative (ampersand) unescape syntax.
* Added boolean sections.

0.1.0 (2010-10-16)
==================
* Added variable tags (escaped and unescaped).
* Added lists.

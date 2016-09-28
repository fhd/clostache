Clostache
=========

[{{ mustache }}](http://mustache.github.com) for Clojure.

Compliant with the [Mustache spec](http://github.com/mustache/spec)
since version 1.0. Supporting lambdas since version 1.1.

Works with Clojure 1.5 since version 1.5. If you want to use Clostache in
Clojure 1.3 or 1.4 projects, use version 1.4. In Clojure 1.2 projects, use
version 1.1.

[![Build Status](https://secure.travis-ci.org/fhd/clostache.png?branch=master)](http://travis-ci.org/fhd/clostache)

Usage
-----

The easiest way to use Clostache in your project is via
[Clojars](http://clojars.org/de.ubercode.clostache/clostache).

Leiningen:

```clj
[de.ubercode.clostache/clostache "1.4.0"]
```

Maven:

```xml
<dependency>
  <groupId>de.ubercode.clostache</groupId>
  <artifactId>clostache</artifactId>
  <version>1.4.0</version>
</dependency>
```

To install it via [cljr](https://github.com/liebke/cljr), run:

```
cljr install de.ubercode.clostache/clostache
```

This is how you use Clostache:

```clj
(use 'clostache.parser)
(render "Hello, {{name}}!" {:name "Felix"})
```

You can render a resource from the classpath like this:

```clj
(use 'clostache.parser)
(render-resource "templates/hello.mustache" {:name "Michael"})
```

Each function supports an optional third argument, containing partials (see below).

Examples
--------

### Variable replacement ###

Variables are tags enclosed by two curly brackets (*mustaches*) and
will be replaced with the respective data.

Template:

```mustache
Hello, {{person}}!
```

Data:

```clj
{:person "World"}
```

Output:

```
Hello, World!
```

### Escaped output ###

The following characters will be replaced with HTML entities:
`&"<>`. Tags that use three curly brackets or start with `{{&` will
not be escaped.

Template:

```mustache
Escaped: {{html}}
Unescaped: {{{html}}}
Unescaped: {{&html}}
```

Data:

```clj
{:html "<h1>Hello, World!</h1>"}
```

Output:

```html
Escaped: &lt;h1&gt;Hello, World!&lt;/h1&gt;
Unescaped: <h1>Hello, World!</h1>
Unescaped: <h1>Hello, World!</h1>
```

### Sections ###

Sections start with a tag beginning with `{{#` and end with one
beginning with `{{/`. Their content is only rendered if the data is
either the boolean value `true`, a value or a non-empty list.

Template:

```mustache
{{#greet}}Hello, World!{{/greet}}
```

Data:

```clj
{:greet true}
```

Output:

```
Hello, World!
```

In case of a list, the section's content is rendered for each element,
and it can contain tags refering to the elements.

Template:

```mustache
<ul>
{{#people}}
    <li>{{name}}</li>
{{/people}}
</ul>
```

Data:

```clj
{:people [{:name "Felix"} {:name "Jenny"}]}
```

Output:

```html
<ul>
    <li>Felix</li>
    <li>Jenny</li>
</ul>
```

For single values, the section is rendered exactly once.

Template:

```mustache
{{#greeting}}{{text}}!{{/greeting}}
```

Data:

```clj
{:greeting {:text "Hello, World"}}
```

Output:

```
Hello, World!
```

### Inverted sections ###

Inverted sections start with a tag beginning with `{{^` and end with one
beginning with `{{/`. Their content is only rendered if the data is
either the boolean value `false` or an empty list.

Template:

```mustache
{{^ignore}}Hello, World!{{/ignore}}
```

Data:

```clj
{:ignore false}
```

Output:

```
Hello, World!
```

### Comments ###

Comments are tags that begin with `{{!`. They will not be rendered.

Template:

```mustache
<h2>Felix' section<h2>
{{! Look ma, I've written a section }}
```

Output:

```html
<h2>Felix' section</h2>
```

### Dotted names ###

Dotted names are a shorter and more convenient way of accessing nested
variables or sections.

Template:

```mustache
{{greeting.text}}
```

Data:

```clj
{:greeting {:text "Hello, World"}}
```

Output:

```
Hello, World
```

### Implicit iterators ###

Implicit iterators allow you to iterate over a one dimensional list of
elements.

Template:

```mustache
<ul>
{{#names}}
    <li>{{.}}</li>
{{/names}}
</ul>
```

Data:

```clj
{:names ["Felix" "Jenny"]}
```

Output:

```html
<ul>
    <li>Felix</li>
    <li>Jenny</li>
</ul>
```

### Partials ###

Partials allow you to include other templates (e.g. from separate files).

Template:

```mustache
Hello{{>names}}!
```

Data:

```clj
{:people [{:name "Felix"} {:name "Jenny"}]}
```

Partials:

```mustache
{:names "{{#people}}, {{name}}{{/people}}"}
```

Output:

```
Hello, Felix, Jenny!
```

### Set delimiters ###

You don't have to use mustaches, you can change the delimiters to
anything you like.

Template:

```mustache
{{=<% %>=}}
Hello, <%name%>!
```

Data:

```clj
{:name "Felix"}
```

Output:

```
Hello, Felix!
```

### Lambdas ###

You can also call functions from templates.

Template:

```mustache
{{hello}}
{{#greet}}Felix{{/greet}}
```

Data:

```clj
{:hello "Hello, World!"}
{:greet #(str "Hello, " %)}
```


Output:

```
Hello, World!
Hello, Felix!
```  

Functions can also render the text given to them if they need to do something more complicated.  

Template:

```mustache
"{{#people}}Hi {{#upper}}{{name}}{{/upper}}{{/people}}"
```

Data:
```clj
{:people [{:name "Felix"}] 
 :upper (fn [text] 
          (fn [render-fn] 
            (clojure.string/upper-case (render-fn text))))}
```

Output:  

```
Hello FELIX
```

Development
-----------

Make sure you have
[Leiningen 2](https://github.com/technomancy/leiningen/wiki/Upgrading)
installed.

To run the spec tests, fetch them like this:

```
git submodule update --init
```

And run them against all supported Clojure versions:

```
lein all test
```

License
-------

Copyright (C) 2014 Felix H. Dahlke

This library is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 2.1 of the
License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; see the file COPYING. If not, write
to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
Floor, Boston, MA 02110-1301 USA

Contributors
------------

* [Rory Geoghegan](https://github.com/rgeoghegan)
* [Santtu Lintervo](https://github.com/santervo)
* [Pierre-Alexandre St-Jean](https://github.com/pastjean)
* [Michael Klishin](https://github.com/michaelklishin)
* [Stan Rozenraukh](https://github.com/stanistan)
* [Tero Parviainen](https://github.com/teropa)
* [Masashi Iizuka](https://github.com/liquidz)
* [Julian Birch](https://github.com/JulianBirch)
* [Ryan Cole](https://github.com/ryancole)
* [Simon Lawrence](https://github.com/simonl2002)
* [Darrell Hamilton](https://github.com/zeroem)

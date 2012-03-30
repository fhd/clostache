Clostache
=========

[{{ mustache }}](http://mustache.github.com) for Clojure.

Compliant with the [Mustache spec](http://github.com/mustache/spec)
since version 1.0. Supporting lambdas since version 1.1.

Works with Clojure 1.3 since version 1.2. If you want to use Clostache
in Clojure 1.2 projects, use version 1.1.

Usage
-----

The easiest way to use Clostache in your project is via
[Clojars](http://clojars.org/de.ubercode.clostache/clostache).

Leiningen:

	[de.ubercode.clostache/clostache "1.2.0"]

Maven:

	<dependency>
	  <groupId>de.ubercode.clostache</groupId>
	  <artifactId>clostache</artifactId>
	  <version>1.2.0</version>
	</dependency>

To install it via [cljr](https://github.com/liebke/cljr), run:

	clrj install de.ubercode.clostache/clostache

This is how you can use Clostache from the REPL:

	=> (use 'clostache.parser)
	=> (render "Hello, {{name}}!" {:name "Felix"})
	"Hello, Felix!"

Examples
--------

### Variable replacement ###

Variables are tags enclosed by two curly brackets (*mustaches*) and
will be replaced with the respective data.

Template:

	Hello, {{person}}!
	
Data:

	{:person "World"}

Output:

	Hello, World!

### Escaped output ###

The following characters will be replaced with HTML entities:
`&"<>`. Tags that use three curly brackets or start with `{{&` will
not be escaped.

Template:

	Escaped: {{html}}
	Unescaped: {{{html}}}
	Unescaped: {{&html}}
	
Data:

	{:html "<h1>Hello, World!</h1>"}
	
Output:

	Escaped: &lt;h1&gt;Hello, World!&lt;/h1&gt;
	Unescaped: <h1>Hello, World!</h1>
	Unescaped: <h1>Hello, World!</h1>

### Sections ###

Sections start with a tag beginning with `{{#` and end with one
beginning with `{{/`. Their content is only rendered if the data is
either the boolean value `true`, a value or a non-empty list.

Template:

	{{#greet}}Hello, World!{{/greet}}
	
Data:

	{:greet true}
	
Output:

	Hello, World!

In case of a list, the section's content is rendered for each element,
and it can contain tags refering to the elements.

Template:

	<ul>
	{{#people}}
	    <li>{{name}}</li>
	{{/people}}
	</ul>
	
Data:

	{:people [{:name "Felix"} {:name "Jenny"}]}
	
Output:

	<ul>
	    <li>Felix</li>
	    <li<Jenny</li>
	</ul>

For single values, the section is rendered exactly once.

Template:

	{{#greeting}}{{text}}!{{/greeting}}

Data:

	{:greeting {:text "Hello, World"}}

Output:

	Hello, World!

### Inverted sections ###

Inverted sections start with a tag beginning with `{{^` and end with one
beginning with `{{/`. Their content is only rendered if the data is
either the boolean value `false` or an empty list.

Template:

	{{^ignore}}Hello, World!{{/ignore}}
	
Data:

	{:ignore false}
	
Output:

	Hello, World!

### Comments ###

Comments are tags that begin with `{{!`. They will not be rendered.

Template:

	<h2>Felix' section<h2>
	{{! Look ma, I've written a section }}
	
Output:

	<h2>Felix' section</h2>

### Dotted names ###

Dotted names are a shorter and more convenient way of accessing nested
variables or sections.

Template:

	{{greeting.text}}

Data:

	{:greeting {:text "Hello, World"}}

Output:

	Hello, World

### Implicit iterators ###

Implicit iterators allow you to iterate over a one dimensional list of
elements.

Template:

	<ul>
	{#names}}
	    <li>{{.}}</li>
	{{/names}}
	</ul>

Data:

	{:names ["Felix" "Jenny"]}

Output:

	<ul>
	    <li>Felix</li>
	    <li<Jenny</li>
	</ul>

### Partials ###

Partials allow you to include other templates (e.g. from separate files).

Template:

	Hello{{>names}}!

Data:

	{:people [{:name "Felix"} {:name "Jenny"}]}

Partials:

	{:names "{{#people}}, {{name}}{{/people}}"}

Output:

	Hello, Felix, Jenny!

### Set delimiters ###

You don't have to use mustaches, you can change the delimiters to
anything you like.

Template:

	{{=<% %>=}}
	Hello, <%name%>!

Data:

	{:name "Felix"}

Output:

	Hello, Felix!

### Lambdas ###

You can call also functions from templates.

Template:

        {{hello}}
	{{#greet}}Felix{{/greet}}

Data:

        {:hello "Hello, World!"}
	{:greet #(str "Hello, " %)}

Output:

        Hello, World!
	Hello, Felix!

Running the spec tests
----------------------

	git submodule update --init
	mvn test

License
-------

Copyright (C) 2010 Felix H. Dahlke

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

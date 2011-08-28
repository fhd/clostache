Clostache
=========

[{{ mustache }}](http://mustache.github.com) for Clojure.

A few features are still missing, but it should work for most
people. See the TODO file for not yet implemented features.

[![Flattr this](http://api.flattr.com/button/button-compact-static-100x17.png "Flattr this")](http://flattr.com/thing/73492/Clostache)

Usage
-----

The easiest way to use Clostache in your project is via
[Clojars](http://clojars.org/com.github.fhd.clostache/clostache).

To install it via [cljr](https://github.com/liebke/cljr), run:

	clrj install com.github.fhd.clostache/clostache

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

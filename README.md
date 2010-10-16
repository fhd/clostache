Clostache
=========

[{{ mustache }}](http://mustache.github.com) for Clojure.

Although it already works with simple templates, this is still work in
progress. See the TODO file for not yet implemented features.

Usage
-----

The easiest way to use Clostache in your project is via
[Clojars](http://clojars.org/com.github.fhd.clostache/clostache).

This is how you can use Clostache from the REPL:

	=> (use 'com.github.fhd.clostache.parser)
	=> (render "Hello, {{name}}!" {:name "Felix"})
	"Hello, Felix!"

See the test cases for more sophisticated examples.

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
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA

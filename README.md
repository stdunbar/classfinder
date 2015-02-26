# classfinder
A simple command line utility to find a particular class name in a directory
containing jar, war, and ear files.


A simple utility to find a particular class name in a directory containing jar,
war, and ear files. The application recursively decends into the directory
looking for files that end in ".jar", ".war", and ".ear" (case insensitive
which is not really the standard). Additionally, the program finds any normal
files that end in .class also.

Why did I write this? Because I find that when I'm looking for a particular
class to compile with I never seem to know which jar file it is in. This
utility simplifies that task. It is basically like a Unix "find" command piped
to the "jar" command that would "grep" for the class name. Cross-platform
issues made it easier to put this into a small java application rather than
have to install Cygwin on every Windows box I use.

 
# Downloads
ClassFinder can be downloaded from:

    https://github.com/stdunbar/classfinder

In here you'll find the build file and source code.


A sample command line might look like (should be all on one line):

`java -classpath lib/classfinder.jar com.xigole.util.ClassFinder -d some/directory/name -c SomeClassName`

or

`java -jar lib/classfinder.jar -d some/directory/name -c SomeClassName`

# Command line arguments
* -d specifies the directory name to use. This must be a directory. This is a required argument.
* -c specifies the class name to look for in the jar files. The search
is case insensitive unless you specify the -p option. This is a required
argument. Note that this argument can be a class name like MyClassName
in which case ClassFinder will look for the provided name regardless
of the package. Alternately, you can specify a package declaration like
com.xigole.MyClassName. Either way ClassFinder is simply looking for a pattern
that looks like the given string. Regular expressions are not supported.
* -p pay attention to the class name case. This makes it easier to find
something like "Log" when many classes may be in the "logging" package
* -v an optional argument that verbosely prints out each file that is being searched.

# Building
ClassFinder uses a standard Maven build file.  Just run

`mvn package`

to generate classfinder.jar

## Copyright
Copyright (C) 2004-2015 Scott Dunbar (scott@xigole.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy
of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.


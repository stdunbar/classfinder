package com.xigole.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A simple utility to find a particular class name in a directory containing
 * jar, war, and ear files. The application recursively descends into the
 * directory looking for files that end in &quot;.jar&quot;, &quot;.war&quot;,
 * and &quot;.ear&quot; (case insensitive which is not really the standard).
 * Additionally, the program finds any normal files that end in .class also.
 * <p>
 * 
 * A sample command line might look like: <br>
 * <code>java -classpath lib/classfinder.jar -d some/directory/name -c SomeClassName</code>
 * <p>
 * 
 * The command line arguments are:
 * <ul>
 * <li><b>-d </b> specifies the directory name to use. This must be a directory.
 * This is a required argument.</li>
 * <li><b>-c </b> specifies the class name to look for in the jar files. The
 * search is case insensitive. This is a required argument. Note that this
 * argument can be a class name like MyClassName in which case ClassFinder will
 * look for the provided name regardless of the package. Alternately, you can
 * specify a package declaration like com.xigole.MyClassName. Either way
 * ClassFinder is simply looking for a pattern that looks like the given string.
 * Regular expressions are not supported.</li>
 * <li><b>-p </b> pay attention to the class name case. This makes it easier to
 * find something like &quot;Log&quot; when many classes may be in the
 * &quot;logging&quot; package</li>
 * <li><b>-v </b> an optional argument that verbosely prints out each file that
 * is being searched.</li>
 * </ul>
 * <p>
 * 
 * Why did I write this? Because I find that when I'm looking for a particular
 * class to compile with I never seem to know which jar file it is in. This
 * utility simplifies that task. It is basically like a Unix &quot;find&quot;
 * command piped to the &quot;jar&quot; command that would &quot;grep&quot; for
 * the class name. Cross-platform issues made it easier to put into a small java
 * application rather than have to install Cygwin on every Windows box I use.
 * 
 * Copyright (C) 2004-2015 Scott Dunbar (scott@xigole.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Changes:
 *
 * November 07, 2006 1) Fixed a bug that prevented searching directories that
 * ended with one of the Java archive extensions (.jar, .ear, .war, etc.).
 * Thanks to Narasimhan Balasubramanian (narsibvl_2006@yahoo.com) for help with
 * this. 2) Added the ability to find .class files too.
 * 
 */
public class ClassFinder {
    private ArrayList<File> files = new ArrayList<>();
    private String dirName = null;
    private String className = null;
    private boolean verbose = false;
    private boolean ignoreCase = true;

    /**
     * The driver for the ClassFinder.
     */
    public static void main(String argv[]) {
        try {
            new ClassFinder(argv);
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }

    /**
     * Public constructor that expects to be given the argument array from the
     * command line.
     * 
     * @param argv
     *            - an array of Strings from the command line.
     * 
     * @throws an
     *             IllegalArgumentException if the command line is invalid or if
     *             the directory name specified is not really a directory.
     * 
     */
    public ClassFinder(String argv[]) throws IllegalArgumentException {
        parseArgs(argv);

        File directory = new File(dirName);
        String classNameToCompare = className;
        boolean foundSomething = false;

        if (ignoreCase)
            classNameToCompare = classNameToCompare.toLowerCase();

        //
        // class files are stored with a forward slash in the jar files
        // so convert any package-like paths into file system paths
        //
        classNameToCompare = classNameToCompare.replaceAll("\\.", "/");

        if (!directory.exists()) {
            usage();
            throw (new IllegalArgumentException("The directory \"" + dirName + "\" does not exist"));
        }

        if (!directory.isDirectory()) {
            usage();
            throw (new IllegalArgumentException("The file \"" + dirName + "\" is not a directory"));
        }

        buildFileList(directory);

        for (int i = 0; i < files.size(); i++) {
            File nextFile = (File) (files.get(i));
            JarFile nextJarFile = null;

            if (nextFile.getAbsolutePath().toLowerCase().endsWith(".class")) {
                boolean found = false;

                if (ignoreCase) {
                    if (nextFile.getAbsolutePath().toLowerCase().indexOf(classNameToCompare) != -1)
                        found = true;
                } else {
                    if (nextFile.getAbsolutePath().indexOf(classNameToCompare) != -1)
                        found = true;
                }

                if (found) {
                    System.out.print("\"" + className + "\" found at ");
                    System.out.println(nextFile.getAbsolutePath());
                    foundSomething = true;
                }

                continue;
            }

            try {
                nextJarFile = new JarFile(nextFile);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            if (verbose) {
                System.out.println("looking in " + nextFile.getAbsolutePath());
            }

            for (Enumeration<JarEntry> jarEntries = nextJarFile.entries(); jarEntries.hasMoreElements();) {
                boolean found = false;
                JarEntry nextEntry = jarEntries.nextElement();

                if (ignoreCase) {
                    if (nextEntry.getName().toLowerCase().indexOf(classNameToCompare) != -1)
                        found = true;
                } else {
                    if (nextEntry.getName().indexOf(classNameToCompare) != -1)
                        found = true;
                }

                if (found) {
                    System.out.print("\"" + className + "\" found in ");
                    System.out.print(nextFile.getAbsolutePath() + " as ");
                    System.out.println(nextEntry.getName());
                    foundSomething = true;
                }
            }

            try {
                nextJarFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!foundSomething)
            System.out.println("no classes with the string \"" + className + "\" found");
    }

    /**
     * Recursively descends into a directory looking for files to search for.
     * This populates the "files" List with files to look at later.
     *
     * @param nextFile
     *            a file or directory that is examined for files to add. If
     *            there are any directories within the File then they are
     *            recursively added.
     *
     */
    private void buildFileList(File nextFile) {
        File[] fileList = nextFile.listFiles();

        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    buildFileList(fileList[i]);
                }

                if (fileList[i].getName().toLowerCase().endsWith(".jar") || fileList[i].getName().toLowerCase().endsWith(".war")
                        || fileList[i].getName().toLowerCase().endsWith(".rar")
                        || fileList[i].getName().toLowerCase().endsWith(".ear")
                        || fileList[i].getName().toLowerCase().endsWith(".class")) {
                    if (!fileList[i].isDirectory())
                        files.add(fileList[i]);
                }
            }
        }
    }

    /**
     * Parses the command line arguments.
     *
     * @param argv
     *            - the command line argument list.
     *
     * @see #usage()
     */
    private void parseArgs(String argv[]) throws IllegalArgumentException {
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-v")) {
                verbose = true;
                continue;
            }

            if (argv[i].equals("-p")) {
                ignoreCase = false;
                continue;
            }

            if (argv[i].equals("-d")) {
                if (i + 1 >= argv.length) {
                    usage();
                    throw (new IllegalArgumentException("Directory name must be specified"));
                }

                dirName = argv[i + 1];
                i++;
                continue;
            }

            if (argv[i].equals("-c")) {
                if (i + 1 >= argv.length) {
                    usage();
                    throw (new IllegalArgumentException("Class name must be specified"));
                }

                className = argv[i + 1];
                i++;
                continue;
            }

            usage();
            throw (new IllegalArgumentException("Unknown argument \"" + argv[i] + "\""));
        }

        if (dirName == null) {
            usage();
            throw (new IllegalArgumentException("Directory name must be specified"));
        }

        if (className == null) {
            usage();
            throw (new IllegalArgumentException("Class name must be specified"));
        }
    }

    /**
     * Prints the usage message to System.err (stderr).
     *
     */
    private void usage() {
        System.err.println("usage: java " + getClass().getName() + " -d <dir_name> -c <class_name> [-p] [-v]");
        System.err.println("ClassFinder v" + ClassFinder.class.getPackage().getImplementationVersion()
                + " copyright (c) 2015 Scott Dunbar");
    }
}

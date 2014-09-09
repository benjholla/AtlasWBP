Atlas WAR Binary Processing (WBP)
========

The AtlasWBP plugin is Eclipse plugin that runs on top of Atlas and Soot to decompile and index Java WAR files to be used in program analysis.

## Background

> In software engineering, a WAR file (or Web application ARchive) is a JAR file used to distribute a collection of JavaServer Pages, Java Servlets, Java classes, XML files, tag libraries, static web pages (HTML and related files) and other resources that together constitute a web application. 
>
Source: https://en.wikipedia.org/wiki/WAR_(file_format)

Java Server Pages (JSPs) are an abstraction of Java Servlets.  A Servlet is a class that implements the Java Servlet API.  The Java Servlet API is a protocol that follows the request<->response programming model.  Servlets are not limited to any particular protocol but usually HTTP is used and while Servlets typically generate HTML or XML content they are not limited to any particular data format.

Looking at the figure below we can see that a client (represented as the computer at the top center) makes a request (most likely an HTTP request) to a .JSP page (Example http://www.domain.com/test.jsp). Once the request is received the JSP translator (such as a Tomcat server) translates the JSP page into a Java Servlet which is then compiled to Java byte code and execute in the Java Virtual Machine on the server. The result of executing the compiled Servlet is then returned as a response (most likely via a HTTP response) to the client machine.

![JSP Lifecycle](./README/JSPLifecycle.png)

## Implementation Details

### Highlevel Process

<ol>
<li>Unpack the WAR file and dump contents in an empty Eclipse Java project</li>
<li>Add all the JAR files found in the <code><project>/WEB-INF</code> directory to the classpath</li>
<li>Run ANT tasks to translate JSP pages to Class files and output to <code>&lt;project&gt;/WEB-INF/classes/*</code></li>
<li>JAR generated Class files into <code>&lt;project&gt;/WEB-INF/classes.jar</code></li>
<li>Convert classes.jar to Jimple and output to <code>&lt;project&gt;/WEB-INF/jimple/*</code></li>
</ol>

### Additional Details

The typical contents of a WAR file include a `META-INF` directory that contains the WAR manifest, a `WEB-INF` directory that contains compiled Java Class files, support libraries, and a `web.xml` file.  At the root level there are static web resources such as JavaScript, CSS, and images, as well as uncompiled JSP files (denoted below as the `myjsps` directory).

	|-META-INF
	|---MANIFEST.MF
	|-WEB-INF
	|---classes
	|-----com
	|-------example
	|---------demo
	|-----------SomeObject.class
	|---lib
	|-----mysql-connector-bin.jar
	|-css
	|--..
	|-img
	|--..
	|-js
	|--..
	|-myjsps
	|---default.jsp
	
Since Soot cannot convert a JSP file to Jimple, the JSP files must be translated (precompiled) to Java Class files.  The AtlasWBP uses a configurable ANT task to precompile the JSPs to Class files given an appropriate translator (ie Tomcat Server).  After the Any build task has completed the directory tree will look something like the following.

	|-META-INF
	|---MANIFEST.MF
	|-WEB-INF
	|---classes
	|-----com
	|-------example
	|---------demo
	|-----------SomeObject.class
    |-----org
	|-------apache
	|---------jsp
	|-----------default_jsp.class
	|-----------...
	|---lib
	|-----mysql-connector-bin.jar
	|-css
	|--..
	|-img
	|--..
	|-js
	|--..
	|-myjsps
	|---default.jsp
	
To clearly represent the application byte code in the project the contents of the `classes` directory are compressed into a single `classes.jar` JAR file at the root of the `WEB-INF` directory.  The AtlasWBP plugin then converts the `classes.jar` file to Jimple using Soot and writes the resulting Jimple source to a new `WEB-INF/jimple` directory.  These contents are included in an Eclipse Java project file that is indexable by Atlas for Jimple.

## Usage

TODO
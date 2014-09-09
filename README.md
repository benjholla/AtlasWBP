Atlas WAR Binary Processing (WBP)========The AtlasWBP plugin is Eclipse plugin that runs on top of Atlas and Soot to decompile and index Java WAR files to be used in program analysis.## Background> In software engineering, a WAR file (or Web application ARchive) is a JAR file used to distribute a collection of JavaServer Pages, Java Servlets, Java classes, XML files, tag libraries, static web pages (HTML and related files) and other resources that together constitute a web application. >Source: https://en.wikipedia.org/wiki/WAR_(file_format)Java Server Pages (JSPs) are an abstraction of Java Servlets.  A Servlet is a class that implements the Java Servlet API.  The Java Servlet API is a protocol that follows the request<->response programming model.  Servlets are not limited to any particular protocol but usually HTTP is used and while Servlets typically generate HTML or XML content they are not limited to any particular data format.Looking at the figure below we can see that a client (represented as the computer at the top center) makes a request (most likely an HTTP request) to a .JSP page (Example http://www.example.com/test.jsp). Once the request is received the JSP translator (such as a Tomcat server) translates the JSP page into a Java Servlet that is then compiled to Java byte code and execute in the Java Virtual Machine on the server. The result of executing the compiled Servlet is then returned as a response (most likely via a HTTP response) to the client machine.![JSP Lifecycle](./README/JSPLifecycle.png)## Implementation Details### High-level Process<old><li>Unpack the WAR file and dump contents in an empty Eclipse Java project</li><li>Add all the JAR files found in the <code><project>/WEB-INF</code> directory to the classpath</li><li>Run ANT tasks to translate JSP pages to Class files and output to <code>&lt;project&gt;/WEB-INF/classes/*</code></li><li>JAR generated Class files into <code>&lt;project&gt;/WEB-INF/classes.jar</code></li><li>Convert classes.jar to Jimple and output to <code>&lt;project&gt;/WEB-INF/jimple/*</code></li></ol>### Additional DetailsThe typical contents of a WAR file include a `META-INF` directory that contains the WAR manifest, a `WEB-INF` directory that contains compiled Java Class files, support libraries, and a `web.xml` file.  At the root level there are static web resources such as JavaScript, CSS, and images, as well as uncompiled JSP files (denoted below as the `myjsps` directory).	|-META-INF	|---MANIFEST.MF	|-WEB-INF	|---classes	|-----com	|-------example	|---------demo	|-----------SomeObject.class	|---lib	|-----mysql-connector-bin.jar	|-css	|--..	|-img	|--..	|-js	|--..	|-myjsps	|---default.jsp	Since Soot cannot convert a JSP file to Jimple, the JSP files must be translated (precompiled) to Java Class files.  The AtlasWBP uses a configurable ANT task to precompile the JSPs to Class files given an appropriate translator (i.e. Tomcat Server).  After the Any build task has completed the directory tree will look something like the following.	|-META-INF	|---MANIFEST.MF	|-WEB-INF	|---classes	|-----com	|-------example	|---------demo	|-----------SomeObject.class    |-----org	|-------apache	|---------jsp	|-----------default_jsp.class	|-----------...	|---lib	|-----mysql-connector-bin.jar	|-css	|--..	|-img	|--..	|-js	|--..	|-myjsps	|---default.jsp	To clearly represent the application byte code in the project the contents of the `classes` directory are compressed into a single `classes.jar` JAR file at the root of the `WEB-INF` directory.  The AtlasWBP plugin then converts the `classes.jar` file to Jimple using Soot and writes the resulting Jimple source to a new `WEB-INF/jimple` directory.  These contents are included in an Eclipse Java project file that is indexable by Atlas for Jimple.## SetupThis usage guide assumes you have an Eclipse with Atlas for Jimple installed already.  Atlas is available from [EnSoft](http://www.ensoftcorp.com/atlas/download/).### 1) Installing TomcatIn theory this plugin should work with any standard JSP compliant translator, but some tweaks may need to be made to this plugin and the ANT build script to get it to work with other translators aside from Tomcat.  For serious program analysis the plugin would ideally use whatever translator the WAR was targeted to run on in the production environment.  This plugin was tested on Ubuntu and OSX with Tomcat version 7 and Java 7.  Note that some of the installation instructions below may differ slightly for different operating systems and different versions of Tomcat.#### Step 1) Download the core Tomcat 7 distribution from [https://tomcat.apache.org/](https://tomcat.apache.org/).#### Step 2) Unzip and move contents to the location where you would like to run Tomcat.`tar xvzf apache-tomcat-7.0.55.tar.gz``sudo mv apache-tomcat-7.0.55 /opt/tomcat`#### Step 3) Set the file permissions.  Tomcat files need to be readable and scripts need to be executable.`sudo chown -R <your_username_here> /opt/tomcat``sudo chmod +x /opt/tomcat/bin/*.sh`#### Step 4) Test the Tomcat installation.Run `/opt/tomcat/bin/startup.sh` to startup the Tomcat server.  Navigate to [http://localhost:8080](http://localhost:8080).  If Tomcat is properly installed you should see a Tomcat welcome page.  Once you know the installation is working you can shutdown the server with the `/opt/tomcat/bin/shutdown.sh` script.### 2) Installing AtlasWBP#### Option A) Installing from SourceImport the `wbp` Eclipse project in this repository into the Eclipse workspace.  Right click on the project and select `Export`.  Select `Plug-in Development`->`Deployable plug-ins and fragments`.  Select the `Install into host. Repository:` radio box and click `Finish`.  Press `OK` for the notice about unsigned software.  Once Eclipse restarts the plugin is installed and it is advisable to close or remove the `wbp` project from the workspace.#### Option B) Install from Update SiteTODO## Usage
### Setting Preferences
During the first use you must configure your WAR Binary Processing preferences.  Specifically the AtlasWBP plugin needs to know where your Tomcat installation directory and your ANT precompile build task instructions are located.
In Eclipse navigate to `Eclipse`->`Preferences...`->`WBP Preferences`.  Select the location of the Tomcat directory and the ANT build task.  The build task file for a Tomcat 7 installation is included in this repo as the [compile-jsp.xml](./ant/compile-jsp.xml) file.
Click `Apply` and `OK` to apply the changes to the AtlasWBP preference settings.
### Creating a Binary WAR Project
Navigate to `File`->`Import`.  Select `Other`->`Import WAR Binary Project` and press `Next`.  Browse to the WAR file to import and enter a name for the Eclipse project to create.  
To index the project, navigate to the `Atlas` menu and select `Manage Project Settings`.  Ensure that the WAR file binary project is listed under the `Index` column and other projects that should be excluded from the analysis are not listed.  Click `OK` when the project index settings are correct, then navigate to the `Atlas` menu and select `Index Workspace`.  When the indexing process is complete you can query against indexed project like normal.  For additional information on using Atlas see [http://www.ensoftcorp.com/atlas/](http://www.ensoftcorp.com/atlas/).Note: This process was tested with a WAR binary file from [https://github.com/benjholla/LoginSideChannels](https://github.com/benjholla/LoginSideChannels).
---
layout: page
title: Install
permalink: /install/
---

Installing the WAR Binary Processing Eclipse plugin is easy.  It is recommended to install the plugin from the provided update site, but it is also possible to install from source.
        
### Installing from Update Site (recommended)
1. Start Eclipse, then select `Help` &gt; `Install New Software`.
2. Click `Add`, in the top-right corner.
3. In the `Add Repository` dialog that appears, enter &quot;Atlas Toolboxes&quot; for the `Name` and &quot;[https://ensoftcorp.github.io/toolbox-repository/](https://ensoftcorp.github.io/toolbox-repository/)&quot; for the `Location`.
4. In the `Available Software` dialog, select the checkbox next to "WAR Binary Processing" and click `Next` followed by `OK`.
5. In the next window, you'll see a list of the tools to be downloaded. Click `Next`.
6. Read and accept the license agreements, then click `Finish`. If you get a security warning saying that the authenticity or validity of the software can't be established, click `OK`.
7. When the installation completes, restart Eclipse.

## Installing from source
If you want to install from source for bleeding edge changes, first grab a copy of the [source](https://github.com/benjholla/AtlasWBP) repository. In the Eclipse workspace, import the `com.benjholla.wbp` Eclipse project located in the source repository.  Right click on the project and select `Export`.  Select `Plug-in Development` &gt; `Deployable plug-ins and fragments`.  Select the `Install into host. Repository:` radio box and click `Finish`.  Press `OK` for the notice about unsigned software.  Once Eclipse restarts the plugin will be installed and it is advisable to close or remove the `com.benjholla.wbp` project from the workspace.

## Changelog
Note that version numbers are based off [Atlas](http://www.ensoftcorp.com/atlas/download/) version numbers.

### 3.9.2
- Updated dependencies

### 3.1.6
- Updated dependencies

### 3.0.10
- Bumped Atlas dependency to 3.0.10 to avoid a discovered bug in Jimple generation

### 3.0.7
- Centralized logging, refactored packages, added toolbox commons menu extension point, updated supplemental files, bug fixes

### 2.3.2
- Compatibility fixes, bug fixes, general refactoring, improvements to import wizard, generalized precompilation task for future non-tomcat containers

### 0.25.0
- Initial release

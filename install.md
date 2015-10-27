---
layout: page
title: Install
permalink: /install/
---

Installing the WAR Binary Processing Eclipse plugin is easy.  It is recommended to install the plugin from the provided update site, but it is also possible to install from source.
        
### Installing Dependencies
1. First make sure you have [Atlas](http://www.ensoftcorp.com/atlas/download/) Standard or Pro installed.
2. When installing Atlas make sure to also include Atlas Experimental features (you must have Atlas for Jimple support).
        
### Installing from update site
Follow the steps below to install the WAR Binary Processing plugin from the Eclipse update site.

1. Start Eclipse, then select `Help` &gt; `Install New Software`.
2. Click `Add`, in the top-right corner.
3. In the `Add Repository` dialog that appears, enter &quot;`WAR Binary Processing`&quot; for the `Name` and &quot;[http://ben-holland.com/AtlasWBP/updates/](http://ben-holland.com/AtlasWBP/updates/)&quot; for the `Location`.
4. In the `Available Software` dialog, select the checkbox next to "WAR Binary Processing" and click `Next` followed by `OK`.
5. In the next window, you'll see a list of the tools to be downloaded. Click `Next`.
6. Read and accept the license agreements, then click `Finish`. If you get a security warning saying that the authenticity or validity of the software can't be established, click `OK`.
7. When the installation completes, restart Eclipse.

## Installing from source
If you want to install from source for bleeding edge changes, follow the instructions at the [source](https://github.com/benjholla/AtlasWBP) repository.
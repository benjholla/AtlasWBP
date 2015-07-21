package wbp.common;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

import soot.G;
import soot.SootClass;
import soot.util.Chain;
import wbp.Activator;
import wbp.log.Log;
import wbp.ui.PreferencePage;

import com.ensoftcorp.abp.common.soot.ConfigManager;
import com.ensoftcorp.abp.common.util.JimpleUtil;
import com.ensoftcorp.abp.common.util.JimpleUtil.JimpleSource;

public class WarToJimple {
	
	/**
	 * Creates an Eclipse project from WAR file
	 * General Overview:
	 * 1) Unpack the WAR file and dump contents in an empty Eclipse Java project
	 * 2) Add all the jar files found in the <project>/WEB-INF directory to the classpath
	 * 3) Run ANT tasks to translate JSP pages to Class files and output to <project>/WEB-INF/classes/*
	 * 4) JAR generated Class files into <project>/WEB-INF/classes.jar
	 * 5) Convert classes.jar to Jimple and output to <project>/WEB-INF/jimple/*
	 */
	public static IStatus createWarBinaryProject(String projectName, IPath projectPath, File warFile, IProgressMonitor monitor) throws CoreException, IOException, SootConversionException {
		IProject project = null;
		try {
			monitor.beginTask("Creating WAR Binary project", 5);
			monitor.setTaskName("Unpacking WAR");
			File projectDirectory = new File(projectPath + File.separator + projectName);
			
			// clean stale files from project directory
			WarUtils.delete(projectDirectory);

			// extract war contents to project directory
			WarUtils.unjar(warFile, projectDirectory);
			monitor.worked(1);

			// create empty Java project
			monitor.setTaskName("Creating Eclipse project");
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
			URI location = null;
			if (projectPath != null){
				location = URIUtil.toURI(projectPath);
			}
			if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
				location = null;
			} else {
				location = URIUtil.toURI(URIUtil.toPath(location) + File.separator + projectName);
			}
			desc.setLocationURI(location);
			desc.setNatureIds(new String[] { JavaCore.NATURE_ID });
			
			// create and open the Eclipse project
			project.create(desc, null);
			IJavaProject jProject = JavaCore.create(project);
			project.open(new NullProgressMonitor());
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			
			// add the default JVM classpath (assuming translator uses the same jvm libraries)
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			for (LibraryLocation element : JavaRuntime.getLibraryLocations(vmInstall)) {
				entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
			}
			
			// add the JAR libraries in the WEB-INF folder to the project classpath
			LinkedList<File> projectJars = getProjectJars(projectDirectory);
			for(File projectJar : projectJars){
				String projectJarCanonicalPath = projectJar.getCanonicalPath();
				String projectCanonicalPath = projectDirectory.getCanonicalPath();
				String projectJarBasePath = projectJarCanonicalPath.substring(projectJarCanonicalPath.indexOf(projectCanonicalPath));
				String projectJarParentCanonicalPath = projectJar.getCanonicalPath();
				String projectJarParentBasePath = projectJarParentCanonicalPath.substring(projectJarParentCanonicalPath.indexOf(projectCanonicalPath));
				entries.add(JavaCore.newLibraryEntry(new Path(projectJarBasePath), null, new Path(projectJarParentBasePath)));
			}
			
			// set the class path
			jProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
			
			monitor.worked(1);
			Log.info("Successfully created WBP project");
			if (monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}

			// run ant tasks to precompile JSPs
			monitor.setTaskName("Translating Java Server Pages");
			String translatorPath = Activator.getDefault().getPreferenceStore().getString(PreferencePage.TRANSLATOR_PATH);
			if(translatorPath == null || translatorPath.equals("")){
				throw new RuntimeException(PreferencePage.TRANSLATOR_PATH_DESCRIPTION + " is not set.");
			}
			File translatorDirectory = new File(translatorPath);
			if(!translatorDirectory.exists()){
				throw new RuntimeException(translatorDirectory.getAbsolutePath() + " does not exist.");
			}
			String buildTaskPath = Activator.getDefault().getPreferenceStore().getString(PreferencePage.ANT_PRECOMPILE_JSP_BUILD_TASK_PATH);
			if(buildTaskPath == null || buildTaskPath.equals("")){
				throw new RuntimeException(PreferencePage.ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DESCRIPTION + " is not set.");
			}
			File buildTaskFile = new File(buildTaskPath);
			if(!buildTaskFile.exists()){
				throw new RuntimeException(buildTaskFile.getAbsolutePath() + " does not exist.");
			}
			precompileJavaServerPages(translatorDirectory, projectDirectory, buildTaskFile, new NullProgressMonitor());
			monitor.worked(1);
			Log.info("Successfully translated JSPs");
			if (monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}

			monitor.setTaskName("Adding translated Class files to Jar");
			File webinfDirectory = new File(projectDirectory.getAbsolutePath() + File.separatorChar + "WEB-INF");
			File classesJar = new File(webinfDirectory.getAbsolutePath() + File.separatorChar + "classes.jar");
			File classesDirectory = new File(webinfDirectory.getAbsolutePath() + File.separatorChar + "classes");
			WarUtils.jar(classesDirectory, classesJar);
			monitor.worked(1);
			if (monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}
			
			monitor.setTaskName("Converting Jar files to Jimple");
			File jimpleDirectory = new File(projectDirectory.getAbsolutePath() + File.separatorChar + "WEB-INF" + File.separatorChar + "jimple");
			jarToJimple(classesJar, jimpleDirectory);
			monitor.worked(1);
			
			return Status.OK_STATUS;
		} finally {
			monitor.done();
			if (project != null && project.exists()){
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}
		}
	}
	
	// helper method for converting a jar file of classes to jimple
	private static void jarToJimple(File jar, File outputDirectory) throws SootConversionException {
		if(!outputDirectory.exists()){
			outputDirectory.mkdirs();
		}
		ConfigManager.getInstance().startTempConfig();
		try {
			G.reset();
		
			String[] args = new String[] {
					"-src-prec", "class", 
					"--xml-attributes",
					"-f", "jimple",
					"-allow-phantom-refs",
					"-output-dir", outputDirectory.getAbsolutePath(),
					"-process-dir", jar.getAbsolutePath()
			};
			
			try {
				soot.Main.main(args);
				JimpleUtil.writeHeaderFile(JimpleSource.JAR, jar.getAbsolutePath(), outputDirectory.getAbsolutePath());
				
				// warn about any phantom references
				Chain<SootClass> phantomClasses = soot.Scene.v().getPhantomClasses();
                if (!phantomClasses.isEmpty()) {
                        TreeSet<String> missingClasses = new TreeSet<String>();
                        for (SootClass sootClass : phantomClasses) {
                                missingClasses.add(sootClass.toString());
                        }
                        StringBuilder message = new StringBuilder();
                        message.append("Some classes were referenced, but could not be found.\n\n");
                        for (String sootClass : missingClasses) {
                                message.append(sootClass);
                                message.append("\n");
                        }
                        Log.warning(message.toString());
                }
			} catch (RuntimeException e) {
				throw new SootConversionException(e);
			}
		} finally {
			// restore the saved config (even if there was an error)
            ConfigManager.getInstance().endTempConfig();
		}
	}
	
	// Throwable exception wrapper to make a runtime soot conversion exception checked
	private static class SootConversionException extends Exception {
		private static final long serialVersionUID = 1L;
		public SootConversionException(Throwable t) {
			super(t);
		}
	}
	
	// helper method for location project jar libraries
	private static LinkedList<File> getProjectJars(File projectDirectory){
		return findJars(new File(projectDirectory.getAbsolutePath() + File.separatorChar + "WEB-INF"));
	}
	
	// helper method for recursively finding jar files in a given directory
	private static LinkedList<File> findJars(File directory){
		LinkedList<File> jars = new LinkedList<File>();
		if(directory.exists()){
			if (directory.isDirectory()) {
				for (File c : directory.listFiles()) {
					jars.addAll(findJars(c));
				}
			}
			File file = directory;
			if(file.getName().endsWith(".jar")){
				jars.add(file);
			}
		}
		return jars;
	}
	
	// helper method to translate the JSPs to class files inside the project
	private static void precompileJavaServerPages(File translatorDirectory, File projectDirectory, File buildFile, IProgressMonitor monitor) throws CoreException {
		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(buildFile.getAbsolutePath());
		runner.setArguments("-Dtranslator.home=\"" + translatorDirectory.getAbsolutePath() 
							+ "\" -Dwebapp.path=\"" + projectDirectory.getAbsolutePath() + "\"");
		runner.run(monitor);
	}
	
}

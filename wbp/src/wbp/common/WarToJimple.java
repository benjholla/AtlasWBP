package wbp.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import wbp.Activator;
import wbp.ui.PreferencePage;

import com.ensoftcorp.atlas.core.log.Log;

public class WarToJimple {

	// helper function to recursively delete a file or directory
	private static void delete(File file) throws FileNotFoundException {
		if(file.exists()) {
			if (file.isDirectory()) {
				for (File c : file.listFiles()) {
					delete(c);
				}
			}
			if (!file.delete()){
				throw new FileNotFoundException("Failed to delete file: " + file);
			}
		}
	}
	
	// helper file to unzip the WAR file contents
	private static void unzip(String zipFile, String outputFolder) throws IOException {
		byte[] buffer = new byte[1024];
		ZipFile archive = new ZipFile(new File(zipFile));
		try {
			Enumeration<? extends ZipEntry> entries = archive.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File file = new File(outputFolder + File.separator + entry.getName());
				// create a place to put the file or make the directory
				if(entry.isDirectory()){
					file.mkdirs();
					continue;
				} else {
					new File(file.getParent()).mkdirs();
				}
				// write the file
				FileOutputStream out = new FileOutputStream(file);
				try {
					InputStream in = archive.getInputStream(entry);
					try {
						int len;
						while ((len = in.read(buffer)) > 0) {
							out.write(buffer, 0, len);
						}
					} finally {
						in.close();
					}
					out.flush();
				} finally {
					out.close();
				}
			}
		} finally {
			archive.close();
		}
	}
	
	/**
	 * Create Eclipse project from WAR file
	 */
	public static IStatus createWarBinaryProject(String projectName, IPath projectPath, File warFile, IProgressMonitor monitor) throws CoreException, IOException {
		IProject project = null;
		
		try {
			monitor.beginTask("Creating new project", 5);
			monitor.setTaskName("Unpacking WAR");
			File projectDirectory = new File(projectPath + File.separator + projectName);
			
			// clean stale files from project directory
			delete(projectDirectory);

			// extract war contents to project directory
			unzip(warFile.getAbsolutePath(), projectDirectory.getAbsolutePath());
			monitor.worked(1);

			// create empty Java project
			monitor.setTaskName("Creating project");
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
			monitor.worked(1);
			
			if (monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}
			
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			jProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
			
			monitor.worked(1);
			if (monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}
			Log.info("Project created successfully!");

			// TODO: run ant tasks to precompile JSPs
			monitor.setTaskName("Translating Java Server Pages");
			
			String tomcatPath = Activator.getDefault().getPreferenceStore().getString(PreferencePage.TOMCAT_PATH);
			if(tomcatPath == null || tomcatPath.equals("")){
				throw new RuntimeException(PreferencePage.TOMCAT_PATH_DESCRIPTION + " is not set.");
			}
			File tomcatDirectory = new File(tomcatPath);
			if(!tomcatDirectory.exists()){
				throw new RuntimeException(tomcatDirectory.getAbsolutePath() + " does not exist.");
			}
			
			String buildTaskPath = Activator.getDefault().getPreferenceStore().getString(PreferencePage.ANT_PRECOMPILE_JSP_BUILD_TASK_PATH);
			if(buildTaskPath == null || buildTaskPath.equals("")){
				throw new RuntimeException(PreferencePage.ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DESCRIPTION + " is not set.");
			}
			File buildTaskFile = new File(buildTaskPath);
			if(!buildTaskFile.exists()){
				throw new RuntimeException(buildTaskFile.getAbsolutePath() + " does not exist.");
			}
			
			precompileJavaServerPages(tomcatDirectory, projectDirectory, buildTaskFile, monitor);
			monitor.worked(1);
			
			// TODO: convert class files to jimple
			monitor.setTaskName("Converting Class files to Jimple");
//			String outputDir = projectPath.toString() + File.separator + projectName + File.separator + "src";
//			WarToJimple.warToJimple(war, outputDir);
			monitor.worked(1);
			
			if (monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}
			
			return Status.OK_STATUS;
		} finally {
			monitor.done();
			if (project != null && project.exists()){
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}
		}
	}
	
	// helper method to translate the JSPs to class files inside the project
	private static void precompileJavaServerPages(File tomcatDirectory, File projectDirectory, File buildFile, IProgressMonitor monitor) throws CoreException {
		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(buildFile.getAbsolutePath());
		runner.setArguments("-Dtomcat.home=\"" + tomcatDirectory.getAbsolutePath() 
							+ "\" -Dwebapp.path=\"" + projectDirectory.getAbsolutePath() + "\"");
		runner.run(monitor);
	}
	
}

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

import com.ensoftcorp.atlas.core.log.Log;

public class WarToJimple {

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
	
	private static void unzip(String zipFile, String outputFolder) throws IOException {
		byte[] buffer = new byte[1024];

		ZipFile archive = new ZipFile(new File(zipFile));
		try {
			Enumeration<? extends ZipEntry> entries = archive.entries();
	
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File newFile = new File(outputFolder + File.separator + entry.getName());
				
				if(entry.isDirectory()){
					newFile.mkdirs();
					continue;
				} else {
					new File(newFile.getParent()).mkdirs();
				}
				
				FileOutputStream out = new FileOutputStream(newFile);
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
	
	public static IStatus createWarBinaryProject(String projectName, IPath projectPath, File warFile, IProgressMonitor monitor) throws CoreException, IOException {
		IProject project = null;
		
		try {
			monitor.beginTask("Creating new project", 6);

			monitor.setTaskName("Unpacking war");
			String unpackedPath = projectPath + File.separator + projectName;

			File projectHandle = new File(unpackedPath);
			delete(projectHandle);

			unzip(warFile.getAbsolutePath(), unpackedPath);
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

			// Disassemble war into project
			monitor.setTaskName("Converting class files to jimple");
			
//			TODO: String outputDir = projectPath.toString() + File.separator + projectName + File.separator + "src";
//			TODO: WarToJimple.warToJimple(war, outputDir);
			monitor.worked(3);
			
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
	
}

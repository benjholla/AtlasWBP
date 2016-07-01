package wbp.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;

import wbp.Activator;
import wbp.core.WarToJimple;

import com.ensoftcorp.atlas.core.log.Log;

public class ImportWARWizard extends Wizard implements IImportWizard {

	private NewWARBinaryProjectPage page;
	
	public ImportWARWizard(String startWARPath) {
		page = new NewWARBinaryProjectPage("Create WAR Binary Project", startWARPath);
		String projectName = new File(startWARPath).getName();
		projectName = projectName.substring(0, projectName.lastIndexOf('.'));
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project.exists()) {
			// find a project name that doesn't collide
			int i = 2;
			while (project.exists()) {
				i++;
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName + "_" + i);
			}
			projectName = projectName + "_" + i;
		}
		page.setInitialProjectName(projectName);
		this.setWindowTitle("Create WAR Binary Project");
	}
	
	public ImportWARWizard() {
		page = new NewWARBinaryProjectPage("Create WAR Binary Project");
		this.setWindowTitle("Create WAR Binary Project");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {}
	
	@Override
	public void addPages() {
		this.addPage(page);
	}

	@Override
	public boolean performFinish() {
		final String projectName = page.getProjectName();
		final IPath projectLocation = page.getLocationPath();
		final File warFile = new File(page.getWARPath());

		IRunnableWithProgress j = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				IStatus result = null;
				try {
					result = WarToJimple.createWarBinaryProject(projectName, projectLocation, warFile, monitor);
				} catch (Throwable t) {
					String message = "Could not create WAR binary project. " + t.getMessage();
					UIJob uiJob = new ShowErrorDialogJob("Showing error dialog", message, projectName);
					uiJob.schedule();
					Log.error(message, t);
				} finally {
					monitor.done();
				}
				if(result.equals(Status.CANCEL_STATUS)) {
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
					deleteProject(project);
				}
			}
		};

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		
		try {
			dialog.run(true, true, j);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static IStatus deleteProject(IProject project) {
		if (project != null && project.exists())
			try {
				project.delete(true, true, new NullProgressMonitor());
			} catch (CoreException e) {
				Log.error("Could not delete project", e);
				return new Status(Status.ERROR, Activator.PLUGIN_ID, "Could not delete project", e);
			}
		return Status.OK_STATUS;
	}
	
	private static class NewWARBinaryProjectPage extends WizardNewProjectCreationPage {
		private String warPath;
		
		public NewWARBinaryProjectPage(String pageName, String startWARPath) {
			super(pageName);
			warPath = startWARPath;
		}
		
		public NewWARBinaryProjectPage(String pageName) {
			this(pageName, "");
		}
		
		public String getWARPath() {
			return warPath;
		}
		
		@Override
		public void createControl(Composite parent) {
			super.createControl(parent);
			Composite composite = (Composite) this.getControl();
			
			final FileDialog fileChooser = new FileDialog(composite.getShell(), SWT.OPEN);
			fileChooser.setFilterExtensions(new String[] { "*.war" });
			
			Composite row = new Composite(composite, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			row.setLayout(layout);
			
			GridData data = new GridData();
			data.horizontalAlignment = SWT.FILL;
			row.setLayoutData(data);
			
			Label labelWAR = new Label(row, SWT.NONE);
			labelWAR.setText("WAR:");
			
			final Text textWAR = new Text(row, SWT.SINGLE | SWT.BORDER);
			textWAR.setText(warPath);
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.horizontalAlignment = SWT.FILL;
			textWAR.setLayoutData(data);
			
			textWAR.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					warPath = textWAR.getText();
				}
			});
			
			Button buttonBrowseWAR = new Button(row, SWT.PUSH);
			buttonBrowseWAR.setText("     Browse...     ");
			buttonBrowseWAR.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!warPath.isEmpty()){
						fileChooser.setFileName(warPath);
					}
					String path = fileChooser.open();
					if (path != null){
						warPath = path;
					}
					textWAR.setText(warPath);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
				
			});
		}
	}
	
	private static class ShowErrorDialogJob extends UIJob {

		private String message, projectName;
		
		public ShowErrorDialogJob(String name, String errorMessage, String projectName) {
			super(name);
			this.message = errorMessage;
			this.projectName = projectName;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			Path iconPath = new Path("icons" + File.separator + "WBP.gif");
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			Image icon = null;
			try {
				icon = new Image(PlatformUI.getWorkbench().getDisplay(), FileLocator.find(bundle, iconPath, null).openStream());
			} catch (IOException e) {
				Log.error("WBP.gif icon is missing.", e);
			};
			MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
													"Could Not Create WAR Binary Project", 
													icon, 
													message, 
													MessageDialog.ERROR,
													new String[] { "Delete Project", "Cancel" }, 
													0);
			int response = dialog.open();

			IStatus status = Status.OK_STATUS;
			if (response == 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				status = deleteProject(project);
			}
			
			if (icon != null){
				icon.dispose();
			}
			
			return status;
		}
		
	}
}

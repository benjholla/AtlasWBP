package wbp.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import wbp.Activator;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// translator path name and label
	public static final String TRANSLATOR_PATH = "TRANSLATOR_PATH";
	public static final String TRANSLATOR_PATH_DESCRIPTION = "Translator Directory";
	
	// precompile jsp build task path name and label
	public static final String ANT_PRECOMPILE_JSP_BUILD_TASK_PATH = "ANT_PRECOMPILE_JSP_BUILD_TASK_PATH";
	public static final String ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DESCRIPTION = "Ant Precompile JSP Build Task";
	
	// copy translator runtime jars option name and label
	public static final String COPY_TRANSLATOR_RUNTIME_JARS_BOOLEAN = "COPY_TRANSLATOR_RUNTIME_JARS";
	public static final String COPY_TRANSLATOR_RUNTIME_JARS_DESCRIPTION = "Copy Translator Runtime Jars into Project";
	
	// allow phantom refs option name and label
	public static final String ALLOW_PHANTOM_REFERENCES_BOOLEAN = "ALLOW_PHANTOM_REFERENCES";
	public static final String ALLOW_PHANTOM_REFERENCES_DESCRIPTION = "Allow Phantom References";
	
	public PreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Configure preferences for the WAR Binary Processing plugin.");
	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(TRANSLATOR_PATH, "&" + TRANSLATOR_PATH_DESCRIPTION + ":", getFieldEditorParent()));
		addField(new FileFieldEditor(ANT_PRECOMPILE_JSP_BUILD_TASK_PATH, "&" + ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DESCRIPTION + ":", getFieldEditorParent()));
		addField(new BooleanFieldEditor(COPY_TRANSLATOR_RUNTIME_JARS_BOOLEAN, "&" + COPY_TRANSLATOR_RUNTIME_JARS_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(ALLOW_PHANTOM_REFERENCES_BOOLEAN, "&" + ALLOW_PHANTOM_REFERENCES_DESCRIPTION, getFieldEditorParent()));
	}

}

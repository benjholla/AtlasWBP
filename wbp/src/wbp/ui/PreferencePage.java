package wbp.ui;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import wbp.Activator;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String TRANSLATOR_PATH = "TRANSLATOR_PATH";
	public static final String TRANSLATOR_PATH_DESCRIPTION = "Translator Directory";
	
	public static final String ANT_PRECOMPILE_JSP_BUILD_TASK_PATH = "ANT_PRECOMPILE_JSP_BUILD_TASK_PATH";
	public static final String ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DESCRIPTION = "Ant Precompile JSP Build Task";
	
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
	}

}

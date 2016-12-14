package com.benjholla.wbp.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.benjholla.wbp.Activator;
import com.benjholla.wbp.preferences.WBPPreferences;

public class WBPPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String TRANSLATOR_PATH_DESCRIPTION = "Translator Directory";
	public static final String ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DESCRIPTION = "Ant Precompile JSP Build Task";
	public static final String COPY_TRANSLATOR_RUNTIME_JARS_DESCRIPTION = "Copy Translator Runtime Jars into Project";
	public static final String PHANTOM_REFERENCES_DESCRIPTION = "Allow Phantom References";
	
	public WBPPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Configure preferences for the WAR Binary Processing plugin.");
	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(WBPPreferences.TRANSLATOR_PATH, "&" + TRANSLATOR_PATH_DESCRIPTION + ":", getFieldEditorParent()));
		addField(new FileFieldEditor(WBPPreferences.ANT_PRECOMPILE_JSP_BUILD_TASK_PATH, "&" + ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DESCRIPTION + ":", getFieldEditorParent()));
		addField(new BooleanFieldEditor(WBPPreferences.COPY_TRANSLATOR_RUNTIME_JARS, "&" + COPY_TRANSLATOR_RUNTIME_JARS_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(WBPPreferences.PHANTOM_REFERENCES, "&" + PHANTOM_REFERENCES_DESCRIPTION, getFieldEditorParent()));
	}

}

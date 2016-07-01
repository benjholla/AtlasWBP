package wbp.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import wbp.Activator;
import wbp.log.Log;

public class WBPPreferences extends AbstractPreferenceInitializer {

	private static boolean initialized = false;
	
	/**
	 * Defines the path to the JSP translator
	 */
	public static final String TRANSLATOR_PATH = "TRANSLATOR_PATH";
	public static final String TRANSLATOR_PATH_DEFAULT = null;
	private static String translatorPathValue = TRANSLATOR_PATH_DEFAULT;
	
	public static String getTranslatorPath(){
		if(!initialized){
			loadPreferences();
		}
		return translatorPathValue;
	}
	
	/**
	 * Defines the path to the ant task to precompile the JSP
	 */
	public static final String ANT_PRECOMPILE_JSP_BUILD_TASK_PATH = "ANT_PRECOMPILE_JSP_BUILD_TASK_PATH";
	public static final String ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DEFAULT = null;
	private static String antPrecompileJSPBuildTaskPathValue = ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DEFAULT;
	
	public static String getAntPrecompileJSPBuildTaskPath(){
		if(!initialized){
			loadPreferences();
		}
		return antPrecompileJSPBuildTaskPathValue;
	}
	
	/**
	 * Enable/disable decompiling with phantom references runtime jars
	 */
	public static final String PHANTOM_REFERENCES = "PHANTOM_REFERENCES";
	public static final Boolean PHANTOM_REFERENCES_DEFAULT = false;
	private static boolean phantomReferencesValue = PHANTOM_REFERENCES_DEFAULT;
	
	public static boolean isPhantomReferencesEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return phantomReferencesValue;
	}
	
	/**
	 * Enable/disable copying translator runtime jars
	 */
	public static final String COPY_TRANSLATOR_RUNTIME_JARS = "COPY_TRANSLATOR_RUNTIME_JARS";
	public static final Boolean COPY_TRANSLATOR_RUNTIME_JARS_DEFAULT = false;
	private static boolean copyTranslatorRuntimeJarsValue = COPY_TRANSLATOR_RUNTIME_JARS_DEFAULT;
	
	public static boolean isCopyTranslatorRuntimeJarsEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return copyTranslatorRuntimeJarsValue;
	}
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setDefault(TRANSLATOR_PATH, TRANSLATOR_PATH_DEFAULT);
		preferences.setDefault(ANT_PRECOMPILE_JSP_BUILD_TASK_PATH, ANT_PRECOMPILE_JSP_BUILD_TASK_PATH_DEFAULT);
		preferences.setDefault(PHANTOM_REFERENCES, PHANTOM_REFERENCES_DEFAULT);
		preferences.setDefault(COPY_TRANSLATOR_RUNTIME_JARS, COPY_TRANSLATOR_RUNTIME_JARS_DEFAULT);
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
			translatorPathValue = preferences.getString(TRANSLATOR_PATH);
			antPrecompileJSPBuildTaskPathValue = preferences.getString(ANT_PRECOMPILE_JSP_BUILD_TASK_PATH);
			phantomReferencesValue = preferences.getBoolean(PHANTOM_REFERENCES);
			copyTranslatorRuntimeJarsValue = preferences.getBoolean(COPY_TRANSLATOR_RUNTIME_JARS);
		} catch (Exception e){
			Log.warning("Error accessing war binary processing preferences, using defaults...", e);
		}
		initialized = true;
	}
}

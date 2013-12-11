package com.likya.myra;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LocaleMessages {
	
	private static final String BUNDLE_NAME = "com.likya.myra.resources.messages";

	/**
	 * @author serkan taş
	 * 25.02.2013
	 * Aşağıdaki linke binaen değiştirildi.
	 * http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle
	 */
	// private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new UTF8Control());

	private LocaleMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}

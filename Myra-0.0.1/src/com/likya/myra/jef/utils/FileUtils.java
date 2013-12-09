/*
 * com.likya.tlos.utils : FileUtils.java
 * @author Serkan Ta�
 * Tarih : Feb 1, 2009 2:04:40 AM
 */

package com.likya.myra.jef.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {

	public static boolean checkFile(String fileName) {
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(fileName);
			fis.close();
		} catch (FileNotFoundException fnfex) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static StringBuffer readFile(String fileName) {

		FileInputStream fis = null;
		BufferedReader bufferedReader = null;
		StringBuffer outputBuffer = new StringBuffer();

		try {
			fis = new FileInputStream(fileName);
			InputStreamReader inputStreamReader = new InputStreamReader(fis, "UTF8");
			bufferedReader = new BufferedReader(inputStreamReader);
			String bufferString = null;

			while ((bufferString = bufferedReader.readLine()) != null) {
				outputBuffer.append(bufferString + '\n');
			}
		} catch (FileNotFoundException fnfex) {
			fnfex.printStackTrace();
			return null;
		} catch (Throwable t) {
			if(t instanceof  java.lang.OutOfMemoryError) {
				// TlosServer.println("Dosya boyutu kapasitenin üstünde, açılamıyor !");
			} else {
				t.printStackTrace();
			}
			return null;
		}

		try {
			fis.close();
		} catch (IOException e) {}
		
		return outputBuffer;
	}
	
	
	public static StringBuffer readXMLFile(String fileName, String coloredLineIndicator) {

		FileInputStream fis = null;
		BufferedReader bufferedReader = null;
		StringBuffer outputBuffer = new StringBuffer();

		try {
			
			fis = new FileInputStream(fileName);
			InputStreamReader inputStreamReader = new InputStreamReader(fis, "UTF8"); //$NON-NLS-1$
			bufferedReader = new BufferedReader(inputStreamReader);
			String bufferString = ""; //$NON-NLS-1$

			//TlosServer.getLogger().debug(LocaleMessages.getString("FileUtils.4")); //$NON-NLS-1$
			final String propertyDescriptor = "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">"; //$NON-NLS-1$

			while ((bufferString = bufferedReader.readLine()) != null) {
//				TlosServer.getLogger().debug(bufferString);
				if(bufferString.contains(propertyDescriptor)) {
//					int indexOfPropertyString = bufferString.toUpperCase().indexOf(propertyDescriptor.toUpperCase());
//					bufferString = new StringBuffer(bufferString).delete(indexOfPropertyString, propertyDescriptor.length()).toString();
					continue;
				}
				outputBuffer.append(bufferString + '\n');
			}

		} catch (FileNotFoundException fnfex) {
			fnfex.printStackTrace();
			return null;
		} catch (Throwable t) {
			if(t instanceof  java.lang.OutOfMemoryError) {
//				TlosServer.println("Dosya boyutu kapasitenin üstünde, açılamıyor !");
			} else {
				t.printStackTrace();
			}
			return null;
		}
		
		try {
			fis.close();
		} catch (IOException e) {}

//		String propertyString = "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">";
//		int indexOfPropertyString = outputBuffer.toString().toUpperCase().lastIndexOf(propertyString.toUpperCase());
//		if (indexOfPropertyString >= 0) {
//			TlosServer.getLogger().debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//			TlosServer.getLogger().debug("BEFORE DELETE XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//			TlosServer.getLogger().debug(outputBuffer);
//			outputBuffer.delete(indexOfPropertyString, propertyString.length());
//			TlosServer.getLogger().debug(LocaleMessages.getString("FileUtils.6")); //$NON-NLS-1$
//			TlosServer.getLogger().debug(outputBuffer);
//			TlosServer.getLogger().debug(LocaleMessages.getString("FileUtils.7")); //$NON-NLS-1$
//		}
		return outputBuffer;
	}

	public static StringBuffer readXSLFile(InputStream inputStream, String coloredLineIndicator) {

		
		StringBuffer outputBuffer = new StringBuffer();

		try {
			
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String bufferString = null;

			while ((bufferString = bufferedReader.readLine()) != null) {
				// if
				// (bufferString.equals("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">"))
				// {
				// continue;
				// }
				 if(bufferString.equals("<h2></h2>")) { //$NON-NLS-1$
//					 bufferString.replace("<h2></h2>", "<h2>" + TlosParameters.getRequestedFileName() + "</h2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 }
				outputBuffer.append(bufferString + '\n');
			}

		} catch (FileNotFoundException fnfex) {
			fnfex.printStackTrace();
			return null;
		} catch (Throwable t) {
			if(t instanceof  java.lang.OutOfMemoryError) {
//				TlosServer.println("Dosya boyutu kapasitenin üstünde, açılamıyor !");
			} else {
				t.printStackTrace();
			}
			return null;
		}

		String propertyString = "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">"; //$NON-NLS-1$
		int indexOfPropertyString = outputBuffer.lastIndexOf(propertyString);
		if (indexOfPropertyString >= 0) {
			outputBuffer.delete(indexOfPropertyString, propertyString.length());
		}
		return outputBuffer;
	}
	
	public static boolean analyzeFileForString(String fileName, String coloredLineIndicator) {

		FileInputStream fis = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader  = null;
		
		boolean retValue = false;
		
		try {
			fis = new FileInputStream(fileName);
			inputStreamReader = new InputStreamReader(fis);
			bufferedReader = new BufferedReader(inputStreamReader);
			String bufferString = ""; //$NON-NLS-1$

			while ((bufferString = bufferedReader.readLine()) != null) {
				if (bufferString.indexOf(coloredLineIndicator) >= 0) {
					retValue = true;
				}
			}
		} catch (FileNotFoundException fnfex) {
			retValue = false;
		} catch (Exception e) {
			e.printStackTrace();
			retValue = false;
		}

		try {
			fis.close();
		} catch (IOException e) {}
		
		return retValue;
	}
	
}

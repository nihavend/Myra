/*
 * com.likya.tlos.utils : FileUtils.java
 * @author Serkan Ta�
 * Tarih : Feb 1, 2009 2:04:40 AM
 */

package com.likya.myra.jef.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang.StringEscapeUtils;

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
	
	public static StringBuffer readTextFile(ArrayList<Long> fileSize, long beginPos, long endPos, String fileName, String coloredLineIndicator, boolean cleanEscapeChars) {

		final int CR = 0x0D;
		final int LF = 0x0A;
		
		StringBuffer outputBuffer = new StringBuffer();

		try {
			
			File file = new File(fileName);
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			fileSize.add(new Long(randomAccessFile.length()));
			System.err.println("File Size : " + fileSize);
			
			// String bufferString = "";
			
			// byte[] byteArray = new byte[1000];
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			
			
			if(beginPos == -1 || endPos == -1) {
//				beginPos = Math.max(0, randomAccessFile.length() - TlosServer.getTlosParameters().getLogPageSize() * 450);
				endPos = randomAccessFile.length();
			}
			

			if(endPos > randomAccessFile.length() || endPos < 0) {
				endPos = randomAccessFile.length();
			}
			
			if((endPos - beginPos) > 1000000) {
				beginPos = endPos - 1000000;
			}
			
			randomAccessFile.seek(beginPos);
			
			long counter = 0;
			
			// int byteIndex = 0;
			
			while (randomAccessFile.getFilePointer() < endPos) {

				byte readByte = randomAccessFile.readByte();

				if (readByte == -1) {
					break;
				}
				
				// byteArray[(int)byteIndex++] = readByte;
				byteArrayOutputStream.write(readByte);
				
				// System.out.print((char) readByte);

				// bufferString = bufferString + (char) readByte;
				
				if(readByte == CR) {
					System.out.print("CR");
				}

				if(readByte == LF) {
					System.out.print("LF");
				} 

				
				if (readByte == CR || readByte == LF) {
					
					if (randomAccessFile.getFilePointer() < endPos) {
						long tmpPointer = randomAccessFile.getFilePointer();
						byte readTmpByte = randomAccessFile.readByte();

						if (readTmpByte != -1) {
							if (readByte == CR && readTmpByte == LF) {
								System.out.print("LF");
								// bufferString = bufferString + (char) readTmpByte;
								// byteArray[(int)byteIndex++] = readTmpByte;
								byteArrayOutputStream.write(readTmpByte);
							} else {
								randomAccessFile.seek(tmpPointer);
							}
						} else {
							randomAccessFile.seek(tmpPointer);
							continue;
						}
					}
					
					// System.out.println("byteArray : " + new String(byteArray, "UTF8"));
					
					// bufferString = ++counter + ":\t" + new String(bufferString.getBytes(), "UTF8");
					
					// if (bufferString.toUpperCase().indexOf(coloredLineIndicator.toUpperCase()) >= 0) {
						// outputBuffer.append("<font color=\"red\">"); //$NON-NLS-1$
						// outputBuffer.append(bufferString);
						// outputBuffer.append("</font>" + '\n'); //$NON-NLS-1$
					// } else {
						// outputBuffer.append(bufferString);
						outputBuffer.append(++counter + ":\t" + byteArrayOutputStream.toString(/*"UTF8"*/));
					// }
					System.out.print("byteArrayOutputStream : " + byteArrayOutputStream.toString(/*"UTF8"*/));
					// System.out.print(bufferString.toString());
					
					// bufferString = "";
					
					byteArrayOutputStream.reset();

				}

			}
			
//			if(outputBuffer.length() == 0 && bufferString.length() != 0) {
//				// Tek satırlık ve satır sonunda CR-LF olmayan loglar için
//				outputBuffer.append(bufferString);
//			}

			if(outputBuffer.length() == 0 && byteArrayOutputStream.toString(/*"UTF8"*/).length() != 0) {
				// Tek satırlık ve satır sonunda CR-LF olmayan loglar için
				outputBuffer.append(byteArrayOutputStream.toString(/*"UTF8"*/));
				byteArrayOutputStream.reset();
			}

			
			// System.out.println(outputBuffer.toString());
			
			try {
				randomAccessFile.close();
			} catch (IOException e) {
			}

		} catch (FileNotFoundException fnfex) {
			return null;
		} catch (Throwable t) {
			if (t instanceof java.lang.OutOfMemoryError) {
//				TlosServer.println("Dosya boyutu kapasitenin üstünde, açılamıyor !");
			} else {
				t.printStackTrace();
			}
			return null;
		}
		
		if(cleanEscapeChars) {
			return new StringBuffer(StringEscapeUtils.escapeHtml(outputBuffer.toString()));
		}

		return outputBuffer;
	}

	public static StringBuffer readTxtFile(String fileName, String coloredLineIndicator, boolean cleanEscapeChars) {

		FileInputStream fis = null;
		BufferedReader bufferedReader = null;
		StringBuffer outputBuffer = new StringBuffer();

		try {
			fis = new FileInputStream(fileName);
			InputStreamReader inputStreamReader = new InputStreamReader(fis);
			bufferedReader = new BufferedReader(inputStreamReader);
			String bufferString = ""; //$NON-NLS-1$

			while ((bufferString = bufferedReader.readLine()) != null) {

				if(cleanEscapeChars) {
					bufferString = StringEscapeUtils.escapeHtml(bufferString);
				}

				if (bufferString.toUpperCase().indexOf(coloredLineIndicator.toUpperCase()) >= 0) {
					outputBuffer.append("<font color=\"red\">"); //$NON-NLS-1$
					outputBuffer.append(bufferString);
					outputBuffer.append("</font>" + '\n'); //$NON-NLS-1$
				} else {
					outputBuffer.append(bufferString + '\n');
				}
			}
		} catch (FileNotFoundException fnfex) {
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
	
	public static ByteArrayOutputStream saveJobListToExcel(ByteArrayOutputStream os) throws IOException, WriteException {
		
		WorkbookSettings workbookSettings = new WorkbookSettings();

		WritableWorkbook writableWorkbook = Workbook.createWorkbook(os, workbookSettings);
//		writableWorkbook.createSheet(LocaleMessages.getString("FileUtils.0"), 0);  //$NON-NLS-1$
		WritableSheet excelSheet = writableWorkbook.getSheet(0);
		
		for(int x=0; x < 10; x++)
		{
		    CellView cell = excelSheet.getColumnView(x);
		    cell.setAutosize(true);
		    excelSheet.setColumnView(x, cell);
		}
		
//		addTitles(excelSheet);
//		addJobList(excelSheet);

		writableWorkbook.write();
		writableWorkbook.close();
		
		os.close();
		
		return os;
	}
	
//	public static void saveJobListToExcelOld() throws IOException, WriteException {
//		
//		File excelFile = new File("IsDokumu.xls");
//		WorkbookSettings workbookSettings = new WorkbookSettings();
//
//		WritableWorkbook writableWorkbook = Workbook.createWorkbook(excelFile, workbookSettings);
//		writableWorkbook.createSheet("Anlik Is Dokumu", 0); 
//		WritableSheet excelSheet = writableWorkbook.getSheet(0);
//		
//		for(int x=0; x < 9; x++)
//		{
//		    CellView cell = excelSheet.getColumnView(x);
//		    cell.setAutosize(true);
//		    excelSheet.setColumnView(x, cell);
//		}
//		
//		addTitles(excelSheet);
//		addJobList(excelSheet);
//
//		writableWorkbook.write();
//		writableWorkbook.close();
//	}
	
//	private static void addJobList(WritableSheet sheet) throws WriteException, RowsExceededException {
//		
//		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
//		
//		WritableCellFormat times = new WritableCellFormat(times10pt);
//		
//		times.setAlignment(Alignment.CENTRE);
//		
//		Job tlosJob = null;
//		HashMap<String, Job> jobQueue = TlosServer.getJobQueue();
//		
//		Iterator<Job> jobsIterator = jobQueue.values().iterator();
//		int row = 1;
//		
//		while (jobsIterator.hasNext()) {
//			tlosJob = jobsIterator.next();
//			
//			String jobKey = tlosJob.getJobProperties().getKey().toString();
//			String groupId = TlosServer.getTlosParameters().getGroupList().get(tlosJob.getJobProperties().getGroupId());
//			String logFile = tlosJob.getJobProperties().getLogFilePath();
//			String nextExecutionDate = (tlosJob.getJobProperties().getTime() == null ? null : DateUtils.getDate(tlosJob.getJobProperties().getTime()));
//			
//			String executionDate = tlosJob.getJobProperties().getExecutionDateStr();
//			if (executionDate == null) {
//				executionDate = "-"; //$NON-NLS-1$
//			}
//			
//			String workDuration = null;
//			
//			if(tlosJob.getJobProperties().getStatus() == JobProperties.WORKING || tlosJob.getJobProperties().getStatus() == JobProperties.TIMEOUT) {
//				if(tlosJob.getJobProperties().getExecutionDate() != null) {
//					workDuration = DateUtils.getDuration(tlosJob.getJobProperties().getExecutionDate());
//				}
//			} else {
//				workDuration = tlosJob.getJobProperties().getWorkDuration();
//			}
//			
//			if (workDuration == null) {
//				workDuration = "-"; //$NON-NLS-1$
//			}
//			
//			String previousStatus = tlosJob.getPreviousStatusListString();
//			if (previousStatus == null) {
//				previousStatus = "-"; //$NON-NLS-1$
//			}
//			
//			int statusValue = tlosJob.getJobProperties().getStatus();
//			int resultCode = tlosJob.getJobProperties().getProcessExitValue();
//
//			String status = tlosJob.getJobProperties().getStatusString(statusValue, resultCode);
//			
//			String dependencyListString = tlosJob.getDependencyListString(jobQueue, tlosJob.getJobProperties().getKey().toString(), true);
//			String restartable = tlosJob.getJobProperties().isSafeRestart() ? LocaleMessages.getString("ViewHandler.81") : LocaleMessages.getString("ViewHandler.88"); //$NON-NLS-1$ //$NON-NLS-2$
//			
//			int column = 0;
//			
//			addText(sheet, column, row, jobKey, times);
//			addText(sheet, ++column, row, groupId, times);
//			addText(sheet, ++column, row, logFile, times);
//			addText(sheet, ++column, row, nextExecutionDate, times);
//			addText(sheet, ++column, row, executionDate, times);
//			addText(sheet, ++column, row, workDuration, times);
//			addText(sheet, ++column, row, previousStatus, times);
//			addText(sheet, ++column, row, status, times);
//			addText(sheet, ++column, row, dependencyListString, times);
//			addText(sheet, ++column, row, restartable, times);
//			
//			row++;
//		}
//	}
	
//	private static void addTitles(WritableSheet sheet) throws WriteException {
//
//		WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false, UnderlineStyle.SINGLE);
//		WritableCellFormat timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
//		
//		timesBoldUnderline.setAlignment(Alignment.CENTRE);
//		
//		int column = 0;
//		
//		addText(sheet, column, 0, LocaleMessages.getString("ViewHandler.47"), timesBoldUnderline); //$NON-NLS-1$
//		addText(sheet, ++column, 0, LocaleMessages.getString("ViewHandler.48"), timesBoldUnderline); //$NON-NLS-1$
//		addText(sheet, ++column, 0, LocaleMessages.getString("ViewHandler.50"), timesBoldUnderline); //$NON-NLS-1$
//		addText(sheet, ++column, 0, LocaleMessages.getString("ViewHandler.51"), timesBoldUnderline); //$NON-NLS-1$
//		addText(sheet, ++column, 0, LocaleMessages.getString("ViewHandler.52"), timesBoldUnderline); //$NON-NLS-1$
//		addText(sheet, ++column, 0, LocaleMessages.getString("ViewHandler.53"), timesBoldUnderline); //$NON-NLS-1$
//		addText(sheet, ++column, 0, LocaleMessages.getString("ViewHandler.54"), timesBoldUnderline); //$NON-NLS-1$
//		addText(sheet, ++column, 0, LocaleMessages.getString("ViewHandler.55"), timesBoldUnderline); //$NON-NLS-1$
//		addText(sheet, ++column, 0, LocaleMessages.getString("ViewHandler.56"), timesBoldUnderline); //$NON-NLS-1$
//		addText(sheet, ++column, 0, LocaleMessages.getString("ViewHandler.57"), timesBoldUnderline); //$NON-NLS-1$
//	}
	
	private static void addText(WritableSheet sheet, int column, int row, String s, WritableCellFormat format) throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, format);
		sheet.addCell(label);
	}
}

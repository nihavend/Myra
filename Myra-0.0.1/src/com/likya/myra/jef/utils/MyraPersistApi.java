package com.likya.myra.jef.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.PersistObject;
import com.likya.xsd.myra.model.config.MyraConfigDocument;
import com.likya.xsd.myra.model.config.MyraConfigDocument.MyraConfig;
import com.likya.xsd.myra.model.config.UsejobnamesforlogDocument.Usejobnamesforlog;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class MyraPersistApi {

	private static boolean persistent = false;
	private static boolean normalize = false;
	private static int frequency = 1;
	private static int higherThreshold = 10;
	private static int lowerThreshold = 3;
	private static String logPath = "logs";
	
	private static String logFileExt = ".log";
	private static String globalLogPath = "logs";
	private static int logbuffersize = 800;
	private static int logpagesize = 10;
	
	public static MyraConfig setDefaults() {

		MyraConfigDocument myraConfigDocument = MyraConfigDocument.Factory.newInstance();
		myraConfigDocument.addNewMyraConfig();
		
		MyraConfig myraConfig = myraConfigDocument.getMyraConfig();
		myraConfig.setPersistent(persistent);
		myraConfig.setNormalize(normalize);
		myraConfig.setFrequency((short) frequency);
		myraConfig.setHigherThreshold((short) higherThreshold);
		myraConfig.setLowerThreshold((short) lowerThreshold);
		myraConfig.setLogPath(logPath);
		myraConfig.setUsejobnamesforlog(Usejobnamesforlog.NO);
		myraConfig.setLogFileExt(logFileExt);
		myraConfig.setGlobalLogPath(globalLogPath);
		myraConfig.setLogbuffersize((short) logbuffersize);
		myraConfig.setLogpagesize((short) logpagesize);

		return myraConfig;
	}
	
	public static void checkDataPath() {
		
		Path dataPath = Paths.get(CoreFactory.MYRA_DATA_PATH);
		
		if (Files.notExists(dataPath)) {
			try {
				Files.createDirectory(dataPath);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
	}
	
	public static boolean checkMyraConfig() {
		
		boolean retValue = false;
		
		Path configPath = Paths.get(CoreFactory.CONFIG_PATH);

		if (Files.notExists(configPath)) {
			try {
				Files.createDirectory(configPath);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		Path configFile = Paths.get(CoreFactory.CONFIG_PATH + File.separator + CoreFactory.CONFIG_FILE);
		if (Files.exists(configFile)) {
			retValue = true;
		}
		
		return retValue;
	}
	
	public static MyraConfig getMyraConfig(boolean initialize) {

		boolean retValue = checkMyraConfig();
		
		MyraConfig myraConfig = null;
		
		Path configFile = Paths.get(CoreFactory.CONFIG_PATH + File.separator + CoreFactory.CONFIG_FILE);
		
		if(retValue) {
			myraConfig = readConfig(configFile).getMyraConfig();
		} else {
			myraConfig = setDefaults();
			if(initialize) {
				createSerializeConfig(configFile.toString(), myraConfig);
			}
		}

		return myraConfig;
	}
	
	public static MyraConfigDocument readConfig(Path configFile) {

		MyraConfigDocument myraConfigDocument = null;

		String myraConfigFile = configFile.toString();

		StringBuffer xmlString = FileUtils.readFile(myraConfigFile);

		//		ConfigurationManager configurationManager;

		try {
			myraConfigDocument = MyraConfigDocument.Factory.parse(xmlString.toString());

			if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), myraConfigDocument)) {
				throw new Exception("myraConfigDocument is null or damaged !");
			}

			//			configurationManager = new ConfigurationManagerImpl(myraConfigDocument);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return myraConfigDocument;
	}
	
	public static void createSerializeConfig(String fileName, MyraConfig myraConfig) {
		MyraConfigDocument myraConfigDocument = MyraConfigDocument.Factory.newInstance();
		myraConfigDocument.addNewMyraConfig().set(myraConfig);
		try {
			MyraPersistApi.serializeConfig(fileName, myraConfigDocument);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void serializeConfig(String fileName, MyraConfigDocument myraConfigDocument) throws Exception {

		String tmpDstFile = fileName + ".tmp";

		FileOutputStream outputStream = new FileOutputStream(tmpDstFile);

		outputStream.write(myraConfigDocument.toString().getBytes());
		outputStream.close();

		// Check the newly created binary senaryo file...
		Path dstPath = FileSystems.getDefault().getPath(fileName);
		Path tmpPath = FileSystems.getDefault().getPath(tmpDstFile);
		if (FileUtils.checkFile(tmpDstFile)) {
			if (FileUtils.checkFile(fileName)) {
				// remove the old binary
				Files.delete(dstPath);
			}
			Files.move(tmpPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
		} else {
			throw new Exception(fileName + " is not created, serialization failed !");
		}
	}

	public static boolean recoverJobQueue(ConfigurationManager configurationManager, HashMap<String, JobImpl> jobQueue, ArrayList<String> messages) {

		CoreFactory.getLogger().info(CoreFactory.getMessage("JobQueueOperations.12"));

		FileInputStream fis = null;
		ObjectInputStream in = null;

		try {
			fis = new FileInputStream(configurationManager.getFileToPersist());
			in = new ObjectInputStream(fis);
			Object input = in.readObject();

			PersistObject persistObject = (PersistObject) input;

			if (!persistObject.getTlosVersion().equals(CoreFactory.getVersion())) {
				CoreFactory.getLogger().error(CoreFactory.getMessage("JobQueueOperations.13"));
				CoreFactory.getLogger().error(CoreFactory.getMessage("JobQueueOperations.14") + CoreFactory.getVersion() + CoreFactory.getMessage("JobQueueOperations.15") + persistObject.getTlosVersion() + CoreFactory.getMessage("JobQueueOperations.16")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				in.close();
				return false;
			}

			jobQueue.putAll(persistObject.getJobQueue());

			// grup listesi de recover dosyasindan okunuyor
			configurationManager.setGroupList(persistObject.getGroupList());

			in.close();

			JobQueueOperations.resetJobQueue(LiveStateInfoUtils.generateLiveStateInfo(StateName.INT_FINISHED, SubstateName.INT_COMPLETED, StatusName.INT_SUCCESS), jobQueue);

		} catch (FileNotFoundException fnf) {
			messages.add(fnf.getMessage());
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			messages.add(ex.getMessage());
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			messages.add(e.getMessage());
			return false;
		}

		// dumpJobQueue(jobQueue);

		CoreFactory.getLogger().info(CoreFactory.getMessage("JobQueueOperations.17"));

		// CoreFactory.setRecovered(true);

		return true;
	}

	public static boolean persistJobQueue(ConfigurationManager configurationManager, HashMap<String, JobImpl> jobQueue) {

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		if (jobQueue.size() == 0) {
			CoreFactory.getLogger().fatal(CoreFactory.getMessage("JobQueueOperations.10"));
			CoreFactory.getLogger().fatal(CoreFactory.getMessage("JobQueueOperations.11"));
			return true;
		}
		
		try {

			File fileTemp = new File(configurationManager.getFileToPersist() + ".temp");
			fos = new FileOutputStream(fileTemp);

			out = new ObjectOutputStream(fos);

			PersistObject persistObject = new PersistObject();

			persistObject.setJobQueue(jobQueue);
			persistObject.setTlosVersion(CoreFactory.getVersion());
			persistObject.setGroupList(configurationManager.getGroupList());

			try {
				out.writeObject(persistObject);
			} catch (Throwable tex) {
				tex.printStackTrace();
			}
			
			out.close();

			File file = new File(configurationManager.getFileToPersist());

			if (file.exists()) {
				file.delete();
			}

			fileTemp.renameTo(file);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;

	}
}

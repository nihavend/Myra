/*******************************************************************************
 * Copyright 2013 Likya Teknoloji
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.likya.myra.jef;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.utils.MyraPersistApi;
import com.likya.xsd.myra.model.config.MyraConfigDocument;
import com.likya.xsd.myra.model.config.MyraConfigDocument.MyraConfig;
import com.likya.xsd.myra.model.config.UsejobnamesforlogDocument.Usejobnamesforlog;

public class ConfigurationManagerImpl implements ConfigurationManager {

	private static final String CONFIG_PATH = "conf";
	private static final String CONFIG_FILE = "myraConfig.xml";

	private MyraConfig myraConfig;

	private final String fileToPersist = "Myra.recover";

	private HashMap<Integer, String> groupList = new HashMap<Integer, String>();

	private boolean isRecovered = false;

	public ConfigurationManagerImpl() {
		super();
		this.myraConfig = checkMyraConfig();
	}

	//	private ConfigurationManagerImpl(MyraConfigDocument myraConfigDocument) {
	//		super();
	//
	//		if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), myraConfigDocument)) {
	//			throw new RuntimeException("MyraConfigDocument is null or damaged !");
	//		}
	//
	//		myraConfig = myraConfigDocument.getMyraConfig();
	//
	//	}

	private static MyraConfig checkMyraConfig() {

		Path dataPath = Paths.get(CONFIG_PATH);
		MyraConfig myraConfig = null;

		if (Files.notExists(dataPath)) {
			try {
				Files.createDirectory(dataPath);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		Path configFile = Paths.get(CONFIG_PATH + File.separator + CONFIG_FILE);
		if (Files.exists(configFile)) {
			myraConfig = readConfig(configFile).getMyraConfig();
		} else {
			myraConfig = setDefaults();
			MyraConfigDocument myraConfigDocument = MyraConfigDocument.Factory.newInstance();
			myraConfigDocument.addNewMyraConfig().set(myraConfig);
			try {
				MyraPersistApi.serializeConfig(configFile.toString(), myraConfigDocument);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
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

	private static MyraConfig setDefaults() {

		MyraConfigDocument myraConfigDocument = MyraConfigDocument.Factory.newInstance();
		myraConfigDocument.addNewMyraConfig();

		MyraConfig myraConfig = myraConfigDocument.getMyraConfig();
		myraConfig.setPersistent(false);
		myraConfig.setNormalize(false);
		myraConfig.setFrequency((short) 1);
		myraConfig.setHigherThreshold((short) 10);
		myraConfig.setLowerThreshold((short) 3);
		myraConfig.setLogPath("./");
		myraConfig.setUsejobnamesforlog(Usejobnamesforlog.NO);
		myraConfig.setLogFileExt(".log");
		myraConfig.setGlobalLogPath("./");
		myraConfig.setLogbuffersize((short) 800);
		myraConfig.setLogpagesize((short) 10);

		return myraConfig;
	}

	public MyraConfig getMyraConfig() {
		return myraConfig;
	}

	public String getFileToPersist() {
		return fileToPersist;
	}

	public HashMap<Integer, String> getGroupList() {
		return groupList;
	}

	public void setGroupList(HashMap<Integer, String> groupList) {
		this.groupList = groupList;
	}

	public boolean isRecovered() {
		return isRecovered;
	}

	public void setRecovered(boolean isRecovered) {
		this.isRecovered = isRecovered;
	}

}

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

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.likya.myra.commons.utils.XMLValidations;
import com.likya.xsd.myra.model.config.MyraConfigDocument;
import com.likya.xsd.myra.model.config.MyraConfigDocument.MyraConfig;

public class ConfigurationManagerImpl implements ConfigurationManager {

	private MyraConfig myraConfig;

	private final String fileToPersist = "Myra.recover";

	private HashMap<Integer, String> groupList = new HashMap<Integer, String>();
	
	private boolean isRecovered = false;

	public ConfigurationManagerImpl(MyraConfigDocument myraConfigDocument) {
		super();

		if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), myraConfigDocument)) {
			throw new RuntimeException("MyraConfigDocument is null or damaged !");
		}

		myraConfig = myraConfigDocument.getMyraConfig();

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

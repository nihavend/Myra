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

import com.likya.myra.jef.model.JobRuntimeProperties;
import com.likya.xsd.myra.model.config.MyraConfigDocument.MyraConfig;
import com.likya.xsd.myra.model.stateinfo.GlobalStateDefinitionDocument.GlobalStateDefinition;

/**
 * @author serkan
 * 
 *         This interface defines the configuration management
 *         strategy for the JEF core.
 * 
 */
public interface ConfigurationManager {

	public MyraConfig getMyraConfig();

	public JobRuntimeProperties getJobRuntimeProperties();

	public GlobalStateDefinition getGlobalStateDefinition();
	
	public void setGlobalStateDefinition(GlobalStateDefinition globalStateDefinition);

	public String getFileToPersist();

	public HashMap<Integer, String> getGroupList();

	public void setGroupList(HashMap<Integer, String> groupList);

}

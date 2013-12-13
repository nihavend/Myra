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

import org.apache.xmlbeans.XmlException;

import com.likya.myra.jef.model.JobRuntimeProperties;
import com.likya.myra.jef.model.TemporaryConfig;
import com.likya.myra.jef.utils.FileUtils;
import com.likya.xsd.myra.model.xbeans.stateinfo.GlobalStateDefinitionDocument;
import com.likya.xsd.myra.model.xbeans.stateinfo.GlobalStateDefinitionDocument.GlobalStateDefinition;

public class ConfigurationManagerBean implements ConfigurationManager {

	private TemporaryConfig temporaryConfig;
	private JobRuntimeProperties jobRuntimeProperties;
	private GlobalStateDefinition globalStateDefinition;
	
	public ConfigurationManagerBean() {
		super();
		this.temporaryConfig = new TemporaryConfig();	
		this.jobRuntimeProperties = new JobRuntimeProperties();
		
		StringBuffer xmlString = FileUtils.readFile("globalStates.xml");
		try {
			GlobalStateDefinitionDocument globalStateDefinitionDocument = GlobalStateDefinitionDocument.Factory.parse(xmlString.toString());
			this.globalStateDefinition = globalStateDefinitionDocument.getGlobalStateDefinition();
		} catch (XmlException e) {
			e.printStackTrace();
		}
	}

	@Override
	public TemporaryConfig getTemporaryConfig() {
		return temporaryConfig;
	}

	@Override
	public JobRuntimeProperties getJobRuntimeProperties() {
		return jobRuntimeProperties;
	}

	public GlobalStateDefinition getGlobalStateDefinition() {
		return globalStateDefinition;
	}

}

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
package com.likya.myra.jef.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.xmlbeans.XmlException;

import com.likya.myra.LocaleMessages;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.InputStrategy;
import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.controller.ControllerInterface;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.InstanceNotFoundException;
import com.likya.myra.jef.model.MyraException;
import com.likya.myra.jef.utils.FileUtils;
import com.likya.xsd.myra.model.xbeans.stateinfo.GlobalStateDefinitionDocument;

public class CoreFactory extends CoreFactoryBase implements CoreFactoryInterface {

	private static CoreFactory coreFactory;

	private ConfigurationManager configurationManager;
	
	private ManagementOperations managementOperations;
	
	private JobOperations jobOperations;
	
	private OutputStrategy outputStrategy;

	private CoreFactory(InputStrategy inputStrategy, OutputStrategy outputStrategy) {
		
		super();
		controllerContainer = new HashMap<String, ControllerInterface>();
		
		this.configurationManager = inputStrategy.getConfigurationManager();
		
		StringBuffer xmlString = FileUtils.readFile("globalStates.xml");
		try {
			GlobalStateDefinitionDocument globalStateDefinitionDocument = GlobalStateDefinitionDocument.Factory.parse(xmlString.toString());
			configurationManager.getTemporaryConfig().setGlobalStateDefinition(globalStateDefinitionDocument.getGlobalStateDefinition());
		} catch (XmlException e) {
			e.printStackTrace();
		}
		
		this.managementOperations= new ManagementOperationsBean(this);
		this.jobListDocument = inputStrategy.getJobListDocument();
		
		this.outputStrategy = outputStrategy;
		
	}

	public static CoreFactoryInterface getInstance() {
		if (coreFactory == null) {
			try {
				throw new InstanceNotFoundException("Use getInstance(ConfigurationManagerBean configurationManagerBean) ");
			} catch (InstanceNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
		return (CoreFactoryInterface) coreFactory;
	}
	
	public static CoreFactoryInterface getInstance(InputStrategy inputStrategy, OutputStrategy outputStrategy) {
		if (coreFactory == null) {
			coreFactory = new CoreFactory(inputStrategy, outputStrategy);
		}
		return (CoreFactoryInterface) coreFactory;
	}

	protected void start() throws Throwable {
		if (coreFactory != null) {
			if (validateFactory()) {
				startControllers();
			} else {
				// TODO log errors and throw exception
			}
		} else {
			throw new MyraException();
		}
		
	}
	
	// Not implemented yet !!!!
	public ArrayList<JobImpl> loadCustomJobTypes(String [] customJobTypes) {
		
		ArrayList<JobImpl> jobInterfaceList = new ArrayList<JobImpl>();
		
		for(String customJobType : customJobTypes) {
			
			try {
				
				JobImpl jobInterface = null;
				
				@SuppressWarnings("rawtypes")
				Class handlerClass = Class.forName(customJobType);

				Object myObject = handlerClass.newInstance();

				if (myObject instanceof JobImpl) {
					jobInterface = (JobImpl) (myObject);
					jobInterfaceList.add(jobInterface);
				}
				
				getLogger().info(LocaleMessages.getString("Tlos.34")); //$NON-NLS-1$
				
			} catch (Exception e) {
				System.out.println(LocaleMessages.getString("Tlos.35") + customJobType); //$NON-NLS-1$
				System.out.println(LocaleMessages.getString("Tlos.36")); //$NON-NLS-1$
				e.printStackTrace();
			}
		}

		return jobInterfaceList;
		
	}

	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public ManagementOperations getManagementOperations() {
		return managementOperations;
	}

	public JobOperations getJobOperations() {
		return jobOperations;
	}

	public OutputStrategy getOutputStrategy() {
		return outputStrategy;
	}

}

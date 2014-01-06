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

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.InputStrategy;
import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.controller.ControllerInterface;
import com.likya.myra.jef.model.InstanceNotFoundException;
import com.likya.myra.jef.model.MyraException;
import com.likya.xsd.myra.model.stateinfo.GlobalStateDefinitionDocument;

public class CoreFactory extends CoreFactoryBase implements CoreFactoryInterface {

	private static CoreFactory coreFactory;

	private ConfigurationManager configurationManager;

	private ManagementOperations managementOperations;

	private MonitoringOperations monitoringOperations;

	private JobOperations jobOperations;

	private OutputStrategy outputStrategy;

	private CoreFactory(InputStrategy inputStrategy, OutputStrategy outputStrategy) {

		super();
		
		try {
			registerMessageBundle();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		controllerContainer = new HashMap<String, ControllerInterface>();

		this.configurationManager = inputStrategy.getConfigurationManager();
		
		String configFile = "globalStates.xml";

		StringBuffer xmlString = FileUtils.readFile(this.getClass(), configFile);
		try {
			GlobalStateDefinitionDocument globalStateDefinitionDocument = GlobalStateDefinitionDocument.Factory.parse(xmlString.toString());
			if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), globalStateDefinitionDocument)) {
				throw new XmlException(configFile + " is null or damaged !");
			}
			configurationManager.setGlobalStateDefinition(globalStateDefinitionDocument.getGlobalStateDefinition());
		} catch (XmlException e) {
			e.printStackTrace();
		}

		this.managementOperations = new ManagementOperationsImpl(this);

		this.monitoringOperations = new MonitoringOperationsImpl(this);

		this.jobListDocument = inputStrategy.getJobListDocument();

		this.outputStrategy = outputStrategy;
		
		this.jobOperations = new JobOperationsImpl(this);

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
				throw new Exception();
			}
		} else {
			throw new MyraException();
		}

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

	public MonitoringOperations getMonitoringOperations() {
		return monitoringOperations;
	}

}

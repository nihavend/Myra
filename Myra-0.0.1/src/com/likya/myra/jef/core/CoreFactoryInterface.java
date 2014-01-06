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

import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.utils.NetTreeManagerInterface;

public interface CoreFactoryInterface {
	
	public MonitoringOperations getMonitoringOperations();
		
	public ManagementOperations getManagementOperations();
	
	public JobOperations getJobOperations();
	
	public ConfigurationManager getConfigurationManager();

	public OutputStrategy getOutputStrategy();
	
	public NetTreeManagerInterface getNetTreeManagerInterface();
}

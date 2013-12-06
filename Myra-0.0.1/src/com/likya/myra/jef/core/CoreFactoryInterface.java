package com.likya.myra.jef.core;

import com.likya.myra.jef.ConfigurationManager;

public interface CoreFactoryInterface {
	
	public ManagementOperations getManagementOperations();
	
	public JobOperations getJobOperations();
	
	public ConfigurationManager getConfigurationManager();

}

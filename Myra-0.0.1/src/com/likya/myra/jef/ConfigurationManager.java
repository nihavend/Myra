package com.likya.myra.jef;

import com.likya.myra.jef.model.JobRuntimeProperties;
import com.likya.myra.jef.model.TemporaryConfig;

/**
 * @author serkan
 * 
 * This interface defines the configuration management
 * strategy for the JEF core. 
 *
 */
public interface ConfigurationManager {
	
	public TemporaryConfig getTemporaryConfig();
	
	public JobRuntimeProperties getJobRuntimeProperties();

}

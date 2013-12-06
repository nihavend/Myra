package com.likya.myra.jef;

import com.likya.myra.jef.model.JobRuntimeProperties;
import com.likya.myra.jef.model.TemporaryConfig;

public class ConfigurationManagerBean implements ConfigurationManager {

	@Override
	public TemporaryConfig getTemporaryConfig() {
		return new TemporaryConfig();		
	}

	@Override
	public JobRuntimeProperties getJobRuntimeProperties() {
		JobRuntimeProperties jobRuntimeProperties = new JobRuntimeProperties();
		return jobRuntimeProperties;
	}

}

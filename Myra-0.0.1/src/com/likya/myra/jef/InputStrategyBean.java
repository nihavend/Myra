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

import com.likya.xsd.myra.model.xbeans.joblist.JobListDocument;

public class InputStrategyBean implements InputStrategy {

	private JobListDocument jobListDocument;
	private ConfigurationManagerBean configurationManagerBean;

	public JobListDocument getJobListDocument() {
		return jobListDocument;
	}

	public void setJobListDocument(JobListDocument jobListDocument) {
		this.jobListDocument = jobListDocument;
	}

	public ConfigurationManagerBean getConfigurationManagerBean() {
		return configurationManagerBean;
	}

	public void setConfigurationManagerBean(ConfigurationManagerBean configurationManagerBean) {
		this.configurationManagerBean = configurationManagerBean;
	}
}

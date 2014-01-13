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


public interface JobOperations {
	
	public void retryExecution(String jobName);

	public void setSuccess(String jobName);

	public void skipJob(String jobName);

	public void stopJob(String jobName);

	public void pauseJob(String jobName);

	public void resumeJob(String jobName);

	public void startJob(String jobName);

	public void disableJob(String jobName);

	public void enableJob(String jobName);

	public String setJobInputParam(String jobName, String parameterList) ;
}

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
	
	public void retryExecution(String jobId);

	public void setSuccess(String jobId);

	public void skipJob(String jobId);

	public void stopJob(String jobId);

	public void pauseJob(String jobId);

	public void resumeJob(String jobId);

	public void startJob(String jobId);

	public void disableJob(String jobId);

	public void enableJob(String jobId);
	
	public void enableJob(String jobId, boolean normalize);

	public String setJobInputParam(String jobId, String paramString) ;
}

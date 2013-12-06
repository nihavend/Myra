package com.likya.myra.jef.core;


public interface JobOperations {
	
	public void retryExecution(String jobName);

	public void setSuccess(String jobName);

	public void skipJob(String jobName);

	public void skipJob(boolean isForced, String jobName);

	public void stopJob(String jobName);

	public void pauseJob(String jobName);

	public void resumeJob(String jobName);

	public void startJob(String jobName);

	public void disableJob(String jobName);

	public void enableJob(String jobName);

	public String setJobInputParam(String jobName, String parameterList) ;
}

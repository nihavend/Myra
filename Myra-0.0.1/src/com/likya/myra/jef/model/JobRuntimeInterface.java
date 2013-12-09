package com.likya.myra.jef.model;

import java.util.Calendar;

import org.apache.log4j.Logger;

public interface JobRuntimeInterface {
	
	public Calendar getCompletionDate();
	public void setCompletionDate(Calendar completionDate);

	public String getWorkDuration();
	public void setWorkDuration(String workDuration);

	public long getWorkDurationNumeric();
	public void setWorkDurationNumeric(long workDurationNumeric);

	public String getRecentWorkDuration() ;
	public void setRecentWorkDuration(String recentWorkDuration);

	public long getRecentWorkDurationNumeric();
	public void setRecentWorkDurationNumeric(long recentWorkDurationNumeric);

	public StringBuffer getMessageBuffer();
	public void setMessageBuffer(StringBuffer messageBuffer);
	
	public String getLogAnalyzeString();
	public void setLogAnalyzeString(String logAnalyzeString);
	
	public int getAutoRetryCount();
	public void setAutoRetryCount(int autoRetryCount);
	
	public int getAutoRetryDelay() ;
	public void setAutoRetryDelay(int autoRetryDelay) ;
	
	public Logger getLogger();
	public void setLogger(Logger myLogger);
	
}

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
	
	public Logger getLogger();
	public void setLogger(Logger myLogger);
	
	public String getMemberIdOfNetTree();
	public void setMemberIdOfNetTree(String memberIdOfNetTree);
	
}

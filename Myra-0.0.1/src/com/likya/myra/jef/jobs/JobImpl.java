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

package com.likya.myra.jef.jobs;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.likya.myra.commons.utils.LogAnalyser;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.OutputData;
import com.likya.xsd.myra.model.config.MyraConfigDocument.MyraConfig;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.wlagen.LogAnalysisDocument.LogAnalysis;

public abstract class JobImpl implements Runnable, Serializable {

	private static final long serialVersionUID = 2540934879831919506L;

	transient public Logger myLogger;
	
	protected int retryCounter = 0;

	// transient private HashMap<String, JobInterface> jobQueue;

	transient protected WatchDogTimer watchDogTimer = null;

	transient private Thread myExecuter;

	private AbstractJobType abstractJobType;
	private JobRuntimeInterface jobRuntimeProperties;

	protected MyraConfig myraConfig;

	protected OutputStrategy outputStrategy;

	protected abstract void localRun();
	protected abstract void processJobResult();
	protected abstract void cleanUp();

	protected abstract void startWathcDogTimer();
	public abstract void stopMyDogBarking();

	public JobImpl(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		this.abstractJobType = abstractJobType;
		this.jobRuntimeProperties = jobRuntimeProperties;

		myLogger = CoreFactory.getLogger();

		outputStrategy = CoreFactory.getInstance().getOutputStrategy();
		myraConfig = CoreFactory.getInstance().getConfigurationManager().getMyraConfig();
	}

	public final void run() {
		
		Thread.currentThread().setName("JobImpl_" + abstractJobType.getId());
		
		localRun();
		
		processJobResult();
		
		performLogAnalyze();

		cleanUp();
	}
	
	private void performLogAnalyze() {
		LogAnalysis logAnalysis = abstractJobType.getLogAnalysis();

		if (logAnalysis != null && logAnalysis.getActive()) {
			StringBuffer logContent = new StringBuffer();
			LiveStateInfo liveStateInfo = new LogAnalyser().evaluate(abstractJobType, logContent);
			ChangeLSI.forValue(abstractJobType, liveStateInfo);
			outputStrategy.sendDataObject(new OutputData(OutputData.types.LOGANALYZER, logContent));
		}
	}
	
	public AbstractJobType getAbstractJobType() {
		return abstractJobType;
	}

	public JobRuntimeInterface getJobRuntimeProperties() {
		return jobRuntimeProperties;
	}

	public Thread getMyExecuter() {
		return myExecuter;
	}

	public void setMyExecuter(Thread myExecuter) {
		this.myExecuter = myExecuter;
	}

	public String getJobInfo() {
		return ""; // jobProperties.getKey() + ":" + jobProperties.getStatusString(jobProperties.getStatus(), jobProperties.getProcessExitValue()); //$NON-NLS-1$
	}

	public Logger getMyLogger() {
		return myLogger;
	}
	
	public String toString() {
		// return "[JobId:" + getAbstractJobType().getId() + "][" + JobHelper.getLastStateInfo(getAbstractJobType()) + "]";
		LiveStateInfo liveStateInfo = JobHelper.getLastStateInfo(getAbstractJobType());
		String startTime = MyraDateUtils.getDate(getAbstractJobType().getManagement().getTimeManagement().getJsPlannedTime().getStartTime());
		String stopTime = MyraDateUtils.getDate(getAbstractJobType().getManagement().getTimeManagement().getJsPlannedTime().getStopTime());
		
		String toString = "";
		if(liveStateInfo.getStateName() != null) {
			toString = liveStateInfo.getStateName().toString();
		}
		
		if(liveStateInfo.getSubstateName() != null) {
			toString = toString + ":" + liveStateInfo.getSubstateName().toString();
		}
		
		if(liveStateInfo.getStatusName() != null) {
			toString = toString + ":" + liveStateInfo.getStatusName().toString();
		}	
		
		return "[JobId:" + getAbstractJobType().getId() + "][start:" + startTime + "|stop:" + stopTime + "][LSIDT:" + liveStateInfo.getLSIDateTime() + "][" + toString + "]";
	}

}

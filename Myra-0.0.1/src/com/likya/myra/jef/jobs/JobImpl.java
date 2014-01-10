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

import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.xsd.myra.model.config.MyraConfigDocument.MyraConfig;
import com.likya.xsd.myra.model.joblist.AbstractJobType;

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
	protected abstract void stopMyDogBarking();

	public JobImpl(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		this.abstractJobType = abstractJobType;
		this.jobRuntimeProperties = jobRuntimeProperties;

		myLogger = CoreFactory.getLogger();

		outputStrategy = CoreFactory.getInstance().getOutputStrategy();
		myraConfig = CoreFactory.getInstance().getConfigurationManager().getMyraConfig();
	}

	public final void run() {
		
		localRun();
		
		processJobResult();

		cleanUp();
	}
	
	protected void sendOutputData(Object object) {
		outputStrategy.sendDataObject(object);
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

}

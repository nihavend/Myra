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
import java.util.Date;

import org.apache.log4j.Logger;

import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.TemporaryConfig;
import com.likya.xsd.myra.model.xbeans.joblist.AbstractJobType;

public abstract class JobImpl implements Runnable, Serializable {

	private static final long serialVersionUID = 2540934879831919506L;

	public Logger myLogger;

	// transient private HashMap<String, JobInterface> jobQueue;

	transient private Thread myExecuter;

	private AbstractJobType abstractJobType;
	private JobRuntimeInterface jobRuntimeProperties;

	protected TemporaryConfig temporaryConfig;

	private OutputStrategy outputStrategy;

	public abstract void stopMyDogBarking();

	protected abstract void localRun();

	public JobImpl(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		this.abstractJobType = abstractJobType;
		this.jobRuntimeProperties = jobRuntimeProperties;

		if (jobRuntimeProperties.getLogger() != null) {
			myLogger = jobRuntimeProperties.getLogger();
		} else {
			myLogger = Logger.getLogger(JobImpl.class);
		}

		outputStrategy = CoreFactory.getInstance().getOutputStrategy();
		temporaryConfig = CoreFactory.getInstance().getConfigurationManager().getTemporaryConfig();
	}

	public final void run() {
		localRun();
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

	public void reportLog(JobImpl jobClass, Date startTime, Date endTime) {

		//		String jobClassName = "JOBSTATS|";
		//
		//		if (jobClass instanceof ExternalProgram) {
		//			jobClassName = jobClassName.concat("STANDART");
		//		} else if (jobClass instanceof ManuelExternalProgram) {
		//			jobClassName = jobClassName.concat("MANUEL");
		//		} else if (jobClass instanceof RepetitiveExternalProgram) {
		//			jobClassName = jobClassName.concat("TEKRARLI");
		//		}
		//
		//		TlosServer.getLogger().info(jobClassName + "|" + TlosServer.getTlosParameters().getScenarioName().toString() + "|" + getJobProperties().getGroupName().toString() + "|" + getJobProperties().getKey().toString() + "|" + DateUtils.getDate(startTime) + "|" + DateUtils.getDate(endTime) + "|" + getJobProperties().getStatusString(getJobProperties().getStatus(), getJobProperties().getProcessExitValue()).toString()); //$NON-NLS-1$
	}

	protected void sendOutputData(Object object) {
		outputStrategy.sendDataObject(object);
	}

	public String[] parseParameter() {

		String[] cmd = null;

		//		if (getJobProperties().getJobParamList() != null && !getJobProperties().getJobParamList().equals("")) {
		//			String tmpCmd[] = ValidPlatforms.getCommand(getJobProperties().getJobCommand());
		//			String tmpPrm[] = getJobProperties().getJobParamList().split(" ").clone();
		//			cmd = new String[tmpCmd.length + tmpPrm.length];
		//			System.arraycopy(tmpCmd, 0, cmd, 0, tmpCmd.length);
		//			System.arraycopy(tmpPrm, 0, cmd, tmpCmd.length, tmpPrm.length);
		//		} else {
		//			cmd = ValidPlatforms.getCommand(getSimpleJobProperties().getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobCommand());
		//		}

		return cmd;
	}

	public Logger getMyLogger() {
		return myLogger;
	}

}

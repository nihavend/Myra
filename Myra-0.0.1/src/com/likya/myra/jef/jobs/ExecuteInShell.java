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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.jvnet.winp.WinProcess;

import com.likya.myra.commons.ValidPlatforms;
import com.likya.myra.commons.grabber.StreamGrabber;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.xsd.myra.model.generics.JobCommandTypeDocument.JobCommandType;
import com.likya.xsd.myra.model.generics.JobCommandTypeDocument.JobCommandType.Enum;
import com.likya.xsd.myra.model.generics.JobTypeDetailsDocument.JobTypeDetails;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.wlagen.LogAnalysisDocument.LogAnalysis;

public class ExecuteInShell extends CommonShell {

	private static final long serialVersionUID = 1L;

	boolean isShell = true;

	public ExecuteInShell(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}

	@Override
	protected void localRun() {

		startTime = Calendar.getInstance();

		try {
			startProcess(startTime);
		} catch (Exception err) {
			handleException(err, myLogger);
		}

	}

//	protected void cleanUp() {
//
//		stopErrorGobbler(myLogger);
//		stopOutputGobbler(myLogger);
//		
//		// restore to the value derived from sernayobilgileri file.
//		//			getJobProperties().setJobParamList(getJobProperties().getJobParamListPerm());
//
//		// sendOutputData();
//
//		setMyExecuter(null);
//		process = null;
//	}

	public void startProcess(Calendar startTime) throws IOException {

		AbstractJobType abstractJobType = getAbstractJobType();

		String startLog = abstractJobType.getId() + CoreFactory.getMessage("ExternalProgram.0") + MyraDateUtils.getDate(startTime.getTime());

		JobHelper.setJsRecordedTimeForStart(abstractJobType, startTime);

		CoreFactory.getLogger().info(startLog);

		String jobId = abstractJobType.getId();

		JobRuntimeInterface jobRuntimeInterface = getJobRuntimeProperties();

		StringBuilder stringBufferForERROR = new StringBuilder();
		StringBuilder stringBufferForOUTPUT = new StringBuilder();

//		jobRuntimeInterface.setRecentWorkDuration(abstractJobType.getManagement().getTimeManagement().getPrevWorkDuration());
//		jobRuntimeInterface.setRecentWorkDurationNumeric(jobRuntimeInterface.getWorkDurationNumeric());

		startWathcDogTimer();

		ProcessBuilder processBuilder = null;

		JobTypeDetails jobTypeDetails =  abstractJobType.getBaseJobInfos().getJobTypeDetails();
		Enum jobCommandType = jobTypeDetails.getJobCommandType();
		isShell = (jobCommandType.equals(JobCommandType.SHELL_SCRIPT) || jobCommandType.equals(JobCommandType.BATCH_PROCESS)) ? true : false;
		String jobCommand = jobTypeDetails.getJobCommand();

		CoreFactory.getLogger().info(" >>" + this.getClass().getSimpleName() + jobId + " Çalıştırılacak komut : " + jobCommand);

		processBuilder = JobHelper.parsJobCmdArgs(isShell, jobCommand, jobTypeDetails.getArgValues());

		String jobWorkDir = abstractJobType.getBaseJobInfos().getJobTypeDetails().getJobWorkDir();
		if (jobWorkDir != null && !jobWorkDir.equals("")) {
			processBuilder.directory(new File(jobWorkDir));
		}

		Map<String, String> tempEnv = new HashMap<String, String>();

		Map<String, String> environmentVariables = new HashMap<String, String>();

		if (environmentVariables != null && environmentVariables.size() > 0) {
			tempEnv.putAll(environmentVariables);
		}

		tempEnv.putAll(JobHelper.entryToMap(jobTypeDetails.getEnvVariables()));

		processBuilder.environment().putAll(tempEnv);

		setRunning(abstractJobType);

		process = processBuilder.start();

		jobRuntimeInterface.getMessageBuffer().delete(0, jobRuntimeInterface.getMessageBuffer().capacity());

		initGrabbers(process, jobId, CoreFactory.getLogger(), myraConfig.getLogbuffersize());

		try {

			process.waitFor();

			int processExitValue = process.exitValue();
			
			Calendar endTime = Calendar.getInstance();
			JobHelper.setJsRecordedTimeForStop(abstractJobType, endTime);
			
			CoreFactory.getLogger().info(jobId + CoreFactory.getMessage("ExternalProgram.6") + processExitValue);

			stopMyDogBarking();

			cleanUpFastEndings(errorGobbler, outputGobbler);

			stringBufferForERROR = errorGobbler.getOutputBuffer();
			stringBufferForOUTPUT = outputGobbler.getOutputBuffer();

			JobHelper.updateDescStr(jobRuntimeInterface.getMessageBuffer(), stringBufferForOUTPUT, stringBufferForERROR);

			StatusName.Enum statusName = JobHelper.searchReturnCodeInStates(abstractJobType, processExitValue, jobRuntimeInterface.getMessageBuffer());

			JobHelper.writeErrorLogFromOutputs(CoreFactory.getLogger(), this.getClass().getName(), stringBufferForOUTPUT, stringBufferForERROR);

			boolean noErrorInLog = true;
			
			try {
				LogAnalysis logAnalysis = abstractJobType.getLogAnalysis();
				if (logAnalysis != null && logAnalysis.getActive()) {
					if(performLogAnalyze(abstractJobType) != null) {
						noErrorInLog = false;
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}

			if (noErrorInLog) {
				setOfCodeMessage(abstractJobType, statusName, processExitValue, jobRuntimeInterface.getMessageBuffer().toString());
			}

			
		} catch (Throwable e) {

			errorGobbler.interrupt();
			outputGobbler.interrupt();
			
			if(e instanceof InterruptedException) {
				
				if (ValidPlatforms.getOSName() != null && ValidPlatforms.getOSName().contains(ValidPlatforms.OS_WINDOWS)) {
					try {
						// System.out.println("Killing windows process tree...");
						WinProcess winProcess = new WinProcess(process);
						winProcess.killRecursively();
						// System.out.println("Killed.");
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				// Stop the process from running
				CoreFactory.getLogger().warn(CoreFactory.getMessage("ExternalProgram.8") + jobId); 

				// process.waitFor() komutu thread'in interrupt statusunu temizlemedigi icin 
				// asagidaki sekilde temizliyoruz
				Thread.interrupted();

				process.destroy();
			}
			
		}

	}

	protected void cleanUpFastEndings(StreamGrabber errorGobbler, StreamGrabber outputGobbler) throws InterruptedException {
		if (errorGobbler.isAlive()) {
			errorGobbler.stopStreamGobbler();
			while (errorGobbler.isAlive()) {
				Thread.sleep(10);
			}
		}
		if (outputGobbler.isAlive()) {
			outputGobbler.stopStreamGobbler();
			while (outputGobbler.isAlive()) {
				Thread.sleep(10);
			}
		}
	}

	public boolean isShell() {
		return isShell;
	}

	public void setShell(boolean isShell) {
		this.isShell = isShell;
	}

}

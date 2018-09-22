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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.likya.myra.commons.grabber.StreamGrabber;
import com.likya.myra.commons.utils.LogAnalyser;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.OutputData;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;

public abstract class CommonShell extends GenericInnerJob {

	private static final long serialVersionUID = 1L;

	transient protected Process process;

	transient protected StreamGrabber errorGobbler;
	transient protected StreamGrabber outputGobbler;

	private final String logLabel = " CommonShell ";

	public CommonShell(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}

	//	protected void cleanUp() {

	//	TL den gelen
	//
	//	stopErrorGobbler(myLogger);
	//	stopOutputGobbler(myLogger);
	//	
	//	
	//	sendOutputData();
	//	
	//	process = null;
	//}

	protected void cleanUp() {

		// TL den gelen....
		// restore to the value derived from sernayobilgileri file.
		// getJobProperties().setJobParamList(getJobProperties().getJobParamListPerm());
//
		AbstractJobType abstractJobType = getAbstractJobType();
//
		CoreFactory.getLogger().debug(" >>" + logLabel + ">> " + "Terminating Error for " + abstractJobType.getId());
		stopErrorGobbler(CoreFactory.getLogger());
//
		CoreFactory.getLogger().debug(" >>" + logLabel + ">> " + "Terminating Output for " + abstractJobType.getId());
		stopOutputGobbler(CoreFactory.getLogger());
//
//		Calendar endTime = DateUtils.getCalendarInstance();
//
//		long timeDiff = endTime.getTime().getTime() - startTime.getTime().getTime();
//
//		String endLog = abstractJobType.getBaseJobInfos().getJsName() + ":Bitis zamani : " + DateUtils.getDate(endTime.getTime());
//		String duration = abstractJobType.getBaseJobInfos().getJsName() + ": islem suresi : " + DateUtils.getFormattedElapsedTime((int) timeDiff / 1000);
//		getJobRuntimeProperties().setCompletionDate(endTime);
//		getJobRuntimeProperties().setWorkDuration(DateUtils.getUnFormattedElapsedTime((int) timeDiff / 1000));
//
//		JobHelper.setJsRealTimeForStop(abstractJobType, endTime);
//
//		CoreFactory.getLogger().info(" >>" + logLabel + ">> " + endLog);
//		CoreFactory.getLogger().info(" >>" + logLabel + ">> " + duration);
//
		if (watchDogTimer != null) {
			CoreFactory.getLogger().debug(" >>" + logLabel + ">> " + "Terminating Watchdog for " + abstractJobType.getBaseJobInfos().getJsName());
			stopMyDogBarking();
		}

		// sendOutputData();

		setMyExecuter(null);

		process = null;

		CoreFactory.getLogger().info(" >>" + logLabel + ">> ExecuterThread:" + Thread.currentThread().getName() + " is over");

	}

	protected void initGrabbers(Channel channel, String jobId, Logger myLogger, int buffSize) throws InterruptedException, IOException {

		myLogger.debug(" >>" + logLabel + ">> " + "Sleeping 100 ms for error and output buffers to get ready...");
		Thread.sleep(100);
		myLogger.info(" >>" + logLabel + ">> " + " OK");

		// errorGobbler = new StreamGrabber(((ChannelExec) channel).getErrStream(), "ERROR", CoreFactory.getLogger(), buffSize);
		errorGobbler = new StreamGrabber(((ChannelExec) channel).getErrStream(), "ERROR", CoreFactory.getLogger(), buffSize);
		errorGobbler.setName(jobId + ".ErrorGobbler.id." + errorGobbler.getId());

		// any output?
		outputGobbler = new StreamGrabber(channel.getInputStream(), "OUTPUT", CoreFactory.getLogger(), buffSize);
		outputGobbler.setName(jobId + ".OutputGobbler.id." + outputGobbler.getId());

		myLogger.info(" >>" + logLabel + " icin islemin hata ve girdi akisi baslatiliyor. " + errorGobbler.getName() + " ve " + outputGobbler.getName());

		// kick them off
		errorGobbler.start();
		outputGobbler.start();
	}

	protected void initGrabbers(Process process, String jobId, Logger myLogger, int buffSize) throws IOException {

		myLogger.debug(" >>" + logLabel + ">> " + "Sleeping 100 ms for error and output buffers to get ready...");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// Do nothing !!
		}
		myLogger.info(" >>" + logLabel + ">> " + " OK");

		//any error message?
		errorGobbler = new StreamGrabber(process.getErrorStream(), "ERROR", CoreFactory.getLogger(), buffSize); //$NON-NLS-1$
		errorGobbler.setName(jobId + ".ErrorGobbler.id." + errorGobbler.getId()); //$NON-NLS-1$

		// any output?
		outputGobbler = new StreamGrabber(process.getInputStream(), "OUTPUT", CoreFactory.getLogger(), buffSize); //$NON-NLS-1$
		outputGobbler.setName(jobId + ".OutputGobbler.id." + outputGobbler.getId()); //$NON-NLS-1$

		myLogger.info(" >>" + logLabel + " icin islemin hata ve girdi akisi baslatiliyor. " + errorGobbler.getName() + " ve " + buffSize);

		// kick them off
		errorGobbler.start();
		outputGobbler.start();
	}

	protected void stopErrorGobbler(Logger logger) {
		if (errorGobbler != null && errorGobbler.isAlive()) {
			errorGobbler.stopStreamGobbler();
			errorGobbler.interrupt();
			logger.debug("  > ExternalProgram -> errorGobbler.isAlive ->" + errorGobbler.isAlive());
			errorGobbler = null;
		}
	}

	protected void stopOutputGobbler(Logger logger) {
		if (outputGobbler != null && outputGobbler.isAlive()) {
			outputGobbler.stopStreamGobbler();
			outputGobbler.interrupt();
			logger.debug("  > ExternalProgram -> outputGobbler.isAlive ->" + outputGobbler.isAlive());
			outputGobbler = null;
		}
	}

	protected LiveStateInfo performLogAnalyze(AbstractJobType abstractJobType) {
		
		String fileP = abstractJobType.getBaseJobInfos().getJobLogPath();
		String fileName = abstractJobType.getBaseJobInfos().getJobLogFile();
		
		Path filePath = Paths.get(fileP + File.separator + fileName);

		if(fileP == null || fileP.length() == 0 || fileName == null || fileName.length() == 0 || !Files.exists(filePath)) {
			CoreFactory.getLogger().debug("  > CommonShell -> performLogAnalyze -> Log file does not exist, skipping log analyze ! \"" + filePath.toString() + "\"");
			return null;
		}
		
		LiveStateInfo liveStateInfo = null;

		StringBuffer logContent = new StringBuffer();
		liveStateInfo = new LogAnalyser().evaluate(abstractJobType, logContent);
		if(liveStateInfo != null) {
			setFailedOfLog(abstractJobType, liveStateInfo);
			outputStrategy.sendDataObject(new OutputData(OutputData.types.LOGANALYZER, logContent));
		}
		
		return liveStateInfo;
	}
}

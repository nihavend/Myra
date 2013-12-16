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

import java.util.Calendar;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.utils.DateUtils;
import com.likya.xsd.myra.model.xbeans.joblist.AbstractJobType;
import com.likya.xsd.myra.model.xbeans.joblist.RemoteSchProperties;
import com.likya.xsd.myra.model.xbeans.rs.ExecuteRShellParamsDocument.ExecuteRShellParams;
import com.likya.xsd.myra.model.xbeans.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.xbeans.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.xbeans.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.xbeans.stateinfo.SubstateNameDocument.SubstateName;

public class ExecuteSchComponent extends CommonShell {

	private static final long serialVersionUID = 7931558555995487881L;
	
	private final String logLabel = " ExecuteSchComponent ";

	public ExecuteSchComponent(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}

	public void startSchProcess(String jobPath, String jobCommand, Map<String, String> environmentVariables, String logClassName, Logger myLogger) throws Exception {

		// JobRuntimeInterface jobRuntimeInterface = getJobRuntimeProperties();

		AbstractJobType abstractJobType = getAbstractJobType();

		String jobId = abstractJobType.getId();

		StringBuilder stringBufferForERROR = new StringBuilder();
		StringBuilder stringBufferForOUTPUT = new StringBuilder();

		JSch jsch = new JSch();

		ExecuteRShellParams executeRShellParams = ((RemoteSchProperties) abstractJobType).getExecuteRShellParams();

		String host = executeRShellParams.getIpAddress() != null ? executeRShellParams.getIpAddress() : executeRShellParams.getHostName(); // "192.168.1.39";
		String user = executeRShellParams.getUserName(); // "likya";
		String password = executeRShellParams.getUserPassword(); // "likya";
		int port = executeRShellParams.getPort(); // "22";
		String fileSeperator = executeRShellParams.getFileSeperator();

		jobCommand = jobPath + fileSeperator + jobCommand; // "/home/likya/murat/Agent/jobs/job1.sh";

		Session session = jsch.getSession(user, host, port);

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.setPassword(password);
		session.connect();

		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(jobCommand);

		channel.setInputStream(System.in);

		((ChannelExec) channel).setErrStream(System.err);

		channel.connect();

		initGrabbers(channel, jobId,  myLogger, temporaryConfig.getLogBufferSize());

		try {

			while (true) {

				if (channel.isClosed()) {
					break;
				}

				Thread.sleep(1000);
			}

			channel.disconnect();
			session.disconnect();

			int processExitValue = channel.getExitStatus();

			myLogger.info(" >>" + logLabel + jobId + " islemi sonlandi, islem bitis degeri : " + processExitValue);

			StringBuffer descStr = new StringBuffer();

			StatusName.Enum statusName = JobHelper.searchReturnCodeInStates(abstractJobType, processExitValue, descStr);

			JobHelper.updateDescStr(descStr, stringBufferForOUTPUT, stringBufferForERROR);

			JobHelper.writetErrorLogFromOutputs(myLogger, logClassName, stringBufferForOUTPUT, stringBufferForERROR);

			setOfCodeMessage(abstractJobType, statusName.intValue(), descStr.toString());

		} catch (InterruptedException e) {

			myLogger.warn(" >>" + logLabel + ">> " + logClassName + " : Job timed-out terminating " + abstractJobType.getBaseJobInfos().getJsName());

			channel.disconnect();
			session.disconnect();

		}
	}

	@Override
	public void localRun() {
		
		Calendar startTime = Calendar.getInstance();
		
		//initStartUp(myLogger);

		AbstractJobType abstractJobType = getAbstractJobType();

		while (true) {

			try {

				startWathcDogTimer();

				String jobPath = abstractJobType.getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobPath();
				String jobCommand = abstractJobType.getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobCommand();

				setRunning(abstractJobType);
				
				startSchProcess(jobPath, jobCommand, null, this.getClass().getName(), CoreFactory.getLogger());

			} catch (Exception err) {
				handleException(err, CoreFactory.getLogger());
			}

			if (processJobResult(true, CoreFactory.getLogger())) {
				//retryFlag = false;
				continue;
			}

			break;
		}
		
		cleanUp(process, startTime);

	}
	
	public void handleException(Exception err, Logger myLogger) {

		AbstractJobType abstractJobType = getAbstractJobType();

		LiveStateInfo liveStateInfo = abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);
		
		/* FINISHED state i yoksa ekle */
		if (!LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.COMPLETED)) {
			setFailedOfMessage(abstractJobType, err.getMessage());
		}

		myLogger.error(err.getMessage());

		myLogger.error(" >>" + logLabel + ">> " + err.getMessage());
		err.printStackTrace();

	}
	
	public boolean processJobResult(boolean retryFlag, Logger myLogger) {

		AbstractJobType abstractJobType = getAbstractJobType();
		
		LiveStateInfo lastState = abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);

		if (lastState.getStateName().equals(StateName.FINISHED)) {

			String logStr = "islem bitirildi : " + abstractJobType.getId() + " => ";
			logStr += StateName.FINISHED.toString() + ":" + lastState.getSubstateName().toString() + ":" + lastState.getStatusName().toString();
			myLogger.info(" >>>>" + logStr + "<<<<");

		} else {

			if (Boolean.parseBoolean(abstractJobType.getCascadingConditions().getJobAutoRetryInfo().getJobAutoRetry().toString()) && retryFlag) {
				myLogger.info(" >> " + logLabel + " : Job Failed ! Restarting " + abstractJobType.getBaseJobInfos().getJsName());
				setRenewByTime(abstractJobType);
				return true;
			} else {
				myLogger.info(" >>" + logLabel + ">> " + abstractJobType.getId() + ":Job Failed ! ");
				myLogger.debug(" >>" + logLabel + ">> " + abstractJobType.getId() + " : Job Failed !");
			}
		}

		return false;
	}
	
	protected void cleanUp(Process process, Calendar startTime) {

		AbstractJobType abstractJobType = getAbstractJobType();

		CoreFactory.getLogger().debug(" >>" + logLabel + ">> " + "Terminating Error for " + abstractJobType.getBaseJobInfos().getJsName());
		stopErrorGobbler(CoreFactory.getLogger());

		CoreFactory.getLogger().debug(" >>" + logLabel + ">> " + "Terminating Output for " + abstractJobType.getBaseJobInfos().getJsName());
		stopOutputGobbler(CoreFactory.getLogger());

		Calendar endTime = Calendar.getInstance();

		long timeDiff = endTime.getTime().getTime() - startTime.getTime().getTime();

		String endLog = abstractJobType.getBaseJobInfos().getJsName() + ":Bitis zamani : " + DateUtils.getDate(endTime.getTime());
		String duration = abstractJobType.getBaseJobInfos().getJsName() + ": islem suresi : " + DateUtils.getFormattedElapsedTime((int) timeDiff / 1000);
		getJobRuntimeProperties().setCompletionDate(endTime);
		getJobRuntimeProperties().setWorkDuration(DateUtils.getUnFormattedElapsedTime((int) timeDiff / 1000));
		
		JobHelper.setJsRealTimeForStop(abstractJobType, endTime);
		
		CoreFactory.getLogger().info(" >>" + logLabel + ">> " + endLog);
		CoreFactory.getLogger().info(" >>" + logLabel + ">> " + duration);

		if (watchDogTimer != null) {
			CoreFactory.getLogger().debug(" >>" + logLabel + ">> " + "Terminating Watchdog for " + abstractJobType.getBaseJobInfos().getJsName());
			stopMyDogBarking();
		}

		process = null;
		// isExecuterOver = true;
		CoreFactory.getLogger().info(" >>" + logLabel + ">> ExecuterThread:" + Thread.currentThread().getName() + " is over");

	}

}

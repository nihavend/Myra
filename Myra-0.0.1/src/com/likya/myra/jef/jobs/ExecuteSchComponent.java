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
import com.likya.commons.utils.DateUtils;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.joblist.RemoteSchProperties;
import com.likya.xsd.myra.model.rs.ExecuteRShellParamsDocument.ExecuteRShellParams;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class ExecuteSchComponent extends CommonShell {

	private static final long serialVersionUID = 7931558555995487881L;

	private final String logLabel = " ExecuteSchComponent ";

	public ExecuteSchComponent(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}
	
	@Override
	public void localRun() {

		startTime = DateUtils.getCalendarInstance();
		
		JobHelper.setPrevWorkDuration(getAbstractJobType());
		
		//initStartUp(myLogger);

		AbstractJobType abstractJobType = getAbstractJobType();

		try {

			startWathcDogTimer();

			String jobCommand = abstractJobType.getBaseJobInfos().getJobTypeDetails().getJobCommand();

			setRunning(abstractJobType);

			startSchProcess(jobCommand, null, this.getClass().getName(), CoreFactory.getLogger());

		} catch (Exception err) {
			handleException(err, CoreFactory.getLogger());
		}

	}

	public void startSchProcess(String jobCommand, Map<String, String> environmentVariables, String logClassName, Logger myLogger) throws Exception {

		// JobRuntimeInterface jobRuntimeInterface = getJobRuntimeProperties();

		AbstractJobType abstractJobType = getAbstractJobType();
		
		JobHelper.setJsRecordedTimeForStart(abstractJobType, startTime);

		String jobId = abstractJobType.getId();

		StringBuilder stringBufferForERROR = new StringBuilder();
		StringBuilder stringBufferForOUTPUT = new StringBuilder();

		JSch jsch = new JSch();

		ExecuteRShellParams executeRShellParams = ((RemoteSchProperties) abstractJobType).getExecuteRShellParams();

		String host = executeRShellParams.getIpAddress() != null ? executeRShellParams.getIpAddress() : executeRShellParams.getHostName(); // "192.168.1.39";
		String user = executeRShellParams.getUserName();
		String password = executeRShellParams.getRshellPassword(); 
		int port = executeRShellParams.getPort();

		Session session = jsch.getSession(user, host, port);

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.setPassword(password);
		
		Channel channel = null;

		try {
			
			session.connect(20000);

			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(jobCommand);

			// channel.setInputStream(System.in);

			((ChannelExec) channel).setErrStream(System.err);

			channel.connect();

			initGrabbers(channel, jobId, myLogger, myraConfig.getLogbuffersize());

			while (true) {

				if (channel.isClosed()) {
					break;
				}

				Thread.sleep(1000);
			}

			channel.disconnect();
			session.disconnect();

			int processExitValue = channel.getExitStatus();
			
			Calendar endTime = DateUtils.getCalendarInstance();
			JobHelper.setJsRecordedTimeForStop(abstractJobType, endTime);

			myLogger.info(" >>" + logLabel + jobId + " islemi sonlandi, islem bitis degeri : " + processExitValue);

			StringBuffer descStr = new StringBuffer();

			StatusName.Enum statusName = JobHelper.searchReturnCodeInStates(abstractJobType, processExitValue, descStr);

			JobHelper.updateDescStr(descStr, stringBufferForOUTPUT, stringBufferForERROR);

			JobHelper.writeErrorLogFromOutputs(myLogger, logClassName, stringBufferForOUTPUT, stringBufferForERROR);

			setOfCodeMessage(abstractJobType, statusName, processExitValue, descStr.toString());

		} catch (InterruptedException e) {

			myLogger.warn(" >>" + logLabel + ">> " + logClassName + " : Job timed-out terminating " + abstractJobType.getBaseJobInfos().getJsName());

			if(channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			
			if(session.isConnected()) {
				session.disconnect();
			}

		}
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

}

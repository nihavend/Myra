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
import com.likya.xsd.myra.model.xbeans.jobprops.SimplePropertiesType;
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

		AbstractJobType abstractJobType = getJobAbstractJobType();

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

		/*
		 * String xhost="127.0.0.1"; int xport=0;
		 * String display=JOptionPane.showInputDialog("Enter display name", xhost+":"+xport);
		 * xhost=display.substring(0, display.indexOf(':'));
		 * xport=Integer.parseInt(display.substring(display .indexOf(':')+1));
		 * session.setX11Host(xhost);
		 * session.setX11Port(xport+6000);
		 */

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.setPassword(password);
		session.connect();

		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(jobCommand);

		// X Forwarding
		// channel.setXForwarding(true);

		channel.setInputStream(System.in);
		// channel.setInputStream(null);

		// channel.setOutputStream(System.out);

		// FileOutputStream fos=new FileOutputStream("/tmp/stderr");
		// ((ChannelExec)channel).setErrStream(fos);
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

			myLogger.info(" >>" + " ExecuteSchComponent " + jobId + " islemi sonlandi, islem bitis degeri : " + processExitValue);

			StringBuffer descStr = new StringBuffer();

			StatusName.Enum statusName = JobHelper.searchReturnCodeInStates(abstractJobType, processExitValue, descStr);

			JobHelper.updateDescStr(descStr, stringBufferForOUTPUT, stringBufferForERROR);

			JobHelper.writetErrorLogFromOutputs(myLogger, logClassName, stringBufferForOUTPUT, stringBufferForERROR);

			JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_FINISHED, SubstateName.INT_COMPLETED, statusName.intValue());

		} catch (InterruptedException e) {

			myLogger.warn(" >>" + " ExecuteSchComponent " + ">> " + logClassName + " : Job timed-out terminating " + abstractJobType.getBaseJobInfos().getJsName());

			channel.disconnect();
			session.disconnect();

		}
	}

	@Override
	public void localRun() {
		
		Calendar startTime = Calendar.getInstance();
		
		//initStartUp(myLogger);

		SimplePropertiesType simpleProperties = getJobAbstractJobType();

		while (true) {

			try {

				startWathcDogTimer();

				String jobPath = simpleProperties.getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobPath();
				String jobCommand = simpleProperties.getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobCommand();

				JobHelper.insertNewLiveStateInfo(simpleProperties, StateName.INT_RUNNING, SubstateName.INT_ON_RESOURCE, StatusName.INT_TIME_IN);

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

		SimplePropertiesType simpleProperties = getJobAbstractJobType();

		LiveStateInfo liveStateInfo = simpleProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);
		
		/* FINISHED state i yoksa ekle */
		if (!LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.COMPLETED)) {
			JobHelper.insertNewLiveStateInfo(simpleProperties, StateName.INT_FINISHED, SubstateName.INT_COMPLETED, StatusName.INT_FAILED, err.getMessage());
		}

		myLogger.error(err.getMessage());

		myLogger.error(" >>" + logLabel + ">> " + err.getMessage());
		err.printStackTrace();

	}
	
	public boolean processJobResult(boolean retryFlag, Logger myLogger) {

		SimplePropertiesType simpleProperties = getJobAbstractJobType();

		if (simpleProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getStateName().equals(StateName.FINISHED)) {

			//sendStatusChangeInfo();

			String logStr = "islem bitirildi : " + simpleProperties.getId() + " => ";
			logStr += StateName.FINISHED.toString() + ":" + simpleProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getSubstateName().toString() + ":" + simpleProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getStatusName().toString();
			myLogger.info(" >>>>" + logStr + "<<<<");

		} else {

			// TODO Hoşuma gitmedi ama tip dönüşümü yaptım
			if (Boolean.parseBoolean(simpleProperties.getCascadingConditions().getJobAutoRetry().toString()) && retryFlag) {

				myLogger.info(" >> " + "ExecuteInShell : Job Failed ! Restarting " + simpleProperties.getBaseJobInfos().getJsName());

				JobHelper.insertNewLiveStateInfo(simpleProperties, StateName.INT_RUNNING, SubstateName.INT_ON_RESOURCE, StatusName.INT_TIME_IN);

				return true;

			} else {

				myLogger.info(" >>" + logLabel + ">> " + simpleProperties.getId() + ":Job Failed ! ");
				myLogger.debug(" >>" + logLabel + ">> " + simpleProperties.getId() + "ExecuteInShell : Job Failed !");

			}
		}

		return false;
	}
	
	protected void cleanUp(Process process, Calendar startTime) {

		AbstractJobType abstractJobType = getJobAbstractJobType();

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
		
		// getJobRuntimeProperties().getJobProperties().getTimeManagement().setJsRealTime(jobRealTime);

		// sendEndInfo(jobRuntimeProperties.getNativeFullJobPath().getFullPath(), jobProperties);

		// GlobalRegistery.getSpaceWideLogger().info(logLabel + endLog);
		// GlobalRegistery.getSpaceWideLogger().info(logLabel + duration);
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

	@Override
	public void stopMyDogBarking() {
		// TODO Auto-generated method stub
		
	}

}

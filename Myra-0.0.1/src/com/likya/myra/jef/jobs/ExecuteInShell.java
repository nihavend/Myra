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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.jvnet.winp.WinProcess;

import com.likya.myra.LocaleMessages;
import com.likya.myra.commons.ValidPlatforms;
import com.likya.myra.commons.grabber.StreamGrabber;
import com.likya.myra.commons.utils.CommonDateUtils;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.xsd.myra.model.xbeans.generics.JobTypeDefDocument.JobTypeDef;
import com.likya.xsd.myra.model.xbeans.joblist.AbstractJobType;
import com.likya.xsd.myra.model.xbeans.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.xbeans.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.xbeans.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.xbeans.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.xbeans.wlagen.JobAutoRetryDocument.JobAutoRetry;

public class ExecuteInShell extends CommonShell {

	private static final long serialVersionUID = 1L;

	boolean isShell = true;

	private boolean retryFlag = true;
	private int retryCounter = 1;

	transient private WatchDogTimer watchDogTimer = null;

	public ExecuteInShell(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}

	public void stopMyDogBarking() {
		if (watchDogTimer != null) {
			watchDogTimer.interrupt();
			watchDogTimer = null;
		}
	}

	@Override
	protected void localRun() {

		Calendar startTime = Calendar.getInstance();

		JobRuntimeInterface jobRuntimeInterface = getJobRuntimeProperties();

		AbstractJobType abstractJobType = getJobAbstractJobType();
		String jobId = abstractJobType.getId();

		String startLog = abstractJobType.getId() + LocaleMessages.getString("ExternalProgram.0") + CommonDateUtils.getDate(startTime.getTime());

		JobHelper.setJsRealTimeForStart(abstractJobType, startTime);

		CoreFactory.getLogger().info(startLog);

		while (true) {

			try {

				StringBuilder stringBufferForERROR = new StringBuilder();
				StringBuilder stringBufferForOUTPUT = new StringBuilder();

				jobRuntimeInterface.setRecentWorkDuration(jobRuntimeInterface.getWorkDuration());
				jobRuntimeInterface.setRecentWorkDurationNumeric(jobRuntimeInterface.getWorkDurationNumeric());

				startWathcDogTimer();

				ProcessBuilder processBuilder = null;

				String jobCommand = abstractJobType.getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobCommand();

				CoreFactory.getLogger().info(" >>" + " ExecuteInShell " + jobId + " Çalıştırılacak komut : " + jobCommand);

				if (isShell) {
					String[] cmd = ValidPlatforms.getCommand(jobCommand);
					processBuilder = new ProcessBuilder(cmd);
				} else {
					processBuilder = JobHelper.parsJobCmdArgs(jobCommand);
				}

				String jobPath = abstractJobType.getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobPath();
				if (jobPath != null) {
					jobCommand = JobHelper.removeSlashAtTheEnd(abstractJobType, jobPath, jobCommand);
					processBuilder.directory(new File(jobPath));
				}

				Map<String, String> tempEnv = new HashMap<String, String>();

				Map<String, String> environmentVariables = new HashMap<String, String>();

				if (environmentVariables != null && environmentVariables.size() > 0) {
					tempEnv.putAll(environmentVariables);
				}

				// tempEnv.putAll(XmlBeansTransformer.entryToMap(jobProperties));

				processBuilder.environment().putAll(tempEnv);

				process = processBuilder.start();

				jobRuntimeInterface.getMessageBuffer().delete(0, jobRuntimeInterface.getMessageBuffer().capacity());

				initGrabbers(process, jobId, CoreFactory.getLogger(), temporaryConfig.getLogBufferSize());
				//				// any error message?
				//				StreamGrabber errorGobbler = new StreamGrabber(process.getErrorStream(), "ERROR", CoreFactory.getLogger(), temporaryConfig.getLogBufferSize()); //$NON-NLS-1$
				//				errorGobbler.setName(jobId + ".ErrorGobbler.id." + errorGobbler.getId()); //$NON-NLS-1$
				//
				//				// any output?
				//				StreamGrabber outputGobbler = new StreamGrabber(process.getInputStream(), "OUTPUT", CoreFactory.getLogger(), temporaryConfig.getLogBufferSize()); //$NON-NLS-1$
				//				outputGobbler.setName(jobId + ".OutputGobbler.id." + outputGobbler.getId()); //$NON-NLS-1$
				//
				//				// kick them off
				//				errorGobbler.start();
				//				outputGobbler.start();

				try {

					process.waitFor();

					int processExitValue = process.exitValue();
					CoreFactory.getLogger().info(jobId + LocaleMessages.getString("ExternalProgram.6") + processExitValue); //$NON-NLS-1$

					String errStr = jobRuntimeInterface.getLogAnalyzeString();
					boolean hasErrorInLog = false;
					//					
					//					if (!getJobProperties().getLogFilePath().equals(ScenarioLoader.UNDEFINED_VALUE)) {
					//						if (errStr != null) {
					//							hasErrorInLog = FileUtils.analyzeFileForString(getJobProperties().getLogFilePath(), errStr);
					//						}
					//					} else if (errStr != null) {
					//						CoreFactory.getLogger().error("jobFailString: \"" + errStr + "\" " + LocaleMessages.getString("ExternalProgram.1") + " !");
					//					}

					if (watchDogTimer != null) {
						watchDogTimer.interrupt();
						watchDogTimer = null;
					}

					cleanUpFastEndings(errorGobbler, outputGobbler);

					stringBufferForERROR = errorGobbler.getOutputBuffer();
					stringBufferForOUTPUT = outputGobbler.getOutputBuffer();

					JobHelper.updateDescStr(jobRuntimeInterface.getMessageBuffer(), stringBufferForOUTPUT, stringBufferForERROR);

					StatusName.Enum statusName = JobHelper.searchReturnCodeInStates(abstractJobType, processExitValue, jobRuntimeInterface.getMessageBuffer());

					JobHelper.writetErrorLogFromOutputs(CoreFactory.getLogger(), this.getClass().getName(), stringBufferForOUTPUT, stringBufferForERROR);

					if (errStr != null && hasErrorInLog) {
						JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_FINISHED, SubstateName.INT_COMPLETED, StatusName.INT_FAILED, "Log da bulunan kelime yüzünden !");
					} else {
						JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_FINISHED, SubstateName.INT_COMPLETED, statusName.intValue(), jobRuntimeInterface.getMessageBuffer().toString());
					}

				} catch (InterruptedException e) {

					errorGobbler.interrupt();
					outputGobbler.interrupt();
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
					CoreFactory.getLogger().warn(LocaleMessages.getString("ExternalProgram.8") + jobId); //$NON-NLS-1$

					// process.waitFor() komutu thread'in interrupt statusunu temizlemedigi icin 
					// asagidaki sekilde temizliyoruz
					Thread.interrupted();

					process.destroy();
					JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_FINISHED, SubstateName.INT_COMPLETED, StatusName.INT_FAILED, e.getMessage());

				}

				errorGobbler.stopStreamGobbler();
				outputGobbler.stopStreamGobbler();
				errorGobbler = null;
				outputGobbler = null;
				watchDogTimer = null;

			} catch (Exception err) {
				if (watchDogTimer != null) {
					watchDogTimer.interrupt();
					watchDogTimer = null;
				}
				JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_FINISHED, SubstateName.INT_COMPLETED, StatusName.INT_FAILED, err.getMessage());
				err.printStackTrace();
			}

			LiveStateInfo liveStateInfo = abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);

			if (/* if not in dependency chain kontrolü eklenecek !!! */LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.COMPLETED, StatusName.SUCCESS)) {

				JobHelper.setWorkDurations(this, startTime);

				int jobType = abstractJobType.getBaseJobInfos().getJobInfos().getJobTypeDef().intValue();

				switch (jobType) {
				case JobTypeDef.INT_EVENT_BASED:
					// Not implemented yet
					break;
				case JobTypeDef.INT_TIME_BASED:
					// DateUtils.iterateNextDate(abstractJobType);
					JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_BYTIME);
					break;
				case JobTypeDef.INT_USER_BASED:
					JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_BYUSER);
					break;

				default:
					break;
				}

				CoreFactory.getLogger().info(LocaleMessages.getString("ExternalProgram.9") + jobId + " => " + liveStateInfo.getStatusName().toString());

			} else {

				JobHelper.setWorkDurations(this, startTime);

				boolean stateCond = LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.STOPPED, StatusName.BYUSER);

				if (abstractJobType.getCascadingConditions().getJobAutoRetry() == JobAutoRetry.YES && retryFlag && stateCond) {
					CoreFactory.getLogger().info(LocaleMessages.getString("ExternalProgram.11") + jobId);

					if (retryCounter < jobRuntimeInterface.getAutoRetryCount()) {
						retryCounter++;
						try {
							Thread.sleep(jobRuntimeInterface.getAutoRetryDelay());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						startTime = Calendar.getInstance();
						JobHelper.setJsRealTimeForStart(abstractJobType, startTime);

						JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_BYTIME);

						continue;
					}
				}

				CoreFactory.getLogger().info(jobId + LocaleMessages.getString("ExternalProgram.12")); //$NON-NLS-1$
				CoreFactory.getLogger().debug(jobId + LocaleMessages.getString("ExternalProgram.13")); //$NON-NLS-1$

			}

			// restore to the value derived from sernayobilgileri file.
			//			getJobProperties().setJobParamList(getJobProperties().getJobParamListPerm());

			retryFlag = false;

			break;
		}

		sendOutputData();
		
		setMyExecuter(null);
		process = null;

	}

	public boolean isRetryFlag() {
		return retryFlag;
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

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
import java.util.Date;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.utils.Scheduler;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.wlagen.CascadingConditionsDocument.CascadingConditions;
import com.likya.xsd.myra.model.wlagen.TriggerDocument.Trigger;

public abstract class GenericInnerJob extends JobImpl {

	private static final long serialVersionUID = -680353114457170591L;

	protected Calendar startTime;

	public GenericInnerJob(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}
	
	protected void setRunning(AbstractJobType abstractJobType) {
		ChangeLSI.forValue(abstractJobType, StateName.RUNNING, SubstateName.ON_RESOURCE, StatusName.TIME_IN);
	}

	protected void setOfCodeMessage(AbstractJobType abstractJobType, StatusName.Enum statusName, int resultCode, String message) {
		ChangeLSI.forValue(abstractJobType, StateName.FINISHED, SubstateName.COMPLETED, statusName, resultCode, message);
	}

	protected void setFailedOfLog(AbstractJobType abstractJobType) {
		ChangeLSI.forValue(abstractJobType, StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED, "Log da bulunan kelime yüzünden !");
	}

	protected void setFailedOfMessage(AbstractJobType abstractJobType, String message) {
		ChangeLSI.forValue(abstractJobType, StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED, message);
	}

	public void setRenewByTime(AbstractJobType abstractJobType) {
		ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, StatusName.BYTIME);
	}

	protected void setRenewByUser(AbstractJobType abstractJobType) {
		ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, StatusName.BYUSER);
	}

	public boolean scheduleForNextExecution(AbstractJobType abstractJobType) {
		return Scheduler.scheduleForNextExecution(abstractJobType);
	}

	//	public boolean processJobResultFromSch() {
	//
	//		AbstractJobType abstractJobType = getAbstractJobType();
	//		
	//		LiveStateInfo lastState = abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);
	//
	//		if (lastState.getStateName().equals(StateName.FINISHED)) {
	//
	//			String logStr = "islem bitirildi : " + abstractJobType.getId() + " => ";
	//			logStr += StateName.FINISHED.toString() + ":" + lastState.getSubstateName().toString() + ":" + lastState.getStatusName().toString();
	//			myLogger.info(" >>>>" + logStr + "<<<<");
	//
	//		} else {
	//
	//			if (Boolean.parseBoolean(abstractJobType.getCascadingConditions().getJobAutoRetryInfo().getJobAutoRetry().toString()) && retryFlag) {
	//				myLogger.info(" >> " + logLabel + " : Job Failed ! Restarting " + abstractJobType.getBaseJobInfos().getJsName());
	//				setRenewByTime(abstractJobType);
	//				return true;
	//			} else {
	//				myLogger.info(" >>" + logLabel + ">> " + abstractJobType.getId() + ":Job Failed ! ");
	//				myLogger.debug(" >>" + logLabel + ">> " + abstractJobType.getId() + " : Job Failed !");
	//			}
	//		}
	//
	//		return false;
	//	}

	private boolean isInDepenedencyChain(String jobId) {
		return !CoreFactory.getInstance().getNetTreeManagerInterface().getFreeJobs().containsKey(jobId);
	}

	public void processJobResult() {

		AbstractJobType abstractJobType = getAbstractJobType();

		String jobId = abstractJobType.getId();

		if (isInDepenedencyChain(jobId)) {
			// Do not touch, leave it to its master !
			return;
		}

		LiveStateInfo liveStateInfo = abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);

		boolean isState = LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.COMPLETED, StatusName.SUCCESS);

		/**
		 * if a job fails; Two parameters; autoRetry and runEvenIfFailed conflicts. In this is case,
		 * the priority of runEvenIfFailed is higher so, the autoRetry is discarded
		 */

		boolean goOnError = (abstractJobType.getManagement().getCascadingConditions() != null && abstractJobType.getManagement().getCascadingConditions().getRunEvenIfFailed());

		if (isState || goOnError) {

			JobHelper.setWorkDurations(this, startTime);

			int jobType = abstractJobType.getManagement().getTrigger().intValue();

			switch (jobType) {
			case Trigger.INT_EVENT:
				// Not implemented yet
				break;
			case Trigger.INT_TIME:
				if (scheduleForNextExecution(abstractJobType)) {
					String startTime = MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime().getTime());
					CoreFactory.getLogger().info("Job [" + abstractJobType.getId() + "] bir sonraki zamana kuruldu : " + startTime);
					setRenewByTime(abstractJobType);
				}
				break;
			case Trigger.INT_USER:
				setRenewByUser(abstractJobType);
				break;

			default:
				break;
			}

			CoreFactory.getLogger().info(CoreFactory.getMessage("ExternalProgram.9") + jobId + " => " + (liveStateInfo.getStatusName() == null ? "" : liveStateInfo.getStatusName().toString()));

		} else {

			JobHelper.setWorkDurations(this, startTime);

			boolean manuelStop = LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.STOPPED, StatusName.BYUSER);

			if (!manuelStop) {

				CascadingConditions cascadingConditions = abstractJobType.getManagement().getCascadingConditions();

				if (cascadingConditions != null && cascadingConditions.getJobAutoRetryInfo().getJobAutoRetry()) {

					boolean retryCondition = true;

					if (cascadingConditions.getJobAutoRetryInfo().getLiveStateInfo() != null) {
						retryCondition = LiveStateInfoUtils.equalStates(liveStateInfo, cascadingConditions.getJobAutoRetryInfo().getLiveStateInfo());
					}

					if (retryCondition) {
						
						if (retryCounter < cascadingConditions.getJobAutoRetryInfo().getMaxCount().intValue()) {

							CoreFactory.getLogger().info(CoreFactory.getMessage("ExternalProgram.11") + jobId);
							retryCounter++;

							long stepTime = MyraDateUtils.getDurationInMilliSecs(abstractJobType.getManagement().getCascadingConditions().getJobAutoRetryInfo().getStep());

							JobHelper.setJsPlannedTimeForStart(abstractJobType, stepTime);
							setRenewByTime(abstractJobType);

						} else {
							// reset counter and leave for normal scheduling
							retryCounter = 0;
						}
					
					}
				}
			}

			/*
			if (abstractJobType.getManagement().getCascadingConditions() != null && abstractJobType.getManagement().getCascadingConditions().getJobAutoRetryInfo().getJobAutoRetry() == true && !manuelStop) {

				if (retryCounter < abstractJobType.getManagement().getCascadingConditions().getJobAutoRetryInfo().getMaxCount().intValue()) {
					CoreFactory.getLogger().info(CoreFactory.getMessage("ExternalProgram.11") + jobId);
					retryCounter++;

					long stepTime = MyraDateUtils.getDurationInMilliSecs(abstractJobType.getManagement().getCascadingConditions().getJobAutoRetryInfo().getStep());

					JobHelper.setJsPlannedTimeForStart(abstractJobType, stepTime);

					setRenewByTime(abstractJobType);

				} else {
					// reset counter and leave for normal scheduling
					retryCounter = 0;
				}

			}
			*/

			CoreFactory.getLogger().info(jobId + CoreFactory.getMessage("ExternalProgram.12"));
			CoreFactory.getLogger().debug(jobId + CoreFactory.getMessage("ExternalProgram.13"));

		}
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

}

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

import com.likya.myra.LocaleMessages;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.OutputData;
import com.likya.myra.jef.utils.Scheduler;
import com.likya.xsd.myra.model.xbeans.generics.JobTypeDefDocument.JobTypeDef;
import com.likya.xsd.myra.model.xbeans.joblist.AbstractJobType;
import com.likya.xsd.myra.model.xbeans.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.xbeans.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.xbeans.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.xbeans.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.xbeans.wlagen.JobAutoRetryDocument.JobAutoRetry;

public abstract class GenericInnerJob extends JobImpl {
	
	private static final long serialVersionUID = -680353114457170591L;
	
	protected Calendar startTime;

	public GenericInnerJob(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}

	protected void setRunning(AbstractJobType abstractJobType) {
		JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_RUNNING, SubstateName.INT_ON_RESOURCE, StatusName.INT_TIME_IN);
		sendOutputData();
	}

	protected void setOfCodeMessage(AbstractJobType abstractJobType, int code, String message) {
		JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_FINISHED, SubstateName.INT_COMPLETED, code, message);
		sendOutputData();
	}

	protected void setFailedOfLog(AbstractJobType abstractJobType) {
		JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_FINISHED, SubstateName.INT_COMPLETED, StatusName.INT_FAILED, "Log da bulunan kelime yüzünden !");
		sendOutputData();
	}

	protected void setFailedOfMessage(AbstractJobType abstractJobType, String message) {
		JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_FINISHED, SubstateName.INT_COMPLETED, StatusName.INT_FAILED, message);
		sendOutputData();
	}

	protected void setRenewByTime(AbstractJobType abstractJobType) {
		JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_BYTIME);
		sendOutputData();
	}

	protected void setRenewByUser(AbstractJobType abstractJobType) {
		JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_BYUSER);
		sendOutputData();
	}
	
	protected void scheduleForNextExecution(AbstractJobType abstractJobType) {
		Scheduler.scheduleForNextExecution(abstractJobType);		
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
	
	public void processJobResult() {

		AbstractJobType abstractJobType = getAbstractJobType();

		String jobId = abstractJobType.getId();

		LiveStateInfo liveStateInfo = abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);

		if (/* if not in dependency chain kontrolü eklenecek !!! */LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.COMPLETED, StatusName.SUCCESS)) {

			JobHelper.setWorkDurations(this, startTime);

			int jobType = abstractJobType.getBaseJobInfos().getJobInfos().getJobTypeDef().intValue();

			switch (jobType) {
			case JobTypeDef.INT_EVENT_BASED:
				// Not implemented yet
				break;
			case JobTypeDef.INT_TIME_BASED:
				scheduleForNextExecution(abstractJobType);
				setRenewByTime(abstractJobType);
				break;
			case JobTypeDef.INT_USER_BASED:
				setRenewByUser(abstractJobType);
				break;

			default:
				break;
			}

			CoreFactory.getLogger().info(LocaleMessages.getString("ExternalProgram.9") + jobId + " => " + liveStateInfo.getStatusName().toString());

		} else {

			JobHelper.setWorkDurations(this, startTime);

			boolean stateCond = LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.STOPPED, StatusName.BYUSER);

			if (abstractJobType.getCascadingConditions().getJobAutoRetryInfo().getJobAutoRetry() == JobAutoRetry.YES && !stateCond) {
				
				if (retryCounter < abstractJobType.getCascadingConditions().getJobAutoRetryInfo().getMaxCount().intValue()) {
					CoreFactory.getLogger().info(LocaleMessages.getString("ExternalProgram.11") + jobId);
					retryCounter++;

					long stepTime = MyraDateUtils.getDurationInMilliSecs(abstractJobType.getCascadingConditions().getJobAutoRetryInfo().getStep());

					JobHelper.setJsPlannedTimeForStart(abstractJobType, stepTime);

					setRenewByTime(abstractJobType);

				} else {
					// reset counter and leave for normal scheduling
					retryCounter = 0;
				}
			
			}

			CoreFactory.getLogger().info(jobId + LocaleMessages.getString("ExternalProgram.12"));
			CoreFactory.getLogger().debug(jobId + LocaleMessages.getString("ExternalProgram.13"));

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

	protected void sendOutputData() {

		OutputData outputData = new OutputData();

		outputData.setGroupName("");
		outputData.setHandleUri(getAbstractJobType().getHandlerURI());
		outputData.setJobId(getAbstractJobType().getId());
		outputData.setStartTime(getAbstractJobType().getTimeManagement().getJsRealTime().getStartTime());
		outputData.setStopTime(getAbstractJobType().getTimeManagement().getJsRealTime().getStopTime());
		outputData.setTreeId("treeId");
		outputData.setStateInfos(getAbstractJobType().getStateInfos());

		super.sendOutputData(outputData);
	}

}

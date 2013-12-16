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

import java.util.Date;

import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.OutputData;
import com.likya.xsd.myra.model.xbeans.joblist.AbstractJobType;
import com.likya.xsd.myra.model.xbeans.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.xbeans.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.xbeans.stateinfo.SubstateNameDocument.SubstateName;

public abstract class GenericInnerJob extends JobImpl {

	public GenericInnerJob(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}

	private static final long serialVersionUID = -680353114457170591L;

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
		outputData.setJobId(getAbstractJobType().getId2());
		outputData.setStartTime(getAbstractJobType().getTimeManagement().getJsRealTime().getStartTime());
		outputData.setStopTime(getAbstractJobType().getTimeManagement().getJsRealTime().getStopTime());
		outputData.setTreeId("treeId");
		outputData.setStateInfos(getAbstractJobType().getStateInfos());
		
		super.sendOutputData(outputData);
	}

}

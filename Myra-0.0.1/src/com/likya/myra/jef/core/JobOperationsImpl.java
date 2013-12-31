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
package com.likya.myra.jef.core;

import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.model.OutputData;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class JobOperationsImpl implements JobOperations {
	
	private CoreFactory coreFactory;
	
	public JobOperationsImpl(CoreFactory coreFactory) {
		super();
		this.coreFactory = coreFactory;
	}

	protected void sendOutputData(Object object) {
		OutputStrategy outputStrategy = coreFactory.getOutputStrategy();
		outputStrategy.sendDataObject(object);
	}

	@Override
	public void retryExecution(String jobName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSuccess(String jobName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skipJob(String jobName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skipJob(boolean isForced, String jobName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopJob(String jobName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseJob(String jobName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resumeJob(String jobName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startJob(String jobName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disableJob(String jobName) {
		
		CoreFactory.getLogger().info(CoreFactory.getMessage("TlosCommInterface.35") + jobName);

		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobName)) {

			AbstractJobType abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().get(jobName).getAbstractJobType();
			
			JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_CANCELLED, SubstateName.INT_DEACTIVATED, StatusName.INT_BYUSER);
			
			OutputData outputData = OutputData.generateDefault(abstractJobType);
			
			sendOutputData(outputData);

//			TODO yeni yapıda bu iş nasıl olacak ?
//			synchronized (TlosServer.getDisabledJobQueue()) {
//				TlosServer.getDisabledJobQueue().put(jobName, jobName);
//			}
			
			CoreFactory.getLogger().info(CoreFactory.getMessage("TlosCommInterface.19") + jobName + " : " + abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).toString());
		}
	}

	@Override
	public void enableJob(String jobName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setJobInputParam(String jobName, String parameterList) {
		// TODO Auto-generated method stub
		return null;
	}

}

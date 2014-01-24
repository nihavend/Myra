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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.likya.myra.commons.utils.DependencyOperations;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.jobs.ChangeLSI;
import com.likya.myra.jef.jobs.GenericInnerJob;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class JobOperationsImpl implements JobOperations {
	
	private CoreFactory coreFactory;
	private Logger logger = CoreFactory.getLogger();
	
	public JobOperationsImpl(CoreFactory coreFactory) {
		super();
		this.coreFactory = coreFactory;
	}

	public void retryExecution(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.310") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {

			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isRetryable = LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED) || LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.FINISHED, SubstateName.STOPPED);
			
			if(isRetryable) {
				((GenericInnerJob) myJob).setRenewByTime(myJob.getAbstractJobType());
				logger.info(CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
			}
			
		}
		
	}

	public void setSuccess(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.303") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isSuccessable = LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING, SubstateName.PAUSED) || LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED);
			
			if(isSuccessable) {
				if(((GenericInnerJob) myJob).scheduleForNextExecution(myJob.getAbstractJobType())) {
					ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.COMPLETED, StatusName.SUCCESS);
					logger.info(CoreFactory.getMessage("Myra.304") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
				} else {
					ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED, "set success yaparken bir hata oluştu !");
				}
			}
		}
		
	}

	public void skipJob(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.306") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isSkipable = LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING, SubstateName.PAUSED) || LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED);

			if(isSkipable) {
				if(((GenericInnerJob) myJob).scheduleForNextExecution(myJob.getAbstractJobType())) {
					ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.SKIPPED);
					logger.info(CoreFactory.getMessage("Myra.307") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
				} else {
					ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED, "set success yaparken bir hata oluştu !");
				}
			}

		}
		
	}

	public void stopJob(String jobName) {
		
		logger.info(CoreFactory.getMessage("Myra.309") + jobName);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobName)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobName);
			
			boolean isStopable = LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.RUNNING);

			if(isStopable) {
				myJob.stopMyDogBarking();
				ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.STOPPED);
				Thread executerThread = myJob.getMyExecuter();
				if (executerThread != null) {
					myJob.getMyExecuter().interrupt();
					myJob.setMyExecuter(null);
				}
				logger.info(CoreFactory.getMessage("Myra.310") + jobName + " : " + JobHelper.getLastStateInfo(myJob));
			}

		}
		
	}

	public void pauseJob(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.312") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isPausable = LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING) ;
			
			if(isPausable) {
				ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.PENDING, SubstateName.PAUSED);
				logger.info(CoreFactory.getMessage("Myra.313") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
			}
			
		}
		
	}

	public void resumeJob(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.315") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isResumable = LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING, SubstateName.PAUSED);
			
			if(isResumable) {
				ChangeLSI.forValue(myJob.getAbstractJobType(), JobHelper.getLastStateInfo(myJob));
				logger.info(CoreFactory.getMessage("Myra.316") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
			}

		}
	}

	public void startJob(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.318") + jobId);

		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {

			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			
			boolean isStartable = LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING) && (myJob.getAbstractJobType().getDependencyList() == null || myJob.getAbstractJobType().getDependencyList().sizeOfItemArray() == 0);
			
			if(isStartable) {
				Calendar nowDateTime = Calendar.getInstance();
				updateStartConditionsOfDepChain(jobId,  nowDateTime);
				myJob.getAbstractJobType().getManagement().getTimeManagement().getJsPlannedTime().setStartTime(nowDateTime);
				ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.PENDING, SubstateName.IDLED, StatusName.BYTIME);
			}
			
			logger.info(CoreFactory.getMessage("Myra.319") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
		}
		
	}

	@Override
	public void disableJob(String jobName) {
		
		logger.info(CoreFactory.getMessage("Myra.335") + jobName);

		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobName)) {

			AbstractJobType abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().get(jobName).getAbstractJobType();
			
			ChangeLSI.forValue(abstractJobType, LiveStateInfoUtils.generateLiveStateInfo(StateName.INT_PENDING, SubstateName.INT_DEACTIVATED));
			
//			TODO yeni yapıda bu iş nasıl olacak ?
//			synchronized (TlosServer.getDisabledJobQueue()) {
//				TlosServer.getDisabledJobQueue().put(jobName, jobName);
//			}
			
			CoreFactory.getLogger().info(CoreFactory.getMessage("Myra.319") + jobName + " : " + JobHelper.getLastStateInfo(abstractJobType));
		}
	}

	public void enableJob(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.336") + jobId);

		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {

			AbstractJobType abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().get(jobId).getAbstractJobType();
			
			if(LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(abstractJobType), StateName.PENDING, SubstateName.DEACTIVATED)) {
				ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, StatusName.BYTIME);
			}
			//			synchronized (TlosServer.getDisabledJobQueue()) {
			//				TlosServer.getDisabledJobQueue().remove(jobName);
			//			}
			
			logger.info(CoreFactory.getMessage("Myra.336") + jobId + " : " + JobHelper.getLastStateInfo(abstractJobType));
		}
	}

	@Override
	public String setJobInputParam(String jobName, String parameterList) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void updateStartConditionsOfDepChain(String jobId, Calendar myDate) {
		
		
		HashMap<String, AbstractJobType> abstractJobTypeList = JobQueueOperations.toAbstractJobTypeList(coreFactory.getMonitoringOperations().getJobQueue());
		
		ArrayList<AbstractJobType> dependencyList = DependencyOperations.getDependencyList(abstractJobTypeList, jobId);
		
		if (dependencyList == null) {
			return;
		}
		
		for(AbstractJobType abstractJobType : dependencyList) {
			
			if(abstractJobType.getDependencyList().getSensInfo() == null || abstractJobType.getDependencyList().getSensInfo().getSensTime() == null) {
				continue;
			}
			
			String tmpJobId = abstractJobType.getId();
			ArrayList<AbstractJobType> tempJobList = DependencyOperations.getDependencyList(abstractJobTypeList, tmpJobId);
			if ((tempJobList != null) && (tempJobList.size() > 0)) {
				updateStartConditionsOfDepChain(tmpJobId, myDate);
			}
			abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().setStartTime(myDate);
		}

	}
	
}

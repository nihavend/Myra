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

import java.net.UnknownServiceException;
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
		
		logger.info(CoreFactory.getMessage("Myra.302") + CoreFactory.getMessage("Myra.300") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {

			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isRetryable = Commandability.isRetryable(myJob);
			
			if(isRetryable) {
				((GenericInnerJob) myJob).setRenewByTime(myJob.getAbstractJobType());
				logger.info(CoreFactory.getMessage("Myra.302") + CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
			}
			
		}
		
	}

	public void setSuccess(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.303") + CoreFactory.getMessage("Myra.300") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isSuccessable = Commandability.isSuccessable(myJob);
			
			if(isSuccessable) {
				if(((GenericInnerJob) myJob).scheduleForNextExecution(myJob.getAbstractJobType())) {
					ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.COMPLETED, StatusName.SUCCESS);
					logger.info(CoreFactory.getMessage("Myra.303") + CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
				} else {
					ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED, "set success yaparken bir hata oluştu !");
				}
			}
		}
		
	}

	public void skipJob(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.304") + CoreFactory.getMessage("Myra.300") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isSkipable = Commandability.isSkipable(myJob);

			if(isSkipable) {
				if(((GenericInnerJob) myJob).scheduleForNextExecution(myJob.getAbstractJobType())) {
					ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.SKIPPED);
					logger.info(CoreFactory.getMessage("Myra.304") + CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
				} else {
					ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED, "set success yaparken bir hata oluştu !");
				}
			}

		}
		
	}

	public void stopJob(String jobName) {
		
		logger.info(CoreFactory.getMessage("Myra.305") + CoreFactory.getMessage("Myra.300") + jobName);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobName)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobName);
			
			boolean isStopable = Commandability.isStopable(myJob);

			if(isStopable) {
				myJob.stopMyDogBarking();
				ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.STOPPED, StatusName.BYUSER);
				Thread executerThread = myJob.getMyExecuter();
				if (executerThread != null) {
					myJob.getMyExecuter().interrupt();
					myJob.setMyExecuter(null);
				}
				logger.info(CoreFactory.getMessage("Myra.305") + CoreFactory.getMessage("Myra.301") + jobName + " : " + JobHelper.getLastStateInfo(myJob));
			}

		}
		
	}

	public void pauseJob(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.316") + CoreFactory.getMessage("Myra.300") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isPausable = Commandability.isPausable(myJob);
			
			if(isPausable) {
				ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.PENDING, SubstateName.PAUSED);
				logger.info(CoreFactory.getMessage("Myra.316") + CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
			}
			
		}
		
	}

	public void resumeJob(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.317") + CoreFactory.getMessage("Myra.300") + jobId);
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			
			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			boolean isResumable = Commandability.isResumable(myJob);
			
			if(isResumable) {
				ChangeLSI.forValue(myJob.getAbstractJobType(), JobHelper.getStateInfo(myJob, 1));
				logger.info(CoreFactory.getMessage("Myra.317") + CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
			}

		}
	}

	public void startJob(String jobId) {
		
		logger.info(CoreFactory.getMessage("Myra.318") + CoreFactory.getMessage("Myra.300") + jobId);

		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {

			JobImpl myJob = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			
			
			boolean isStartable = Commandability.isStartable(myJob);
			
			if(isStartable) {
				Calendar nowDateTime = Calendar.getInstance();
				updateStartConditionsOfDepChain(jobId,  nowDateTime);
				myJob.getAbstractJobType().getManagement().getTimeManagement().getJsPlannedTime().setStartTime(nowDateTime);
				ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.PENDING, SubstateName.IDLED, StatusName.BYTIME);
			}
			
			logger.info(CoreFactory.getMessage("Myra.318") + CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
		}
		
	}

	@Override
	public void disableJob(String jobName) {
		
		logger.info(CoreFactory.getMessage("Myra.319") + CoreFactory.getMessage("Myra.300") + jobName);

		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobName)) {

			AbstractJobType abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().get(jobName).getAbstractJobType();
			
			ChangeLSI.forValue(abstractJobType, LiveStateInfoUtils.generateLiveStateInfo(StateName.INT_PENDING, SubstateName.INT_DEACTIVATED, LiveStateInfoUtils.getLastStateInfo(abstractJobType).getStatusName().intValue()));
			
//			TODO yeni yapıda bu iş nasıl olacak ?
//			synchronized (TlosServer.getDisabledJobQueue()) {
//				TlosServer.getDisabledJobQueue().put(jobName, jobName);
//			}
			
			CoreFactory.getLogger().info(CoreFactory.getMessage("Myra.319") + CoreFactory.getMessage("Myra.301") + jobName + " : " + LiveStateInfoUtils.getLastStateInfo(abstractJobType));
		}
	}

	public void enableJob(String jobId) {
		enableJob(jobId, false);
		return;
	}
	
	public void enableJob(String jobId, boolean normalize) {
		
		logger.info(CoreFactory.getMessage("Myra.310") + CoreFactory.getMessage("Myra.300") + jobId);

		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {

			AbstractJobType abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().get(jobId).getAbstractJobType();
			
			String logStr = "Enabling job >> " + abstractJobType.getId();
			
			if(LiveStateInfoUtils.equalStatesPD(abstractJobType)) {
				if(normalize) {
					logStr = logStr + " after normalizing !";
					JobHelper.resetJob(abstractJobType);
				} else { 
					logStr = logStr + " without normalizing !";
				}
				ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, LiveStateInfoUtils.getLastStateInfo(abstractJobType).getStatusName());
				CoreFactory.getLogger().info(logStr);
			}
			//			synchronized (TlosServer.getDisabledJobQueue()) {
			//				TlosServer.getDisabledJobQueue().remove(jobName);
			//			}
			
			logger.info(CoreFactory.getMessage("Myra.310") + CoreFactory.getMessage("Myra.301") + jobId + " : " + LiveStateInfoUtils.getLastStateInfo(abstractJobType));
		}
	}

	public String setJobInputParam(String jobId, String paramString) {
		
		logger.info(CoreFactory.getMessage("Myra.311") + CoreFactory.getMessage("Myra.300") + jobId);
		
		String returnValue = "";
		
		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			AbstractJobType abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().get(jobId).getAbstractJobType();
			if(LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING)) {
				abstractJobType.getBaseJobInfos().getJobTypeDetails().setArgValues(paramString);
				returnValue = paramString + " " + CoreFactory.getMessage("Myra.873")  + " " + jobId;
			} else {
				logger.info(CoreFactory.getMessage("Myra.847"));
				returnValue = CoreFactory.getMessage("Myra.874");
			}

			logger.info(CoreFactory.getMessage("Myra.311") + CoreFactory.getMessage("Myra.301") + jobId + " : " + LiveStateInfoUtils.getLastStateInfo(abstractJobType));
		}
		
		return returnValue;
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

	public void addJob(AbstractJobType abstractJobType, boolean persist) throws Exception {
		JobImpl jobImpl = JobQueueOperations.transformJobTypeToImpl(abstractJobType);
		synchronized (coreFactory.getMonitoringOperations().getJobQueue()) {
			coreFactory.getMonitoringOperations().getJobQueue().put(abstractJobType.getId(), jobImpl);
			coreFactory.getManagementOperations().sendReIndexSignal();
		}

		if (abstractJobType.getDependencyList() == null || abstractJobType.getDependencyList().sizeOfItemArray() == 0) { // No dependency free job
			synchronized (coreFactory.getNetTreeManagerInterface().getFreeJobs()) {
				coreFactory.getNetTreeManagerInterface().getFreeJobs().put(abstractJobType.getId(), abstractJobType);
			}
		} else { // has dependency, if nettreemap exist, then add to that map. If not, then create new map and move all to new net tree map
			throw new UnknownServiceException("Not implemented yet !");
		}
	}
	
	public void removeJob(String jobId, boolean persist)  throws Exception {
		throw new UnknownServiceException("Not implemented yet !");
	}
	
	public void updateJob(AbstractJobType abstractJobType, boolean persist)  throws Exception {
		throw new UnknownServiceException("Not implemented yet !");
	}

	public void readJob(String jobId)  throws Exception {
		throw new UnknownServiceException("Use monitoringOperations.getJobQueue().get(jobId); instead !");
	}
}

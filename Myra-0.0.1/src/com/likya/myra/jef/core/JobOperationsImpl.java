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

import com.likya.commons.utils.SortUtils;
import com.likya.myra.commons.utils.DependencyOperations;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
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
			
			boolean isRetryable = Commandability.isRetryable(myJob.getAbstractJobType());
			
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
			
			boolean isSuccessable = Commandability.isSuccessable(myJob.getAbstractJobType());
			
			if(isSuccessable) {
				if(((GenericInnerJob) myJob).scheduleForNextExecution(myJob.getAbstractJobType())) {
					ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.FINISHED, SubstateName.COMPLETED, StatusName.SUCCESS, "Reason : SetSucces Command Received");
					logger.info(CoreFactory.getMessage("Myra.303") + CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
					if(JobQueueOperations.isMeFree(myJob.getAbstractJobType())) {
						ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.PENDING, SubstateName.IDLED, StatusName.BYTIME);
						logger.info(CoreFactory.getMessage("Myra.303") + CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
					}
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
			
			boolean isSkipable = Commandability.isSkipable(myJob.getAbstractJobType());

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
			
			boolean isStopable = Commandability.isStopable(myJob.getAbstractJobType());

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
			
			boolean isPausable = Commandability.isPausable(myJob.getAbstractJobType());
			
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
			
			boolean isResumable = Commandability.isResumable(myJob.getAbstractJobType());
			
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
			
			
			boolean isStartable = Commandability.isStartable(myJob.getAbstractJobType());
			
			if(isStartable) {
				Calendar nowDateTime = Calendar.getInstance();
				updateStartConditionsOfDepChain(jobId,  nowDateTime);
				if(myJob.getAbstractJobType().getManagement().getTimeManagement() == null) {
					myJob.getAbstractJobType().getManagement().addNewTimeManagement().addNewJsActualTime();
				}
				myJob.getAbstractJobType().getManagement().getTimeManagement().getJsActualTime().setStartTime(nowDateTime);
				
				// İlk önce grup işlemini yapıp sonra job statu degisikligi yapıyoruz
				// Aksi taktirde grup işlmleri job statuye takılıp gecersiz oluyor.
				NetTree netTree = JobQueueOperations.getNetTree(jobId);
				if(netTree != null) { // Member of dependency tree
					if(!netTree.isActive()) {
						enableGroup(netTree.getVirtualId());
						netTree.setActive(true);
					}
				}

				ChangeLSI.forValue(myJob.getAbstractJobType(), StateName.PENDING, SubstateName.IDLED, StatusName.BYTIME);
			}
			
			logger.info(CoreFactory.getMessage("Myra.318") + CoreFactory.getMessage("Myra.301") + jobId + " : " + JobHelper.getLastStateInfo(myJob));
		}
		
	}

	public void disableJob(String jobName) {
		disableJob(jobName, false);
	}
	
	@Override
	public void disableJob(String jobName, boolean isGroupCommand) {
		
		logger.info(CoreFactory.getMessage("Myra.319") + CoreFactory.getMessage("Myra.300") + jobName);

		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobName)) {

			AbstractJobType abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().get(jobName).getAbstractJobType();
			
			if (!isGroupCommand) {
				if (!Commandability.isDisablableForFree(abstractJobType)) {
					CoreFactory.getLogger().info(CoreFactory.getMessage("Myra.3191") + jobName + " : " + LiveStateInfoUtils.getLastStateInfo(abstractJobType));
					return;
				}
			}
			
			ChangeLSI.forValue(abstractJobType, LiveStateInfoUtils.generateLiveStateInfo(StateName.INT_PENDING, SubstateName.INT_DEACTIVATED));
			CoreFactory.getLogger().info(CoreFactory.getMessage("Myra.319") + CoreFactory.getMessage("Myra.301") + jobName + " : " + LiveStateInfoUtils.getLastStateInfo(abstractJobType));
		}
	}

	public void enableJob(String jobId) {
		enableJob(jobId, false);
		return;
	}
	
	public void enableJob(String jobId, boolean normalize, boolean isGroupCommand) {
		
		logger.info(CoreFactory.getMessage("Myra.310") + CoreFactory.getMessage("Myra.300") + jobId);

		if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {

			AbstractJobType abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().get(jobId).getAbstractJobType();
			
			if(!isGroupCommand) {
				if(!Commandability.isEnablableForFree(abstractJobType)) {
					CoreFactory.getLogger().info(CoreFactory.getMessage("Myra.3101") + jobId + " : " + LiveStateInfoUtils.getLastStateInfo(abstractJobType));
					return;
				}
			}
			
			String logStr = "Enabling job >> " + abstractJobType.getId();
			
			if(LiveStateInfoUtils.equalStatesPD(abstractJobType)) {
				if(normalize) {
					logStr = logStr + " after normalizing !";
					JobHelper.resetJob(abstractJobType);
				} else { 
					logStr = logStr + " without normalizing !";
				}
				
				if(LiveStateInfoUtils.getLastStateInfo(abstractJobType).getStatusName() == null) {
					JobHelper.evaluateTriggerType(abstractJobType, true, false);
				} else {
					ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, LiveStateInfoUtils.getLastStateInfo(abstractJobType).getStatusName());
				}
				
				CoreFactory.getLogger().info(logStr);
			}
			//			synchronized (TlosServer.getDisabledJobQueue()) {
			//				TlosServer.getDisabledJobQueue().remove(jobName);
			//			}
			
			logger.info(CoreFactory.getMessage("Myra.310") + CoreFactory.getMessage("Myra.301") + jobId + " : " + LiveStateInfoUtils.getLastStateInfo(abstractJobType));
		}
	}
	
	/**
	 * Preserved for backward compatibility
	 */
	public void enableJob(String jobId, boolean normalize) {
		enableJob(jobId, normalize, false);
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
			abstractJobType.getManagement().getTimeManagement().getJsActualTime().setStartTime(myDate);
		}

	}
	
	private void addUpdateJobQueue(JobImpl jobImpl, boolean persist, boolean isNew) throws UnknownServiceException  {
		
		synchronized (coreFactory.getMonitoringOperations().getJobQueue()) {
			
			if(isNew) {
				int maxId = 0;
				if(coreFactory.getMonitoringOperations().getJobQueue().size() > 0) {
					String [] idArray = SortUtils.sortKeys(coreFactory.getMonitoringOperations().getJobQueue().keySet());
					maxId = Integer.parseInt(idArray[idArray.length - 1]);
				}
				jobImpl.getAbstractJobType().setId("" + (maxId + 1));
			} 
			
			coreFactory.getMonitoringOperations().getJobQueue().put(jobImpl.getAbstractJobType().getId(), jobImpl);
					
			
//			// new on not empty list
//			if(isNew && coreFactory.getMonitoringOperations().getJobQueue().size() > 0) {
//				String [] idArray = SortUtils.sortKeys(coreFactory.getMonitoringOperations().getJobQueue().keySet());
//				int maxId = Integer.parseInt(idArray[idArray.length - 1]);
//				
//				if(maxId < 0) maxId = 1;
//				
//				jobImpl.getAbstractJobType().setId("" + (maxId + 1));
//				coreFactory.getMonitoringOperations().getJobQueue().put(jobImpl.getAbstractJobType().getId(), jobImpl);
//			} else { // update existing, or the first record to be inserted
//				coreFactory.getMonitoringOperations().getJobQueue().put(jobImpl.getAbstractJobType().getId(), jobImpl);
//			}
			
			coreFactory.getManagementOperations().sendReIndexSignal();
		}
		
		synchronized (coreFactory.getJobListDocument().getJobList()) {
			if (isNew) {
				coreFactory.getJobListDocument().getJobList().addNewGenericJob().set(jobImpl.getAbstractJobType());
			} else {
				JobQueueOperations.updateJobType(jobImpl.getAbstractJobType(), coreFactory.getJobListDocument().getJobList());
			}
		}
	}
	
	private void addUpdateJob(AbstractJobType abstractJobType, boolean persist, boolean isNew) throws Exception  {
		
		JobImpl jobImpl = JobQueueOperations.transformJobTypeToImpl(abstractJobType);
		
		if (jobImpl.getAbstractJobType().getDependencyList() == null || jobImpl.getAbstractJobType().getDependencyList().sizeOfItemArray() == 0) { // No dependency free job
			synchronized (coreFactory.getNetTreeManagerInterface().getFreeJobs()) {
				addUpdateJobQueue(jobImpl, persist, isNew);
				coreFactory.getNetTreeManagerInterface().getFreeJobs().put(jobImpl.getAbstractJobType().getId(), jobImpl.getAbstractJobType().getId());
			}
		} else { // has dependency, if nettreemap exist, then add to that map. If not, then create new map and move all to new net tree map
			
			// 1. Validate dependency list  if (!DependencyOperations.validateDependencyList(logger, abstractJobTypeQueue)) {....
			HashMap<String, AbstractJobType> abstractJobTypeQueue = JobQueueOperations.toAbstractJobTypeList(coreFactory.getMonitoringOperations().getJobQueue());
			if (!DependencyOperations.validateDependencyList(logger, abstractJobTypeQueue)) {
				throw new Exception("JobList.xml is dependency definitions are not  valid !");
			}
			// 2. Bağımlılık listesinde ağacı olan var mı kontrol et, 
			
			addUpdateJobQueue(jobImpl, persist, isNew);
		}

		resetViewTree();

	}
	
//	private void addUpdateJob(AbstractJobType abstractJobType, boolean persist, boolean isNew) throws Exception  {
//		
//		JobImpl jobImpl = JobQueueOperations.transformJobTypeToImpl(abstractJobType);
//		
//		if (jobImpl.getAbstractJobType().getDependencyList() == null || jobImpl.getAbstractJobType().getDependencyList().sizeOfItemArray() == 0) { // No dependency free job
//			synchronized (coreFactory.getNetTreeManagerInterface().getFreeJobs()) {
//				addUpdateJobQueue(jobImpl, persist, isNew);
//				coreFactory.getNetTreeManagerInterface().getFreeJobs().put(jobImpl.getAbstractJobType().getId(), jobImpl.getAbstractJobType().getId());
//			}
//		} else { // has dependency, if nettreemap exist, then add to that map. If not, then create new map and move all to new net tree map
//			
//			// 1. Validate dependency list  if (!DependencyOperations.validateDependencyList(logger, abstractJobTypeQueue)) {....
//			HashMap<String, AbstractJobType> abstractJobTypeQueue = JobQueueOperations.toAbstractJobTypeList(coreFactory.getMonitoringOperations().getJobQueue());
//			if (!DependencyOperations.validateDependencyList(logger, abstractJobTypeQueue)) {
//				throw new Exception("JobList.xml is dependency definitions are not  valid !");
//			}
//			// 2. Bağımlılık listesinde ağacı olan var mı kontrol et, 
//			
//			addUpdateJobQueue(jobImpl, persist, isNew);
//			
//			boolean found = false;
//			for(Item myItem : jobImpl.getAbstractJobType().getDependencyList().getItemArray()) {
//				JobImpl tmpJobImpl = coreFactory.getMonitoringOperations().getJobQueue().get(myItem.getJsId());
//				if(tmpJobImpl.getJobRuntimeProperties().getMemberIdOfNetTree() != null) {
//					jobImpl.getJobRuntimeProperties().setMemberIdOfNetTree(tmpJobImpl.getJobRuntimeProperties().getMemberIdOfNetTree());
//					ArrayList<String> netTreeMembers = coreFactory.getNetTreeManagerInterface().getNetTreeMap().get(tmpJobImpl.getJobRuntimeProperties().getMemberIdOfNetTree()).getMembers();
//					
//					coreFactory.getNetTreeManagerInterface().getFreeJobs().remove(jobImpl.getAbstractJobType().getId());
//					coreFactory.getNetTreeManagerInterface().getFreeJobs().remove(tmpJobImpl.getAbstractJobType().getId());
//					if(!netTreeMembers.contains(tmpJobImpl.getAbstractJobType().getId())) {
//						netTreeMembers.add(tmpJobImpl.getAbstractJobType().getId());
//					}
//					if(!netTreeMembers.contains(jobImpl.getAbstractJobType().getId())) {
//						netTreeMembers.add(jobImpl.getAbstractJobType().getId());
//					}
//					found = true;
//					break;
//				}
//			}
//			
//			if(!found) {
//				coreFactory.getNetTreeManagerInterface().getFreeJobs().remove(jobImpl.getAbstractJobType().getId());
//				NetTree netTree = new NetTree();
//				netTree.getMembers().add(jobImpl.getAbstractJobType().getId());
//				jobImpl.getJobRuntimeProperties().setMemberIdOfNetTree(netTree.getVirtualId());
//				
//				for(Item myItem : jobImpl.getAbstractJobType().getDependencyList().getItemArray()) {
//					JobImpl tmpJobImpl = coreFactory.getMonitoringOperations().getJobQueue().get(myItem.getJsId());
//					coreFactory.getNetTreeManagerInterface().getFreeJobs().remove(tmpJobImpl.getAbstractJobType().getId());
//					if(!netTree.getMembers().contains(tmpJobImpl.getAbstractJobType())) {
//						netTree.getMembers().add(tmpJobImpl.getAbstractJobType().getId());
//					}
//					tmpJobImpl.getJobRuntimeProperties().setMemberIdOfNetTree(netTree.getVirtualId());
//				}
//				
//				coreFactory.getNetTreeManagerInterface().getNetTreeMap().put(netTree.getVirtualId(), netTree);
//			}
//			
//		}
//
//	}
	
	public void addJob(AbstractJobType abstractJobType, boolean persist) throws Exception  {
		addUpdateJob(abstractJobType, persist, true);
	}
	
	public void updateJob(AbstractJobType abstractJobType, boolean persist)  throws Exception {
		addUpdateJob(abstractJobType, persist, false);
	}

	public void deleteJob(String jobId, boolean persist)  throws Exception {
		
		if(coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
			
			JobImpl jobImpl = coreFactory.getMonitoringOperations().getJobQueue().get(jobId);
			// if ((jobImpl.getAbstractJobType().getDependencyList() != null && jobImpl.getAbstractJobType().getDependencyList().sizeOfItemArray() != 0) || 
			if(NetTreeResolver.findMeInDeps(jobImpl.getAbstractJobType(), toMap())) { // No dependency to me
				throw new Exception("Can not delete job with dependencies :" + jobId);
			}
			
			AbstractJobType abstractJobType = null;
			
			synchronized (coreFactory.getMonitoringOperations().getJobQueue()) {
				abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().remove(jobId).getAbstractJobType();
				coreFactory.getManagementOperations().sendReIndexSignal();
			}
			
			synchronized (coreFactory.getJobListDocument().getJobList()) {
				JobQueueOperations.deleteJobType(abstractJobType, coreFactory.getJobListDocument().getJobList());
			}

			resetViewTree();
			
		} else {
			throw new Exception("Job not found with id :" + jobId);
		}
		
	}
	
//	public void deleteJob(String jobId, boolean persist)  throws Exception {
//		if(coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
//			
//			AbstractJobType abstractJobType = null;
//			
//			synchronized (coreFactory.getMonitoringOperations().getJobQueue()) {
//				abstractJobType = coreFactory.getMonitoringOperations().getJobQueue().remove(jobId).getAbstractJobType();
//				coreFactory.getManagementOperations().sendReIndexSignal();
//			}
//			
//			if (abstractJobType.getDependencyList() == null || abstractJobType.getDependencyList().sizeOfItemArray() == 0) { // No dependency free job
//				synchronized (coreFactory.getNetTreeManagerInterface().getFreeJobs()) {
//					coreFactory.getNetTreeManagerInterface().getFreeJobs().remove(abstractJobType.getId());
//				}
//			} else { // has dependency, if nettreemap exist, then remove from that map.
//				throw new UnknownServiceException("Not implemented yet !");
//			}
//			
//			synchronized (coreFactory.getJobListDocument().getJobList()) {
//				JobQueueOperations.deleteJobType(abstractJobType, coreFactory.getJobListDocument().getJobList());
//			}
//			
//		} else {
//			throw new Exception("Job not found with id :" + jobId);
//		}
//		
//	}
	
	public void enableGroup(String grpId) {
		
		HashMap<String, NetTree> netTreeMap = coreFactory.getNetTreeManagerInterface().getNetTreeMap();
		
		if(netTreeMap.containsKey(grpId)) {
			NetTree netTree = netTreeMap.get(grpId);
			synchronized (netTree) {
				boolean isOk = Commandability.isNetTreeEnablable(grpId);
				if(isOk) {
					// All jobs are ok, so enable all
					for (String jobId : netTree.getMembers()) {
						System.err.println(jobId);
						enableJob(jobId, false, true);
					}
					// All jobs are enabled, enable group at last
					netTree.setActive(true);
				}
			}
		}
		
	}
	
	public void disableGroup(String grpId) {
		
		HashMap<String, NetTree> netTreeMap = coreFactory.getNetTreeManagerInterface().getNetTreeMap();
		
		if(netTreeMap.containsKey(grpId)) {
			NetTree netTree = netTreeMap.get(grpId);
			synchronized (netTree) {
				boolean isOk = Commandability.isNetTreeDisablable(grpId);
				if (isOk) {
					// First disable group
					netTree.setActive(false);
					// All jobs are ok, so disable all
					for (String jobId : netTree.getMembers()) {
						disableJob(jobId, true);
					}
				}
			}
		}
		
	}
	
	private void resetViewTree() {
		HashMap<String, AbstractJobType> abstractJobTypeQueue = JobQueueOperations.toAbstractJobTypeList(coreFactory.getMonitoringOperations().getJobQueue());
		AbstractJobType[] abscAbstractJobTypes = abstractJobTypeQueue.values().toArray(new AbstractJobType[abstractJobTypeQueue.values().size()]);
		coreFactory.getNetTreeManagerInterface().refresh(abscAbstractJobTypes);
	}
	
	private HashMap<String, AbstractJobType> toMap() {

		HashMap<String, AbstractJobType> tmpMap = new HashMap<String, AbstractJobType>();

		for (JobImpl jobImpl : coreFactory.getMonitoringOperations().getJobQueue().values()) {
			tmpMap.put(jobImpl.getAbstractJobType().getId(), jobImpl.getAbstractJobType());
		}

		return tmpMap;
	}
	
	public ArrayList<AbstractJobType> changeGrpName(ArrayList<String> jobIdList, String newGrpName) {
		ArrayList<AbstractJobType> jobList = new ArrayList<>();
		AbstractJobType abstractJob = null;
		
		for(String jobId : jobIdList) {
			abstractJob = ((JobImpl) coreFactory.getMonitoringOperations().getJobQueue().get(jobId)).getAbstractJobType();
			abstractJob.setScenarioId(newGrpName);
			jobList.add(abstractJob);
		}
		
		resetViewTree();
		
		return jobList;
	}
	
}

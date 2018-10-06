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
package com.likya.myra.jef.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.iterators.ArrayIterator;

import com.likya.myra.commons.utils.DependencyOperations;
import com.likya.myra.commons.utils.JobListFilter;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.commons.utils.StateFilter;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.JobRuntimeProperties;
import com.likya.myra.jef.model.SortType;
import com.likya.xsd.myra.model.generics.DangerZone;
import com.likya.xsd.myra.model.generics.DangerZoneTypeDocument.DangerZoneType;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.joblist.JobListDocument;
import com.likya.xsd.myra.model.joblist.JobListDocument.JobList;
import com.likya.xsd.myra.model.jobprops.ManagementDocument.Management;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;

public class JobQueueOperations {

	public static boolean hasActiveThreads(HashMap<String, JobImpl> jobQueue) {

		Iterator<JobImpl> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			JobImpl scheduledJob = jobsIterator.next();
			Thread myExecuter = scheduledJob.getMyExecuter();
			if ((myExecuter != null) && myExecuter.isAlive()) {
				return true;
			}
		}

		return false;
	}

	public static ArrayList<SortType> createProrityIndex(HashMap<String, JobImpl> jobQueue) {

		ArrayList<SortType> jobQueueArray = new ArrayList<SortType>();

		JobImpl job = null;

		Iterator<JobImpl> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {

			job = jobsIterator.next();

			AbstractJobType abstractJobType = job.getAbstractJobType();

			SortType mySortType = new SortType(abstractJobType.getId(), abstractJobType.getBaseJobInfos().getJobPriority().intValue());
			jobQueueArray.add(mySortType);
		}

		return jobQueueArray;
	}

	public static boolean persistDisabledJobQueue(ConfigurationManager configurationManager, HashMap<String, String> jobQueue) {

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		if (jobQueue.size() == 0) {
			return true;
		}

		try {

			File fileTemp = new File(configurationManager.getFileToPersist() + "_disabled.temp"); //$NON-NLS-1$
			fos = new FileOutputStream(fileTemp);

			out = new ObjectOutputStream(fos);

			out.writeObject(jobQueue);
			out.close();

			File file = new File(configurationManager.getFileToPersist() + "_disabled");

			if (file.exists()) {
				file.delete();
			}

			fileTemp.renameTo(file);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;

	}

	public static void normalizeJobQueue(HashMap<String, JobImpl> jobQueue) {
		resetJobQueue(null, jobQueue);
		return;
	}

	public static void resetJobQueue(LiveStateInfo exceptionLiveStateInfo, HashMap<String, JobImpl> jobQueue) {

		for (JobImpl tmpJobImpl : jobQueue.values()) {

			AbstractJobType abstractJobType = tmpJobImpl.getAbstractJobType();

			if (exceptionLiveStateInfo != null && LiveStateInfoUtils.equalStates(exceptionLiveStateInfo, LiveStateInfoUtils.getLastStateInfo(abstractJobType))) {
				continue;
			}

			JobHelper.resetJob(abstractJobType);

			//			if (scheduledJob.getJobQueue() == null) {
			//				/**
			//				 * jobQueue transient olduğunudun, serialize etmiyor Recover
			//				 * ederken, bu alan null geliyor. Bu nedenle null ise yeninde
			//				 * okumak gerekiyor.
			//				 */
			//				scheduledJob.setJobQueue(jobQueue);
			//			}
		}

		return;
	}

	//	public static HashMap<String, JobImpl> transformJobQueueOld(JobListDocument jobListDocument) {
	//
	//		HashMap<String, JobImpl> jobQueue = new HashMap<String, JobImpl>();
	//
	//		int remoteSchLength = jobListDocument.getJobList().getJobRemoteSchPropertiesArray().length;
	//		int simple = jobListDocument.getJobList().getJobSimplePropertiesArray().length;
	//
	//		int length = remoteSchLength + simple;
	//
	//		Object[] objectArray = new Object[length];
	//
	//		System.arraycopy(jobListDocument.getJobList().getJobRemoteSchPropertiesArray(), 0, objectArray, 0, remoteSchLength);
	//		System.arraycopy(jobListDocument.getJobList().getJobSimplePropertiesArray(), 0, objectArray, remoteSchLength, simple);
	//
	//		ArrayIterator jobArrayIterator = new ArrayIterator(objectArray);
	//
	//		while (jobArrayIterator.hasNext()) {
	//
	//			Object retObject = jobArrayIterator.next();
	//
	//			int jobType = -1;
	//			// Deneme amaçlı
	//			if (retObject instanceof RemoteSchProperties) {
	//				jobType = JobCommandType.INT_REMOTE_SHELL;
	//			} else if (retObject instanceof SimpleProperties) {
	//				jobType = JobCommandType.INT_BATCH_PROCESS;
	//			}
	//
	//			SimpleProperties simpleProperties = (SimpleProperties) (retObject);
	//
	//			JobRuntimeInterface jobRuntimeInterface = new JobRuntimeProperties();
	//
	//			JobImpl jobImpl = null;
	//
	//			switch (jobType) {
	//			case JobCommandType.INT_BATCH_PROCESS:
	//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
	//				((ExecuteInShell) jobImpl).setShell(true);
	//				break;
	//			case JobCommandType.INT_SYSTEM_COMMAND:
	//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
	//				((ExecuteInShell) jobImpl).setShell(false);
	//				break;
	//			case JobCommandType.INT_REMOTE_SHELL:
	//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
	//				break;
	//
	//			default:
	//				break;
	//			}
	//
	//			jobQueue.put(simpleProperties.getId(), jobImpl);
	//		}
	//
	//		return jobQueue;
	//	}

	public static HashMap<String, JobImpl> transformJobQueue(JobListDocument jobListDocument) {

		HashMap<String, JobImpl> jobQueue = new HashMap<String, JobImpl>();

		Object[] objectArray = jobListDocument.getJobList().getGenericJobArray();

		ArrayIterator jobArrayIterator = new ArrayIterator(objectArray);

		while (jobArrayIterator.hasNext()) {

			AbstractJobType abstractJobType = (AbstractJobType) jobArrayIterator.next();
			
			JobImpl jobImpl = transformJobTypeToImpl(abstractJobType);

			CoreFactory.getLogger().info("Transformed " + abstractJobType.getHandlerURI() + " Job Id : " + abstractJobType.getId());

			jobQueue.put(abstractJobType.getId(), jobImpl);
		}

		return jobQueue;
	}
	
	public static JobImpl transformJobTypeToImpl(AbstractJobType abstractJobType) {

		String handlerUri = abstractJobType.getHandlerURI();

		Class<?> abstractClass;
		try {
			// abstractClass = Class.forName("com.likya.myra.jef.jobs.ExecuteInShell");
			abstractClass = Class.forName(handlerUri);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		// levelize the initial state according to trigger value

		Management management = abstractJobType.getManagement();

		JobHelper.evaluateTriggerType(abstractJobType, false);

		// remove the difference between borned and planned time 
		if(management.getTimeManagement() != null) { // USER triggered job submitted
			management.getTimeManagement().getJsActualTime().setStartTime(management.getTimeManagement().getJsScheduledTime().getStartTime());
		}
		
		JobRuntimeInterface jobRuntimeInterface = new JobRuntimeProperties();
		JobImpl jobImpl = null;

		try {
			jobImpl = (JobImpl) abstractClass.getDeclaredConstructor(new Class[] { AbstractJobType.class, JobRuntimeInterface.class }).newInstance(abstractJobType, jobRuntimeInterface);
			jobImpl.getJobInfo();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//			switch (jobType) {
		//			case JobCommandType.INT_BATCH_PROCESS:
		//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
		//				((ExecuteInShell) jobImpl).setShell(true);
		//				break;
		//			case JobCommandType.INT_SYSTEM_COMMAND:
		//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
		//				((ExecuteInShell) jobImpl).setShell(false);
		//				break;
		//			case JobCommandType.INT_REMOTE_SHELL:
		//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
		//				break;
		//
		//			default:
		//				break;
		//			}
		
		return jobImpl;
	}

	public static HashMap<String, AbstractJobType> toAbstractJobTypeList(HashMap<String, JobImpl> jobQueue) {

		HashMap<String, AbstractJobType> tmpList;
		
		while (true) {

			tmpList = new HashMap<String, AbstractJobType>();
			
			Iterator<String> jobsIterator = jobQueue.keySet().iterator();
			
			try {
				while (jobsIterator.hasNext()) {
					String jobKey = jobsIterator.next();
					tmpList.put(jobKey, jobQueue.get(jobKey).getAbstractJobType());
				}
			} catch (ConcurrentModificationException c) {
				continue;
			}
			break;
		}

		return tmpList;
	}

	public static Collection<String> getKeyList(Collection<AbstractJobType> abstractJobTypeList) {

		ArrayList<String> jobKeys = new ArrayList<>();
		Iterator<AbstractJobType> abstractJobTypeIterator = abstractJobTypeList.iterator();

		while (abstractJobTypeIterator.hasNext()) {
			AbstractJobType abstractJobType = abstractJobTypeIterator.next();
			jobKeys.add(abstractJobType.getId());
		}

		return jobKeys;
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<AbstractJobType> getJobList(HashMap<String, AbstractJobType> abstractJobTypeList, Predicate predicate) {
		
		Collection<AbstractJobType> filteredList = CollectionUtils.select(abstractJobTypeList.values(), predicate);
		
		return filteredList;
	}
	
	public static Collection<AbstractJobType> getJobListForImpl(HashMap<String, JobImpl> jobQueue, Predicate predicate) {
		
		HashMap<String, AbstractJobType> abstractJobTypeList = JobQueueOperations.toAbstractJobTypeList(jobQueue);
		
		return getJobList(abstractJobTypeList, predicate);
	}
	
	public static Collection<AbstractJobType> getJobList(HashMap<String, String> jobIdList, StateName.Enum filterStates[]) {
		
		JobListFilter jobListFilter = new StateFilter(filterStates);
		
		return getJobList(getSubset(jobIdList), jobListFilter.anyPredicate());
	}
	
	public static Collection<AbstractJobType> getJobListForImpl(HashMap<String, JobImpl> jobQueue, StateName.Enum filterStates[]) {
		
		JobListFilter jobListFilter = new StateFilter(filterStates);
		
		return getJobListForImpl(jobQueue, jobListFilter.anyPredicate());
	}
	
	public static Collection<AbstractJobType> getJobList(ArrayList<String> jobIdList, StateName.Enum filterStates[]) {

		JobListFilter jobListFilter = new StateFilter(filterStates);

		return getJobList(getSubsetList(jobIdList), jobListFilter.anyPredicate());
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<AbstractJobType> getJobList(ArrayList<AbstractJobType> abstractJobTypeList, Predicate predicate) {

		Collection<AbstractJobType> filteredList = CollectionUtils.select(abstractJobTypeList, predicate);

		return filteredList;
	}
	
	public static int indexOfJobType(AbstractJobType newAbstractJobType, JobList jobList) {
		
		int index = 0;
		boolean found = false;
		
		for(AbstractJobType abstractJobType : jobList.getGenericJobArray()) {
			// System.out.println(abstractJobType.getId());
			// System.out.println(newAbstractJobType.getId());
			if(abstractJobType.getId().equals(newAbstractJobType.getId())) {
				found = true;
				break;
			}
			++index;
		}

		return found ? index:-1;
	}
	
	public static boolean updateJobType(AbstractJobType newAbstractJobType, JobList jobList) {
		
		int index = indexOfJobType(newAbstractJobType, jobList);
		
		if(index < 0) return false;
		
		jobList.getGenericJobArray(index).set(newAbstractJobType);
		
		return true;
	}
	
	public static boolean deleteJobType(AbstractJobType newAbstractJobType, JobList jobList) {
		
		int index = indexOfJobType(newAbstractJobType, jobList);
		
		jobList.removeGenericJob(index);

		return true;
	}
	
	public static ArrayList<AbstractJobType> getSubset(HashMap<String, String> jobIdList) {
		
		ArrayList<AbstractJobType> abstractJobTypeList = new ArrayList<AbstractJobType>();
		
		for(String jobId : jobIdList.keySet()) {
			AbstractJobType tmpAbstractJobType = CoreFactory.getInstance().getMonitoringOperations().getJobQueue().get(jobId).getAbstractJobType();
			abstractJobTypeList.add(tmpAbstractJobType);
		}
		
		return abstractJobTypeList;
	}

	public static ArrayList<AbstractJobType> getSubsetList(ArrayList<String> jobIdList) {
		
		ArrayList<AbstractJobType> abstractJobTypeList = new ArrayList<AbstractJobType>();
		
		for(String jobId : jobIdList) {
			AbstractJobType tmpAbstractJobType = CoreFactory.getInstance().getMonitoringOperations().getJobQueue().get(jobId).getAbstractJobType();
			abstractJobTypeList.add(tmpAbstractJobType);
		}
		
		return abstractJobTypeList;
	}
	
	public static boolean isMeFree(AbstractJobType me) {
		
		if(me.getDependencyList() != null && me.getDependencyList().sizeOfItemArray() != 0) {
			return false;
		}
		
		HashMap<String, JobImpl> jobQueue = CoreFactory.getInstance().getMonitoringOperations().getJobQueue();
		HashMap<String, AbstractJobType> abstractJobTypeList = JobQueueOperations.toAbstractJobTypeList(jobQueue);
		ArrayList<AbstractJobType> dependentList = DependencyOperations.getDependencyList(abstractJobTypeList, me.getId());
		
		if(dependentList != null && dependentList.size() != 0) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param jobId
	 * @return null if free job
	 */
	public static NetTree getNetTree(String jobId) {
		
		if(CoreFactory.getInstance().getNetTreeManagerInterface().getFreeJobs().containsKey(jobId)) {
			return null;
		}
		
		HashMap<String, NetTree> netTreeMap = CoreFactory.getInstance().getNetTreeManagerInterface().getNetTreeMap();

		/**
		 * During the iteration, the content of the map may change. This change is not so important
		 * for a small interval of time, so the copy of the object is used as snapshot of the map for the iteration
		 */
		
		try {
			for (NetTree netTree : netTreeMap.values()) {
				for (String tmpId : netTree.getMembers()) {
					if (tmpId.equals(jobId)) {
						return netTree;
					}
				}
			}
		} catch (ConcurrentModificationException e) {
			// DO NOTHING
		}
		
		return null;
	}
	
	public static String getNetTreeId(String jobId) {
		
		NetTree netTreeId = getNetTree(jobId);
		
		return netTreeId == null ? null : netTreeId.getVirtualId();
	}
	
	public static HashMap<String, DangerZone> fetchListForDangerZone(HashMap<String, JobImpl> jobQueue) {

		HashMap<String, DangerZone> dzList;
		String groupId;
		
		while (true) {

			dzList = new HashMap<String, DangerZone>();
			
			Iterator<String> jobsIterator = jobQueue.keySet().iterator();
			
			try {
				while (jobsIterator.hasNext()) {
					String jobKey = jobsIterator.next();
					
					DangerZone dzJob = DangerZone.Factory.newInstance();
					dzJob.setDangerZoneType(DangerZoneType.JOB);
					dzJob.setDangerZoneId(jobKey);
					dzList.put(jobKey, dzJob);
					
					groupId = jobQueue.get(jobKey).getAbstractJobType().getGroupId();
					if(groupId != null && !"".equals(groupId)) {
						DangerZone dzGrp = DangerZone.Factory.newInstance();
						dzGrp.setDangerZoneType(DangerZoneType.GROUP);
						dzGrp.setDangerZoneId(groupId);
						dzList.put(groupId, dzGrp);
					}
				}
			} catch (ConcurrentModificationException c) {
				continue;
			}
			break;
		}

		return dzList;
	}

}

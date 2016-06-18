package com.likya.myra.jef.core;

import java.util.HashMap;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class Commandability {

	public static boolean isRetryable(JobImpl myJob) {
		return LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED) || LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.FINISHED, SubstateName.STOPPED);
	}

	public static boolean isSuccessable(JobImpl myJob) {
		return LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING, SubstateName.PAUSED) || LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED);
	}

	public static boolean isSkipable(JobImpl myJob) {
		return LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING, SubstateName.PAUSED) || LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED);
	}

	public static boolean isStopable(JobImpl myJob) {
		return LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.RUNNING);
	}

	public static boolean isPausable(JobImpl myJob) {
		return LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING);
	}

	public static boolean isResumable(JobImpl myJob) {
		return LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING, SubstateName.PAUSED);
	}

	public static boolean isStartable(JobImpl myJob) {
		return LiveStateInfoUtils.equalStates(JobHelper.getLastStateInfo(myJob), StateName.PENDING) && (myJob.getAbstractJobType().getDependencyList() == null || myJob.getAbstractJobType().getDependencyList().sizeOfItemArray() == 0);
	}
	
	public static boolean isRetryable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED) || LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.FINISHED, SubstateName.STOPPED);
	}

	public static boolean isSuccessable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING, SubstateName.PAUSED) || LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED);
	}

	public static boolean isSkipable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING, SubstateName.PAUSED) || LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED);
	}

	public static boolean isStopable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.RUNNING);
	}

	public static boolean isPausable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING);
	}

	public static boolean isResumable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING, SubstateName.PAUSED);
	}

	public static boolean isStartable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING) && (abstractJobType.getDependencyList() == null || abstractJobType.getDependencyList().sizeOfItemArray() == 0);
	}

	/**
	 * Rule of isDisablableForFree :
	 * 1. Job must be free job
	 * 2. Job StateName == StateName.PENDING 
	 * 3. !(Job StateName == StateName.PENDING && SubStateName == SubStateName.DEACTIVATED) zaten deactive olmayan bir job olmalı
	 * 
	 * @param abstractJobType
	 * @return isDisablable
	 */
	public static boolean isDisablableForFree(AbstractJobType abstractJobType) {
		boolean isMeFree = JobQueueOperations.isMeFree(abstractJobType);
		return isMeFree && LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING) && !LiveStateInfoUtils.equalStatesPD(abstractJobType);
	}
	
	/**
	 * Rule of isEnablableForFree :
	 * 1. Job must be free job
	 * 2. Job StateName == StateName.PENDING  and SubStateName == SubStateName.DEACTIVATED
	 * 
	 * @param abstractJobType
	 * @return isEnablable
	 */
	public static boolean isEnablableForFree(AbstractJobType abstractJobType) {
		boolean isMeFree = JobQueueOperations.isMeFree(abstractJobType);
		return isMeFree && LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING, SubstateName.DEACTIVATED);
	}

	/**
	 * Rule of isDisablableForGroup :
	 * 1. Job StateName == StateName.PENDING 
	 * 
	 * @param abstractJobType
	 * @return isDisablable
	 */
	public static boolean isDisablableForGroup(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING);
	}
	
	/**
	 * Rule of isEnablableForGroup :
	 * 1. Job StateName == StateName.PENDING  and SubStateName == SubStateName.DEACTIVATED
	 * 
	 * @param abstractJobType
	 * @return isEnablable
	 */
	public static boolean isEnablableForGroup(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING, SubstateName.DEACTIVATED);
	}
	
	public static boolean isNetTreeEnablable(String netTreeId) {
		
		HashMap<String, NetTree> netTreeMap = CoreFactory.getInstance().getNetTreeManagerInterface().getNetTreeMap();
		HashMap<String, JobImpl> jobQueue = CoreFactory.getInstance().getMonitoringOperations().getJobQueue();
		NetTree netTree = netTreeMap.get(netTreeId);
		for (String jobId : netTree.getMembers()) {
			AbstractJobType abstractJobType = jobQueue.get(jobId).getAbstractJobType();
			if (!Commandability.isEnablableForGroup(abstractJobType)) {
				// Group contains a job that is not available for EnablableForGroup 
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isNetTreeDisablable(String netTreeId) {
		
		HashMap<String, NetTree> netTreeMap = CoreFactory.getInstance().getNetTreeManagerInterface().getNetTreeMap();
		HashMap<String, JobImpl> jobQueue = CoreFactory.getInstance().getMonitoringOperations().getJobQueue();
		NetTree netTree = netTreeMap.get(netTreeId);
		for (String jobId : netTree.getMembers()) {
			AbstractJobType abstractJobType = jobQueue.get(jobId).getAbstractJobType();
			if (!Commandability.isDisablableForGroup(abstractJobType)) {
				// Group contains a job that is not available for EnablableForGroup 
				return false;
			}
		}
		
		return true;
	}
}

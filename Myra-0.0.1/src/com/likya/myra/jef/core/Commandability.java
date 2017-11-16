package com.likya.myra.jef.core;

import java.util.HashMap;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class Commandability {

	/**
	 * isPausable, isResumable, isRetryable, isSkipable, isStartable, isStopable, isSuccessable, isDisablable, isEnablable
	 */
	
	public static boolean isPausable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING) && !LiveStateInfoUtils.eq_PENDING_PAUSED(abstractJobType);
	}

	public static boolean isResumable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.eq_PENDING_PAUSED(abstractJobType);
	}

	public static boolean isRetryable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.eq_FINISHED_COMPLETED_FAILED(abstractJobType) || LiveStateInfoUtils.eq_FINISHED_STOPPED(abstractJobType);
	}

	public static boolean isSkipable(AbstractJobType abstractJobType) {
		boolean isMeFree = JobQueueOperations.isMeFree(abstractJobType);
		return !isMeFree && (LiveStateInfoUtils.eq_PENDING_PAUSED(abstractJobType) || LiveStateInfoUtils.eq_FINISHED_COMPLETED_FAILED(abstractJobType) || LiveStateInfoUtils.eq_FINISHED_STOPPED(abstractJobType));
	}

	public static boolean isStartable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING) && (abstractJobType.getDependencyList() == null || abstractJobType.getDependencyList().sizeOfItemArray() == 0);
	}

	public static boolean isStopable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.RUNNING);
	}

	public static boolean isSuccessable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.eq_FINISHED_COMPLETED_FAILED(abstractJobType) || LiveStateInfoUtils.eq_PENDING_PAUSED(abstractJobType) || LiveStateInfoUtils.eq_FINISHED_STOPPED(abstractJobType);
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
		return isMeFree && (LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING) || LiveStateInfoUtils.eq_FINISHED_COMPLETED_FAILED(abstractJobType) || LiveStateInfoUtils.eq_FINISHED_STOPPED(abstractJobType)) && !LiveStateInfoUtils.equalStatesPD(abstractJobType);
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
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING) || LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED);
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

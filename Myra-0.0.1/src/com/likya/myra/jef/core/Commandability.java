package com.likya.myra.jef.core;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.jobs.JobImpl;
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

	public static boolean isDisablable(AbstractJobType abstractJobType) {
		return !isEnablable(abstractJobType) && isStartable(abstractJobType);
	}
	
	public static boolean isEnablable(AbstractJobType abstractJobType) {
		return LiveStateInfoUtils.equalStates(LiveStateInfoUtils.getLastStateInfo(abstractJobType), StateName.PENDING, SubstateName.DEACTIVATED);
	}

}

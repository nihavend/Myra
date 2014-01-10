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
package com.likya.myra.jef.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.likya.myra.commons.model.UnresolvedDependencyException;
import com.likya.myra.commons.utils.JobDependencyResolver;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.commons.utils.PeriodCalculations;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.CoreFactoryInterface;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.xsd.myra.model.config.MyraConfigDocument.MyraConfig;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.DependencyListDocument.DependencyList;
import com.likya.xsd.myra.model.jobprops.SensInfoDocument.SensInfo;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.wlagen.ItemDocument.Item;

public class BaseSchedulerController {

	protected HashMap<String, JobImpl> jobQueue;

	protected HashMap<String, String> disabledJobQueue;

	protected boolean executionPermission = true;

	protected int cycleFrequency = 1000;

	private boolean thresholdOverflow = false;

	private boolean isPersistent = false;

	protected CoreFactoryInterface coreFactoryInterface;

	public BaseSchedulerController(CoreFactoryInterface coreFactoryInterface, HashMap<String, JobImpl> jobQueue) {
		super();
		this.coreFactoryInterface = coreFactoryInterface;
		this.jobQueue = jobQueue;
		this.isPersistent = coreFactoryInterface.getConfigurationManager().getMyraConfig().getPersistent();
	}

	protected boolean hasTimeCome(AbstractJobType abstractJobType) {

		Date scheduledTime = abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime().getTime();
		Date currentTime = Calendar.getInstance().getTime();

		if (scheduledTime.before(currentTime)) {
			return true;
		}

		return false;
	}

	protected boolean isTimeSensitive(DependencyList dependencyList) {

		SensInfo sensInfo = dependencyList.getSensInfo();

		if (sensInfo != null && sensInfo.getSensTime() != null) {
			return true;
		}

		return false;

	}
	
	/**
	 * @param dependencyArray
	 * @return true if at least one of the dependent jobs's status is different from PENDING-IDLED
	 */
	protected boolean atLeastOneParentNOTPI(AbstractJobType abstractJobType, String indent) {
		
		// System.err.println(indent + " Checking for job " + abstractJobType.getId());
		
		DependencyList dependencyList = abstractJobType.getDependencyList();

		boolean isFound = false;
		
		for (Item item : dependencyList.getItemArray()) {
			AbstractJobType innerAbstractJobType = jobQueue.get(item.getJsId()).getAbstractJobType();
			indent = "	" + indent;
			// System.err.println(indent + " Checking for inner job " + innerAbstractJobType.getId());
			if(innerAbstractJobType.getDependencyList() != null && innerAbstractJobType.getDependencyList().sizeOfItemArray() > 0) {
				// System.err.println(indent + " Has dependency, recursing... ");
				if(atLeastOneParentNOTPI(innerAbstractJobType, "	" + indent)) {
					isFound = true;
					// System.err.println(indent + " Evraka ");
					break;
				}
			} else {
				// System.err.println(indent + " No dependency ");
				LiveStateInfo innerLiveStateInfo = JobHelper.getLastStateInfo(innerAbstractJobType);
				if (!LiveStateInfoUtils.equalStates(innerLiveStateInfo, StateName.PENDING, SubstateName.IDLED)) {
					isFound = true;
					// System.err.println(indent + " Evraka ");
					break;
				}
			}
		}
		
		if(isFound) {
			// System.err.println(indent + " found case for job " + abstractJobType.getId() + " Setting PRI !");
			LiveStateInfo liveStateInfo = JobHelper.getLastStateInfo(abstractJobType);
			liveStateInfo.setSubstateName(SubstateName.READY);
			liveStateInfo.setStatusName(StatusName.WAITING);
		}			

		return isFound;
	}

	/**
	 * @param dependencyArray
	 * @return the maximum date of the dependent jobs
	 */
	protected Calendar getMaxBaseDate(Item[] dependencyArray) {

		Calendar maxBaseDate = jobQueue.get(dependencyArray[0].getJsId()).getAbstractJobType().getManagement().getTimeManagement().getJsRealTime().getStartTime();

		// System.err.println("Current : " + MyraDateUtils.getDate(maxBaseDate.getTime()));
		for (Item item : dependencyArray) {
			AbstractJobType depJob = jobQueue.get(item.getJsId()).getAbstractJobType();
			Calendar startTime = depJob.getManagement().getTimeManagement().getJsRealTime().getStartTime();
			// System.err.println("Dep Job : " + item.getJsId() + " Time " + MyraDateUtils.getDate(maxBaseDate.getTime()));
			if (startTime.after(maxBaseDate)) {
				maxBaseDate = startTime;
			}
		}

		// System.err.println("After : " + MyraDateUtils.getDate(maxBaseDate.getTime()));

		return maxBaseDate;

	}

	/**
	 * Calculates the new time according to the time sensitivity parameters
	 * 
	 * @param abstractJobType
	 * @param dependencyList
	 */
	protected void handleTimeSensitivity(AbstractJobType abstractJobType, DependencyList dependencyList) {
		Calendar newTime = getMaxBaseDate(dependencyList.getItemArray());
		abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().setStartTime(newTime);
		SensInfo sensInfo = dependencyList.getSensInfo();
		System.err.println("Before : " + MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime().getTime()));
		JobHelper.setJsPlannedTimeForStart(abstractJobType, PeriodCalculations.getDurationInMilliSecs(sensInfo.getSensTime().getDelay()));
		System.err.println("After : " + MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime().getTime()));
	}

	protected boolean checkDependency(JobImpl meJob, DependencyList dependencyList) throws UnresolvedDependencyException {

		if (dependencyList == null || dependencyList.getItemArray().length == 0) {
			return true;
		}

		String dependencyExpression = dependencyList.getDependencyExpression().trim().toUpperCase();

		Item[] dependencyArray = dependencyList.getItemArray();

		boolean retValue = JobDependencyResolver.isResolved(CoreFactory.getLogger(), meJob.getAbstractJobType(), dependencyExpression, dependencyArray, JobQueueOperations.toAbstractJobTypeList(jobQueue));

		return retValue;

	}

	protected void executeJob(JobImpl scheduledJob) throws InterruptedException {

		if (!checkDangerGroupZoneIntrusion(scheduledJob)) {
			// schedulerLogger.debug("Grup kısıtı nedeni ile çalışmıyor ! ==> " + scheduledJob.getJobProperties().getKey());
			return;
		}

		//		if (getScenarioRuntimeProperties().getCurrentState() == ScenarioRuntimeProperties.STATE_WAITING) {
		//			getScenarioRuntimeProperties().setCurrentState(ScenarioRuntimeProperties.STATE_RUNNING);
		//			getScenarioRuntimeProperties().setStartTime(Calendar.getInstance().getTime());
		//		}

		CoreFactory.getLogger().debug(CoreFactory.getMessage("Myra.66"));
		//		logger.debug(scheduledJob.getJobProperties().toString());

		LiveStateInfoUtils.insertNewLiveStateInfo(scheduledJob.getAbstractJobType(), StateName.INT_RUNNING, SubstateName.INT_ON_RESOURCE, StatusName.INT_TIME_IN);

		Thread starterThread = new Thread(scheduledJob);
		//		if (scheduledJob.getJobProperties().isManuel()) {
		//			starterThread.setName("TlosLite-M-" + scheduledJob.getJobProperties().getKey());
		//		} else {
		//			starterThread.setName("TlosLite-S-" + scheduledJob.getJobProperties().getKey());
		//		}
		scheduledJob.setMyExecuter(starterThread);
		// // starterThread.setDaemon(true);
		// starterThread.start();
		scheduledJob.getMyExecuter().start();

		return;
	}

	//	protected void executeRepetitiveJob(Job scheduledJob) throws InterruptedException {
	//
	//		/**
	//		 * Tekrarlı işlerin yönetimi kendi Thread'leri içinden olacak. Başlama
	//		 * ve bitiş koşulları kendi içinden yönetilecek.
	//		 * 
	//		 * @author serkan taş
	//		 * @date 26.03.2011
	//		 */
	//
	//		int jobStatus = scheduledJob.getJobProperties().getStatus();
	//
	//		if (scheduledJob.getMyExecuter() == null && (jobStatus != JobProperties.FAIL && jobStatus != JobProperties.DISABLED)) {
	//			Thread starterThread = new Thread(scheduledJob);
	//			starterThread.setName("TlosLite-T-" + scheduledJob.getJobProperties().getKey());
	//			scheduledJob.setMyExecuter(starterThread);
	//			scheduledJob.getMyExecuter().start();
	//		}
	//		return;
	//	}

	//	protected void executeManuelJob(Job scheduledJob) throws InterruptedException {
	//
	//		/**
	//		 * Manuel iş başlatılması hk.
	//		 * 
	//		 * @author serkan taş
	//		 * @date 28.02.2013
	//		 */
	//
	//		if (scheduledJob.getJobProperties().getStatus() == JobProperties.MSTART) {
	//			executeJob(scheduledJob);
	//		}
	//
	//		return;
	//	}

	protected synchronized boolean checkThresholdOverflow() {

		MyraConfig myraConfig = coreFactoryInterface.getConfigurationManager().getMyraConfig();

		int lowerLimit = myraConfig.getLowerThreshold();
		int higherLimit = myraConfig.getHigherThreshold();

		if (lowerLimit >= higherLimit) {
			return false;
		}

		int numOfActiveJobs = getNumOfActiveJobs();

		if ((!thresholdOverflow) && (numOfActiveJobs >= higherLimit)) {
			CoreFactory.getLogger().info(CoreFactory.getMessage("Myra.68") + numOfActiveJobs + CoreFactory.getMessage("Myra.69") + lowerLimit);
			thresholdOverflow = true;
		} else if (thresholdOverflow && (numOfActiveJobs <= lowerLimit)) {
			thresholdOverflow = false;
			CoreFactory.getLogger().info(CoreFactory.getMessage("Myra.70") + numOfActiveJobs);
		}

		// System.out.println("lowerLimit : " + lowerLimit + " higherLimit : " +
		// higherLimit + " numOfActiveJobs : " + numOfActiveJobs +
		// " thresholdOverflow : " + thresholdOverflow);

		return thresholdOverflow;

	}

	public int getNumOfActiveJobs() {

		int numOfWorkingJobs = 0; // getNumOfJobsInStatus(JobProperties.WORKING);
		int numOfTimeoutJobs = 0; // getNumOfJobsInStatus(JobProperties.TIMEOUT);

		return numOfWorkingJobs + numOfTimeoutJobs;
	}

	public int getNumOfJobsInStatus(int status) {

		int counter = 0;

		Iterator<JobImpl> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {
			//			JobImpl scheduledJob = jobsIterator.next();
			//			if (scheduledJob.getJobProperties().getStatus() == status) {
			//				counter += 1;
			//			}

		}

		return counter;
	}

	public static boolean checkDangerGroupZoneIntrusion(JobImpl currentJob) {

		//		JobProperties currentJobProperties = currentJob.getJobProperties();
		//
		//		for (Job myJob : jobQueue.values()) {
		//			JobProperties myJobProperties = myJob.getJobProperties();
		//			if (currentJobProperties.getDangerZoneGroup() != null && currentJobProperties.getDangerZoneGroup().equals(myJobProperties.getDangerZoneGroup()) && (myJobProperties.getStatus() == JobProperties.WORKING)) {
		//				return false;
		//			}
		//		}
		return true;
	}

	public HashMap<String, JobImpl> getJobQueue() {
		return jobQueue;
	}

	public HashMap<String, String> getDisabledJobQueue() {
		return disabledJobQueue;
	}

	public void setExecutionPermission(boolean executionPermission) {
		this.executionPermission = executionPermission;
	}

	public void setCycleFrequency(int cycleFrequency) {
		this.cycleFrequency = cycleFrequency;
	}

	public boolean isThresholdOverflow() {
		return thresholdOverflow;
	}

	public boolean isPersistent() {
		return isPersistent;
	}
}

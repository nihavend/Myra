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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;

import com.likya.myra.commons.model.UnresolvedDependencyException;
import com.likya.myra.commons.utils.JobDependencyResolver;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.StateFilter;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.CoreFactoryInterface;
import com.likya.myra.jef.jobs.ChangeLSI;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.myra.jef.utils.timeschedules.TimeScheduler;
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
	
	private boolean reIndexJobQueue = false;

	protected CoreFactoryInterface coreFactoryInterface;

	public BaseSchedulerController(CoreFactoryInterface coreFactoryInterface, HashMap<String, JobImpl> jobQueue) {
		super();
		this.coreFactoryInterface = coreFactoryInterface;
		this.jobQueue = jobQueue;
		this.isPersistent = coreFactoryInterface.getConfigurationManager().getMyraConfig().getPersistent();
	}

	protected boolean hasTimeCome(AbstractJobType abstractJobType) {

		Date scheduledTime = abstractJobType.getManagement().getTimeManagement().getJsActualTime().getStartTime().getTime();
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
	 * 
	 * @param abstractJobType
	 * @param indent
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
			if (innerAbstractJobType.getDependencyList() != null && innerAbstractJobType.getDependencyList().sizeOfItemArray() > 0) {
				// System.err.println(indent + " Has dependency, recursing... ");
				if (atLeastOneParentNOTPI(innerAbstractJobType, "	" + indent)) {
					isFound = true;
					// System.err.println(indent + " Evraka ");
					break;
				}
			} else {
				// System.err.println(indent + " No dependency ");
				LiveStateInfo innerLiveStateInfo = LiveStateInfoUtils.getLastStateInfo(innerAbstractJobType);

				boolean isState = LiveStateInfoUtils.equalStates(innerLiveStateInfo, StateName.PENDING, SubstateName.IDLED);

				if (!isState) {
					isFound = true;
					// System.err.println(indent + " Evraka ");
					break;
				}
			}
		}

		if (isFound && LiveStateInfoUtils.equalStatesPIT(LiveStateInfoUtils.getLastStateInfo(abstractJobType))) {
			// System.err.println(indent + " found case for job " + abstractJobType.getId() + " Setting PRI !");
			LiveStateInfo liveStateInfo = LiveStateInfoUtils.getLastStateInfo(abstractJobType);
			liveStateInfo.setSubstateName(SubstateName.READY);
			liveStateInfo.setStatusName(StatusName.WAITING);
		}

		return isFound;
	}

	/**
	 * @param dependencyArray
	 * @return the maximum end date of the dependent jobs
	 */
	protected Calendar getMaxBaseDate(Item[] dependencyArray) {

		Calendar maxBaseDate = jobQueue.get(dependencyArray[0].getJsId()).getAbstractJobType().getManagement().getTimeManagement().getJsRecordedTime().getStopTime();

		// System.err.println("Current : " + MyraDateUtils.getDate(maxBaseDate.getTime()));
		for (Item item : dependencyArray) {
			AbstractJobType depJob = jobQueue.get(item.getJsId()).getAbstractJobType();
			Calendar stopTime = depJob.getManagement().getTimeManagement().getJsRecordedTime().getStopTime();
			// System.err.println("Dep Job : " + item.getJsId() + " Time " + MyraDateUtils.getDate(maxBaseDate.getTime()));
			if (stopTime.after(maxBaseDate)) {
				maxBaseDate = stopTime;
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
		// System.err.println("Before : " + MyraDateUtils.getDate(newTime.getTime()));
		SensInfo sensInfo = dependencyList.getSensInfo();
		// System.err.println("Before : " + MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime().getTime()));
		String timeZone = abstractJobType.getManagement().getTimeManagement().getTimeZone();
		Calendar returnCal = TimeScheduler.addPeriod(newTime, TimeScheduler.getDurationInMilliSecs(sensInfo.getSensTime().getDelay()), timeZone);
		abstractJobType.getManagement().getTimeManagement().getJsActualTime().setStartTime(returnCal);
		// System.err.println("After : " + MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime().getTime()));
	}

	protected boolean checkDependency(JobImpl currentJob) throws UnresolvedDependencyException {

		boolean retValue = JobDependencyResolver.isResolved(CoreFactory.getLogger(), currentJob.getAbstractJobType(), JobQueueOperations.toAbstractJobTypeList(jobQueue));

		return retValue;

	}

	protected void executeJob(JobImpl scheduledJob) throws InterruptedException {

		if (inDangerGroupZoneIntrusion(scheduledJob)) {
			CoreFactory.getLogger().debug("Tehlikeli Grup kısıtı nedeni ile çalışmıyor ! ==> " + scheduledJob.getAbstractJobType().getId());
			return;
		}

		//		if (getScenarioRuntimeProperties().getCurrentState() == ScenarioRuntimeProperties.STATE_WAITING) {
		//			getScenarioRuntimeProperties().setCurrentState(ScenarioRuntimeProperties.STATE_RUNNING);
		//			getScenarioRuntimeProperties().setStartTime(Calendar.getInstance().getTime());
		//		}

		ChangeLSI.forValue(scheduledJob.getAbstractJobType(), StateName.RUNNING, SubstateName.STAGE_IN);

		CoreFactory.getLogger().debug(CoreFactory.getMessage("Myra.66"));

		Thread starterThread = new Thread(scheduledJob);

		scheduledJob.setMyExecuter(starterThread);
		starterThread.setDaemon(true);

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

		if (lowerLimit > higherLimit) {
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

	@SuppressWarnings("unchecked")
	public int getNumOfActiveJobs() {

		StateName.Enum filterStates[] = { StateName.RUNNING };

		HashMap<String, AbstractJobType> abstractJobTypeList = JobQueueOperations.toAbstractJobTypeList(jobQueue);

		Collection<AbstractJobType> filteredList;

		int listSize = 0;

		try {
			filteredList = CollectionUtils.select(abstractJobTypeList.values(), new StateFilter(filterStates).anyPredicate());
			listSize = filteredList.size();
		} catch (NullPointerException n) {
			n.printStackTrace();
		}
		// System.err.println("filteredList.size() : " + filteredList.size());

		return listSize;
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

	public boolean inDangerGroupZoneIntrusion(JobImpl currentJob) {

		AbstractJobType myAbstractJobType = currentJob.getAbstractJobType();

		String dangerZoneGroupId = myAbstractJobType.getDangerZoneGroupId();

		if (dangerZoneGroupId == null) {
			return false;
		}

		for (JobImpl tmpJobImpl : jobQueue.values()) {

			AbstractJobType tmpAbstractJobType = tmpJobImpl.getAbstractJobType();

			if (tmpAbstractJobType.getId().equals(myAbstractJobType.getId())) {
				// self intrusion, discarding
				continue;
			}

			if (dangerZoneGroupId.equals(tmpAbstractJobType.getDangerZoneGroupId())) {
				// System.err.println("Job " + myAbstractJobType.getId() + " and " + tmpAbstractJobType.getId() + " are in same group >> " + tmpAbstractJobType.getDangerZoneGroupId());
				// System.err.println("Job " + myAbstractJobType.getId() + " statu : " + myAbstractJobType.getStateInfos());
				// System.err.println("Job " +  tmpAbstractJobType.getId() + " statu : " + tmpAbstractJobType.getStateInfos());
				if (StateName.RUNNING.equals(LiveStateInfoUtils.getLastStateInfo(tmpAbstractJobType).getStateName())) {
					// System.err.println("Can not execute !!!!!!!!!!!");
					return true;
				}
			}
		}

		return false;

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
	
	public boolean isReIndexJobQueue() {
		return reIndexJobQueue;
	}

	public void setReIndexJobQueue(boolean reIndexJobQueue) {
		this.reIndexJobQueue = reIndexJobQueue;
	}
}

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

import java.util.HashMap;
import java.util.Iterator;

import com.likya.myra.commons.model.UnresolvedDependencyException;
import com.likya.myra.commons.utils.JobDependencyResolver;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.CoreFactoryInterface;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.xsd.myra.model.config.MyraConfigDocument.MyraConfig;
import com.likya.xsd.myra.model.jobprops.DependencyListDocument.DependencyList;
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

	protected CoreFactoryInterface coreFactoryInterface;

	public BaseSchedulerController(CoreFactoryInterface coreFactoryInterface, HashMap<String, JobImpl> jobQueue) {
		super();
		this.coreFactoryInterface = coreFactoryInterface;
		this.jobQueue = jobQueue;
	}

	
	protected void cleanUpQueueIssues() {

//		// Hepsi T-tekrarlı ise ve blocker değilse check yapma direk çık
//		// Hepsi S-standart veya S-T Karışık ise
//		// 1. T-ler için blocker değilse hiç bir şey yapma
//		// 2. S-ler için if(Hepsi Non-Blocker ise succ-skip-fail ise reset) else
//		// (succ-skip reset)
//
//		boolean resetQueue = false;
//
//		boolean isAllRepetitive = JobQueueOperations.isAllRepetitive(jobQueue);
//		boolean isAllDisabled = JobQueueOperations.isAllDisabled(jobQueue);
//
//		boolean isAllStandart = JobQueueOperations.isAllStandart(jobQueue);
//		// boolean isAllStandartBlocker =
//		// JobQueueOperations.isAllStandartBlocker(jobQueue);
//		boolean isAllStandartNonBlocker = JobQueueOperations.isAllStandartNonBlocker(jobQueue);
//
//		boolean isAllSuccessOrSkip = JobQueueOperations.isAllSuccessOrSkip(jobQueue);
//		boolean isAllSuccessOrSkipOrFail = JobQueueOperations.isAllSuccessOrSkipOrFail(jobQueue);
//
//		if (isAllRepetitive || isAllDisabled) {
//			// Do nothing
//		} else if (isAllStandart) {
//			// S-ler için if(Hepsi Non-Blocker ise succ-skip-fail ise reset)
//			// else (succ-skip reset)
//			if (isAllStandartNonBlocker) {
//				if (isAllSuccessOrSkipOrFail) {
//					// reset queue
//					resetQueue = true;
//				}
//			} else {
//				// blocker ise için success fail ise true
//				// non-blocker ise success fail skip ise true
//				if (isAllSuccessOrSkip) {
//					// reset
//					resetQueue = true;
//				}
//			}
//		} else {
//			Iterator<Job> jobsIterator = jobQueue.values().iterator();
//
//			while (jobsIterator.hasNext()) {
//				Job scheduledJob = jobsIterator.next();
//
//				int myJobsStatus = scheduledJob.getJobProperties().getStatus();
//
//				if (myJobsStatus != JobProperties.DISABLED) {
//
//					if (scheduledJob instanceof RepetitiveExternalProgram) {
//						if (!scheduledJob.getJobProperties().isBlocker()) {
//							// do nothing
//						} else {
//							if (isAllSuccessOrSkip) {
//								// reset queue
//								resetQueue = resetQueue || true;
//							} else {
//								resetQueue = resetQueue && false;
//							}
//						}
//					} else if (scheduledJob instanceof ExternalProgram) {
//						if (isAllStandartNonBlocker) {
//							if (isAllSuccessOrSkip) {
//								// reset queue
//								resetQueue = resetQueue || true;
//							} else {
//								resetQueue = resetQueue && false;
//							}
//						} else {
//							// blocker ise için success fail ise true
//							// non-blocker ise success fail skip ise true
//							if (isAllSuccessOrSkip) {
//								// reset
//								resetQueue = resetQueue || true;
//							} else {
//								resetQueue = resetQueue && false;
//							}
//						}
//					}
//
//				}
//			}

//		if (resetQueue) {
//			JobQueueOperations.resetJobQueue(jobQueue);
//			getScenarioRuntimeProperties().setCurrentState(ScenarioRuntimeProperties.STATE_WAITING);
//			getScenarioRuntimeProperties().setEndTime(Calendar.getInstance().getTime());
//			if (tlosParameters.isNormalizable()) {
//				schedulerLogger.info(LocaleMessages.getString("TlosServer.43"));
//				JobQueueOperations.normalizeJobQueue(jobQueue);
//				schedulerLogger.info(LocaleMessages.getString("TlosServer.44"));
//			}
//			if (tlosParameters.isMail()) {
//				tlosMailServer.sendMail(new EndOfCycleMail(jobQueue));
//			}
//			if (tlosParameters.isSms()) {
//				tlosSMSServer.sendSMS(new SMSType(LocaleMessages.getString("TlosServer.45") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("TlosServer.46"))); //$NON-NLS-2$
//			}
//			if (loadTest) {
//				++loadTestTurnCount;
//				schedulerLogger.info(LocaleMessages.getString("TlosServer.47") + loadTestTurnCount);
//				setUpForTest();
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}

//	private static void cleanCyclecDeps(Job meJob, JobRuntimeProperties jobRuntimeProperties, int status) {
//		if (meJob != null && DependencyOperations.hasDependentWithStatus(jobRuntimeProperties.getJobSimpleProperties().getId(), status) && jobProperties.getStatus() == status) {
//			// Bu kod muhtemelen cyclic bağımlılık ayıklaması yapıyor
//			// Benim beklediklerimden beni bekleyen var ise, onları skip yapıp ben devam ediyorum
//			// TlosServer.getTlosCommInterface().skipJob(true, meJob.getJobProperties().getKey().toString());
//			CoreFactory.getInstance().getJobOperations().skipJob(jobRuntimeProperties.getJobSimpleProperties().getId());
//		}
//	}

	// protected boolean checkDependency(Job meJob, ArrayList<DependencyInfo> jobDependencyInfoList) {
	protected boolean checkDependency(JobImpl meJob, DependencyList dependencyList) throws UnresolvedDependencyException {
		
		if(dependencyList == null || dependencyList.getItemArray().length == 0) {
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
		
		if(lowerLimit >= higherLimit) {
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
}

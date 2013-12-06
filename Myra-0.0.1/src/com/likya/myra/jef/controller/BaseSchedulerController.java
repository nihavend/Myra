package com.likya.myra.jef.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.java.dev.eval.Expression;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.log4j.Logger;

import com.likya.myra.LocaleMessages;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.core.CoreFactoryInterface;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.xsd.myra.model.xbeans.jobprops.DependencyListDocument.DependencyList;
import com.likya.xsd.myra.model.xbeans.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.xbeans.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.xbeans.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.xbeans.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.xbeans.wlagen.ItemDocument.Item;

public class BaseSchedulerController {

	protected Logger logger = Logger.getLogger(SchedulerController.class);

	protected HashMap<String, JobImpl> jobQueue;

	protected HashMap<String, String> disabledJobQueue;

	protected boolean executionPermission = true;
	
	protected int cycleFrequency = 1000;
	
	private boolean thresholdOverflow = false;

	private int lowerLimit;
	private int higherLimit;
	
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
//				schedulerLogger.info(LocaleMessages.getString("TlosServer.43")); //$NON-NLS-1$
//				JobQueueOperations.normalizeJobQueue(jobQueue);
//				schedulerLogger.info(LocaleMessages.getString("TlosServer.44")); //$NON-NLS-1$
//			}
//			if (tlosParameters.isMail()) {
//				tlosMailServer.sendMail(new EndOfCycleMail(jobQueue));
//			}
//			if (tlosParameters.isSms()) {
//				tlosSMSServer.sendSMS(new SMSType(LocaleMessages.getString("TlosServer.45") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("TlosServer.46"))); //$NON-NLS-1$ //$NON-NLS-2$
//			}
//			if (loadTest) {
//				++loadTestTurnCount;
//				schedulerLogger.info(LocaleMessages.getString("TlosServer.47") + loadTestTurnCount); //$NON-NLS-1$
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
	protected boolean checkDependency(JobImpl meJob, DependencyList dependencyList) {

		boolean retValue = false;
		
		Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
		
		String dependencyExpression = dependencyList.getDependencyExpression().trim().toUpperCase();
		
		Expression exp = new Expression(dependencyExpression);
		
		Item[] dependencyArray = dependencyList.getItemArray();
		
		ArrayIterator dependencyArrayIterator = new ArrayIterator(dependencyArray);
		
		while (dependencyArrayIterator.hasNext()) {

			Item item = (Item) (dependencyArrayIterator.next());
			
			if (jobQueue.get(item.getJsId()) == null) {
				return false;
			}
			
			// Cyclic dependency shoud be checked !!!
			// cleanCyclecDeps(meJob, jobProperties, ?);
			
			LiveStateInfo liveStateInfo = jobQueue.get(item.getJsId()).getJobAbstractJobType().getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);
			
			boolean compResult = LiveStateInfoUtils.compareDepencyRule(variables, item, liveStateInfo);
			
			if(!compResult) {
				return compResult;
			}
			
		}
		
		BigDecimal evResult = exp.eval(variables);
		
		retValue = evResult.intValue() == 0 ? false : true;
		
		return retValue; 
		
//		int i = 0;
//
//		while (i < jobDependencyInfoList.size()) {
//			
//			Job selectedJob = jobQueue.get(jobDependencyInfoList.get(i).getJobKey());
//			
//			if (!(selectedJob instanceof RepetitiveExternalProgram)) {
//			
//				JobProperties jobProperties = selectedJob.getJobProperties();
//				
//				if ((jobProperties.getStatus() != JobProperties.DISABLED)) {
//
//					if (jobDependencyInfoList.get(i).getStatus() == JobProperties.SUCCESS && jobProperties.getStatus() != JobProperties.SUCCESS && jobProperties.getStatus() != JobProperties.SKIP) {
//						cleanCyclecDeps(meJob, jobProperties, JobProperties.FAIL);
//						return false;
//					} else if (jobDependencyInfoList.get(i).getStatus() == JobProperties.FAIL && jobProperties.getStatus() != JobProperties.FAIL) {
//						cleanCyclecDeps(meJob, jobProperties, JobProperties.SUCCESS);
//						return false;
//					} else if (jobDependencyInfoList.get(i).getStatus() == JobProperties.SUCSFAIL && jobProperties.getStatus() != JobProperties.FAIL && jobProperties.getStatus() != JobProperties.SUCCESS) {
//						cleanCyclecDeps(meJob, jobProperties, JobProperties.FAIL);
//						cleanCyclecDeps(meJob, jobProperties, JobProperties.SUCCESS);
//						return false;
//					}
//				}
//			}
//			i++;
//		}
//
//		return true;
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
		
		logger.debug(LocaleMessages.getString("TlosServer.66")); //$NON-NLS-1$
//		logger.debug(scheduledJob.getJobProperties().toString());
		
		LiveStateInfoUtils.insertNewLiveStateInfo(scheduledJob.getJobAbstractJobType(), StateName.INT_RUNNING, SubstateName.INT_ON_RESOURCE, StatusName.INT_TIME_IN);
		
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

	public synchronized boolean checkThresholdOverflow() {

		//		int lowerLimit = tlosParameters.getSchedulerLowerThreshold();
		//		int higherLimit = tlosParameters.getSchedulerHigherThreshold();

		int numOfActiveJobs = getNumOfActiveJobs();

		if ((!thresholdOverflow) && (numOfActiveJobs >= higherLimit)) {
			logger.info(LocaleMessages.getString("TlosServer.68") + numOfActiveJobs + LocaleMessages.getString("TlosServer.69") + lowerLimit); //$NON-NLS-1$ //$NON-NLS-2$
			thresholdOverflow = true;
		} else if (thresholdOverflow && (numOfActiveJobs <= lowerLimit)) {
			thresholdOverflow = false;
			logger.info(LocaleMessages.getString("TlosServer.70") + numOfActiveJobs); //$NON-NLS-1$
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

	public void setLogger(Logger logger) {
		this.logger = logger;
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
}

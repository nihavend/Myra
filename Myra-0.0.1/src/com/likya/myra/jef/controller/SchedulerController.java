package com.likya.myra.jef.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.likya.myra.LocaleMessages;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.core.CoreFactoryInterface;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.CoreStateInfo;
import com.likya.myra.jef.model.SortType;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.xsd.myra.model.xbeans.jobprops.DependencyListDocument.DependencyList;
import com.likya.xsd.myra.model.xbeans.jobprops.SimplePropertiesType;
import com.likya.xsd.myra.model.xbeans.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.xbeans.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.xbeans.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.xbeans.stateinfo.SubstateNameDocument.SubstateName;

public class SchedulerController extends BaseSchedulerController implements ControllerInterface {

	private boolean isPersistent = false;

	public SchedulerController(CoreFactoryInterface coreFactoryInterface, HashMap<String, JobImpl> jobQueue) {
		super(coreFactoryInterface, jobQueue);
	}

	@Override
	public void run() {

		ArrayList<SortType> jobIndex = JobQueueOperations.createProrityIndex(jobQueue);
		Collections.sort(jobIndex);
		
		logger.info("Starting : ");
		logger.debug(LocaleMessages.getString("TlosServer.38")); 
		logger.info(LocaleMessages.getString("TlosServer.39") + jobQueue.size());

		while (executionPermission) {

			try {

				Iterator<SortType> indexIterator = jobIndex.iterator();

				while (indexIterator.hasNext()) {

					if ((coreFactoryInterface.getManagementOperations().getExecutionState() == CoreStateInfo.STATE_SUSPENDED) || checkThresholdOverflow()) {
						// TlosServer.print(".");
						break;
					}

					SortType mySortType = indexIterator.next();
					JobImpl scheduledJob = jobQueue.get(mySortType.getJobKey());

					SimplePropertiesType simpleProperties = scheduledJob.getJobAbstractJobType();
					
					DependencyList dependencyList = simpleProperties.getDependencyList();

					// ArrayList<DependencyInfo> dependentJobList = scheduledJob.getJobProperties().getJobDependencyInfoList();

					// if (scheduledJob instanceof ExternalProgram) {
						// if (scheduledJob.getJobProperties().getStatus() == JobProperties.READY) {

					LiveStateInfo liveStateInfo = simpleProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);

					if(LiveStateInfoUtils.equalStates(liveStateInfo, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_BYTIME)) {
						// Waiting for time to execute
						Date scheduledTime = simpleProperties.getTimeManagement().getJsPlannedTime().getStartTime().getTime().getTime();
						Date currentTime = Calendar.getInstance().getTime();
						
						if (scheduledTime.before(currentTime)) {
							if (checkDependency(scheduledJob, dependencyList)) {
								executeJob(scheduledJob);
							} else {
								// Time ok but dependency, so change status !
								liveStateInfo.setStatusName(StatusName.WAITING);
							}
						}
						
					} else if(LiveStateInfoUtils.equalStates(liveStateInfo, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_WAITING)) {
						// Waiting for dependency to execute
						if (checkDependency(scheduledJob, dependencyList)) {
							executeJob(scheduledJob);
						}
					}
					
//					if (scheduledJob.getJobRuntimeProperties().getJobSimpleProperties().getStatus() == JobProperties.READY) {
//					
//							Date scheduledTime = scheduledJob.getJobProperties().getTime();
//							Date currentTime = Calendar.getInstance().getTime();
//
//							if (scheduledTime.before(currentTime)) {
//								if (dependentJobList.get(0).getJobKey().equals(ScenarioLoader.UNDEFINED_VALUE)) {
//									executeJob(scheduledJob);
//								} else {
//									if (checkDependency(scheduledJob, dependentJobList)) {
//										executeJob(scheduledJob);
//									} else {
//										if (scheduledJob.getJobProperties().getStatus() != JobProperties.SKIP) {
//											scheduledJob.getJobProperties().setStatus(JobProperties.WAITING);
//										}
//									}
//
//								}
//							}
//
//						} else if (scheduledJob.getJobProperties().getStatus() == JobProperties.WAITING) {
//
//							if (checkDependency(scheduledJob, dependentJobList)) {
//								executeJob(scheduledJob);
//							} else {
//								if (scheduledJob.getJobProperties().getStatus() != JobProperties.SKIP) {
//									scheduledJob.getJobProperties().setStatus(JobProperties.WAITING);
//								}
//							}
//
//						}
//					} else if (scheduledJob instanceof RepetitiveExternalProgram) {
//						executeRepetitiveJob(scheduledJob);
//					} else if (scheduledJob instanceof ManuelExternalProgram) {
//						executeManuelJob(scheduledJob);
//					}
				
				
				} // end of while
				
				if (isPersistent) {
					 JobQueueOperations.persistJobQueue(coreFactoryInterface.getConfigurationManager(), jobQueue);
					 JobQueueOperations.persistDisabledJobQueue(coreFactoryInterface.getConfigurationManager(), disabledJobQueue);
				}

				cleanUpQueueIssues();

				// TlosServer.print("."); //$NON-NLS-1$
				Thread.sleep(cycleFrequency);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}

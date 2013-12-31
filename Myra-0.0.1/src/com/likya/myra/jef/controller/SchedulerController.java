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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.likya.commons.model.UnresolvedDependencyException;
import com.likya.commons.utils.PrintVantil;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.CoreFactoryInterface;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.CoreStateInfo;
import com.likya.myra.jef.model.SortType;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.DependencyListDocument.DependencyList;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class SchedulerController extends BaseSchedulerController implements ControllerInterface {

	private boolean isPersistent = false;

	public SchedulerController(CoreFactoryInterface coreFactoryInterface, HashMap<String, JobImpl> jobQueue) {
		super(coreFactoryInterface, jobQueue);
	}

	@Override
	public void run() {
		
		Logger logger = CoreFactory.getLogger();

		ArrayList<SortType> jobIndex = JobQueueOperations.createProrityIndex(jobQueue);
		Collections.sort(jobIndex);

		logger.info("Starting : ");
		logger.debug(CoreFactory.getMessage("MyraServer.38"));
		logger.info(CoreFactory.getMessage("MyraServer.39") + jobQueue.size());

		while (executionPermission) {

			try {

				Iterator<SortType> indexIterator = jobIndex.iterator();

				logger.debug("Job Queue Size " + jobQueue.size());
				logger.debug("Job Index Size " + jobIndex.size());

				while (indexIterator.hasNext()) {

					if ((coreFactoryInterface.getManagementOperations().getExecutionState() == CoreStateInfo.STATE_SUSPENDED) || checkThresholdOverflow()) {
						// TlosServer.print(".");
						break;
					}

					SortType mySortType = indexIterator.next();
					JobImpl scheduledJob = jobQueue.get(mySortType.getJobKey());

					AbstractJobType abstractJobType = scheduledJob.getAbstractJobType();

					DependencyList dependencyList = abstractJobType.getDependencyList();

					// ArrayList<DependencyInfo> dependentJobList = scheduledJob.getJobProperties().getJobDependencyInfoList();

					// if (scheduledJob instanceof ExternalProgram) {
					// if (scheduledJob.getJobProperties().getStatus() == JobProperties.READY) {

					LiveStateInfo liveStateInfo = abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);

					try {

						if (LiveStateInfoUtils.equalStates(liveStateInfo, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_BYTIME)) {
							// Waiting for time to execute
							Date scheduledTime = abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime().getTime();
							Date currentTime = Calendar.getInstance().getTime();

							if (scheduledTime.before(currentTime)) {
								if (checkDependency(scheduledJob, dependencyList)) {
									executeJob(scheduledJob);
								} else {
									// Time ok but dependency, so change status !
									liveStateInfo.setStatusName(StatusName.WAITING);
								}
							}

						} else if (LiveStateInfoUtils.equalStates(liveStateInfo, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_WAITING)) {
							// Waiting for dependency to execute
							if (checkDependency(scheduledJob, dependencyList)) {
								executeJob(scheduledJob);
							}
						}

					} catch (UnresolvedDependencyException ude) {
						LiveStateInfoUtils.insertNewLiveStateInfo(scheduledJob.getAbstractJobType(), StateName.INT_CANCELLED, SubstateName.INT_STOPPED, StatusName.INT_BYEVENT);
						logger.fatal("Job " + scheduledJob.getAbstractJobType().getId() + " disabled due to invalid dependency definiton !");
						ude.printStackTrace();
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

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (isPersistent) {
				JobQueueOperations.persistJobQueue(coreFactoryInterface.getConfigurationManager(), jobQueue);
				JobQueueOperations.persistDisabledJobQueue(coreFactoryInterface.getConfigurationManager(), disabledJobQueue);
			}

			cleanUpQueueIssues();

			try {
				Thread.sleep(cycleFrequency);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.err.print(PrintVantil.getVantil() + "\r");
		}

	}
}

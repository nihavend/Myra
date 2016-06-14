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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.likya.commons.utils.PrintVantil;
import com.likya.myra.commons.model.UnresolvedDependencyException;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.CoreFactoryInterface;
import com.likya.myra.jef.core.ManagementOperationsImpl;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.CoreStateInfo;
import com.likya.myra.jef.model.SortType;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.myra.jef.utils.MyraPersistApi;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.DependencyListDocument.DependencyList;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class SchedulerController extends BaseSchedulerController implements ControllerInterface {

	public SchedulerController(CoreFactoryInterface coreFactoryInterface, HashMap<String, JobImpl> jobQueue) {
		super(coreFactoryInterface, jobQueue);
	}

	@Override
	public void run() {
		
		Thread.currentThread().setName("SchedulerController_" + System.currentTimeMillis());
		
		Logger logger = CoreFactory.getLogger();

		logger.info("Starting : ");
		logger.debug(CoreFactory.getMessage("Myra.38"));
		logger.info(CoreFactory.getMessage("Myra.39") + jobQueue.size());

		if (coreFactoryInterface.getConfigurationManager().getMyraConfig().getNormalize() && !coreFactoryInterface.getConfigurationManager().isRecovered()) {
			logger.info(CoreFactory.getMessage("Myra.40"));
			JobQueueOperations.normalizeJobQueue(jobQueue);
			logger.info(CoreFactory.getMessage("Myra.41"));
			coreFactoryInterface.getConfigurationManager().setRecovered(false);
		}

		ArrayList<SortType> jobIndex = JobQueueOperations.createProrityIndex(jobQueue);
		Collections.sort(jobIndex);

		while (executionPermission) {

			try {
				
				if(isReIndexJobQueue()) {
					setReIndexJobQueue(false);
					jobIndex = JobQueueOperations.createProrityIndex(jobQueue);
					Collections.sort(jobIndex);
				}

				Iterator<SortType> indexIterator = jobIndex.iterator();

				// logger.debug("Job Queue Size " + jobQueue.size());
				// logger.debug("Job Index Size " + jobIndex.size());

				while (indexIterator.hasNext()) {

					if ((ManagementOperationsImpl.getExecutionState() == CoreStateInfo.STATE_SUSPENDED) || checkThresholdOverflow()) {
						// TlosServer.print(".");
						break;
					}

					SortType mySortType = indexIterator.next();
					JobImpl scheduledJob = jobQueue.get(mySortType.getJobKey());

					AbstractJobType abstractJobType = scheduledJob.getAbstractJobType();

					/**
					 * if searching for depId takes so much time than 
					 * a new field to hold the depGrpId should be added to abstractJobType
					 * and may be used to directly for getting activity info of the Dep Group
					 */
					NetTree netTree = JobQueueOperations.getNetTree(abstractJobType.getId());
					
					/*
					 * if nettree is null, it means that this job is not a member of any dependency list
					 * and belongs to free jobs list.
					 * 
					 */
					if(netTree != null && !netTree.isActive()) {
						continue;
					}
					
					if (!abstractJobType.getBaseJobInfos().getJsIsActive()) {
						continue;
					}

					DependencyList dependencyList = abstractJobType.getDependencyList();

					LiveStateInfo liveStateInfo = JobHelper.getLastStateInfo(scheduledJob);

					try {
						if (dependencyList == null) {
							if (LiveStateInfoUtils.equalStatesPIT(liveStateInfo) && hasTimeCome(abstractJobType)) {
								executeJob(scheduledJob);
							}
						} else {
							if (LiveStateInfoUtils.equalStatesPIT(liveStateInfo) || LiveStateInfoUtils.equalStatesPRW(liveStateInfo)) {
								if (checkDependency(scheduledJob)) {
									if (isTimeSensitive(dependencyList)) {
										if (!dependencyList.getSensInfo().getSensTime().getRelativeStart()) {
											if (hasTimeCome(abstractJobType)) {
												executeJob(scheduledJob);
											} else {
												if (!LiveStateInfoUtils.equalStatesPIT(liveStateInfo)) {
													liveStateInfo.setSubstateName(SubstateName.IDLED);
													liveStateInfo.setStatusName(StatusName.BYTIME);
												}
											}
										} else { // Relative Time Sensitive
											if(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime() == null) { 
												handleTimeSensitivity(abstractJobType, dependencyList);
											}
											if (hasTimeCome(abstractJobType)) {
												executeJob(scheduledJob);
											} else {
												if (!LiveStateInfoUtils.equalStatesPIT(liveStateInfo)) {
													liveStateInfo.setSubstateName(SubstateName.IDLED);
													liveStateInfo.setStatusName(StatusName.BYTIME);
												}
											}
										}
									} else {
										executeJob(scheduledJob);
									}
								} else {
									if (!LiveStateInfoUtils.equalStatesPRW(liveStateInfo)) {
										if (isTimeSensitive(dependencyList) && dependencyList.getSensInfo().getSensTime().getRelativeStart()) {
											abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().setStartTime(null);
										}
										liveStateInfo.setSubstateName(SubstateName.READY);
										liveStateInfo.setStatusName(StatusName.WAITING);
									}
								}
							}
						}

					} catch (UnresolvedDependencyException ude) {
						LiveStateInfoUtils.insertNewLiveStateInfo(scheduledJob.getAbstractJobType(), StateName.INT_CANCELLED, SubstateName.INT_STOPPED, StatusName.INT_BYEVENT);
						logger.fatal("Job " + scheduledJob.getAbstractJobType().getId() + " disabled due to invalid dependency definiton !");
						ude.printStackTrace();
					}

				} // end of while

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (isPersistent()) {
				MyraPersistApi.persistJobQueue(coreFactoryInterface.getConfigurationManager(), jobQueue);
			}

			try {
				Thread.sleep(cycleFrequency);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(System.console() != null) { 
				System.err.print(PrintVantil.getVantil() + "\r");
			} else {
				// System.err.print(".");
			}
		}

	}
}

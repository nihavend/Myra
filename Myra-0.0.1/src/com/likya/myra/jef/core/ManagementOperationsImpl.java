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
package com.likya.myra.jef.core;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.controller.ControllerInterface;
import com.likya.myra.jef.controller.SchedulerController;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.CoreStateInfo;
import com.likya.myra.jef.utils.JobQueueOperations;

public class ManagementOperationsImpl implements ManagementOperations {

	private CoreFactory coreFactory;
	private Logger logger = CoreFactory.getLogger();

	public ManagementOperationsImpl(CoreFactory coreFactory) {
		super();
		this.coreFactory = coreFactory;
	}

	private void sendTermSignalToControllers() {

		HashMap<String, ControllerInterface> controllerContainer = coreFactory.getControllerContainer();

		for (String key : controllerContainer.keySet()) {
			SchedulerController schedulerController = (SchedulerController) controllerContainer.get(key);
			schedulerController.setExecutionPermission(false);
		}
	}

	public void suspend() {
		if (coreFactory.getExecutionState() == CoreStateInfo.STATE_WORKING) {
			logger.info(CoreFactory.getMessage("TlosCommInterface.21"));
			coreFactory.setExecutionState(CoreStateInfo.STATE_SUSPENDED);
			logger.info(CoreFactory.getMessage("TlosCommInterface.22"));
		}
	}

	public void resume() {
		if (coreFactory.getExecutionState() == CoreStateInfo.STATE_SUSPENDED) {
			logger.info(CoreFactory.getMessage("TlosCommInterface.23")); //$NON-NLS-1$
			coreFactory.setExecutionState(CoreStateInfo.STATE_WORKING);
			logger.info(CoreFactory.getMessage("TlosCommInterface.24")); //$NON-NLS-1$
		}
	}

	public void gracefulShutDown() {

		HashMap<String, ControllerInterface> controllerContainer = coreFactory.getControllerContainer();

		sendTermSignalToControllers();

		logger.info(CoreFactory.getMessage("Myra.49"));

		for (String key : controllerContainer.keySet()) {
			SchedulerController schedulerController = (SchedulerController) controllerContainer.get(key);
			while (JobQueueOperations.hasActiveThreads(schedulerController.getJobQueue())) {
				try {
					// print("."); 
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			schedulerController.setExecutionPermission(false);

		}

		ConfigurationManager configurationManager = coreFactory.getConfigurationManager();

		for (String key : controllerContainer.keySet()) {
			SchedulerController schedulerController = (SchedulerController) controllerContainer.get(key);
			if (configurationManager.getMyraConfig().getPersistent()) {
				JobQueueOperations.persistJobQueue(configurationManager, schedulerController.getJobQueue());
				// JobQueueOperations.persistDisabledJobQueue(configurationManager, schedulerController.getDisabledJobQueue());
			}
		}

		coreFactory.setExecutionState(CoreStateInfo.STATE_STOP);

	}

	public void forceFullShutDown() {

		Iterator<JobImpl> jobsIterator = coreFactory.getMonitoringOperations().getJobQueue().values().iterator();

		while (jobsIterator.hasNext()) {
			JobImpl scheduledJob = jobsIterator.next();
			Thread executerThread = scheduledJob.getMyExecuter();
			if (executerThread != null) {
				scheduledJob.getMyExecuter().interrupt();
			}
		}

		gracefulShutDown();
	}

	@Override
	public CoreStateInfo getExecutionState() {
		return coreFactory.getExecutionState();
	}

	@Override
	public void setExecutionState(CoreStateInfo coreStateInfo) {
		coreFactory.setExecutionState(coreStateInfo);
	}

	@Override
	public void start() throws Throwable {
		// TODO Auto-generated method stub
		coreFactory.start();
	}

}

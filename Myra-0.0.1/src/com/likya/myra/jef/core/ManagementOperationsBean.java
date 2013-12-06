package com.likya.myra.jef.core;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.likya.myra.LocaleMessages;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.controller.ControllerInterface;
import com.likya.myra.jef.controller.SchedulerController;
import com.likya.myra.jef.utils.JobQueueOperations;

public class ManagementOperationsBean implements ManagementOperations {

	private CoreFactory coreFactory;
	private Logger logger = CoreFactory.getLogger();
	
	public ManagementOperationsBean(CoreFactory coreFactory) {
		super();
		this.coreFactory = coreFactory;
	}
	
	public void shutDown() {
		
		HashMap<String, ControllerInterface> controllerContainer = coreFactory.getControllerContainer();

		sendTermSignalToControllers();

		logger.info(LocaleMessages.getString("TlosServer.49")); //$NON-NLS-1$

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

		}
		
		ConfigurationManager configurationManager = coreFactory.getConfigurationManager();
		
		for (String key : controllerContainer.keySet()) {
			SchedulerController schedulerController = (SchedulerController) controllerContainer.get(key);
			if (configurationManager.getTemporaryConfig().isPersistent()) {
				JobQueueOperations.persistJobQueue(configurationManager, schedulerController.getJobQueue());
				JobQueueOperations.persistDisabledJobQueue(configurationManager, schedulerController.getDisabledJobQueue());
			}
		}

	}
	
	private void sendTermSignalToControllers() {
		
		HashMap<String, ControllerInterface> controllerContainer = coreFactory.getControllerContainer();
		
		for (String key : controllerContainer.keySet()) {
			SchedulerController schedulerController = (SchedulerController) controllerContainer.get(key);
			schedulerController.setExecutionPermission(false);
		}
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gracefulShutDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forceFullShutDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getExecutionState() {
		return coreFactory.getExecutionState();
	}

	@Override
	public void setExecutionState(int executionState) {
		coreFactory.setExecutionState(executionState);
	}

	@Override
	public void start() throws Throwable {
		// TODO Auto-generated method stub
		coreFactory.start();
	}

}

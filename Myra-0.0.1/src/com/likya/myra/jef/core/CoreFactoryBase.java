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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.likya.commons.utils.LocaleMessages;
import com.likya.myra.commons.utils.DependencyOperations;
import com.likya.myra.commons.utils.NetTreeResolver;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.controller.ControllerInterface;
import com.likya.myra.jef.controller.SchedulerController;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.CoreStateInfo;
import com.likya.myra.jef.utils.JobQueueOperations;
import com.likya.myra.jef.utils.NetTreeManagerImp;
import com.likya.myra.jef.utils.NetTreeManagerInterface;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.joblist.JobListDocument;

public class CoreFactoryBase {

	private static final String version = "0.0.1";

	protected final static String localePath = "com.likya.myra.resources.messages";

	private CoreStateInfo coreStateInfo = CoreStateInfo.STATE_STARTING;

	private static Logger logger = Logger.getLogger("Myra");

	private ConfigurationManager configurationManager;

	private NetTreeManagerInterface netTreeManagerInterface;

	/**
	 * For current version it is limited to one
	 * For future releases, it may be extended
	 * according to distribution strategy
	 * 
	 * @author serkan taş
	 */

	protected int numOfSchedulerControllers = 1;

	protected HashMap<String, ControllerInterface> controllerContainer;

	protected JobListDocument jobListDocument;

	protected boolean validateFactory() throws Exception {

		if (!XMLValidations.validateWithXSDAndLog(getLogger(), jobListDocument)) {
			throw new Exception("JobList.xml is null or damaged !");
		}

		return true;
	}

	protected void initializeFactory() throws Exception {

		ArrayList<String> messages = new ArrayList<>();
		
		HashMap<String, JobImpl> jobQueue = new HashMap<String, JobImpl>();
		AbstractJobType[] abstractJobTypes = null;
		
		if (configurationManager.getMyraConfig().getPersistent() && JobQueueOperations.recoverJobQueue(configurationManager, jobQueue, messages) && jobQueue.size() != 0) {
			// TODO abstractJobTypes = (AbstractJobType[]) JobQueueOperations.toAbstractJobTypeList(jobQueue).values().toArray();
			// Yukarısı çözülene dek aşağıyı kullanıyoruz
			abstractJobTypes = jobListDocument.getJobList().getGenericJobArray();
		} else {
			abstractJobTypes = jobListDocument.getJobList().getGenericJobArray();
			if(abstractJobTypes.length == 0) {
				throw new Exception("jobListDocument.getJobList size is 0 !");
			}
			
			jobQueue.putAll(JobQueueOperations.transformJobQueue(jobListDocument));
			
			HashMap<String, AbstractJobType> abstractJobTypeQueue = JobQueueOperations.toAbstractJobTypeList(jobQueue);

			if (!DependencyOperations.validateDependencyList(logger, abstractJobTypeQueue)) {
				throw new Exception("JobList.xml is dependency definitions are not  valid !");
			}
			
			
		}
		
		netTreeManagerInterface = new NetTreeManagerImp(abstractJobTypes);
		updateNetTreeStatus(jobQueue);
		controllerContainer.put("1", new SchedulerController((CoreFactoryInterface) this, jobQueue));
		
		
	}

	private void updateNetTreeStatus(HashMap<String, JobImpl> jobQueue) {

		for (NetTreeResolver.NetTree netTreeMap : netTreeManagerInterface.getNetTreeMap().values()) {

			for (AbstractJobType abstractJobType : netTreeMap.getMembers()) {
				if (jobQueue.containsKey(abstractJobType.getId())) {
					JobImpl jobImpl = jobQueue.get(abstractJobType.getId());
					jobImpl.getJobRuntimeProperties().setMemberIdOfNetTree(netTreeMap.getVirtualId());
				}
			}

			// System.err.println("netTree.virtualId : " + netTree.getVirtualId());
			// System.err.println("netTree.members.size : " + netTree.getMembers().size());
		}

	}

	protected void startControllers() {

		// TODO Evaluate distribution strategy

		// if (tlosParameters.isNormalizable() && !TlosServer.isRecovered()) {
		logger.info("nomalizing !"/* LocaleMessages.getString("TlosServer.40") */); //$NON-NLS-1$
		// JobQueueOperations.normalizeJobQueueForStartup(jobQueue);
		// schedulerLogger.info(LocaleMessages.getString("TlosServer.41")); //$NON-NLS-1$
		// }

		// if (tlosParameters.isPersistent()) {
		// 	JobQueueOperations.recoverDisabledJobQueue(tlosParameters, disabledJobQueue, jobQueue);
		// }

		for (String key : controllerContainer.keySet()) {

			Thread controller = new Thread(controllerContainer.get(key));
			controller.setName(this.getClass().getName() + "_" + key);
			controller.start();

		}

		netTreeManagerInterface.startMe();
	}

	public static Logger getLogger() {
		return logger;
	}

	protected static void registerMessageBundle() {
		LocaleMessages.registerBundle(localePath);
	}

	public static String getMessage(String key) {
		return LocaleMessages.getString(localePath, key);
	}

	protected HashMap<String, ControllerInterface> getControllerContainer() {
		return controllerContainer;
	}

	public static String getVersion() {
		return version;
	}

	protected CoreStateInfo getExecutionState() {
		return coreStateInfo;
	}

	protected synchronized void setExecutionState(CoreStateInfo coreStateInfo) {
		this.coreStateInfo = coreStateInfo;
	}

	protected int getNumOfSchedulerControllers() {
		return numOfSchedulerControllers;
	}

	public static void setLogger(Logger logger) {
		CoreFactoryBase.logger = logger;
	}

	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public NetTreeManagerInterface getNetTreeManagerInterface() {
		return netTreeManagerInterface;
	}

	protected void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}
}

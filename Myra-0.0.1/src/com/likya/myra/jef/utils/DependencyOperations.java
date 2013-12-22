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
package com.likya.myra.jef.utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import net.java.dev.eval.Expression;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.log4j.Logger;

import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.JobRuntimeProperties;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.wlagen.ItemDocument.Item;

public class DependencyOperations {

	
	/*
	public static ArrayList<String> getDependencyJobKeys(ArrayList<DependencyInfo> dependencyInfoList) {

		ArrayList<String> myList = new ArrayList<String>();

		Iterator<DependencyInfo> myIterator = dependencyInfoList.iterator();

		while (myIterator.hasNext()) {

			DependencyInfo dependencyInfo = myIterator.next();

			myList.add(dependencyInfo.getJobKey());
		}

		return myList;
	}

	public static ArrayList<Integer> getDependencyStatusList(ArrayList<DependencyInfo> dependencyInfoList) {

		ArrayList<Integer> myList = new ArrayList<Integer>();

		Iterator<DependencyInfo> myIterator = dependencyInfoList.iterator();

		while (myIterator.hasNext()) {

			DependencyInfo dependencyInfo = myIterator.next();

			myList.add(dependencyInfo.getStatus());
		}

		return myList;
	}

	public static ArrayList<Integer> getDependencyStatusList(String jobKey, ArrayList<DependencyInfo> dependencyInfoList) {

		ArrayList<Integer> myList = new ArrayList<Integer>();

		Iterator<DependencyInfo> myIterator = dependencyInfoList.iterator();

		while (myIterator.hasNext()) {

			DependencyInfo dependencyInfo = myIterator.next();
			if (dependencyInfo.getJobKey().equals(jobKey)) {
				myList.add(dependencyInfo.getStatus());
			}
		}

		return myList;
	}

	public static boolean hasDependentWithStatus(HashMap<String, JobImpl> jobQueue, String jobKey, int status) {

		// remove myself
		jobQueue.remove(jobKey);

		Iterator<JobImpl> jobListIterator = jobQueue.values().iterator();

		while (jobListIterator.hasNext()) {

			JobImpl tmpJob = jobListIterator.next();

			Iterator<DependencyInfo> dependentToMeListIterator = tmpJob.getSimpleJobProperties().getJobDependencyInfoList().iterator();

			while (dependentToMeListIterator.hasNext()) {
				DependencyInfo tmpDependencyInfo = dependentToMeListIterator.next();

				if (tmpDependencyInfo.getJobKey().equals(jobKey)) {
					if (getDependencyStatusList(tmpJob.getSimpleJobProperties().getJobDependencyInfoList()).indexOf(new Integer(status)) >= 0) {
						return true;
					}
				}
			}

		}

		return false;
	}

	public static ArrayList<JobImpl> getDependencyList(HashMap<String, JobImpl> jobQueue, Object jobKey) {

		ArrayList<JobImpl> jobList = new ArrayList<JobImpl>();

		try {
			Iterator<JobImpl> jobsIterator = jobQueue.values().iterator();
			while (jobsIterator.hasNext()) {
				JobImpl scheduledJob = jobsIterator.next();
				ArrayList<String> dependentJobList = DependencyOperations.getDependencyJobKeys(scheduledJob.getJobAbstractJobType().getDependencyList());
				int indexOfJob = dependentJobList.indexOf(jobKey);
				if (indexOfJob > -1) {
					jobList.add(scheduledJob);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jobList.size() == 0 ? null : jobList;

	}

	public static boolean checkCyclicDependency() {

		HashMap<String, JobImpl> jobQueue = CoreFactory.getJobQueue();

		try {
			Iterator<JobImpl> jobsIterator = jobQueue.values().iterator();

			while (jobsIterator.hasNext()) {
				JobImpl scheduledJob = jobsIterator.next();
				ArrayList<String> dependentToJobList = DependencyOperations.getDependencyJobKeys(scheduledJob.getJobAbstractJobType().getDependencyList());
				CoreFactory.getLogger().warn("         >> " + scheduledJob.getJobAbstractJobType().getId());
				if (recurseInToCycle(scheduledJob, dependentToJobList)) {
					return true;
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	private static boolean recurseInToCycle(JobImpl scheduledJob, ArrayList<String> dependentToJobList) {

		if (dependentToJobList.indexOf(scheduledJob.getJobAbstractJobType().getId().toString()) >= 0) {
			return true;
		} else {
			Iterator<String> jobsIterator = dependentToJobList.iterator();
			while (jobsIterator.hasNext()) {
				String recurJobKey = jobsIterator.next();
				if (recurJobKey != null && recurJobKey.equals(ScenarioLoader.UNDEFINED_VALUE)) {
					continue;
				}
				JobImpl recurJob = CoreFactory.getJobQueue().get(recurJobKey);
				ArrayList<String> tmpDependentToJobList = DependencyOperations.getDependencyJobKeys(recurJob.getJobAbstractJobType().getDependencyList());
				CoreFactory.getLogger().warn("  Analyzing dependency list of          >> " + recurJob.getJobAbstractJobType().getId());
				if (recurseInToCycle(scheduledJob, tmpDependentToJobList)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean validateDependencyList(Logger schedulerLogger, HashMap<String, JobImpl> jobQueue) {

		// For Test
		Date startTime = Calendar.getInstance().getTime();
		boolean cyclCheck = checkCyclicDependency();
		String duration = "" + DateUtils.getDurationNumeric(startTime);
		CoreFactory.getLogger().warn("Cyclicdependency checkduration : " + duration);

		if (cyclCheck) {
			schedulerLogger.info("Cyclic dependency resloved !");
			return false;
		}
		Iterator<JobImpl> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {

			JobImpl scheduledJob = jobsIterator.next();

			ArrayList<DependencyInfo> dependentJobList = scheduledJob.getJobAbstractJobType().getDependencyList();

			if (getDependencyJobKeys(dependentJobList).indexOf(scheduledJob.getJobProperties().getKey()) >= 0) {
				schedulerLogger.info(scheduledJob.getJobProperties().getKey() + LocaleMessages.getString("ScenarioLoader.441")); //$NON-NLS-1$
				return false;
			}

			int i = 0;
			while (i < dependentJobList.size()) {
				String key = (dependentJobList.get(i)).getJobKey();

				if (key.equals(ScenarioLoader.UNDEFINED_VALUE)) {
					++i;
					continue;
				}

				if ((!key.equals(ScenarioLoader.UNDEFINED_VALUE)) && (jobQueue.get(key) == null)) {
					schedulerLogger.info(scheduledJob.getJobProperties().getKey() + LocaleMessages.getString("ScenarioLoader.44") + key); //$NON-NLS-1$
					return false;
				}
				if (jobQueue.get(key) instanceof RepetitiveExternalProgram) {
					schedulerLogger.info(scheduledJob.getJobAbstractJobType().getId() + LocaleMessages.getString("ScenarioLoader.19") + key); //$NON-NLS-1$
					return false;
				}

				if (jobQueue.get(key).getJobProperties().isManuel()) {
					schedulerLogger.info(scheduledJob.getJobAbstractJobType().getId() + LocaleMessages.getString("ScenarioLoader.191") + key); //$NON-NLS-1$
					return false;
				}

				// Her bir işin teker teker bağımlılık listesi alınır
				// Geçerli işin bağımlılık listesinde FAIL tipli bir bağımlılık tanımı var ise, bağlı olduğu iş bulunur.
				// Bağlı olunan iş non-blocker yapılır
				if (DependencyOperations.getDependencyStatusList(dependentJobList).indexOf(new Integer(JobProperties.FAIL)) >= 0) {
					// Sadece fail bağımlısı olduğu iş değil, bu iş de non-blocker olmalı.
					scheduledJob.getJobProperties().setBlocker(false);
					ArrayList<String> keyList = getDependencyJobKeys(dependentJobList);

					Iterator<String> keyListIterator = keyList.iterator();

					while (keyListIterator.hasNext()) {
						String tmpKey = keyListIterator.next();
						jobQueue.get(tmpKey).getJobProperties().setBlocker(false);
					}

				}

				++i;
			}
		}

		return true;

	}
*/
	public static boolean resolveDep(AbstractJobType abstractJobType, HashMap<String, JobImpl> jobQueue) throws Exception {

		Logger logger = CoreFactory.getLogger();

		String dependencyExpression = abstractJobType.getDependencyList().getDependencyExpression();
		Item[] dependencyArray = abstractJobType.getDependencyList().getItemArray();

		String ownerJsName = abstractJobType.getBaseJobInfos().getJsName();

		dependencyExpression = dependencyExpression.replace("AND", "&&");
		dependencyExpression = dependencyExpression.replace("OR", "||");

		Expression exp = new Expression(dependencyExpression);
		BigDecimal result = new BigDecimal(0);

		ArrayIterator dependencyArrayIterator = new ArrayIterator(dependencyArray);

		Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();

		while (dependencyArrayIterator.hasNext()) {

			Item item = (Item) (dependencyArrayIterator.next());
			JobRuntimeProperties jobRuntimeProperties = null;

			if (dependencyExpression.indexOf(item.getDependencyID().toUpperCase()) < 0) {
				String errorMessage = "     > " + ownerJsName + " isi icin hatali bagimlilik tanimlamasi yapilmis ! (" + dependencyExpression + ") kontrol ediniz.";
				logger.info(errorMessage);
				logger.error(errorMessage);
				throw new Exception(errorMessage);
			}
			
			JobImpl jobImpl = jobQueue.get(item.getJsId());

			LiveStateInfo liveStateInfo = jobImpl.getAbstractJobType().getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);

			StateName.Enum jobStateName = liveStateInfo.getStateName();
			SubstateName.Enum jobSubstateName = liveStateInfo.getSubstateName();
			StatusName.Enum jobStatusName = liveStateInfo.getStatusName();

			StateName.Enum itemStateName = item.getJsDependencyRule().getStateName();
			SubstateName.Enum itemSubstateName = item.getJsDependencyRule().getSubstateName();
			StatusName.Enum itemStatusName = item.getJsDependencyRule().getStatusName();

			if (itemStateName != null && itemSubstateName == null && itemStatusName == null) {
				if (jobStateName.equals(itemStateName)) {
					variables.put(item.getDependencyID(), new BigDecimal(1)); // true
				} else {
					variables.put(item.getDependencyID(), new BigDecimal(0)); // false
				}
			} else if (itemStateName != null && itemSubstateName != null && itemStatusName == null) {
				if (jobStateName.equals(itemStateName) && jobSubstateName.equals(itemSubstateName)) {
					variables.put(item.getDependencyID(), new BigDecimal(1)); // true
				} else {
					variables.put(item.getDependencyID(), new BigDecimal(0)); // false
				}
			} else if (itemStateName != null && itemSubstateName != null && itemStatusName != null && jobStateName != null && jobSubstateName != null) {
				if (jobStateName.equals(itemStateName) && jobSubstateName.equals(itemSubstateName)) {
					if (jobStatusName != null)
						if (jobStatusName.equals(itemStatusName)) {
							variables.put(item.getDependencyID(), new BigDecimal(1)); // true
						} else {
							variables.put(item.getDependencyID(), new BigDecimal(0)); // false
						}
				} else {
					variables.put(item.getDependencyID(), new BigDecimal(0)); // false
				}
			} else {
				return false;
			}

		}

		result = exp.eval(variables);

		return result.intValue() == 0 ? false : true;

	}
}

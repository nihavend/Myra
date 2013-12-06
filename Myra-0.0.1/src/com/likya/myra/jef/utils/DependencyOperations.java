package com.likya.myra.jef.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.likya.myra.LocaleMessages;
import com.likya.tlos.lite.model.DependencyInfo;

public class DependencyOperations {
	
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
			if(dependencyInfo.getJobKey().equals(jobKey)) {
				myList.add(dependencyInfo.getStatus());
			}
		}

		return myList;
	}
	
	public static boolean hasDependentWithStatus(HashMap<String, JobInterface> jobQueue, String jobKey, int status) {

		// remove myself
		jobQueue.remove(jobKey);

		Iterator<JobInterface> jobListIterator = jobQueue.values().iterator();

		while (jobListIterator.hasNext()) {

			JobInterface tmpJob = jobListIterator.next();

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

	public static ArrayList<JobInterface> getDependencyList(HashMap<String, JobInterface> jobQueue, Object jobKey) {

		ArrayList<JobInterface> jobList = new ArrayList<JobInterface>();

		try {
			Iterator<JobInterface> jobsIterator = jobQueue.values().iterator();
			while (jobsIterator.hasNext()) {
				JobInterface scheduledJob = jobsIterator.next();
				ArrayList<String> dependentJobList = DependencyOperations.getDependencyJobKeys(scheduledJob.getSimpleJobProperties().getJobDependencyInfoList());
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
		
		HashMap<String, Job> jobQueue = TlosServer.getJobQueue();
		
		try {
			Iterator<Job> jobsIterator = jobQueue.values().iterator();
			
			while (jobsIterator.hasNext()) {
				Job scheduledJob = jobsIterator.next();
				ArrayList<String> dependentToJobList = DependencyOperations.getDependencyJobKeys(scheduledJob.getSimpleJobProperties().getJobDependencyInfoList());
				TlosServer.getLogger().warn("         >> " + scheduledJob.getSimpleJobProperties().getKey());
				if(recurseInToCycle(scheduledJob, dependentToJobList)) {
					return true;
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static boolean recurseInToCycle(JobInterface scheduledJob, ArrayList<String> dependentToJobList) {
		 
		if(dependentToJobList.indexOf(scheduledJob.getJobProperties().getKey().toString()) >= 0) {
			return true;
		} else {
			Iterator<String> jobsIterator = dependentToJobList.iterator();
			while(jobsIterator.hasNext()) {
				String recurJobKey = jobsIterator.next(); 
				if(recurJobKey != null && recurJobKey.equals(ScenarioLoader.UNDEFINED_VALUE)) {
					continue;
				}
				JobInterface recurJob = 	TlosServer.getJobQueue().get(recurJobKey);
				ArrayList<String> tmpDependentToJobList = DependencyOperations.getDependencyJobKeys(recurJob.getJobProperties().getJobDependencyInfoList());
				TlosServer.getLogger().warn("  Analyzing dependency list of          >> " + recurJob.getJobProperties().getKey());
				if(recurseInToCycle(scheduledJob, tmpDependentToJobList)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean validateDependencyList(Logger schedulerLogger, HashMap<String, JobInterface> jobQueue) {

		// For Test
		Date startTime = Calendar.getInstance().getTime();
		boolean cyclCheck = checkCyclicDependency();
		String duration = "" + DateUtils.getDurationNumeric(startTime);
		TlosServer.getLogger().warn("Cyclicdependency checkduration : " + duration);
		
		if(cyclCheck) {
			schedulerLogger.info("Cyclic dependency resloved !");/*LocaleMessages.getString("ScenarioLoader.441")); //$NON-NLS-1$*/
			return false;
		}
		Iterator<JobInterface> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {
			
			Job scheduledJob = jobsIterator.next();
			
			ArrayList<DependencyInfo> dependentJobList = scheduledJob.getJobProperties().getJobDependencyInfoList();

			if(getDependencyJobKeys(dependentJobList).indexOf(scheduledJob.getJobProperties().getKey()) >= 0) {
				schedulerLogger.info(scheduledJob.getJobProperties().getKey() + LocaleMessages.getString("ScenarioLoader.441")); //$NON-NLS-1$
				return false;
			}
			
			int i = 0;
			while (i < dependentJobList.size()) {
				String key = (dependentJobList.get(i)).getJobKey();
				
				if(key.equals(ScenarioLoader.UNDEFINED_VALUE)) {
					++i;
					continue;
				}
				
				if ((!key.equals(ScenarioLoader.UNDEFINED_VALUE)) && (jobQueue.get(key) == null)) {
					schedulerLogger.info(scheduledJob.getJobProperties().getKey() + LocaleMessages.getString("ScenarioLoader.44") + key); //$NON-NLS-1$
					return false;
				}
				if (jobQueue.get(key) instanceof RepetitiveExternalProgram) {
					schedulerLogger.info(scheduledJob.getJobProperties().getKey() + LocaleMessages.getString("ScenarioLoader.19") + key); //$NON-NLS-1$
					return false;
				}
				
				if (jobQueue.get(key) .getJobProperties().isManuel()) {
					schedulerLogger.info(scheduledJob.getJobProperties().getKey() + LocaleMessages.getString("ScenarioLoader.191") + key); //$NON-NLS-1$
					return false;
				}
				
				// Her bir işin teker teker bağımlılık listesi alınır
				// Geçerli işin bağımlılık listesinde FAIL tipli bir bağımlılık tanımı var ise, bağlı olduğu iş bulunur.
				// Bağlı olunan iş non-blocker yapılır
				if(DependencyOperations.getDependencyStatusList(dependentJobList).indexOf(new Integer(JobProperties.FAIL)) >= 0) {
					// Sadece fail bağımlısı olduğu iş değil, bu iş de non-blocker olmalı.
					scheduledJob.getJobProperties().setBlocker(false);
					ArrayList<String> keyList = getDependencyJobKeys(dependentJobList);
					
					Iterator<String> keyListIterator = keyList.iterator();
					
					while(keyListIterator.hasNext()) {
						String tmpKey = keyListIterator.next();
						jobQueue.get(tmpKey).getJobProperties().setBlocker(false);
					}
					
				}

				++i;
			}
		}

		return true;

	}
}

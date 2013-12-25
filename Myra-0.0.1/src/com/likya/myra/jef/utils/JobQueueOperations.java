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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.log4j.Logger;

import com.likya.myra.LocaleMessages;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.JobRuntimeProperties;
import com.likya.myra.jef.model.PersistObject;
import com.likya.myra.jef.model.SortType;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.joblist.JobListDocument;

public class JobQueueOperations {

	public static boolean hasActiveThreads(HashMap<String, JobImpl> jobQueue) {

		Iterator<JobImpl> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			JobImpl scheduledJob = jobsIterator.next();
			Thread myExecuter = scheduledJob.getMyExecuter();
			if ((myExecuter != null) && myExecuter.isAlive()) {
				return true;
			}
		}

		return false;
	}

	public static ArrayList<SortType> createProrityIndex(HashMap<String, JobImpl> jobQueue) {

		ArrayList<SortType> jobQueueArray = new ArrayList<SortType>();

		JobImpl job = null;

		Iterator<JobImpl> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {

			job = jobsIterator.next();

			AbstractJobType abstractJobType = job.getAbstractJobType();

			SortType mySortType = new SortType(abstractJobType.getId(), abstractJobType.getBaseJobInfos().getJobPriority().intValue());
			jobQueueArray.add(mySortType);
		}

		return jobQueueArray;
	}

	public static boolean persistDisabledJobQueue(ConfigurationManager configurationManager, HashMap<String, String> jobQueue) {

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		if (jobQueue.size() == 0) {
			return true;
		}

		try {

			File fileTemp = new File(configurationManager.getTemporaryConfig().getFileToPersist() + "_disabled.temp"); //$NON-NLS-1$
			fos = new FileOutputStream(fileTemp);

			out = new ObjectOutputStream(fos);

			out.writeObject(jobQueue);
			out.close();

			File file = new File(configurationManager.getTemporaryConfig().getFileToPersist() + "_disabled");

			if (file.exists()) {
				file.delete();
			}

			fileTemp.renameTo(file);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;

	}

	public static boolean persistJobQueue(ConfigurationManager configurationManager, HashMap<String, JobImpl> jobQueue) {

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		if (jobQueue.size() == 0) {
			CoreFactory.getLogger().fatal(LocaleMessages.getString("JobQueueOperations.10")); //$NON-NLS-1$
			CoreFactory.getLogger().fatal(LocaleMessages.getString("JobQueueOperations.11")); //$NON-NLS-1$
			System.exit(-1);
		}
		try {

			File fileTemp = new File(configurationManager.getTemporaryConfig().getFileToPersist() + ".temp"); //$NON-NLS-1$
			fos = new FileOutputStream(fileTemp);

			out = new ObjectOutputStream(fos);

			PersistObject persistObject = new PersistObject();

			persistObject.setJobQueue(jobQueue);
			persistObject.setTlosVersion(CoreFactory.getVersion());
			persistObject.setGroupList(configurationManager.getTemporaryConfig().getGroupList());

			out.writeObject(persistObject);
			out.close();

			File file = new File(configurationManager.getTemporaryConfig().getFileToPersist());

			if (file.exists()) {
				file.delete();
			}

			fileTemp.renameTo(file);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;

	}

//	public static HashMap<String, JobImpl> transformJobQueueOld(JobListDocument jobListDocument) {
//
//		HashMap<String, JobImpl> jobQueue = new HashMap<String, JobImpl>();
//
//		int remoteSchLength = jobListDocument.getJobList().getJobRemoteSchPropertiesArray().length;
//		int simple = jobListDocument.getJobList().getJobSimplePropertiesArray().length;
//
//		int length = remoteSchLength + simple;
//
//		Object[] objectArray = new Object[length];
//
//		System.arraycopy(jobListDocument.getJobList().getJobRemoteSchPropertiesArray(), 0, objectArray, 0, remoteSchLength);
//		System.arraycopy(jobListDocument.getJobList().getJobSimplePropertiesArray(), 0, objectArray, remoteSchLength, simple);
//
//		ArrayIterator jobArrayIterator = new ArrayIterator(objectArray);
//
//		while (jobArrayIterator.hasNext()) {
//
//			Object retObject = jobArrayIterator.next();
//
//			int jobType = -1;
//			// Deneme amaçlı
//			if (retObject instanceof RemoteSchProperties) {
//				jobType = JobCommandType.INT_REMOTE_SHELL;
//			} else if (retObject instanceof SimpleProperties) {
//				jobType = JobCommandType.INT_BATCH_PROCESS;
//			}
//
//			SimpleProperties simpleProperties = (SimpleProperties) (retObject);
//
//			JobRuntimeInterface jobRuntimeInterface = new JobRuntimeProperties();
//
//			JobImpl jobImpl = null;
//
//			switch (jobType) {
//			case JobCommandType.INT_BATCH_PROCESS:
//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
//				((ExecuteInShell) jobImpl).setShell(true);
//				break;
//			case JobCommandType.INT_SYSTEM_COMMAND:
//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
//				((ExecuteInShell) jobImpl).setShell(false);
//				break;
//			case JobCommandType.INT_REMOTE_SHELL:
//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
//				break;
//
//			default:
//				break;
//			}
//
//			jobQueue.put(simpleProperties.getId(), jobImpl);
//		}
//
//		return jobQueue;
//	}

	public static HashMap<String, JobImpl> transformJobQueue(JobListDocument jobListDocument) {

		HashMap<String, JobImpl> jobQueue = new HashMap<String, JobImpl>();

		JobRuntimeInterface jobRuntimeInterface = new JobRuntimeProperties();
		
		Object[] objectArray = jobListDocument.getJobList().getGenericJobArray();

		ArrayIterator jobArrayIterator = new ArrayIterator(objectArray);

		while (jobArrayIterator.hasNext()) {

			AbstractJobType abstractJobType = (AbstractJobType) jobArrayIterator.next();
			
			String handlerUri = abstractJobType.getHandlerURI();

			Class<?> abstractClass;
			try {
				// abstractClass = Class.forName("com.likya.myra.jef.jobs.ExecuteInShell");
				abstractClass = Class.forName(handlerUri);
				jobRuntimeInterface.setLogger(Logger.getLogger(abstractClass));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			JobImpl jobImpl = null;
					
			try {
				jobImpl = (JobImpl) abstractClass.getDeclaredConstructor(new Class[] { AbstractJobType.class, JobRuntimeInterface.class }).newInstance(abstractJobType, jobRuntimeInterface);
				jobImpl.getJobInfo();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//			switch (jobType) {
//			case JobCommandType.INT_BATCH_PROCESS:
//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
//				((ExecuteInShell) jobImpl).setShell(true);
//				break;
//			case JobCommandType.INT_SYSTEM_COMMAND:
//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
//				((ExecuteInShell) jobImpl).setShell(false);
//				break;
//			case JobCommandType.INT_REMOTE_SHELL:
//				jobImpl = new ExecuteInShell(simpleProperties, jobRuntimeInterface);
//				break;
//
//			default:
//				break;
//			}
			
			CoreFactory.getLogger().info("Transformed " + handlerUri + " Job Id : " + abstractJobType.getId());

			jobQueue.put(abstractJobType.getId(), jobImpl);
		}

		return jobQueue;
	}
	
	public static HashMap<String, AbstractJobType> toAbstractJobTypeList(HashMap<String, JobImpl> jobQueue) {
		
		HashMap<String, AbstractJobType> tmpList = new HashMap<String, AbstractJobType>();
		
		Iterator<String> jobsIterator = jobQueue.keySet().iterator();

		while (jobsIterator.hasNext()) {
			String jobKey = jobsIterator.next();
			tmpList.put(jobKey, jobQueue.get(jobKey).getAbstractJobType());
		}
		
		return tmpList;
	}
	
	public static Collection<String> getKeyList(Collection<AbstractJobType> abstractJobTypeList) {
		
		ArrayList<String> jobKeys = new ArrayList<>();
		Iterator<AbstractJobType> abstractJobTypeIterator = abstractJobTypeList.iterator();
		
		while (abstractJobTypeIterator.hasNext()) {
			AbstractJobType abstractJobType = abstractJobTypeIterator.next();
			jobKeys.add(abstractJobType.getId());
		}
		
		return jobKeys;
	}
}

package com.likya.myra.test;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.InputStrategy;
import com.likya.myra.jef.InputStrategyImpl;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.ManagementOperations;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.JobRuntimeProperties;
import com.likya.myra.samples.TestOutput;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.joblist.JobListDocument;

public class DataFileLoader1 {
	
	private static CoreFactory restart(JobListDocument jobListDocument) {
		
		InputStrategy inputStrategy = new InputStrategyImpl();

		inputStrategy.setJobListDocument(jobListDocument);

		final TestOutput testOutput = new TestOutput();

		CoreFactory coreFactory = (CoreFactory) CoreFactory.getInstance(inputStrategy, testOutput);

		checkObject(jobListDocument);

		ManagementOperations managementOperations = coreFactory.getManagementOperations();
		
		try {
			managementOperations.start();
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
		
		return coreFactory;
	}
	
	public static CoreFactory resetAndStart() throws Exception {
		
		JobListDocument jobListDocument = JobListDocument.Factory.newInstance();

		jobListDocument.addNewJobList();
		
		CoreFactory coreFactory = restart(jobListDocument);
		
		return coreFactory;
	}
	
	public static CoreFactory loadAndStart(String senaryo) throws Exception {
		
		StringBuffer xmlString = getData(senaryo);

		JobListDocument jobListDocument = JobListDocument.Factory.parse(xmlString.toString());

		CoreFactory coreFactory = restart(jobListDocument);
		
		return coreFactory;
	}
	
	public static StringBuffer getData(String senaryo) throws Exception {

		StringBuffer xmlString;

		if (senaryo == null) {
			throw new Exception(senaryo + " is null or damaged !");
		}

		xmlString = FileUtils.readFile(senaryo);

		JobListDocument jobListDocument = JobListDocument.Factory.parse(xmlString.toString());

		if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), jobListDocument)) {
			throw new Exception("JobList.xml is null or damaged !");
		}

		return xmlString;
	}
	
	public static void checkObject(JobListDocument jobListDocument) {

		XmlObject[] objectArray = jobListDocument.getJobList().getGenericJobArray();

		ArrayIterator jobArrayIterator = new ArrayIterator(objectArray);

		JobRuntimeInterface jobRuntimeInterface = new JobRuntimeProperties();

		while (jobArrayIterator.hasNext()) {

			AbstractJobType abstractJobType = (AbstractJobType) jobArrayIterator.next();

			Class<?> abstractClass;
			try {
				abstractClass = Class.forName(abstractJobType.getHandlerURI());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}

			try {
				JobImpl jobImpl = (JobImpl) abstractClass.getDeclaredConstructor(new Class[] { AbstractJobType.class, JobRuntimeInterface.class }).newInstance(abstractJobType, jobRuntimeInterface);
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

			//			if (retObject instanceof RemoteSchProperties) {
			//				System.err.println(retObject.getClass().getName());
			//			} else if (retObject instanceof TlosLiteInterCommProperties) {
			//				System.err.println(retObject.getClass().getName());
			//			} else {
			//				System.err.println(retObject.getClass().getName());
			//			}
		}
	}

}

package com.likya.myra.jef.utils;

import org.apache.log4j.Logger;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.InputStrategy;
import com.likya.myra.jef.InputStrategyImpl;
import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.ManagementOperations;
import com.likya.xsd.myra.model.joblist.JobListDocument;

public class Starter {
	
	public static CoreFactory startForce(JobListDocument jobListDocument, OutputStrategy outputStrategy) throws Exception {
		
		CoreFactory coreFactory;
		
		if(jobListDocument == null) {
			jobListDocument = JobListDocument.Factory.newInstance();
			jobListDocument.addNewJobList();
		} 
		
		coreFactory = start(jobListDocument, outputStrategy);
		
		return coreFactory;
		
	}
	
	public static CoreFactory startForce(String senaryo, OutputStrategy outputStrategy) throws Exception {
		
		CoreFactory coreFactory;
		JobListDocument jobListDocument;
		
		StringBuffer xmlString = getData(senaryo);
		
		if(xmlString == null) {
			jobListDocument = JobListDocument.Factory.newInstance();
			jobListDocument.addNewJobList();
		} else {
			jobListDocument = JobListDocument.Factory.parse(xmlString.toString());
		}
		
		coreFactory = start(jobListDocument, outputStrategy);
		
		return coreFactory;
	}

	public static CoreFactory start(OutputStrategy outputStrategy) throws Exception {
		
		JobListDocument jobListDocument = JobListDocument.Factory.newInstance();

		jobListDocument.addNewJobList();
		
		CoreFactory coreFactory = start(jobListDocument, outputStrategy);
		
		return coreFactory;
	}
	
	public static CoreFactory start(String senaryo, OutputStrategy outputStrategy) throws Exception {
		
		StringBuffer xmlString = getData(senaryo);

		JobListDocument jobListDocument = JobListDocument.Factory.parse(xmlString.toString());

		CoreFactory coreFactory = start(jobListDocument, outputStrategy);
		
		return coreFactory;
	}
	
	public static CoreFactory start(JobListDocument jobListDocument, OutputStrategy outputStrategy ) {
		
		InputStrategy inputStrategy = new InputStrategyImpl();

		inputStrategy.setJobListDocument(jobListDocument);

		CoreFactory coreFactory = (CoreFactory) CoreFactory.getInstance(inputStrategy, outputStrategy);

		ManagementOperations managementOperations = coreFactory.getManagementOperations();
		
		try {
			managementOperations.start();
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
		
		return coreFactory;
	}
	
	public static StringBuffer getData(String senaryo) throws Exception {

		StringBuffer xmlString;

		if (senaryo == null || FileUtils.checkFile(senaryo) == false) {
			// throw new Exception(senaryo + " is null, damaged or not exists at all !");
			CoreFactory.getLogger().debug(senaryo + " is null, damaged or not exists at all !");
			return null;
		}

		xmlString = FileUtils.readFile(senaryo);

		JobListDocument jobListDocument = JobListDocument.Factory.parse(xmlString.toString());

		if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), jobListDocument)) {
			throw new Exception(senaryo + " is null or damaged !");
		}

		return xmlString;
	}
}

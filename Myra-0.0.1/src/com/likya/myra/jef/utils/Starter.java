package com.likya.myra.jef.utils;

import org.apache.log4j.Logger;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.InputStrategy;
import com.likya.myra.jef.InputStrategyImpl;
import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.CoreFactoryInterface;
import com.likya.myra.jef.core.ManagementOperations;
import com.likya.myra.jef.model.CoreStateInfo;
import com.likya.xsd.myra.model.joblist.JobListDocument;

public class Starter {
	
	public static CoreFactoryInterface startRecover(OutputStrategy outputStrategy) throws Exception {
		return start(null, outputStrategy, CoreStateInfo.STATE_RECOVER);
	}
	
	public static CoreFactoryInterface start(OutputStrategy outputStrategy) throws Exception {
		return start("", outputStrategy);
	}
	
	public static CoreFactoryInterface start(String senaryo, OutputStrategy outputStrategy) throws Exception {
		
		CoreFactoryInterface coreFactoryInterface;
		JobListDocument jobListDocument;
		
		StringBuffer xmlString = null;
		
		if(senaryo != null && !senaryo.equals("")) {
			xmlString = getData(senaryo);
		}
		
		if(xmlString == null) {
			jobListDocument = JobListDocument.Factory.newInstance();
			jobListDocument.addNewJobList();
		} else {
			jobListDocument = JobListDocument.Factory.parse(xmlString.toString());
		}
		
		coreFactoryInterface = start(jobListDocument, outputStrategy, null);
		
		return coreFactoryInterface;
	}

	public static CoreFactoryInterface start(JobListDocument jobListDocument, OutputStrategy outputStrategy) {
		return start(jobListDocument, outputStrategy, null);
	}
	
	public static CoreFactoryInterface start(JobListDocument jobListDocument, OutputStrategy outputStrategy, CoreStateInfo coreStateInfo) {
		
		InputStrategy inputStrategy = new InputStrategyImpl();

		inputStrategy.setJobListDocument(jobListDocument);

		CoreFactoryInterface coreFactoryInterface = CoreFactory.getInstance(inputStrategy, outputStrategy);
		
		if(coreStateInfo != null) {
			coreFactoryInterface.getManagementOperations().setExecutionState(coreStateInfo);
		}

		ManagementOperations managementOperations = coreFactoryInterface.getManagementOperations();
		
		try {
			managementOperations.start();
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
		
		return coreFactoryInterface;
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

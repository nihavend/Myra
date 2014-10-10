package com.likya.myra.samples;

import org.apache.log4j.Logger;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.ManagementOperations;
import com.likya.xsd.myra.model.joblist.JobListDocument;

public class NofilesMyra {

	public static void main(String[] args) throws Exception {
		
		
		final TestOutput testOutput = new TestOutput();
		
		CoreFactory coreFactory = (CoreFactory) CoreFactory.getInstance(testOutput);
		
		ManagementOperations managementOperations = coreFactory.getManagementOperations();
		try {
			managementOperations.start();
		} catch (Throwable e) {
			e.printStackTrace();
			return;
		}
		

	}

	public static StringBuffer getData(String senaryo) throws Exception {

		StringBuffer xmlString;

		xmlString = FileUtils.readFile(senaryo);

		JobListDocument jobListDocument = JobListDocument.Factory.parse(xmlString.toString());

		if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), jobListDocument)) {
			throw new Exception("JobList.xml is null or damaged !");
		}

		return xmlString;
	}
}

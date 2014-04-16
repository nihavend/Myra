package com.likya.myra.samples;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.ConfigurationManagerImpl;
import com.likya.myra.jef.InputStrategy;
import com.likya.myra.jef.InputStrategyImpl;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.ManagementOperations;
import com.likya.xsd.myra.model.config.MyraConfigDocument;
import com.likya.xsd.myra.model.joblist.JobListDocument;

public class FirstMyra {

	public static void main(String[] args) throws Exception {
		
		StringBuffer xmlString = getData("data/1.xml");
		
		JobListDocument jobListDocument = JobListDocument.Factory.parse(xmlString.toString());
		
		String configFile = "conf/myraConfig.xml";
		
		xmlString = FileUtils.readFile(configFile);
		
		InputStrategy inputStrategy = new InputStrategyImpl();
		
		ConfigurationManager configurationManager;

		MyraConfigDocument myraConfigDocument = null;

		try {
			myraConfigDocument = MyraConfigDocument.Factory.parse(xmlString.toString());
			configurationManager = new ConfigurationManagerImpl(myraConfigDocument);
		} catch (XmlException e) {
			e.printStackTrace();
			return;
		}

		inputStrategy.setConfigurationManager(configurationManager);
		inputStrategy.setJobListDocument(jobListDocument);
		
		final TestOutput testOutput = new TestOutput();
		
		CoreFactory coreFactory = (CoreFactory) CoreFactory.getInstance(inputStrategy, testOutput);
		
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

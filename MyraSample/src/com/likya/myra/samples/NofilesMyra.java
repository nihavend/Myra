package com.likya.myra.samples;

import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.ManagementOperations;

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

}

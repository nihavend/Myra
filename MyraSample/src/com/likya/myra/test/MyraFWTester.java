package com.likya.myra.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.MonitoringOperations;
import com.likya.myra.samples.TestOutput;

public class MyraFWTester {

	private static GlobalCarrier globalCarrier = new GlobalCarrier();
	
	public static void main(String[] args) throws Exception {

		Thread.currentThread().setName("MyraFWTester");

		String senaryoDosya = null;
		// String ayar = null;
		// String loglevel = null;

		String arg;
		int i = 0;

		while (i < args.length && args[i].startsWith("-")) {

			arg = args[i++];

			if (arg.equals("-senaryo")) {
				senaryoDosya = args[i++];
//			} else if (arg.equals("-ayar")) {
//				ayar = args[i++];
			} else if (arg.equals("-loglevel")) {
				// loglevel = args[i++];
			}
		}
		
		CoreFactory coreFactory = DataFileLoader.loadAndStart(senaryoDosya);
		
		globalCarrier.setCoreFactory(coreFactory);

		MonitoringOperations monitoringOperations = globalCarrier.getCoreFactory().getMonitoringOperations();
		
		FactoryTester.doTest(monitoringOperations, (TestOutput) coreFactory.getOutputStrategy());
		
		new Thread(new ConsoleManager()).start();
	}

	public static class ConsoleManager implements Runnable {

		public void run() {

			Thread.currentThread().setName("ConsoleManager");

			boolean runMe = true;

			CommandList.printList();

			while (runMe) {

				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

				runMe = CommandList.doCommand(globalCarrier, br);

			}

			System.out.println("Terminated !");
			System.exit(0);
		}
	}
	
}

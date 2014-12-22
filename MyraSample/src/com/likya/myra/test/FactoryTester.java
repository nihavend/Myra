package com.likya.myra.test;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.jef.core.MonitoringOperations;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.OutputData;
import com.likya.myra.samples.TestOutput;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;

public class FactoryTester {
	
	private static Logger myLogger = Logger.getRootLogger();

	public static void doTest(MonitoringOperations monitoringOperations, final TestOutput testOutput) {


		final HashMap<String, JobImpl> jobQueue = monitoringOperations.getJobQueue();

		new Thread(new Runnable() {

			@Override
			public void run() {

				Thread.currentThread().setName("TestMyra-OutputQueueLogger");
				// System.err.println("*************************************************************");

				myLogger.info("*************************************************************");

				boolean value = false;
				
				while (value) {

					String logstr_1 = "Job Id  	" + "Job Name : 		" + "Schedule :			";
					logstr_1 += "State : 	" + "Substate : 	" + "Status : " + "	Handler Uri 					";

					// System.err.println(logstr_1);

					myLogger.info(logstr_1);

					for (String key : jobQueue.keySet()) {
						JobImpl jobImpl = (JobImpl) jobQueue.get(key);

						LiveStateInfo liveStateInfo = JobHelper.getLastStateInfo(jobImpl);

						String logstr_2 = jobImpl.getAbstractJobType().getId();
						logstr_2 += "		";
						logstr_2 += jobImpl.getAbstractJobType().getBaseJobInfos().getJsName();

						String dateStr = null;
						if (jobImpl.getAbstractJobType().getManagement().getTimeManagement().getJsPlannedTime().getStartTime() != null) {
							dateStr = MyraDateUtils.getDate(jobImpl.getAbstractJobType().getManagement().getTimeManagement().getJsPlannedTime().getStartTime().getTime());
						}

						logstr_2 += "		" + dateStr;
						logstr_2 += "		" + liveStateInfo.getStateName() + "		" + liveStateInfo.getSubstateName() + "		" + liveStateInfo.getStatusName();
						logstr_2 += "		" + jobImpl.getAbstractJobType().getHandlerURI();

						// System.err.println(logstr_2);

						myLogger.info(logstr_2);
					}

					// System.err.println("*************************************************************");

					// System.err.println("Size of the output queue : " +
					// testOutput.getOutputList().size());

					myLogger.info("*************************************************************");
					myLogger.info("Size of the output queue : " + testOutput.getOutputList().size());

					while (!testOutput.getOutputList().isEmpty()) {
						OutputData outputData = testOutput.getOutputList().remove(0);

						String logstr_3 = "Job Id : " + outputData.getJobId();

						LiveStateInfo liveStateInfo = outputData.getLiveStateInfo();

						logstr_3 += "	Live State : " + (liveStateInfo.getStateName() == null ? "" : liveStateInfo.getStateName().toString()) + "-"
								+ (liveStateInfo.getSubstateName() == null ? "" : liveStateInfo.getSubstateName().toString()) + "-"
								+ (liveStateInfo.getStatusName() == null ? "" : liveStateInfo.getStatusName().toString());
						// System.err.println(logstr_3);
						myLogger.info(logstr_3);
					}

					// System.err.println("*************************************************************");
					myLogger.info("*************************************************************");
					try {
						Thread.sleep(5000);
						// System.err.print(PrintVantil.getVantil() + "\r");
						// System.err.print(".");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}
		}).start();

		return;
	}
	
}

package com.likya.myra.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.ConfigurationManagerImpl;
import com.likya.myra.jef.InputStrategy;
import com.likya.myra.jef.InputStrategyImpl;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.core.ManagementOperations;
import com.likya.myra.jef.core.MonitoringOperations;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.CoreStateInfo;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.JobRuntimeProperties;
import com.likya.myra.jef.model.OutputData;
import com.likya.myra.samples.TestOutput;
import com.likya.xsd.myra.model.config.MyraConfigDocument;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.joblist.JobListDocument;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.wlagen.TimeManagementDocument.TimeManagement;

public class CopyOfMyraFWTester {

	private static Logger myLogger = Logger.getRootLogger();

	private static CoreFactory coreFactory;

	public static class ConsoleManager implements Runnable {

		private void printList() {
			System.out.println();
			System.out.println();
			System.out.println("**************************************************");
			System.out.println("Sistem komutları :");
			System.out.println();
			System.out.println("1 : İşlem Listesi");
			System.out.println("2 : Beklemeye al");
			System.out.println("3 : Beklemeden çıkar");
			System.out.println("4 : Normal kapat");
			System.out.println("5 : Zorla Kapat");
			System.out.println("6 : Myra durumunu göster");
			System.out.println();
			System.out.println("İş komutları :");
			System.out.println();
			System.out.println("20 : İşi yeniden çalıştır");
			System.out.println("21 : İşi başarılı yap");
			System.out.println("22 : İşi atla");
			System.out.println("23 : İşi durdur");
			System.out.println("24 : İşi beklet");
			System.out.println("25 : İşi sürdür");
			System.out.println("26 : İşi başlat");
			System.out.println("27 : İşi geçersiz yap");
			System.out.println("28 : İşi geçerli yap");
			System.out.println("29 : İşe parametre ver");
			System.out.println("30 : İş durumunu göster");
			System.out.println();
			System.out.println("İzleme komutları :");
			System.out.println();
			System.out.println("40 : İş kuyruğundaki iş sayısı");
			System.out.println("41 : İş kuyruğundaki tüm işler");
			System.out.println();
			System.out.println("99 : Çıkış");
			System.out.println();
			System.out.println("**************************************************");
			System.out.println();
			System.out.println();
		}

		private String checkJobId(BufferedReader br) throws IOException {

			while (true) {
				System.out.print("Lütfen job no giriniz (Bir üst menu için Q): ");
				String jobId = br.readLine();
				if ("Q".equals(jobId.toUpperCase())) {
					break;
				}
				if (coreFactory.getMonitoringOperations().getJobQueue().containsKey(jobId)) {
					return jobId;
				} else {
					System.out.println(jobId + " numaralı bir iş bulunamadı !");
				}
			}

			return null;
		}

		public void run() {

			Thread.currentThread().setName("ConsoleManager");

			boolean runMe = true;

			printList();

			while (runMe) {

				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

				try {

					System.out.print("Lütfen bir komut giriniz : ");

					String jobId = "";

					String command = br.readLine();

					switch (command) {

					case "1":
						printList();
						break;

					case "2":
						coreFactory.getManagementOperations().suspend();
						break;

					case "3":
						coreFactory.getManagementOperations().resume();
						break;

					case "4":
						coreFactory.getManagementOperations().gracefulShutDown();
						break;

					case "5":
						coreFactory.getManagementOperations().forceFullShutDown();
						break;

					case "6":
						CoreStateInfo coreStateInfo = coreFactory.getManagementOperations().getExecutionState();
						System.out.println(coreStateInfo.toString());
						break;

					case "20":
						jobId = checkJobId(br);
						if (jobId != null) {
							coreFactory.getJobOperations().retryExecution(jobId);
						}
						break;

					case "21":
						jobId = checkJobId(br);
						if (jobId != null) {
							coreFactory.getJobOperations().setSuccess(jobId);
						}
						break;

					case "22":
						jobId = checkJobId(br);
						if (jobId != null) {
							coreFactory.getJobOperations().skipJob(jobId);
						}
						break;

					case "23":
						jobId = checkJobId(br);
						if (jobId != null) {
							coreFactory.getJobOperations().stopJob(jobId);
						}
						break;

					case "24":
						jobId = checkJobId(br);
						if (jobId != null) {
							coreFactory.getJobOperations().pauseJob(jobId);
						}
						break;

					case "25":
						jobId = checkJobId(br);
						if (jobId != null) {
							coreFactory.getJobOperations().resumeJob(jobId);
						}
						break;

					case "26":
						jobId = checkJobId(br);
						if (jobId != null) {
							coreFactory.getJobOperations().startJob(jobId);
						}
						break;

					case "27":
						jobId = checkJobId(br);
						if (jobId != null) {
							coreFactory.getJobOperations().disableJob(jobId);
						}
						break;

					case "28":
						jobId = checkJobId(br);
						if (jobId != null) {
							TimeManagement timeManagement = coreFactory.getMonitoringOperations().getJobQueue().get(jobId).getAbstractJobType().getManagement().getTimeManagement();

							if (timeManagement.getJsPlannedTime().getStartTime().before(Calendar.getInstance())) {
								while (true) {
									System.out.print("Başlama zamanı geçmiş hemen çalışsın mı ? (E, H veya Bir üst menu için Q): ");
									String elCevap = br.readLine();
									if ("Q".equals(elCevap.toUpperCase())) {
										break;
									} else if ("E".equals(elCevap.toUpperCase())) {
										System.out.print("Hemen çalışacak !");
										coreFactory.getJobOperations().enableJob(jobId);
										break;
									} else if ("H".equals(elCevap.toUpperCase())) {
										coreFactory.getJobOperations().enableJob(jobId, true);
										System.out.print("Bir sonraki zamana kuruldu !");
										break;
									}
								}
							} else {
								coreFactory.getJobOperations().enableJob(jobId);
							}
						}
						break;

					case "29":
						jobId = checkJobId(br);
						if (jobId != null) {
							while (true) {
								System.out.print("Parametereleri giriniz (Bir üst menu için ENTER):");
								String elCevap = br.readLine();
								if (elCevap.toUpperCase().length() > 0) {
									coreFactory.getJobOperations().setJobInputParam(jobId, elCevap);
									break;
								} else {
									break;
								}
							}

						}
						break;

					case "30":

						jobId = checkJobId(br);

						if (jobId != null) {

							LiveStateInfo liveStateInfo = LiveStateInfoUtils.getLastStateInfo(coreFactory.getMonitoringOperations().getJobQueue().get(jobId).getAbstractJobType());

							String sonDurum = "Son durum : ";

							if (liveStateInfo.getStateName() != null) {
								sonDurum = sonDurum + liveStateInfo.getStateName().toString();
							}

							if (liveStateInfo.getSubstateName() != null) {
								sonDurum = sonDurum + ":" + liveStateInfo.getSubstateName().toString();
							}

							if (liveStateInfo.getStatusName() != null) {
								sonDurum = sonDurum + ":" + liveStateInfo.getStatusName().toString();
							}

							System.out.println(sonDurum);
						}

						break;

					case "40":
						int sizeOf = coreFactory.getMonitoringOperations().getJobQueue().size();
						System.out.println(sizeOf);
						break;

					case "41":

						Collection<AbstractJobType> jobList = coreFactory.getNetTreeManagerInterface().getFreeJobs().values();
						System.err.println(">> Serbest İşler");
						for (AbstractJobType abstractJobType : jobList) {
							JobImpl jobImpl = coreFactory.getMonitoringOperations().getJobQueue().get(abstractJobType.getId());
							System.out.println("	>> " + (JobImpl) jobImpl);
						}

						Collection<NetTree> netTreeList = coreFactory.getNetTreeManagerInterface().getNetTreeMap().values();

						System.err.println(">> Bağımlılık Grupları");
						for (NetTree netTree : netTreeList) {
							System.err.println("	>> Grup : " + netTree.getVirtualId());
							ArrayList<AbstractJobType> abstractJobTypeList = netTree.getMembers();
							for (AbstractJobType abstractJobType : abstractJobTypeList) {
								JobImpl jobImpl = coreFactory.getMonitoringOperations().getJobQueue().get(abstractJobType.getId());
								System.out.println("		>> " + (JobImpl) jobImpl);
							}
						}

						/*
						Collection<JobImpl> jobList = coreFactory.getMonitoringOperations().getJobQueue().values();
						for(Object jobImpl : jobList.toArray()) {
							System.out.println((JobImpl)jobImpl);
						}
						*/

						break;

					case "99":
						coreFactory.getManagementOperations().forceFullShutDown();
						runMe = false;
						break;

					default:
						System.out.println("Bilinmeyen komut !");
						printList();
						break;
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			System.out.println("Terminated !");
			System.exit(0);
		}
	}

	public static void main(String[] args) throws Exception {

		Thread.currentThread().setName("MyraFWDemo");

		String senaryoDosya = null;
		String ayar = null;
		String loglevel = null;

		String arg;
		int i = 0;

		while (i < args.length && args[i].startsWith("-")) {

			arg = args[i++];

			if (arg.equals("-senaryo")) {
				senaryoDosya = args[i++];
			} else if (arg.equals("-ayar")) {
				ayar = args[i++];
			} else if (arg.equals("-loglevel")) {
				loglevel = args[i++];
			}
		}

		// Artık bu dosyalar olmadan da çalışabilir oldu !
		// if (senaryoDosya == null || ayar == null) {
		// System.err.println("Kullanım : -senaryo senaryo.xml -ayar myra.xml [-loglevel info]");
		// return;
		// }

		testFactory(senaryoDosya, ayar, loglevel);

	}

	public static StringBuffer getData(String senaryo) throws Exception {

		StringBuffer xmlString;

		if (senaryo == null) {

			// xmlString = FileUtils.readFile("/Users/serkan/git/localgit/TL-2.0.0-Test/JobList.xml");
			// xmlString = FileUtils.readFile("/Users/serkan/git/localgit/TL-2.0.0-Test/JobList02.xml");
			// xmlString = FileUtils.readFile("/Users/serkan/git/localgit/TL-2.0.0-Test/executeSch.xml");
			// xmlString = FileUtils.readFile("/Users/serkan/git/localgit/TL-2.0.0-Test/executeInShell.xml");
			// xmlString = FileUtils.readFile("/Users/serkan/git/localgit/TL-2.0.0-Test/executeInShellBulk.xml");
			// xmlString = FileUtils.readFile("/Users/serkan/git/localgit/TL-2.0.0-Test/8dep.xml");

			xmlString = FileUtils.readFile("/Users/serkan/git/localgit/TL-2.0.0-Test/xmls/2dep.xml");
		}

		xmlString = FileUtils.readFile(senaryo);

		// System.err.println(jobListDocument.toString());

		// GlobalStateDefinitionDocument globalStateDefinitionDocument = null;

		//		globalStateDefinitionDocument = GlobalStateGenerator.generate();
		//		
		//		if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), globalStateDefinitionDocument)) {
		//			throw new Exception("JobList.xml is null or damaged !");
		//		}

		// xmlString = FileUtils.readFile("/Users/serkan/git/localgit/TL-2.0.0-Test/globalStates.xml");

		// globalStateDefinitionDocument = GlobalStateDefinitionDocument.Factory.parse(xmlString.toString());

		// configurationManager.getTemporaryConfig().setGlobalStateDefinition(globalStateDefinitionDocument.getGlobalStateDefinition());

		JobListDocument jobListDocument = JobListDocument.Factory.parse(xmlString.toString());

		if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), jobListDocument)) {
			throw new Exception("JobList.xml is null or damaged !");
		}

		return xmlString;
	}

	public static void testFactory(String senaryo, String ayar, String loglevel) throws Exception {

		StringBuffer xmlString = getData(senaryo);

		JobListDocument jobListDocument = JobListDocument.Factory.parse(xmlString.toString());

		InputStrategy inputStrategy = new InputStrategyImpl();

		String configFile = ayar;

		if (configFile == null) {
			configFile = "myraConfig.xml";
		}

		xmlString = FileUtils.readFile(configFile);

		ConfigurationManager configurationManager;

		MyraConfigDocument myraConfigDocument = null;

		configurationManager = new ConfigurationManagerImpl();

		inputStrategy.setConfigurationManager(configurationManager);
		inputStrategy.setJobListDocument(jobListDocument);

		final TestOutput testOutput = new TestOutput();

		coreFactory = (CoreFactory) CoreFactory.getInstance(inputStrategy, testOutput);

		checkObject(jobListDocument);

		//		if("info".equals(loglevel)) {
		//			MyraLogManager.setLogLevelMin(MyraAppender.MYRA_CONSOLE, Level.INFO);
		//		}

		ManagementOperations managementOperations = coreFactory.getManagementOperations();
		try {
			managementOperations.start();
		} catch (Throwable e) {
			e.printStackTrace();
			return;
		}
		//		try {
		//			Thread.currentThread().join();
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}

		//

		// testOutput.getOutputMap().size();

		// coreFactory.getJobOperations().disableJob(jobName);

		MonitoringOperations monitoringOperations = coreFactory.getMonitoringOperations();

		final HashMap<String, JobImpl> jobQueue = monitoringOperations.getJobQueue();

		new Thread(new Runnable() {

			@Override
			public void run() {

				Thread.currentThread().setName("TestMyra-OutputQueueLogger");
				// System.err.println("*************************************************************");

				myLogger.info("*************************************************************");

				while (true) {

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

					// System.err.println("Size of the output queue : " + testOutput.getOutputList().size());

					myLogger.info("*************************************************************");
					myLogger.info("Size of the output queue : " + testOutput.getOutputList().size());

					while (!testOutput.getOutputList().isEmpty()) {
						OutputData outputData = testOutput.getOutputList().remove(0);

						String logstr_3 = "Job Id : " + outputData.getJobId();

						LiveStateInfo liveStateInfo = outputData.getLiveStateInfo();

						logstr_3 += "	Live State : " + (liveStateInfo.getStateName() == null ? "" : liveStateInfo.getStateName().toString()) + "-" + (liveStateInfo.getSubstateName() == null ? "" : liveStateInfo.getSubstateName().toString()) + "-"
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

					//					Iterator<String> i = testOutput.getOutputMap().keySet().iterator();
					//					while (i.hasNext()) {
					//						String str = i.next();
					//						System.err.println(str);
					//					}

					//					try {
					//						Thread.sleep(1000);
					//						// System.err.print(PrintVantil.getVantil() + "\r");
					//					} catch (InterruptedException e) {
					//						e.printStackTrace();
					//					}
				}
			}
		}).start();

		new Thread(new ConsoleManager()).start();
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

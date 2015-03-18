package com.likya.myra.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.commons.utils.XMLValidations;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.CoreStateInfo;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.joblist.JobListDocument;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.wlagen.TimeManagementDocument.TimeManagement;

public class CommandList {

	public static void printList() {

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
		System.out.println("7 : Senaryo dosyasını yükle ve çalıştır");
		System.out.println("8 : Senaryo dosyasını sil sıfırla");

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
		System.out.println("31 : Yeni iş ekle");
		System.out.println("32 : İşi güncelle");
		System.out.println("33 : İşi sil");

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

	public static boolean doCommand(GlobalCarrier globalCarrier, BufferedReader br) {

		CoreFactory coreFactory = globalCarrier.getCoreFactory();

		boolean runMe = true;

		try {

			System.out.print("Lütfen bir komut giriniz : ");

			String jobId = "";

			String command = br.readLine();

			switch (command) {

			// Sistem ile ilgili komutlar

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

			case "7": // 7 : Senaryo dosyasını yükle ve çalıştır
				coreFactory.getManagementOperations().forceFullShutDown();
				while (true) {
					System.out.println();
					System.out.print("Lütfen senaryo dosyasını giriniz, path ile birlikte (Bir üst menu için Q) : ");
					String elCevap = br.readLine();
					if ("Q".equals(elCevap.toUpperCase())) {
						break;
					} else {
						try {
							System.out.print("Loading " + elCevap + " ...");
							System.out.println(coreFactory.getMonitoringOperations().getJobQueue().size());
							coreFactory = DataFileLoader1.loadAndStart(elCevap);
							System.out.println(coreFactory.getMonitoringOperations().getJobQueue().size());
							globalCarrier.setCoreFactory(coreFactory);
							break;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				break;

			case "8": // 8 : Senaryo dosyasını sil sıfırla
				try {
					System.out.println(coreFactory.getMonitoringOperations().getJobQueue().size());
					coreFactory = DataFileLoader1.resetAndStart();
					System.out.println(coreFactory.getMonitoringOperations().getJobQueue().size());
					globalCarrier.setCoreFactory(coreFactory);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			// İşlerle ilgili komutlar

			case "20":
				jobId = checkJobId(coreFactory, br);
				if (jobId != null) {
					coreFactory.getJobOperations().retryExecution(jobId);
				}
				break;

			case "21":
				jobId = checkJobId(coreFactory, br);
				if (jobId != null) {
					coreFactory.getJobOperations().setSuccess(jobId);
				}
				break;

			case "22":
				jobId = checkJobId(coreFactory, br);
				if (jobId != null) {
					coreFactory.getJobOperations().skipJob(jobId);
				}
				break;

			case "23":
				jobId = checkJobId(coreFactory, br);
				if (jobId != null) {
					coreFactory.getJobOperations().stopJob(jobId);
				}
				break;

			case "24":
				jobId = checkJobId(coreFactory, br);
				if (jobId != null) {
					coreFactory.getJobOperations().pauseJob(jobId);
				}
				break;

			case "25":
				jobId = checkJobId(coreFactory, br);
				if (jobId != null) {
					coreFactory.getJobOperations().resumeJob(jobId);
				}
				break;

			case "26":
				jobId = checkJobId(coreFactory, br);
				if (jobId != null) {
					coreFactory.getJobOperations().startJob(jobId);
				}
				break;

			case "27":
				jobId = checkJobId(coreFactory, br);
				if (jobId != null) {
					coreFactory.getJobOperations().disableJob(jobId);
				}
				break;

			case "28":
				jobId = checkJobId(coreFactory, br);
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
				jobId = checkJobId(coreFactory, br);
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

				jobId = checkJobId(coreFactory, br);

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

			case "31": // 31 : Yeni iş ekle

				String header = "<myra:jobList xmlns:myra=\"http://www.likyateknoloji.com/myra-joblist\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
						"xmlns:myra-jobprops=\"http://www.likyateknoloji.com/myra-jobprops\" xmlns:wla=\"http://www.likyateknoloji.com/wla-gen\" " +
						"xmlns:lik=\"http://www.likyateknoloji.com/likya-gen\" xmlns:myra-stateinfo=\"http://www.likyateknoloji.com/myra-stateinfo\">";
				String footer = "</myra:jobList>";
				
				StringBuffer xmlString = new StringBuffer(header);
				
				while (true) {
					System.out.println("Lütfen yeni iş bilgilerini giriniz (Bir üst menu için İPTAL) : ");

					String elCevap = br.readLine();
					
					xmlString.append(elCevap);

					if (elCevap.toString().contains("İPTAL")) {
						System.out.println("İptal edildi bir üst menuye çıkıyor...");
						break;
					} else if (elCevap.toString().contains("</myra:genericJob>") /*&& elCevap.toString().contains("</myra:jobList>")*/) {
						xmlString.append(footer);
						System.out.println("İş alındı doğrulanıyor...");
						try {
							JobListDocument jobListDocument = JobListDocument.Factory.parse(xmlString.toString());
							if (!XMLValidations.validateWithXSDAndLog(Logger.getRootLogger(), jobListDocument)) {
								System.err.println("JobList.xml is null or damaged !");
							}
							AbstractJobType abstractJobType = jobListDocument.getJobList().getGenericJobArray()[0];
							
							coreFactory.getJobOperations().addJob(abstractJobType, false);
							
						} catch (XmlException e) {
							e.printStackTrace();
							System.err.println("Invalid job xml : " + elCevap);
							continue;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// valide et ve listeye ekle...
						break;
					}
				}

				break;

			case "32": // 32 : İşi güncelle
				break;

			case "33": // 33 : İşi sil
				break;

			// İzleme komutları

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
				 * Collection<JobImpl> jobList =
				 * coreFactory.getMonitoringOperations().getJobQueue().values();
				 * for(Object jobImpl : jobList.toArray()) {
				 * System.out.println((JobImpl)jobImpl); }
				 */

				break;

			case "99":
				coreFactory.getManagementOperations().forceFullShutDown();
				runMe = false;
				break;

			default:
				System.out.println("Bilinmeyen komut => " + command);
				// printList();
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return runMe;
	}

	public static String checkJobId(CoreFactory coreFactory, BufferedReader br) throws IOException {

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
}

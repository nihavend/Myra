package com.likya.myra.jef.utils;

import java.util.ConcurrentModificationException;
import java.util.HashMap;

import com.likya.commons.utils.DateUtils;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.OutputData;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;

public class NetTreeManagerImpl implements NetTreeManagerInterface, Runnable {

	private HashMap<String, NetTreeMonitor> netTreeMonitorMap = new HashMap<String, NetTreeMonitor>();

	private HashMap<String, NetTree> netTreeMap = new HashMap<String, NetTree>();
	private HashMap<String, String> freeJobs = new HashMap<String, String>();

	transient private Thread myExecuter;

	public NetTreeManagerImpl(AbstractJobType[] abscAbstractJobTypes) {
		super();
		refresh(abscAbstractJobTypes);
	}

	public class NetTreeMonitor implements Runnable {

		transient private Thread myExecuter;

		private boolean loop = true;

		// private NetTreeResolver.NetTree netTree;

		public NetTreeMonitor(/*NetTreeResolver.NetTree netTree*/) {
			super();
			// this.netTree = netTree;
		}

		public void run() {

			int normalFreq = 5000;
			
			Thread.currentThread().setName("NetTreeMonitor_" + DateUtils.getCurrentTimeMilliseconds()/*netTree.getVirtualId()*/);

			HashMap<String, JobImpl> jobQueue = CoreFactory.getInstance().getMonitoringOperations().getJobQueue();
			
			HashMap<String, Boolean> mailSendingMemo = new HashMap<>();
			
			int freq = 1000;
			while (loop) {
				
				try {

					for (NetTreeResolver.NetTree netTree : netTreeMap.values()) {

						boolean confirmForReset = true;
						boolean firstLevelJobStarted = false;
						mailSendingMemo.putIfAbsent(netTree.getVirtualId(), false);

						for (String jobId : netTree.getMembers()) {

							AbstractJobType abstractJobType = jobQueue.get(jobId).getAbstractJobType();
							
							boolean isDeadBeanch =   abstractJobType.getGraphInfo().getDeadBranch();

							if (isDeadBeanch) {
								// he branch of live is dead due to dependency
								// decision, so continue to next
								continue;
							}

							LiveStateInfo lastLiveStateInfo = LiveStateInfoUtils.getLastStateInfo(abstractJobType);

							boolean isPending = LiveStateInfoUtils.equalStates(lastLiveStateInfo, StateName.PENDING);

							if (!isPending) {
								// One of the branch(s) is started, lower the
								// check interval
								freq = 1000;
							}
							
							firstLevelJobStarted = ((abstractJobType.getDependencyList() == null || abstractJobType.getDependencyList().sizeOfItemArray() == 0) && LiveStateInfoUtils.equalStates(lastLiveStateInfo, StateName.RUNNING)) ? true : false;
							
							boolean isFinished = LiveStateInfoUtils.equalStates(lastLiveStateInfo, StateName.FINISHED);
							boolean isLastJobOfBranch = abstractJobType.getGraphInfo().getLastNodeOfBranch();
							boolean isBlockBranchOnFail = abstractJobType.getGraphInfo().getBlockBranchOnFail();

							boolean secondCond = (isFinished && isLastJobOfBranch && isBlockBranchOnFail && StatusName.FAILED.equals(lastLiveStateInfo.getStatusName()));

							if (!isFinished || secondCond) {
								confirmForReset = false;
								break;
							}
							
						}
						
						if(!mailSendingMemo.get(netTree.getVirtualId()) && firstLevelJobStarted) {
							CoreFactory.getInstance().getOutputStrategy().sendDataObject(new OutputData(OutputData.types.BEGINOFCYCLE, netTree));
							mailSendingMemo.put(netTree.getVirtualId(), true);
						}
						
						if (confirmForReset) {
							for (String jobId : netTree.getMembers()) {
								// System.err.println("Reset all NetTree members functionality not implemented yet !");
								AbstractJobType abstractJobType = jobQueue.get(jobId).getAbstractJobType();
								synchronized (abstractJobType) {
									JobHelper.resetJob(abstractJobType);
								}
							}
							freq = normalFreq;
							CoreFactory.getInstance().getOutputStrategy().sendDataObject(new OutputData(OutputData.types.ENDOFCYCLE, netTree));
							mailSendingMemo.put(netTree.getVirtualId(), false);

						}

					}

					Thread.sleep(freq);
					
				} catch (InterruptedException e) {
					CoreFactory.getLogger().info(e.getMessage());
				} catch (ConcurrentModificationException c) {
					// DO NOTHING
				}
			}
		}

		public Thread getMyExecuter() {
			return myExecuter;
		}

		public void setMyExecuter(Thread myExecuter) {
			this.myExecuter = myExecuter;
		}

		public void setLoop(boolean loop) {
			this.loop = loop;
		}

	}

	public void startMe() {
		myExecuter = new Thread(this);
		myExecuter.start();
	}
	
	public void refresh(AbstractJobType[] abscAbstractJobTypes) {
		StringBuilder logString;
		try {
			netTreeMap.clear();
			freeJobs.clear();
			logString = NetTreeResolver.runAlgorythm(abscAbstractJobTypes, netTreeMap, freeJobs);
			CoreFactory.getLogger().info(logString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {

		Thread.currentThread().setName("NetTreeManagerImp" + DateUtils.getCurrentTimeMilliseconds());
		
		//for (NetTreeResolver.NetTree netTree : netTreeMap.values()) {
			NetTreeMonitor netTreeMonitor = new NetTreeMonitor(/*netTree*/);
			Thread starterThread = new Thread(netTreeMonitor);
			netTreeMonitor.setMyExecuter(starterThread);
			starterThread.setDaemon(true);
			netTreeMonitorMap.put(netTreeMonitor.myExecuter.getName(), netTreeMonitor);
			netTreeMonitor.getMyExecuter().start();
		//}

	}

	public HashMap<String, NetTree> getNetTreeMap() {
		return netTreeMap;
	}

	public HashMap<String, String> getFreeJobs() {
		return freeJobs;
	}

	public HashMap<String, NetTreeMonitor> getNetTreeMonitorMap() {
		return netTreeMonitorMap;
	}

	public Thread getMyExecuter() {
		return myExecuter;
	}

}

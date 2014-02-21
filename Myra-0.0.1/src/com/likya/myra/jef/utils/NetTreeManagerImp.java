package com.likya.myra.jef.utils;

import java.util.HashMap;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;

public class NetTreeManagerImp implements NetTreeManagerInterface, Runnable {

	private HashMap<String, NetTreeMonitor> netTreeMonitorMap = new HashMap<String, NetTreeMonitor>();

	private HashMap<String, NetTree> netTreeMap = new HashMap<String, NetTree>();
	private HashMap<String, AbstractJobType> freeJobs = new HashMap<String, AbstractJobType>();

	transient private Thread myExecuter;

	public NetTreeManagerImp(AbstractJobType[] abscAbstractJobTypes) {
		super();
		StringBuilder logString;
		try {
			logString = NetTreeResolver.runAlgorythm(abscAbstractJobTypes, netTreeMap, freeJobs);
			CoreFactory.getLogger().info(logString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class NetTreeMonitor implements Runnable {

		transient private Thread myExecuter;
		
		private boolean loop = true;

		private NetTreeResolver.NetTree netTree;

		public NetTreeMonitor(NetTreeResolver.NetTree netTree) {
			super();
			this.netTree = netTree;
		}

		public void run() {
			
			Thread.currentThread().setName("NetTreeMonitor_" + netTree.getVirtualId());
			
			int freq = 1000;
			while (loop) {
				try {
					boolean isAllFinished = true;
					for (AbstractJobType abstractJobType : netTree.getMembers()) {
						boolean isFinished = LiveStateInfoUtils.equalStates(abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0), StateName.FINISHED);
						boolean isPending = LiveStateInfoUtils.equalStates(abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0), StateName.PENDING);
						if (!isFinished) {
							isAllFinished = false;
						}
						if (!isPending) {
							freq = 1000;
						}
					}

					if (isAllFinished) {
						for (AbstractJobType abstractJobType : netTree.getMembers()) {
							// System.err.println("Reset all NetTree members functionality not implemented yet !");
							JobHelper.resetJob(abstractJobType);
						}
						freq = 60000;
					}

					Thread.sleep(freq);
				} catch (InterruptedException e) {
					CoreFactory.getLogger().info(e.getMessage());
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

	public void run() {
		
		Thread.currentThread().setName("NetTreeManagerImp" + System.currentTimeMillis());
		
		for (NetTreeResolver.NetTree netTree : netTreeMap.values()) {
			NetTreeMonitor netTreeMonitor = new NetTreeMonitor(netTree);
			netTreeMonitorMap.put(netTree.getVirtualId(), netTreeMonitor);
			
			Thread starterThread = new Thread(netTreeMonitor);
			netTreeMonitor.setMyExecuter(starterThread);
			starterThread.setDaemon(true);
			
			netTreeMonitor.getMyExecuter().start();
		}

	}

	public HashMap<String, NetTree> getNetTreeMap() {
		return netTreeMap;
	}

	public HashMap<String, AbstractJobType> getFreeJobs() {
		return freeJobs;
	}

	public HashMap<String, NetTreeMonitor> getNetTreeMonitorMap() {
		return  netTreeMonitorMap;
	}

	public Thread getMyExecuter() {
		return myExecuter;
	}

}

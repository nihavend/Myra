package com.likya.myra.jef.utils;

import java.util.HashMap;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.jobs.JobHelper;
import com.likya.myra.jef.model.OutputData;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class NetTreeManagerImp implements NetTreeManagerInterface, Runnable {

	private HashMap<String, NetTreeMonitor> netTreeMonitorMap = new HashMap<String, NetTreeMonitor>();

	private HashMap<String, NetTree> netTreeMap = new HashMap<String, NetTree>();
	private HashMap<String, AbstractJobType> freeJobs = new HashMap<String, AbstractJobType>();

	private Thread myThread;

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

	private class NetTreeMonitor implements Runnable {

		private boolean loop = true;

		private NetTreeResolver.NetTree netTree;

		public NetTreeMonitor(NetTreeResolver.NetTree netTree) {
			super();
			this.netTree = netTree;
		}

		public void run() {
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
							if (Scheduler.scheduleForNextExecution(abstractJobType)) {
								JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_BYTIME);
								OutputData outputData = OutputData.generateDefault(abstractJobType);
								CoreFactory.getInstance().getOutputStrategy().sendDataObject(outputData);
								CoreFactory.getLogger().info("Job id :" + abstractJobType.getId() + " is scheduled for new time " + abstractJobType.getManagement().getTimeManagement().getJsPlannedTime());
							}
						}
						freq = 60000;
					}

					Thread.sleep(freq);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void startMe() {
		myThread = new Thread(this);
		myThread.start();
	}

	public void run() {
		for (NetTreeResolver.NetTree netTree : netTreeMap.values()) {
			NetTreeMonitor netTreeMonitor = new NetTreeMonitor(netTree);
			netTreeMonitorMap.put(netTree.getVirtualId(), netTreeMonitor);
			new Thread(netTreeMonitor).start();
		}

	}

	public HashMap<String, NetTree> getNetTreeMap() {
		return netTreeMap;
	}

	public HashMap<String, AbstractJobType> getFreeJobs() {
		return freeJobs;
	}

}

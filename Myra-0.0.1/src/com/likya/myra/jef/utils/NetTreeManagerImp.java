package com.likya.myra.jef.utils;

import java.util.HashMap;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.NetTreeResolver;
import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;

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
			while(loop) {
				try {
					boolean isAllFinished = true;
					for(AbstractJobType abstractJobType : netTree.getMembers()) {
						boolean isEqual = LiveStateInfoUtils.equalStates(abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0), StateName.FINISHED);
						if(!isEqual) {
							isAllFinished = false;
						}
					}
					
					if(isAllFinished) {
						// reset NetTree
					}
					
					Thread.sleep(1000);
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

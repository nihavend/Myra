package com.likya.myra.jef.utils;

import java.util.HashMap;

import com.likya.myra.commons.utils.NetTreeResolver.NetTree;
import com.likya.myra.jef.utils.NetTreeManagerImpl.NetTreeMonitor;

public interface NetTreeManagerInterface {

	public HashMap<String, NetTree> getNetTreeMap();

	public HashMap<String, String> getFreeJobs();
	
	public HashMap<String, NetTreeMonitor> getNetTreeMonitorMap();
		
	public void startMe();
	
	public Thread getMyExecuter();
}

package com.likya.myra.jef.model;

import java.util.HashMap;

public class TemporaryConfig {

	private String fileToPersist = "Tlos.recover";
	private boolean isPersistent;
	
	private int logBufferSize;
	
	private HashMap<Integer, String> groupList = new HashMap<Integer, String>();
	
	private int lowerLimit;
	private int higherLimit;
	
	public boolean isPersistent() {
		return isPersistent;
	}

	public void setPersistent(boolean isPersistent) {
		this.isPersistent = isPersistent;
	}

	public String getFileToPersist() {
		return fileToPersist;
	}

	public void setFileToPersist(String fileToPersist) {
		this.fileToPersist = fileToPersist;
	}

	public HashMap<Integer, String> getGroupList() {
		return groupList;
	}

	public void setGroupList(HashMap<Integer, String> groupList) {
		this.groupList = groupList;
	}

	public int getLogBufferSize() {
		return logBufferSize;
	}

	public void setLogBufferSize(int logBufferSize) {
		this.logBufferSize = logBufferSize;
	}

	public int getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(int lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	public int getHigherLimit() {
		return higherLimit;
	}

	public void setHigherLimit(int higherLimit) {
		this.higherLimit = higherLimit;
	}
	
}
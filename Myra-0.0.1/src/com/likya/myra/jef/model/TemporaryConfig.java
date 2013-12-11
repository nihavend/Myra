/*******************************************************************************
 * Copyright 2013 Likya Teknoloji
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

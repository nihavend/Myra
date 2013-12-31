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

import java.io.Serializable;
import java.util.HashMap;

import com.likya.myra.jef.jobs.JobImpl;

public class PersistObject implements Serializable {

	private static final long serialVersionUID = -7212438515231106250L;

	private String tlosVersion;
	private HashMap<String, JobImpl> jobQueue;
	private HashMap<Integer, String> groupList;

	public String getTlosVersion() {
		return tlosVersion;
	}

	public void setTlosVersion(String tlosVersion) {
		this.tlosVersion = tlosVersion;
	}

	public HashMap<String, JobImpl> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(HashMap<String, JobImpl> jobQueue) {
		this.jobQueue = jobQueue;
	}

	public HashMap<Integer, String> getGroupList() {
		return groupList;
	}

	public void setGroupList(HashMap<Integer, String> groupList) {
		this.groupList = groupList;
	}
}

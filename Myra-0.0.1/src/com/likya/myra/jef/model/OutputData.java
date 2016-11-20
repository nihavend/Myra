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
import java.util.Calendar;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;

public class OutputData implements Serializable {

	private static final long serialVersionUID = -6075964245318352700L;

	public static enum types {
		DEFAULT(0), JOSTATECHANGE(10), LOGANALYZER(20);
		
		private int value;

		private types(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
		
	};
	
	private types outputType;
	
	private String jobId;
	private String handleUri;
	private String treeId;
	private String groupName;

	private Calendar startTime;
	private Calendar stopTime;

	private LiveStateInfo liveStateInfo;
	
	private Object outputContent;

	public OutputData(types outputType, Object outputContent, String jobId, String handleUri, String treeId, String groupName, Calendar startTime, Calendar stopTime, LiveStateInfo liveStateInfo) {
		super();
		this.outputType = outputType;
		this.outputContent = outputContent;
		this.jobId = jobId;
		this.handleUri = handleUri;
		this.treeId = treeId;
		this.groupName = groupName;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.liveStateInfo = liveStateInfo;
	}
	
	public OutputData(types outputType, Object outputContent) {
		super();
		this.outputType = outputType;
		this.outputContent = outputContent;
	}
	
	public OutputData() {
		
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getHandleUri() {
		return handleUri;
	}

	public void setHandleUri(String handleUri) {
		this.handleUri = handleUri;
	}

	public String getTreeId() {
		return treeId;
	}

	public void setTreeId(String treeId) {
		this.treeId = treeId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public Calendar getStopTime() {
		return stopTime;
	}

	public void setStopTime(Calendar stopTime) {
		this.stopTime = stopTime;
	}

	public LiveStateInfo getLiveStateInfo() {
		return liveStateInfo;
	}

	public void setLiveStateInfo(LiveStateInfo liveStateInfo) {
		this.liveStateInfo = liveStateInfo;
	}
	
	public static OutputData generateDefault(AbstractJobType abstractJobType) {

		OutputData outputData = new OutputData();

		outputData.setGroupName("");
		outputData.setHandleUri(abstractJobType.getHandlerURI());
		outputData.setJobId(abstractJobType.getId());
		
		if(abstractJobType.getManagement().getTimeManagement().getJsRecordedTime() != null) {
			outputData.setOutputType(OutputData.types.DEFAULT);
	 		outputData.setStartTime(abstractJobType.getManagement().getTimeManagement().getJsRecordedTime().getStartTime());
			outputData.setStopTime(abstractJobType.getManagement().getTimeManagement().getJsRecordedTime().getStopTime());
		} else {
			outputData.setOutputType(OutputData.types.JOSTATECHANGE);
		}

		outputData.setTreeId("treeId");
		outputData.setLiveStateInfo(LiveStateInfoUtils.getLastStateInfo(abstractJobType));

		return outputData;

	}

	public types getOutputType() {
		return outputType;
	}

	public void setOutputType(types outputType) {
		this.outputType = outputType;
	}

	public Object getOutputContent() {
		return outputContent;
	}

	public void setOutputContent(Object outputContent) {
		this.outputContent = outputContent;
	}
}

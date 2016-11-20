package com.likya.myra.jef.utils.timeschedules;

import java.util.ArrayList;
import java.util.Calendar;

import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.wlagen.JsExecutionTimeFrameDocument.JsExecutionTimeFrame;

public class ExecutionTimeFrameValidator {
	
	public static boolean validateEndTime(AbstractJobType abstractJobType, ArrayList<String> messages) {
		
		JsExecutionTimeFrame jsExecutionTimeFrame = abstractJobType.getManagement().getTimeManagement().getJsExecutionTimeFrame();
		
		if(jsExecutionTimeFrame != null) {
			if(jsExecutionTimeFrame.getStopTime() != null) {
				if(jsExecutionTimeFrame.getStopTime().before(Calendar.getInstance())) {
					if(messages != null) {
						messages.add("Execution Time Frame End Boundary is passed, not scheduled !");
						messages.add(("Calculated schedule >> " + MyraDateUtils.getDate(Calendar.getInstance()) + " is after Execution Time Frame End Time : " + MyraDateUtils.getDate(jsExecutionTimeFrame.getStopTime())));
					}
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static boolean validateStartTime(AbstractJobType abstractJobType, ArrayList<String> messages) {
		
		JsExecutionTimeFrame jsExecutionTimeFrame = abstractJobType.getManagement().getTimeManagement().getJsExecutionTimeFrame();
		
		if(jsExecutionTimeFrame != null) {
			if(jsExecutionTimeFrame.getStartTime() != null) {
				if(jsExecutionTimeFrame.getStartTime().after(Calendar.getInstance())) {
					if(messages != null) {
						messages.add("Execution Time Frame Start Boundary is not passed yet, not scheduled !");
						messages.add(("Calculated schedule >> " + MyraDateUtils.getDate(Calendar.getInstance()) + " is before Execution Time Frame Start Time : " + MyraDateUtils.getDate(jsExecutionTimeFrame.getStartTime())));
					}
					return false;
				}
			}
		}
		
		return true;
	}
	
}

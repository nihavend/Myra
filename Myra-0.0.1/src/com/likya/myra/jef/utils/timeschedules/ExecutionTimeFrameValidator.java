package com.likya.myra.jef.utils.timeschedules;

import java.util.ArrayList;

import com.likya.commons.utils.DateUtils;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.wlagen.JsExecutionTimeFrameDocument.JsExecutionTimeFrame;

public class ExecutionTimeFrameValidator {

	public static boolean validateEndTime(AbstractJobType abstractJobType) {

		JsExecutionTimeFrame jsExecutionTimeFrame = abstractJobType.getManagement().getTimeManagement().getJsExecutionTimeFrame();

		if (jsExecutionTimeFrame != null) {
			if (jsExecutionTimeFrame.getStopTime() != null) {
				if (jsExecutionTimeFrame.getStopTime().before(DateUtils.getCalendarInstance())) {
					CoreFactory.getLogger().warn("Execution Time Frame End Boundary is passed, not scheduled !");
					CoreFactory.getLogger().warn("Calculated schedule >> " + MyraDateUtils.getDate(DateUtils.getCalendarInstance()) + " is after Execution Time Frame End Time : " + MyraDateUtils.getDate(jsExecutionTimeFrame.getStopTime()));
					return false;
				}
			}
		}

		return true;
	}

	public static boolean validateStartTime(AbstractJobType abstractJobType, ArrayList<String> messages) {

		JsExecutionTimeFrame jsExecutionTimeFrame = abstractJobType.getManagement().getTimeManagement().getJsExecutionTimeFrame();

		if (jsExecutionTimeFrame != null) {
			if (jsExecutionTimeFrame.getStartTime() != null) {
				if (jsExecutionTimeFrame.getStartTime().after(DateUtils.getCalendarInstance())) {
					CoreFactory.getLogger().warn("Execution Time Frame Start Boundary is not passed yet, not scheduled !");
					CoreFactory.getLogger().warn("Calculated schedule >> " + MyraDateUtils.getDate(DateUtils.getCalendarInstance()) + " is before Execution Time Frame Start Time : " + MyraDateUtils.getDate(jsExecutionTimeFrame.getStartTime()));
					return false;
				}
			}
		}

		return true;
	}
}

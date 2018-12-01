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

package com.likya.myra.jef.utils.timeschedules;

import java.util.Calendar;

import org.apache.xmlbeans.GDuration;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.jobs.ChangeLSI;
import com.likya.myra.jef.model.Forwarder;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.PeriodInfoDocument.PeriodInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.wlagen.TriggerDocument.Trigger;

public class TimeScheduler extends TimeSchedulerBase implements TimeSchedulerInterface {

	public static boolean scheduleForNextExecution(AbstractJobType abstractJobType) {

		if(!ExecutionTimeFrameValidator.validateEndTime(abstractJobType)) {
			return false;
		}
		
		//UserTrigger job fail durumunda setSuccess ve skip edilirken sonraki çalışma zamanına kurulMAMALI!
		if(abstractJobType.getManagement().getTrigger().intValue() == Trigger.INT_USER) {
			return true;
		}
		
		PeriodInfo periodInfo = abstractJobType.getManagement().getPeriodInfo();
		// TODO Special care for daily jobs, may be done compatible with general periodicity
		if (periodInfo == null || periodInfo.getStep() == null || periodInfo.getStep().equals(new GDuration("P1D"))) {
			
			// Calendar selectedSchedule = regularSchedule(abstractJobType);
			Forwarder regularForwarder = regularSchedule(abstractJobType);
			
			// if (selectedSchedule != null /*&& selectedSchedule.after(DateUtils.getCalendarInstance())*/) {
			if (regularForwarder.equals(Forwarder.CALENDAR_CALCULATED)) {
				Calendar selectedSchedule = (Calendar) regularForwarder.getObject();
				abstractJobType.getManagement().getTimeManagement().getJsActualTime().setStartTime(selectedSchedule);
				if(!ExecutionTimeFrameValidator.validateEndTime(abstractJobType)) {
					abstractJobType.getManagement().getTimeManagement().getJsActualTime().setStartTime(null);
					return false;
				}
			} else {
				// yeni zamana kurulmadı, artık çalışmayacak
				ChangeLSI.forValue(abstractJobType, LiveStateInfoUtils.generateLiveStateInfo(StateName.INT_PENDING, SubstateName.INT_DEACTIVATED));
				return false;
			}
		} else {
			if(periodicSchedule(abstractJobType)) {
				if(!ExecutionTimeFrameValidator.validateEndTime(abstractJobType)) {
					abstractJobType.getManagement().getTimeManagement().getJsActualTime().setStartTime(null);
					return false;
				}
			} else {
				// yeni zamana kurulmadı, artık çalışmayacak
				ChangeLSI.forValue(abstractJobType, LiveStateInfoUtils.generateLiveStateInfo(StateName.INT_PENDING, SubstateName.INT_DEACTIVATED));
				return false;
			}
		}

		return true;
	}

	public static Calendar addPeriod(Calendar startDateTime, long period, String selectedTZone) {
		return PeriodCalculations.addPeriod(startDateTime, period, selectedTZone);
	}
	
	public static long getDurationInMilliSecs(GDuration gDuration) {
		return PeriodCalculations.getDurationInMilliSecs(gDuration);
	}
}

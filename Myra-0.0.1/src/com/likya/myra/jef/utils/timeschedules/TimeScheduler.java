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

import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.PeriodInfoDocument.PeriodInfo;

public class TimeScheduler extends TimeSchedulerBase implements TimeSchedulerInterface {

	public static boolean scheduleForNextExecution(AbstractJobType abstractJobType) {

		boolean retValue = true;

		PeriodInfo periodInfo = abstractJobType.getManagement().getPeriodInfo();
		// TODO Special care for daily jobs, may be done compatible with general periodicity
		if (periodInfo == null || periodInfo.getStep() == null || periodInfo.getStep().equals(new GDuration("P1D"))) {
			Calendar selectedSchedule = regularSchedule(abstractJobType);
			if (selectedSchedule != null /*&& selectedSchedule.after(Calendar.getInstance())*/) {
				abstractJobType.getManagement().getTimeManagement().getJsActualTime().setStartTime(selectedSchedule);
			} else {
				// yeni zamana kurulmadı, artık çalışmayacak
				retValue = false;
			}
		} else {
			retValue = periodicSchedule(abstractJobType);
		}

		return retValue;
	}

}

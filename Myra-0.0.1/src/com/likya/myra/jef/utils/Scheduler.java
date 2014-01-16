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

package com.likya.myra.jef.utils;

import java.util.Arrays;
import java.util.Calendar;

import com.likya.myra.commons.utils.PeriodCalculations;
import com.likya.myra.commons.utils.RestrictedDailyIterator;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.DaysOfMonthDocument.DaysOfMonth;
import com.likya.xsd.myra.model.jobprops.ScheduleInfoDocument.ScheduleInfo;

public class Scheduler {

	public static boolean scheduleForNextExecution(AbstractJobType abstractJobType) {

		boolean retValue = true;

		if (abstractJobType.getManagement().getPeriodInfo() != null) {
			retValue = periodicSchedule(abstractJobType);
		} else {
			Calendar selectedSchedule = regularSchedule(abstractJobType);
			abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().setStartTime(selectedSchedule);
		}

		return retValue;
	}

	private static boolean periodicSchedule(AbstractJobType abstractJobType) {

		boolean retValue = true;

		Calendar nextPeriodTime = PeriodCalculations.forward(abstractJobType);

		if (nextPeriodTime == null) {
			Calendar selectedSchedule = regularSchedule(abstractJobType);
			if (selectedSchedule != null && selectedSchedule.after(Calendar.getInstance()) && !selectedSchedule.after(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStopTime())) {
				abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().setStartTime(selectedSchedule);
				// yeni zamana kuruldu
			} else {
				// yeni zamana kurulmadı, artık çalışmayacak
				retValue = false;
			}
		}

		return retValue;
	}

	private static Calendar regularSchedule(AbstractJobType abstractJobType) {

		Calendar tmpCal = abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime();

		Calendar selectedSchedule = null;

		ScheduleInfo scheduleInfo = abstractJobType.getScheduleInfo();

		int daysOfWeek[] = scheduleInfo.getDaysOfWeekIntTypeArray();

		RestrictedDailyIterator restrictedDailyIterator = new RestrictedDailyIterator(tmpCal.get(Calendar.HOUR_OF_DAY), tmpCal.get(Calendar.MINUTE), tmpCal.get(Calendar.SECOND), daysOfWeek);
		Calendar restCal = restrictedDailyIterator.next();
		selectedSchedule = restCal;

		DaysOfMonth daysOfMonth = scheduleInfo.getDaysOfMonth();

		if (daysOfMonth != null) {
			
			int dayList[] = daysOfMonth.getDaysArray();

			if (Arrays.binarySearch(dayList, tmpCal.get(Calendar.DAY_OF_MONTH)) > 0) {
				if (tmpCal.before(selectedSchedule)) {
					selectedSchedule = tmpCal;
				}
			}

			String firstDay = daysOfMonth.getFirstDayOfMonth();

			if (firstDay != null && !firstDay.equals("")) {
				Calendar localCal = Calendar.getInstance();
				int lastDayOfMonth = localCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				localCal.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
				if (localCal.before(selectedSchedule)) {
					selectedSchedule = localCal;
				}
			}

			String lastDay = daysOfMonth.getLastDayOfMonth();

			if (lastDay != null && !lastDay.equals("")) {
				Calendar localCal = Calendar.getInstance();
				int lastDayOfMonth = localCal.getActualMinimum(Calendar.DAY_OF_MONTH);
				localCal.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
				if (localCal.before(selectedSchedule)) {
					selectedSchedule = localCal;
				}
			}
		}

		// System.err.println(MyraDateUtils.getDate(selectedSchedule.getTime()));

		return selectedSchedule;

	}
}

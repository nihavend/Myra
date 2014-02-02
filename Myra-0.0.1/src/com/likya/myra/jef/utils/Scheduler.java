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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.commons.utils.PeriodCalculations;
import com.likya.myra.commons.utils.RestrictedDailyIterator;
import com.likya.myra.jef.core.CoreFactory;
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
			if (selectedSchedule != null /*&& selectedSchedule.after(Calendar.getInstance())*/) {
				abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().setStartTime(selectedSchedule);
			} else {
				// yeni zamana kurulmadı, artık çalışmayacak
				retValue = false;
			}
		}

		return retValue;
	}

	private static boolean periodicSchedule(AbstractJobType abstractJobType) {

		boolean retValue = true;

		Calendar nextPeriodTime = PeriodCalculations.forward(abstractJobType);

		if (nextPeriodTime == null) {
			Calendar bornedCal = abstractJobType.getManagement().getTimeManagement().getBornedPlannedTime().getStartTime();
			// System.err.println(c);
			//c.add(Calendar.DAY_OF_MONTH, 1);
			//abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().setStartTime(c);
			// System.err.println(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime());
			Calendar selectedSchedule = regularSchedule(abstractJobType);
			if (selectedSchedule != null /*&& selectedSchedule.after(Calendar.getInstance())*/) {
				MyraDateUtils.setTimePart(bornedCal, selectedSchedule);
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

		Calendar jsPlannedStartTime = abstractJobType.getManagement().getTimeManagement().getBornedPlannedTime().getStartTime();

		Calendar selectedSchedule = null;

		ArrayList<Calendar> floatingSchedules = new ArrayList<Calendar>();

		ScheduleInfo scheduleInfo = abstractJobType.getScheduleInfo();

		int hourOfPlannedTime = jsPlannedStartTime.get(Calendar.HOUR_OF_DAY);
		int minuteOfPlannedTime = jsPlannedStartTime.get(Calendar.MINUTE);
		int secondOfPlannedTime = jsPlannedStartTime.get(Calendar.SECOND);

		int daysOfWeek[] = scheduleInfo.getDaysOfWeekIntTypeArray();

		RestrictedDailyIterator restrictedDailyIterator = null;

		Calendar restCal = null;

		if (daysOfWeek.length > 0) {
			restrictedDailyIterator = new RestrictedDailyIterator(hourOfPlannedTime, minuteOfPlannedTime, secondOfPlannedTime, daysOfWeek);
			restCal = restrictedDailyIterator.next();
			floatingSchedules.add(restCal);
			CoreFactory.getLogger().debug("Option for daysOfWeek : " + MyraDateUtils.getDate(restCal));
		}

		DaysOfMonth daysOfMonth = scheduleInfo.getDaysOfMonth();

		if (daysOfMonth != null) {

			int dayList[] = daysOfMonth.getDaysArray();

			String firstDay = daysOfMonth.getFirstDayOfMonth();

			String lastDay = daysOfMonth.getLastDayOfMonth();

			if (dayList.length > 0) {
				restrictedDailyIterator = new RestrictedDailyIterator(hourOfPlannedTime, minuteOfPlannedTime, secondOfPlannedTime, dayList);
				restCal = restrictedDailyIterator.next(Calendar.DAY_OF_MONTH);
				floatingSchedules.add(restCal);
				CoreFactory.getLogger().debug("Option for daysOfMonth : " + MyraDateUtils.getDate(restCal));
			}

			if (firstDay != null) {
				int firstDayOfMonth = 1;
				restCal = MyraDateUtils.setTimePart(jsPlannedStartTime);
				restCal.set(Calendar.MONTH, restCal.get(Calendar.MONTH) + 1);
				restCal.set(Calendar.DAY_OF_MONTH, firstDayOfMonth);
				floatingSchedules.add(restCal);
				CoreFactory.getLogger().debug("Option for firstDayOfMonth : " + MyraDateUtils.getDate(restCal));
			}

			if (lastDay != null) {
				int lastDayOfMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
				if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == lastDayOfMonth) {
					restCal.set(Calendar.MONTH, restCal.get(Calendar.MONTH) + 1);
					lastDayOfMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
				}
				restCal = MyraDateUtils.setTimePart(jsPlannedStartTime);
				restCal.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
				floatingSchedules.add(restCal);
				CoreFactory.getLogger().debug("Option for lastDayOfMonth : " + MyraDateUtils.getDate(restCal));
			}

		}

		Calendar[] sortedCals = floatingSchedules.toArray(new Calendar[0]);

		// System.err.println(sortedCals[0].getTime());
		// System.err.println(sortedCals[1].getTime());
		// System.err.println(sortedCals[2].getTime());		

		Arrays.sort(sortedCals);

		// System.err.println(sortedCals[0].getTime());
		// System.err.println(sortedCals[1].getTime());
		// System.err.println(sortedCals[2].getTime());

		// System.err.println(MyraDateUtils.getDate(selectedSchedule.getTime()));

		if (sortedCals.length > 0) {
			selectedSchedule = Calendar.getInstance();
			selectedSchedule.setTime(sortedCals[0].getTime());
			CoreFactory.getLogger().debug("Minimum of options : " + MyraDateUtils.getDate(selectedSchedule));
		}

		return selectedSchedule;

	}
}

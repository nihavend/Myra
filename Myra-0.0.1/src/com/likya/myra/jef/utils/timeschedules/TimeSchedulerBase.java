package com.likya.myra.jef.utils.timeschedules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import com.likya.commons.utils.DateUtils;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.commons.utils.RestrictedDailyIterator;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.Forwarder;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.DaysOfMonthDocument.DaysOfMonth;
import com.likya.xsd.myra.model.jobprops.PeriodInfoDocument.PeriodInfo;
import com.likya.xsd.myra.model.jobprops.ScheduleInfoDocument.ScheduleInfo;

public class TimeSchedulerBase {

	
	protected static boolean periodicSchedule(AbstractJobType abstractJobType) {

		boolean retValue = true;

		ArrayList<String> errorMessages = new ArrayList<String>();

		// Calendar nextPeriodTime = PeriodCalculations.forward(abstractJobType, errorMessages);
		Forwarder forwarder = PeriodCalculations.forward(abstractJobType, errorMessages);

		for (String message : errorMessages) {
			CoreFactory.getLogger().warn(message);
		}
		
		if (forwarder.equals(Forwarder.MAX_COUNT_EXCEEDED)) {
			// yeni zamana kurulmadı, artık çalışmayacak
			retValue = false;
		} else if (forwarder.equals(Forwarder.CALENDAR_NOT_CALCULATED)) { // if (nextPeriodTime == null) { // CALENDAR_NOT_CALCULATED olduğundan kesin null

			// Calendar selectedSchedule = regularSchedule(abstractJobType);
			Forwarder regularForwarder = regularSchedule(abstractJobType);

			// if (selectedSchedule != null /* && // selectedSchedule.after(DateUtils.getCalendarInstance()) */) {
			if (regularForwarder.equals(Forwarder.CALENDAR_CALCULATED)) {
				Calendar selectedSchedule = (Calendar) regularForwarder.getObject();
				Calendar bornedCal = abstractJobType.getManagement().getTimeManagement().getJsScheduledTime().getStartTime();
				// System.err.println(c);
				// c.add(Calendar.DAY_OF_MONTH, 1);
				// abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().setStartTime(c);
				// System.err.println(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime());
				MyraDateUtils.setTimePart(bornedCal, selectedSchedule);
				abstractJobType.getManagement().getTimeManagement().getJsActualTime().setStartTime(selectedSchedule);
				// yeni zamana kuruldu
			} else {
				// forwarder.equals(Forwarder.CALENDAR_NOT_CALCULATED) veya forwarder.equals(Forwarder.MAX_COUNT_EXCEEDED)
				// yeni zamana kurulmadı, artık çalışmayacak
				retValue = false;
			}
			// }

		} else if (forwarder.equals(Forwarder.CALENDAR_CALCULATED)) {
			// bu durum da true ile çıkıyoruz
		}

		return retValue;
	}
		
	protected static Forwarder regularSchedule(AbstractJobType abstractJobType) {

		ScheduleInfo scheduleInfo = abstractJobType.getScheduleInfo();
		
		if(scheduleInfo == null) {
			String msgTxt = "No scheduling rule is defined !";
			CoreFactory.getLogger().warn(msgTxt);
			// return null;
			Forwarder forwarder = Forwarder.CALENDAR_NOT_CALCULATED;
			forwarder.setObject(msgTxt);
			return forwarder;
		}
		
		PeriodInfo periodInfo = abstractJobType.getManagement().getPeriodInfo();

		if (PeriodCalculations.isMaxCountExceeded(periodInfo)) {
			Forwarder forwarder = Forwarder.MAX_COUNT_EXCEEDED;
			// CoreFactory.getLogger().warn("Execution count exceeded the value defined for maxCount !");
			CoreFactory.getLogger().warn(forwarder.getObject());
			return forwarder;
		}
		
		Calendar jsScheduledTime = abstractJobType.getManagement().getTimeManagement().getJsScheduledTime().getStartTime();

		Calendar selectedSchedule = null;

		ArrayList<Calendar> floatingSchedules = new ArrayList<Calendar>();

		int hourOfPlannedTime = jsScheduledTime.get(Calendar.HOUR_OF_DAY);
		int minuteOfPlannedTime = jsScheduledTime.get(Calendar.MINUTE);
		int secondOfPlannedTime = jsScheduledTime.get(Calendar.SECOND);

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
				restCal = MyraDateUtils.setTimePart(jsScheduledTime);
				restCal.add(Calendar.MONTH, 1);
				restCal.set(Calendar.DAY_OF_MONTH, firstDayOfMonth);
				floatingSchedules.add(restCal);
				CoreFactory.getLogger().debug("Option for firstDayOfMonth : " + MyraDateUtils.getDate(restCal));
			}

			if (lastDay != null) {
				int lastDayOfMonth = DateUtils.getCalendarInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
				restCal = MyraDateUtils.setTimePart(jsScheduledTime);
				if (DateUtils.getCalendarInstance().get(Calendar.DAY_OF_MONTH) == lastDayOfMonth) {
					restCal.add(Calendar.MONTH, 1);
					lastDayOfMonth = restCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				}
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
			selectedSchedule = DateUtils.getCalendarInstance();
			selectedSchedule.setTime(sortedCals[0].getTime());
			if (periodInfo != null) {
				periodInfo.setCounter(BigInteger.valueOf(periodInfo.getCounter().intValue() + 1));
			}
			CoreFactory.getLogger().debug("Minimum of options : " + MyraDateUtils.getDate(selectedSchedule));
		}

		Forwarder forwarder = Forwarder.CALENDAR_CALCULATED;
		forwarder.setObject(selectedSchedule);
		
		return forwarder;

	}
	
}

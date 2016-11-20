package com.likya.myra.jef.utils.timeschedules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.xsd.myra.model.generics.TypeOfTimeType;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.PeriodInfoDocument.PeriodInfo;
import com.likya.xsd.myra.model.wlagen.JsExecutionTimeFrameDocument.JsExecutionTimeFrame;
import com.likya.xsd.myra.model.wlagen.TimeManagementDocument.TimeManagement;

public class PeriodCalculations {

	protected static Calendar forward(AbstractJobType abstractJobType, ArrayList<String> messages) {
		
		PeriodInfo periodInfo = abstractJobType.getManagement().getPeriodInfo();
		
		if(!checkMaxCount(periodInfo)) {
			return null;
		}
		
		TimeManagement timeManagement = abstractJobType.getManagement().getTimeManagement();

		String selectedTZone = timeManagement.getTimeZone();
		TypeOfTimeType.Enum typeOfTimeType = timeManagement.getTypeOfTime();
		Calendar startTime = timeManagement.getJsScheduledTime().getStartTime();

		// System.out.println("periodInfo.getMaxCount().intValue() : " + periodInfo.getMaxCount().intValue());
		// System.out.println("periodInfo.getCounter().intValue() : " + periodInfo.getCounter().intValue());

		periodInfo.setCounter(BigInteger.valueOf(periodInfo.getCounter().intValue() + 1));

		GDuration gDuration = periodInfo.getStep();

		long periodOfRepeatance = getDurationInMilliSecs(gDuration);

		if(selectedTZone == null) {
			selectedTZone= TimeZone.getDefault().getID();
		}
		
		if(typeOfTimeType == null) {
			typeOfTimeType= TypeOfTimeType.ACTUAL;
		}
		
		String startTimeStr = MyraDateUtils.getW3CDateTime(startTime, selectedTZone, typeOfTimeType);
		
		Calendar startDateTime = dateToXmlTime(startTimeStr, selectedTZone);

		Calendar newDateTime = findNextPeriod(startDateTime, periodOfRepeatance, selectedTZone, periodInfo.getRelativeStart());
		
		JsExecutionTimeFrame jsExecutionTimeFrame =  timeManagement.getJsExecutionTimeFrame();
		
		if (jsExecutionTimeFrame != null && jsExecutionTimeFrame.getStartTime() != null) {
			Calendar jsExecutionTimeFrameStartTime = jsExecutionTimeFrame.getStartTime();
			if (newDateTime.before(jsExecutionTimeFrameStartTime)) {
				if(messages != null) {
					messages.add("Calculated schedule >> " + MyraDateUtils.getDate(newDateTime) + " is before Execution Time Frame Start Time : " + MyraDateUtils.getDate(jsExecutionTimeFrameStartTime));
				}
				return null;
			}
		}
		
		
		// if (/*!checkStayInDay(newDateTime) ||*/ newDateTime.before(startTime)) {
		//	if(messages != null) {
		//		messages.add("Calculated schedule >> " + MyraDateUtils.getDate(newDateTime) + " is before startTime : " + MyraDateUtils.getDate(startTime));	
		//	}
		//	return null;
		//}

		// System.err.println("before : " + MyraDateUtils.getDate(timeManagement.getJsPlannedTime().getStartTime().getTime()));
		timeManagement.getJsActualTime().setStartTime(newDateTime);
		// System.err.println("real : " + MyraDateUtils.getDate(newDateTime.getTime()));
		// System.err.println("after : " + MyraDateUtils.getDate(timeManagement.getJsPlannedTime().getStartTime().getTime()));
		
		return newDateTime;
	}

	private static Calendar findNextPeriod(Calendar startDateTime, long period, String selectedTZone, boolean isRelativeStart) {

		Date currentTime = Calendar.getInstance().getTime();

		if (isRelativeStart) {
			startDateTime.setTime(currentTime);
		} else {

			long diffDate = currentTime.getTime() - startDateTime.getTimeInMillis();

			if (diffDate < 0) {
				return startDateTime;
			}

			long divDate = diffDate / period;

			if ((divDate * period) < diffDate) {
				++divDate;
			}

			period = divDate * period;

		}
		
		
		// System.err.println(MyraDateUtils.getDate(startDateTime.getTime()));

		Calendar returnCal = addPeriod(startDateTime, period, selectedTZone);

		// System.err.println(MyraDateUtils.getDate(returnCal.getTime()));
		
		return returnCal;

	}

	protected static Calendar addPeriod(Calendar startDateTime, long period, String selectedTZone) {

		DateTimeZone zonex = DateTimeZone.forID(selectedTZone);

		// construct DateTime from JDK Date
		DateTime dt = new DateTime(startDateTime, zonex);

		Period periodInJoda = new Period(period);

		DateTime newDateTime = dt.plus(periodInJoda);

		//String outputFormat = new String("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
		//String dateStr = newDateTime.toString(outputFormat);

		return newDateTime.toCalendar(Locale.ENGLISH);
	}

	protected static Date changeYMDPart(Date firstDate, Date secondDate) {

		Calendar calendarFirst = Calendar.getInstance();
		calendarFirst.setTime(firstDate);

		Calendar calendarSecond = Calendar.getInstance();
		calendarSecond.setTime(secondDate);

		calendarSecond.set(Calendar.YEAR, calendarFirst.get(Calendar.YEAR));
		calendarSecond.set(Calendar.MONTH, calendarFirst.get(Calendar.MONTH));
		calendarSecond.set(Calendar.DAY_OF_MONTH, calendarFirst.get(Calendar.DAY_OF_MONTH));

		return calendarSecond.getTime();
	}

//	private static boolean checkStayInDay(Calendar date) {
//
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(date.getTime());
//
//		if (cal.get(Calendar.DAY_OF_MONTH) != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
//			return false;
//		}
//
//		return true;
//	}

	protected static long getDurationInMilliSecs(GDuration gDuration) {

		// GDuration gDuration = new GDuration(durationString);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);

		GDate base = new GDate(cal);
		GDate d = base.add(gDuration);

		long durationInMillis = d.getDate().getTime();

		return durationInMillis;
	}

	protected static Calendar dateToXmlTime(String time, String selectedTZone) {
		
		DateTimeParser[] parsers = { DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").getParser(), DateTimeFormat.forPattern("HH:mm:ss.SSSZZ").getParser(), DateTimeFormat.forPattern("HH:mm:ss.SSS").getParser(), DateTimeFormat.forPattern("HH:mm:ss").getParser() };

		DateTimeFormatter dtf = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

		DateTime tx = dtf.parseDateTime(time);
		
		return tx.toCalendar(Locale.US);
	}

	// Alternative way of finding datetime from time with timeZone
	protected static String calendarToStringTimeFormat(Calendar time, String selectedTZone, String timeOutputFormat) {

		DateTimeZone zone = DateTimeZone.forID(selectedTZone);
		LocalTime jobLocalTime = new LocalTime(time);
		DateTimeFormatter formatter = DateTimeFormat.forPattern(timeOutputFormat);
		String timeString = jobLocalTime.toDateTimeToday(zone).toString(formatter);

		return timeString;
	}
	
	protected static boolean checkMaxCount(PeriodInfo periodInfo) {
		
		if (periodInfo != null && periodInfo.getMaxCount() != null && periodInfo.getMaxCount().intValue() > 0 && (periodInfo.getCounter().intValue() + 1) >= periodInfo.getMaxCount().intValue()) {
			periodInfo.setCounter(BigInteger.valueOf(0));
			return false;
			
		}
		
		return true;
	}

}

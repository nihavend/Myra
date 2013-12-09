package com.likya.myra.jef.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.likya.myra.LocaleMessages;

public class DateUtils {
	public static String getDate(Date executionTime) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //$NON-NLS-1$
		return formatter.format(executionTime);
	}

	public static String getTime(Date executionTime) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
		return formatter.format(executionTime);
	}

	public static long daysBetween(Date d1, Date d2) {
		//return ((d2.getTime() - d1.getTime() + ONE_HOUR) / (ONE_HOUR * 24));
		return ((d2.getTime() - d1.getTime() + (60 * 60 * 1000L)) / (60 * 60 * 1000L * 24));
	}
	
	public static String getFormattedElapsedTime(int timeInSeconds) {
		int hours, minutes, seconds;
		hours = timeInSeconds / 3600;
		timeInSeconds = timeInSeconds - (hours * 3600);
		minutes = timeInSeconds / 60;
		timeInSeconds = timeInSeconds - (minutes * 60);
		seconds = timeInSeconds;
//		System.out.println(hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)");
		
		return hours + LocaleMessages.getString("DateUtils.2") + minutes + LocaleMessages.getString("DateUtils.3") + seconds + LocaleMessages.getString("DateUtils.4"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	} 
	
	public static String getUnFormattedElapsedTime(int timeInSeconds) {
		int hours, minutes, seconds;
		
		hours = timeInSeconds / 3600;
		timeInSeconds = timeInSeconds - (hours * 3600);
		minutes = timeInSeconds / 60;
		timeInSeconds = timeInSeconds - (minutes * 60);
		seconds = timeInSeconds;
//		System.out.println(hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)");
		
		return getDigitStr(hours) + ":" + getDigitStr(minutes) + ":" + getDigitStr(seconds); //$NON-NLS-1$ //$NON-NLS-2$
	} 
	
	private static String getDigitStr(int digit) {
		if(digit == 0) {
			return "00"; //$NON-NLS-1$
		} else if(digit < 10) {
			return "0" + digit; //$NON-NLS-1$
		} else {
			return "" + digit; //$NON-NLS-1$
		}
	}
	/**
	 * Serkan Taş 09.09.08
	 * Öteleme bir sonraki güne değil, eğer çalışma saati geçmemiş ise,
	 * Bu günün o saatine erteliyor. 
	 * Eğer çalışma saati de geçmiş ise o zaman bir gün sonraya öteliyor
	 * dailyIterator.next()
	 */
//	public static void iterateNextDate(SimpleProperties simpleProperties) {
//		
//		// Date tmpDate = jobProperties.getTime();
//		Date scenarioDate = jobProperties.getScenarioTime();
//		Date executionDate = jobProperties.getTime();
//		Calendar tmpCal = Calendar.getInstance();
//		CoreFactory.getLogger().info(LocaleMessages.getString("DateUtils.10") + DateUtils.getDate(tmpCal.getTime())); //$NON-NLS-1$
//		tmpCal.setTime(scenarioDate);
//		CoreFactory.getLogger().info(LocaleMessages.getString("DateUtils.11") + DateUtils.getDate(tmpCal.getTime())); //$NON-NLS-1$
//		CoreFactory.getLogger().debug(LocaleMessages.getString("DateUtils.12") + DateUtils.getDate(executionDate)); //$NON-NLS-1$
//		/**
//		 * Haftanın hangi günlerinde çalışacağı artık tanımalanabilir olduğundan aşağıdaki kısım
//		 * değişecek
//		 */
//		// DailyIterator dailyIterator = new DailyIterator(tmpCal.get(Calendar.HOUR_OF_DAY), tmpCal.get(Calendar.MINUTE), tmpCal.get(Calendar.SECOND));
//		RestrictedDailyIterator restrictedDailyIterator = new RestrictedDailyIterator(tmpCal.get(Calendar.HOUR_OF_DAY), tmpCal.get(Calendar.MINUTE), tmpCal.get(Calendar.SECOND), CoreFactory.getTlosParameters().getScheduledDays());
//		jobProperties.setPreviousTime(DateUtils.getDate(executionDate));
//		jobProperties.setTime(restrictedDailyIterator.next());	
//		CoreFactory.getLogger().debug(LocaleMessages.getString("DateUtils.13") + DateUtils.getDate(jobProperties.getTime())); //$NON-NLS-1$
//		CoreFactory.getLogger().info(LocaleMessages.getString("DateUtils.14") + DateUtils.getDate(jobProperties.getTime())); //$NON-NLS-1$
//	}
	
	public static String getDuration(Date sDate) {
		Date now = Calendar.getInstance().getTime();
		long timeDiff = now.getTime() - sDate.getTime();
		return DateUtils.getUnFormattedElapsedTime((int) timeDiff / 1000);
	}

	public static long diff(Date sDate, Date fDate) {
		
		long timeDiff = fDate.getTime() - sDate.getTime();
		return timeDiff;
	}

	public static long getDurationNumeric(Date sDate) {
		Date now = Calendar.getInstance().getTime();
		long timeDiff = now.getTime() - sDate.getTime();
		return timeDiff;
	}
	
	public static Date findNextPeriod1(Date nextPeriodTime, Long period) {
		
		boolean loop = true;
		
		while(loop) {
			Date currentTime = Calendar.getInstance().getTime();
			if(nextPeriodTime.before(currentTime)) {
				nextPeriodTime = DateUtils.longtoDate(nextPeriodTime.getTime() + period);
			} else {
				loop = false;
			}
			
		}
		
		return nextPeriodTime;
	}
	
	private static Date findNextPeriod(Date nextPeriodTime, Long period) {
		
		period = period * 1000; // Convert to milliseconds
		Date currentTime = Calendar.getInstance().getTime();
		// System.out.println(currentTime + "\n" + nextPeriodTime);
		
		long diffDate = currentTime.getTime() - nextPeriodTime.getTime();
		
		if(diffDate < 0) {
			return nextPeriodTime;
		}
		
		long divDate = diffDate / period;
		// System.out.println(diffDate);
		// System.out.println(divDate * period);
		if((divDate * period) < diffDate) {
			++ divDate;
		}
		
		long newTime = divDate * period;
		// System.out.println(newTime);
		nextPeriodTime = new Date(nextPeriodTime.getTime() + newTime);
		// System.out.println(nextPeriodTime.getTime() + newTime);
		
		// System.out.println(nextPeriodTime);
		return nextPeriodTime;
	}
	
	public static Date changeDateValue(Date firstDate, Date secondDate) {

		Calendar calendarFirst = Calendar.getInstance();
		calendarFirst.setTime(firstDate);
		
		Calendar calendarSecond = Calendar.getInstance();
		calendarSecond.setTime(secondDate);
		
		calendarSecond.set(Calendar.YEAR, calendarFirst.get(Calendar.YEAR));
		calendarSecond.set(Calendar.MONTH, calendarFirst.get(Calendar.MONTH));
		calendarSecond.set(Calendar.DAY_OF_MONTH, calendarFirst.get(Calendar.DAY_OF_MONTH));
		
		return calendarSecond.getTime();
	}
	
//	public static Date findRangedNextPeriod(JobProperties jobProperties) {
//		
//		Date myDate = findNextPeriod(jobProperties.getTime(), jobProperties.getPeriodTime());
//		
//		jobProperties.setJobPlannedEndTime(changeDateValue(myDate, jobProperties.getJobPlannedEndTime()));
//		jobProperties.setJobPlannedStartTime(changeDateValue(myDate, jobProperties.getJobPlannedStartTime()));
//		
//		boolean notInScheduledDays = Arrays.binarySearch(CoreFactory.getTlosParameters().getScheduledDays(), Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) < 0;
//		
//		if (!checkStayInDay(myDate) || myDate.after(jobProperties.getJobPlannedEndTime()) || myDate.before(jobProperties.getJobPlannedStartTime()) || notInScheduledDays) {
//			iterateNextDate(jobProperties);
//			myDate = jobProperties.getTime();
//		}
//		
//		return myDate;
//	}
	
	private static boolean checkStayInDay(Date date) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		if(cal.get(Calendar.DAY_OF_MONTH) != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
			return false;
		}
			
		return true;
	}
	
	public static Date longtoDate(Long longDate) {
		Date date = new Date();
	    date.setTime(longDate);

	    return  date;
	}
	
	public static Date generateJobDate(String dateTimeInfo) {

		StringTokenizer dateTokenizer = new StringTokenizer(dateTimeInfo, " "); //$NON-NLS-1$
		
		Calendar calendar = Calendar.getInstance();
		
		String timeToken = null;
		String dateToken = null;
		
		/**
		 * Eğer tarih de verildiyse token = 2 olacak Sadece saat
		 * verildiyse token = 1 olacak
		 */
		if (dateTokenizer.countTokens() == 1) {
			timeToken = dateTokenizer.nextToken();
		} else {
			dateToken = dateTokenizer.nextToken();
			timeToken = dateTokenizer.nextToken();
		}
		
		if((dateToken != null && !DateUtils.dateValidator(dateToken)) || !DateUtils.timeValidator(timeToken)) {
			return null;
		}
		
		if (dateToken != null) {
			StringTokenizer dTokenizer = new StringTokenizer(dateToken, "/"); //$NON-NLS-1$
			calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dTokenizer.nextToken()));
			calendar.set(Calendar.MONTH, Integer.parseInt(dTokenizer.nextToken()) - 1);
			calendar.set(Calendar.YEAR, Integer.parseInt(dTokenizer.nextToken()));
		}

		StringTokenizer timeTokenizer = new StringTokenizer(timeToken, ":"); //$NON-NLS-1$

		int hour = Integer.parseInt(timeTokenizer.nextToken());
		int minute = Integer.parseInt(timeTokenizer.nextToken());
		int second = Integer.parseInt(timeTokenizer.nextToken());

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);

		calendar.set(Calendar.MILLISECOND, 0);

	    return  calendar.getTime();
	}
	
	public static Date setForToday(Date myDate) {
		
		Calendar todayCalendar = Calendar.getInstance();
		Calendar myCalendar = Calendar.getInstance();
		myCalendar.setTime(myDate);
		
		myCalendar.set(Calendar.MONTH, todayCalendar.get(Calendar.MONTH));
		myCalendar.set(Calendar.DAY_OF_MONTH, todayCalendar.get(Calendar.DAY_OF_MONTH));
		myCalendar.set(Calendar.YEAR, todayCalendar.get(Calendar.YEAR));
		
		return myCalendar.getTime();
	}
	
	public static boolean dateValidator(String dateToValidate) {
		return dateValidator(dateToValidate, "dd/MM/yyyy");
	}
	
	public static boolean dateValidator(String dateToValidate, String dateFromat) {
	
		/**
		 * Thanks to;
		 * http://www.mkyong.com/java/how-to-check-if-date-is-valid-in-java/
		 */
		
		if(dateToValidate == null){
			return false;
		}
 
		SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
		sdf.setLenient(false);
 
		try {
 
			//if not valid, it will throw ParseException
			// Date date = 
			sdf.parse(dateToValidate);
			// System.out.println(date);
 
		} catch (ParseException e) {
			// e.printStackTrace();
			return false;
		}
 
		return true;
	}
	
	public static boolean timeValidator(String timeToValidate) {

		/**
		 * Thanks to;
		 * http://www.mkyong.com/regular-expressions/how-to-validate-time-in-24-
		 * hours-format-with-regular-expression/
		 */

		final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";
		
		Pattern pattern;
		Matcher matcher;
		  
		pattern = Pattern.compile(TIME24HOURS_PATTERN);

		matcher = pattern.matcher(timeToValidate);
		
		return matcher.matches();

	}
}

package com.likya.myra.jef.utils.timeschedules;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.likya.commons.utils.DateUtils;

/**
 * A <code>RestrictedDailyIterator</code> returns a sequence of dates on
 * subsequent days (restricted to a set of days, e.g. weekdays only)
 * representing the same time each day.
 */
public class RestrictedDailyIterator {

	private final int[] days;
	private final Calendar calendar = DateUtils.getCalendarInstance();

	public RestrictedDailyIterator(int hourOfDay, int minute, int second, int[] days) {
		this(hourOfDay, minute, second, days, new Date());
	}

	public RestrictedDailyIterator(int hourOfDay, int minute, int second, int[] days, Date date) {

		this.days = (int[]) days.clone();
		Arrays.sort(this.days);

		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, 0);
		if (!calendar.getTime().before(date)) {
			calendar.add(Calendar.DATE, -1);
		}
	}
	
	public Calendar next() {
		return next(Calendar.DAY_OF_WEEK);
	}

	public Calendar next(int fieldNumber) {
		do {
			calendar.add(Calendar.DATE, 1);
		} while (Arrays.binarySearch(days, calendar.get(fieldNumber)) < 0);

		return calendar;
	}

}

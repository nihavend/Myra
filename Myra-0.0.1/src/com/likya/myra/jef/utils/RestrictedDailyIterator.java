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
import java.util.Date;

/**
 * A <code>RestrictedDailyIterator</code> returns a sequence of dates on
 * subsequent days (restricted to a set of days, e.g. weekdays only)
 * representing the same time each day.
 */
public class RestrictedDailyIterator {
	@SuppressWarnings("unused")
	private final int hourOfDay, minute, second;
	private final int[] days;
	private final Calendar calendar = Calendar.getInstance();

	public RestrictedDailyIterator(int hourOfDay, int minute, int second, int[] days) {
		this(hourOfDay, minute, second, days, new Date());
	}
	
	public RestrictedDailyIterator(int hourOfDay, int minute, int second, int[] days, Date date) {
		this.hourOfDay = hourOfDay;
		this.minute = minute;
		this.second = second;
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

	public Date next() {
		do {
			calendar.add(Calendar.DATE, 1);
		} while (Arrays.binarySearch(days, calendar.get(Calendar.DAY_OF_WEEK)) < 0);
		return calendar.getTime();
	}

}

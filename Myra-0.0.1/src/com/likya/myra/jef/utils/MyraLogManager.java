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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.varia.LevelRangeFilter;

public class MyraLogManager {

	public static final String MYRA_CONSOLE = "myraconsole";
	public static final String MYRA_SUMMARYFILE = "myrasummaryfile";
	public static final String MYRA_TRACEFILE = "myratracefile";

	public enum MyraAppender {

		MYRA_CONSOLE("myraconsole"), MYRA_SUMMARYFILE("myrasummaryfile"), MYRA_TRACEFILE("myratracefile");

		private String value;

		private MyraAppender(String value) {
			this.value = value;
		}
	}

	public static void setLogLevelMax(MyraAppender appnderName, Level logLevel) {
		if (logLevel != null) {
			AppenderSkeleton appndr = (AppenderSkeleton) Logger.getRootLogger().getAppender(appnderName.value);
			LevelRangeFilter levelRangeFilter = (LevelRangeFilter) appndr.getFilter();
			levelRangeFilter.setLevelMax(logLevel);
			appndr.activateOptions();
		}
	}

	public static void setLogLevelMin(MyraAppender appnderName, Level logLevel) {
		if (logLevel != null) {
			AppenderSkeleton appndr = (AppenderSkeleton) Logger.getRootLogger().getAppender(appnderName.value);
			LevelRangeFilter levelRangeFilter = (LevelRangeFilter) appndr.getFilter();
			levelRangeFilter.setLevelMin(logLevel);
			appndr.activateOptions();
		}
	}

	public static void setLogFileName(MyraAppender appnderName, String fileNameAndPath) {
		if (fileNameAndPath != null) {
			RollingFileAppender appndr = (RollingFileAppender) Logger.getRootLogger().getAppender(appnderName.value);
			appndr.setFile(fileNameAndPath);
			appndr.activateOptions();
		}
	}

}

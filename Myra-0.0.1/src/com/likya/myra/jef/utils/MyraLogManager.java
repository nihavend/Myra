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

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
package com.likya.myra.jef.jobs;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.commons.utils.PeriodCalculations;
import com.likya.myra.commons.utils.StateUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.OutputData;
import com.likya.myra.jef.utils.Scheduler;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.SimplePropertiesType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.ReturnCodeListDocument.ReturnCodeList.OsType;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.Status;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class JobHelper {

	protected static void updateDescStr(StringBuffer descStr, StringBuilder stringBufferForOUTPUT, StringBuilder stringBufferForERROR) {

		if (stringBufferForOUTPUT != null && stringBufferForOUTPUT.length() > 1) {
			descStr.append("OUTPUT : " + stringBufferForOUTPUT);
		}

		if (stringBufferForERROR != null && stringBufferForERROR.length() > 1) {
			descStr.append("\nERROR : " + stringBufferForERROR);
		}

		return;
	}

	protected static void setWorkDurations(JobImpl jobClassName, Calendar startTime) {

		AbstractJobType abstractJobType = jobClassName.getAbstractJobType();

		Calendar endTime = Calendar.getInstance();
		long timeDiff = endTime.getTime().getTime() - startTime.getTime().getTime();

		String endLog = abstractJobType.getId() + CoreFactory.getMessage("ExternalProgram.14") + MyraDateUtils.getDate(endTime.getTime());
		String duration = abstractJobType.getId() + CoreFactory.getMessage("ExternalProgram.15") + MyraDateUtils.getFormattedElapsedTime((int) timeDiff / 1000);

		abstractJobType.getManagement().getTimeManagement().getJsRealTime().setStopTime(endTime);

		jobClassName.getJobRuntimeProperties().setCompletionDate(endTime);
		// getJobProperties().setCompletionDateTime(endTime);

		jobClassName.getJobRuntimeProperties().setWorkDuration(MyraDateUtils.getUnFormattedElapsedTime((int) timeDiff / 1000));
		// getJobProperties().setWorkDurationNumeric(timeDiff);

		CoreFactory.getLogger().info(endLog);
		CoreFactory.getLogger().info(duration);

		// reportLog(jobClassName, startTime, endTime);

	}

	public static void setJsPlannedTimeForStart(AbstractJobType abstractJobType, long period) {

		// System.err.println("1 : " + CommonDateUtils.getDate(startTime.getTime()));

		// Calendar jobCalendar = abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime();

		// System.err.println("Before : " + CommonDateUtils.getDate(jobCalendar.getTime()));

		//		jobCalendar.set(Calendar.YEAR, startTime.get(Calendar.YEAR));
		//		jobCalendar.set(Calendar.MONTH, startTime.get(Calendar.MONTH));
		//		jobCalendar.set(Calendar.DAY_OF_MONTH, startTime.get(Calendar.DAY_OF_MONTH));

		String timeZone = abstractJobType.getManagement().getTimeManagement().getTimeZone();

		Calendar returnCal = PeriodCalculations.addPeriod(Calendar.getInstance(), period, timeZone);

		abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().setStartTime(returnCal);

		// System.err.println("After : " + CommonDateUtils.getDate(returnCal.getTime()));

		// System.err.println(startTime.before(returnCal));

		// System.err.println();

		//		System.err.println("1 : " + CommonDateUtils.getDate(startTime.getTime()));
		//
		//		System.err.println("Before : " + CommonDateUtils.getDate(abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().getTime().getTime()));
		//		
		//		Calendar startDateTime = PeriodCalculations.dateToXmlTime(abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().getTime().toString());
		//		
		//		System.err.println("2 : " + CommonDateUtils.getDate(startDateTime.getTime()));
		//		
		//		Calendar returnCal = PeriodCalculations.addPeriod(startDateTime, period);
		//		
		//		System.err.println("3 : " + CommonDateUtils.getDate(returnCal.getTime()));
		//
		//		abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().setTime(returnCal);
		//		
		//		System.err.println("After : " + CommonDateUtils.getDate(abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().getTime().getTime()));
		//		
		// GDateBuilder gDateBuilder = new GDateBuilder(startTime);

		// System.err.println("2 : " + CommonDateUtils.getDate(abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().getTime().getTime()));

		// abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().setDate(xmlDateTime.getCalendarValue());

		// System.err.println("3 : " + CommonDateUtils.getDate(abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().getDate().getTime()));

		//		Calendar cal = abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().getTime();
		//		
		//		System.err.println("Before : " + CommonDateUtils.getDate(cal.getTime()));
		//
		//		Calendar startDateTime = PeriodCalculations.dateToXmlTime(cal.toString());
		//		Calendar returnCal = PeriodCalculations.addPeriod(startDateTime, period);
		//
		//		abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().setTime(returnCal);
		//		
		//		cal = abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().getTime();
		//
		//		System.err.println("After : " + CommonDateUtils.getDate(cal.getTime()));

		//		Date scheduledTime = abstractJobType.getTimeManagement().getJsPlannedTime().getStartTime().getTime();
		//		System.err.println(scheduledTime);

	}

	protected static void setJsRealTimeForStart(AbstractJobType abstractJobType, Calendar startTime) {
		// System.err.println("Beofer : " + MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsRealTime().getStartTime().getTime().getTime()));
		abstractJobType.getManagement().getTimeManagement().getJsRealTime().setStartTime(startTime);
		// System.err.println("Beofer : " + MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsRealTime().getStartTime().getTime().getTime()));
	}

	protected static void setJsRealTimeForStop(AbstractJobType abstractJobType, Calendar stopTime) {
		abstractJobType.getManagement().getTimeManagement().getJsRealTime().setStopTime(stopTime);
	}

	public static String removeSlashAtTheEnd(SimplePropertiesType simpleProperties, String jobPath, String jobCommand) {

		String pathSeperator;

		switch (simpleProperties.getBaseJobInfos().getOSystem().intValue()) {
		case OsType.INT_WIN_3_X:
		case OsType.INT_WIN_95:
		case OsType.INT_WIN_98:
		case OsType.INT_WINCE:
		case OsType.INT_WINDOWS:
		case OsType.INT_WINDOWS_2000:
		case OsType.INT_WINDOWS_R_ME:
		case OsType.INT_WINDOWS_XP:
		case OsType.INT_WINNT:
			pathSeperator = "\\";
			break;

		default:
			pathSeperator = "/";
			break;
		}

		if (jobPath.endsWith(pathSeperator)) {
			jobCommand = jobPath + jobCommand;
		} else {
			jobCommand = jobPath + pathSeperator + jobCommand;
		}

		return jobCommand;
	}

	public static ProcessBuilder parsJobCmdArgs(String jobCommand) {

		ProcessBuilder processBuilder;

		String realCommand = "";
		String arguments = "";

		int indexOfSpace = jobCommand.indexOf(" ");

		if (indexOfSpace > 0) {
			realCommand = jobCommand.substring(0, indexOfSpace).trim();
			arguments = jobCommand.substring(jobCommand.indexOf(" ")).trim();
			processBuilder = new ProcessBuilder(realCommand, arguments);
		} else {
			realCommand = jobCommand.trim();
			processBuilder = new ProcessBuilder(realCommand);
		}

		return processBuilder;

	}

	public static StatusName.Enum searchReturnCodeInStates(AbstractJobType abstractJobType, int processExitValue, StringBuffer descStr) {

		Status localStateCheck = null;
		StatusName.Enum statusName = null;

		if ((abstractJobType.getStateInfos().getJobStatusList() != null) && (localStateCheck = StateUtils.contains(abstractJobType.getStateInfos().getJobStatusList(), processExitValue)) != null) {
			statusName = localStateCheck.getStatusName();
		} else {
			if(processExitValue == 0) {
				statusName = StatusName.SUCCESS;
			} else {
				statusName = StatusName.FAILED;
			}
		}

		if (StatusName.FAILED.equals(statusName)) {
			descStr.append("Fail Reason depends on ReturnCode of job through processExitValue : " + processExitValue);
		}

		return statusName;
	}

	protected static void writeErrorLogFromOutputs(Logger myLogger, String logClassName, StringBuilder stringBufferForOUTPUT, StringBuilder stringBufferForERROR) {

		StringBuffer descStr = new StringBuffer();

		updateDescStr(descStr, stringBufferForOUTPUT, stringBufferForERROR);

		if (descStr.length() > 1) {
			myLogger.error(" >>" + " writetErrorLogFromOutputs " + ">> " + logClassName + " : Job has error, terminating " + descStr.toString());
		}

	}

	public static LiveStateInfo insertNewLiveStateInfo(AbstractJobType abstractJobType, int enumStateName, int enumSubstateName, int enumStatusName) {
		LiveStateInfo liveStateInfo = LiveStateInfoUtils.insertNewLiveStateInfo(abstractJobType, enumStateName, enumSubstateName, enumStatusName);
		//sendStatusChangeInfo();
		return liveStateInfo;
	}

	public static LiveStateInfo insertNewLiveStateInfo(AbstractJobType abstractJobType, int enumStateName, int enumSubstateName, int enumStatusName, String retCodeDesc) {
		LiveStateInfo liveStateInfo = LiveStateInfoUtils.insertNewLiveStateInfo(abstractJobType, enumStateName, enumSubstateName, enumStatusName);
		liveStateInfo.addNewReturnCode().setDesc(retCodeDesc);
		//sendStatusChangeInfo();
		return liveStateInfo;
	}

	public static void resetJob(AbstractJobType abstractJobType) {
		resetJob(abstractJobType, null);
		return;
	}

	public static void resetJob(AbstractJobType abstractJobType, LiveStateInfo liveStateInfo) {
		if (Scheduler.scheduleForNextExecution(abstractJobType)) {
			if (liveStateInfo == null) {
				JobHelper.insertNewLiveStateInfo(abstractJobType, StateName.INT_PENDING, SubstateName.INT_IDLED, StatusName.INT_BYTIME);
			}
			OutputData outputData = OutputData.generateDefault(abstractJobType);
			CoreFactory.getInstance().getOutputStrategy().sendDataObject(outputData);
			CoreFactory.getLogger().info("Job id :" + abstractJobType.getId() + " is scheduled for new time " + abstractJobType.getManagement().getTimeManagement().getJsPlannedTime());
		}
	}

}

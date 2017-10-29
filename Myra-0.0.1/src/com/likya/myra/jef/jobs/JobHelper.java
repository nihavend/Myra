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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.likya.myra.commons.ValidPlatforms;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.commons.utils.StateUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.utils.timeschedules.TimeScheduler;
import com.likya.xsd.myra.model.generics.EntryDocument.Entry;
import com.likya.xsd.myra.model.generics.EnvVariablesDocument.EnvVariables;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.jobprops.SimplePropertiesType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.ReturnCodeListDocument.ReturnCodeList.OsType;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.Status;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.wlagen.TriggerDocument.Trigger;

public class JobHelper {

	public static void updateDescStr(StringBuffer descStr, StringBuilder stringBufferForOUTPUT, StringBuilder stringBufferForERROR) {

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

		int durationList[] = MyraDateUtils.getFormattedElapsedTime((int) timeDiff / 1000);

		String endLog = abstractJobType.getId() + CoreFactory.getMessage("ExternalProgram.14") + MyraDateUtils.getDate(endTime.getTime());
		String duration = abstractJobType.getId() + CoreFactory.getMessage("ExternalProgram.15") + durationList[0] + " saat " + durationList[1] + " dakika " + durationList[2] + " saniye";

		if(abstractJobType.getManagement().getTimeManagement().getJsRecordedTime() == null) {
			abstractJobType.getManagement().getTimeManagement().addNewJsRecordedTime();
		}
		abstractJobType.getManagement().getTimeManagement().getJsRecordedTime().setStopTime(endTime);

		// NOT-USED
		// jobClassName.getJobRuntimeProperties().setCompletionDate(endTime);
		// getJobProperties().setCompletionDateTime(endTime);

		abstractJobType.getManagement().getTimeManagement().setPrevWorkDuration(MyraDateUtils.getUnFormattedElapsedTime((int) timeDiff / 1000));
		// getJobProperties().setWorkDurationNumeric(timeDiff);

		CoreFactory.getLogger().info(endLog);
		CoreFactory.getLogger().info(duration);

		// reportLog(jobClassName, startTime, endTime);

	}

	public static void setJsActualTimeForStart(AbstractJobType abstractJobType, long period) {
		String timeZone = abstractJobType.getManagement().getTimeManagement().getTimeZone();
		Calendar returnCal = TimeScheduler.addPeriod(Calendar.getInstance(), period, timeZone);
		abstractJobType.getManagement().getTimeManagement().getJsActualTime().setStartTime(returnCal);
	}

	protected static void setJsRecordedTimeForStart(AbstractJobType abstractJobType, Calendar startTime) {
		// System.err.println("Before : " + MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsRealTime().getStartTime().getTime().getTime()));
		if(abstractJobType.getManagement().getTimeManagement().getJsRecordedTime() == null) {
			abstractJobType.getManagement().getTimeManagement().addNewJsRecordedTime();
		}
		abstractJobType.getManagement().getTimeManagement().getJsRecordedTime().setStartTime(startTime);
		// System.err.println("Beofer : " + MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsRealTime().getStartTime().getTime().getTime()));
	}

	public static void setJsRecordedTimeForStop(AbstractJobType abstractJobType, Calendar stopTime) {
		if(abstractJobType.getManagement().getTimeManagement().getJsRecordedTime() == null) {
			abstractJobType.getManagement().getTimeManagement().addNewJsRecordedTime();
		}
		abstractJobType.getManagement().getTimeManagement().getJsRecordedTime().setStopTime(stopTime);
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

	public static ProcessBuilder parsJobCmdArgs(boolean isShell, String jobCommand, String extArgValues) {

		ProcessBuilder processBuilder;

		String realCommand = "";
		String[] inlineArgs = null;

		int indexOfSpace = jobCommand.indexOf(" ");

		if (indexOfSpace > 0) {
			realCommand = jobCommand.substring(0, indexOfSpace).trim();
			inlineArgs = jobCommand.substring(jobCommand.indexOf(" ")).trim().split(" ");
		} else {
			realCommand = jobCommand.trim();
		}

		String[] commandArr;

		if (isShell) {
			commandArr = ValidPlatforms.getCommand(realCommand);
		} else {
			commandArr = new String[] { realCommand };
		}

		if (inlineArgs != null && inlineArgs.length > 0) {
			commandArr = concat(commandArr, inlineArgs);
		}

		if (extArgValues != null && extArgValues.length() > 0) {
			commandArr = concat(commandArr, extArgValues.trim().split(" "));
		}

		processBuilder = new ProcessBuilder(commandArr);

		return processBuilder;

	}

	public static ProcessBuilder parsJobCmdArgsOld(String jobCommand) {

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
			if (processExitValue == 0) {
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

	public static void writeErrorLogFromOutputs(Logger myLogger, String logClassName, StringBuilder stringBufferForOUTPUT, StringBuilder stringBufferForERROR) {

		StringBuffer descStr = new StringBuffer();

		updateDescStr(descStr, stringBufferForOUTPUT, stringBufferForERROR);

		if (stringBufferForERROR != null && stringBufferForERROR.length() > 1) {
			myLogger.error(" >>" + " writetErrorLogFromOutputs " + ">> " + logClassName + " : Job has error, terminating " + descStr.toString());
		}

	}

	public static void resetJob(AbstractJobType abstractJobType) {
		// resetJob(abstractJobType, null);
		evaluateTriggerType(abstractJobType, true);
		return;
	}

//	public static void resetJob(AbstractJobType abstractJobType, LiveStateInfo liveStateInfo) {
//
//		StatusName.Enum statusName = LiveStateInfoUtils.getLastStateInfo(abstractJobType).getStatusName();
//		
//		boolean isByTime = (statusName == null || StatusName.BYTIME.equals(statusName));
//
//		if (isByTime && Scheduler.scheduleForNextExecution(abstractJobType)) {
//			if (liveStateInfo == null) {
//				liveStateInfo = LiveStateInfoUtils.generateLiveStateInfo(StateName.INT_PENDING, SubstateName.INT_IDLED, StatusName.INT_BYTIME);
//			}
//			ChangeLSI.forValue(abstractJobType, liveStateInfo);
//			CoreFactory.getLogger().info("Job id :" + abstractJobType.getId() + " is scheduled for new time " + abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime());
//		}
//	}

	public static LiveStateInfo getStateInfo(AbstractJobType abstractJobType, int index) {
		return abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(index);
	}

	/**
	 * @param jobImpl
	 * @param index
	 * @return the state values stored in the given index, the most last value has the index 0
	 */

	public static LiveStateInfo getStateInfo(JobImpl jobImpl, int index) {
		return getStateInfo(jobImpl.getAbstractJobType(), index);
	}

	public static LiveStateInfo getLastStateInfo(JobImpl jobImpl) {
		return LiveStateInfoUtils.getLastStateInfo(jobImpl.getAbstractJobType());
	}

	public static String[] concat(String[] first, String[] second) {

		String[] result = new String[first.length + second.length];

		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);

		return result;
	}

	public static Map<String, String> entryToMap(EnvVariables envVariables) {

		Map<String, String> envMap = new HashMap<String, String>();

		if (envVariables != null) {
			Entry[] envVars = envVariables.getEntryArray();

			for (Entry myEntry : envVars) {
				envMap.put(myEntry.getKey(), myEntry.getStringValue());
			}
		}

		return envMap;

	}
	
	private static void doControlForNextTime(AbstractJobType abstractJobType) {
		if (TimeScheduler.scheduleForNextExecution(abstractJobType)) {
			String startTime = MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsActualTime().getStartTime().getTime());
			CoreFactory.getLogger().info("Job [" + abstractJobType.getId() + "] bir sonraki zamana kuruldu : " + startTime);
			ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, StatusName.BYTIME);
		}
	}
	
	public static void evaluateTriggerType(AbstractJobType abstractJobType, boolean forward) {
		 evaluateTriggerType(abstractJobType, forward, true);
	}
	
	public static void evaluateTriggerType(AbstractJobType abstractJobType, boolean forward, boolean isNew) {
		
		if(abstractJobType.getGraphInfo() != null) {
			abstractJobType.getGraphInfo().setDeadBranch(false);
		}
		
		if(isNew && LiveStateInfoUtils.equalStates(abstractJobType.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0), StateName.PENDING, SubstateName.DEACTIVATED)) {
			// new added job, just leave it's state
			return;
		}
		
		int jobType = abstractJobType.getManagement().getTrigger().intValue();

		switch (jobType) {
		case Trigger.INT_EVENT:
			ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, StatusName.BYEVENT);
			break;
		case Trigger.INT_TIME:
			if(forward) {
				doControlForNextTime(abstractJobType);
			} else {
				ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, StatusName.BYTIME);
			}
			break;
		case Trigger.INT_USER:
			ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, StatusName.BYUSER);
			break;

		default:
			break;
		}
	}

}

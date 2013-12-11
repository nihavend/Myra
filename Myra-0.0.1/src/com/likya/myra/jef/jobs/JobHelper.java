package com.likya.myra.jef.jobs;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.likya.myra.LocaleMessages;
import com.likya.myra.commons.utils.CommonDateUtils;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.StateUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.xsd.myra.model.xbeans.joblist.AbstractJobType;
import com.likya.xsd.myra.model.xbeans.jobprops.SimplePropertiesType;
import com.likya.xsd.myra.model.xbeans.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.xbeans.stateinfo.ReturnCodeListDocument.ReturnCodeList.OsType;
import com.likya.xsd.myra.model.xbeans.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.xbeans.stateinfo.Status;
import com.likya.xsd.myra.model.xbeans.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.xbeans.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.xbeans.wlagen.JsRealTimeDocument.JsRealTime;
import com.likya.xsd.myra.model.xbeans.wlagen.StartTimeDocument.StartTime;
import com.likya.xsd.myra.model.xbeans.wlagen.StopTimeDocument.StopTime;

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

		AbstractJobType abstractJobType = jobClassName.getJobAbstractJobType();
		
		Calendar endTime = Calendar.getInstance();
		long timeDiff = endTime.getTime().getTime() - startTime.getTime().getTime();

		String endLog = abstractJobType.getId() + LocaleMessages.getString("ExternalProgram.14") + CommonDateUtils.getDate(endTime.getTime());
		String duration = abstractJobType.getId() + LocaleMessages.getString("ExternalProgram.15") + CommonDateUtils.getFormattedElapsedTime((int) timeDiff / 1000);

		
		StopTime stopTimeTemp = StopTime.Factory.newInstance();
		stopTimeTemp.setTime(endTime);
		stopTimeTemp.setDate(endTime);
		abstractJobType.getTimeManagement().getJsRealTime().setStopTime(stopTimeTemp);
		
		jobClassName.getJobRuntimeProperties().setCompletionDate(endTime);
		// getJobProperties().setCompletionDateTime(endTime);

		jobClassName.getJobRuntimeProperties().setWorkDuration(CommonDateUtils.getUnFormattedElapsedTime((int) timeDiff / 1000));
		// getJobProperties().setWorkDurationNumeric(timeDiff);

		CoreFactory.getLogger().info(endLog);
		CoreFactory.getLogger().info(duration);

		// reportLog(jobClassName, startTime, endTime);

	}
	
	protected static void setJsRealTimeForStart(SimplePropertiesType simpleProperties, Calendar startTime) {

		JsRealTime jobRealTime;
		
		jobRealTime = JsRealTime.Factory.newInstance();
		StartTime startTimeTemp = StartTime.Factory.newInstance();
		startTimeTemp.setTime(startTime);
		startTimeTemp.setDate(startTime);
		simpleProperties.getTimeManagement().setJsRealTime(jobRealTime);
	}
	
	protected static void setJsRealTimeForStop(SimplePropertiesType simpleProperties, Calendar stopTime) {

		StopTime stopTimeTemp = StopTime.Factory.newInstance();
		stopTimeTemp.setTime(stopTime);
		stopTimeTemp.setDate(stopTime);
		
		simpleProperties.getTimeManagement().getJsRealTime().setStopTime(stopTimeTemp);;
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
			Status mySubStateStatuses = StateUtils.globalContains(StateName.FINISHED, SubstateName.COMPLETED, processExitValue);
			if (mySubStateStatuses != null) {
				statusName = mySubStateStatuses.getStatusName();
			} else {
				statusName = StatusName.FAILED;
			}
		}
		
		if(StatusName.FAILED.equals(statusName)) {
			descStr.append("Fail Reason depends on ReturnCode of job through processExitValue : " + processExitValue);
		}
		
		return statusName;
	}
	
	protected static void writetErrorLogFromOutputs(Logger myLogger, String logClassName, StringBuilder stringBufferForOUTPUT, StringBuilder stringBufferForERROR) {
		
		StringBuffer descStr = new StringBuffer();
		
		updateDescStr(descStr, stringBufferForOUTPUT, stringBufferForERROR);
		
		if(descStr.length() > 1) {
			myLogger.error(" >>" + " writetErrorLogFromOutputs " + ">> " + logClassName + " : Job has error, terminating " +descStr.toString());
		}
		
	}
	
	public static LiveStateInfo insertNewLiveStateInfo(SimplePropertiesType simpleProperties, int enumStateName, int enumSubstateName, int enumStatusName) {
		LiveStateInfo liveStateInfo = LiveStateInfoUtils.insertNewLiveStateInfo(simpleProperties, enumStateName, enumSubstateName, enumStatusName);
		//sendStatusChangeInfo();
		return liveStateInfo;
	}
	
	public static LiveStateInfo insertNewLiveStateInfo(SimplePropertiesType simpleProperties, int enumStateName, int enumSubstateName, int enumStatusName, String retCodeDesc) {
		LiveStateInfo liveStateInfo = LiveStateInfoUtils.insertNewLiveStateInfo(simpleProperties, enumStateName, enumSubstateName, enumStatusName);
		liveStateInfo.addNewReturnCode().setDesc(retCodeDesc);
		//sendStatusChangeInfo();
		return liveStateInfo;
	}

}

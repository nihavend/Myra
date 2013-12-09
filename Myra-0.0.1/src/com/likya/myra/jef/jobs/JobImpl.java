package com.likya.myra.jef.jobs;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.InstanceNotFoundException;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.model.TemporaryConfig;
import com.likya.xsd.myra.model.xbeans.joblist.AbstractJobType;

public abstract class JobImpl implements Runnable, Serializable {

	private static final long serialVersionUID = 2540934879831919506L;

	private Logger myLogger = Logger.getLogger(JobImpl.class);
	
	// transient private HashMap<String, JobInterface> jobQueue;

	transient private Thread myExecuter;

	private AbstractJobType abstractJobType;
	private JobRuntimeInterface jobRuntimeProperties;
	
	protected TemporaryConfig temporaryConfig;
	
	public abstract void stopMyDogBarking();
	
	protected abstract void localRun();
	
	public JobImpl(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		this.abstractJobType = abstractJobType;
		this.jobRuntimeProperties = jobRuntimeProperties;
		
		if(jobRuntimeProperties.getLogger() != null) {
			myLogger = jobRuntimeProperties.getLogger();
		}
		
		try {
			temporaryConfig = CoreFactory.getInstance().getConfigurationManager().getTemporaryConfig();
		} catch (InstanceNotFoundException e2) {
			e2.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public final void run() {
		localRun();
	}

	public AbstractJobType getJobAbstractJobType() {
		return abstractJobType;
	}

	public JobRuntimeInterface getJobRuntimeProperties() {
		return jobRuntimeProperties;
	}

	public Thread getMyExecuter() {
		return myExecuter;
	}

	public void setMyExecuter(Thread myExecuter) {
		this.myExecuter = myExecuter;
	}

	public String getJobInfo() {
		return ""; // jobProperties.getKey() + ":" + jobProperties.getStatusString(jobProperties.getStatus(), jobProperties.getProcessExitValue()); //$NON-NLS-1$
	}

	public void reportLog(JobImpl jobClass, Date startTime, Date endTime) {

		//		String jobClassName = "JOBSTATS|";
		//
		//		if (jobClass instanceof ExternalProgram) {
		//			jobClassName = jobClassName.concat("STANDART");
		//		} else if (jobClass instanceof ManuelExternalProgram) {
		//			jobClassName = jobClassName.concat("MANUEL");
		//		} else if (jobClass instanceof RepetitiveExternalProgram) {
		//			jobClassName = jobClassName.concat("TEKRARLI");
		//		}
		//
		//		TlosServer.getLogger().info(jobClassName + "|" + TlosServer.getTlosParameters().getScenarioName().toString() + "|" + getJobProperties().getGroupName().toString() + "|" + getJobProperties().getKey().toString() + "|" + DateUtils.getDate(startTime) + "|" + DateUtils.getDate(endTime) + "|" + getJobProperties().getStatusString(getJobProperties().getStatus(), getJobProperties().getProcessExitValue()).toString()); //$NON-NLS-1$
	}

	public String[] parseParameter() {

		String[] cmd = null;

		//		if (getJobProperties().getJobParamList() != null && !getJobProperties().getJobParamList().equals("")) {
		//			String tmpCmd[] = ValidPlatforms.getCommand(getJobProperties().getJobCommand());
		//			String tmpPrm[] = getJobProperties().getJobParamList().split(" ").clone();
		//			cmd = new String[tmpCmd.length + tmpPrm.length];
		//			System.arraycopy(tmpCmd, 0, cmd, 0, tmpCmd.length);
		//			System.arraycopy(tmpPrm, 0, cmd, tmpCmd.length, tmpPrm.length);
		//		} else {
		//			cmd = ValidPlatforms.getCommand(getSimpleJobProperties().getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobCommand());
		//		}

		return cmd;
	}

	public Logger getMyLogger() {
		return myLogger;
	}

}

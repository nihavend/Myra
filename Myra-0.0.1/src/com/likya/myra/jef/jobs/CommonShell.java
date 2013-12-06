package com.likya.myra.jef.jobs;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.likya.myra.commons.grabber.StreamGrabber;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.xsd.myra.model.xbeans.jobprops.SimpleProperties;
import com.likya.xsd.myra.model.xbeans.wlagen.JobAutoRetryDocument.JobAutoRetry;
import com.likyateknoloji.myraJoblist.AbstractJobType;

public abstract class CommonShell extends JobImpl {

	private static final long serialVersionUID = 1L;
	
	transient protected Process process;
	
	transient protected StreamGrabber errorGobbler;
	transient protected StreamGrabber outputGobbler;
	
	transient protected WatchDogTimer watchDogTimer = null;
	
	transient private int wdtCounter = 0;
	
	private final String logLabel = " CommonShell ";

	public CommonShell(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}

	protected void startWathcDogTimer() {
		// TL deki 
		
		SimpleProperties simpleProperties = getJobAbstractJobType();
		long timeout = simpleProperties.getTimeManagement().getJsTimeOut().getValueInteger().longValue();
		
		if(!(simpleProperties.getCascadingConditions().getJobAutoRetry() == JobAutoRetry.YES && wdtCounter > 0)) {
			watchDogTimer = new WatchDogTimer(this, simpleProperties.getId(), Thread.currentThread(), timeout);
			watchDogTimer.setName(simpleProperties.getId() + ".WatchDogTimer.id." + watchDogTimer.getId());
			watchDogTimer.start();

			wdtCounter++;
		}
		
		// sw deki
		
//		if (simpleProperties.getTimeManagement().getJsTimeOut().getUnit() == Unit.HOURS) {
//			timeOut = timeOut * 3600;
//		} else if (simpleProperties.getTimeManagement().getJsTimeOut().getUnit() == Unit.MINUTES) {
//			timeOut = timeOut * 60;
//		}
//
//		watchDogTimer = new WatchDogTimer(this, simpleProperties.getId(), Thread.currentThread(), timeOut * 1000, globalLogger);
//		watchDogTimer.setName(simpleProperties.getId() + ".WatchDogTimer.id." + watchDogTimer.getId());
//		watchDogTimer.start();
		
	}
	
	protected void initGrabbers(Channel channel, String jobId, Logger myLogger, int buffSize) throws InterruptedException, IOException {

		myLogger.debug(" >>" + logLabel + ">> " + "Sleeping 100 ms for error and output buffers to get ready...");
		Thread.sleep(100);
		myLogger.info(" >>" + logLabel + ">> " + " OK");

		// errorGobbler = new StreamGrabber(((ChannelExec) channel).getErrStream(), "ERROR", CoreFactory.getLogger(), buffSize);
		errorGobbler = new StreamGrabber(((ChannelExec) channel).getErrStream(), "ERROR", CoreFactory.getLogger(), buffSize);
		errorGobbler.setName(jobId + ".ErrorGobbler.id." + errorGobbler.getId());

		// any output?
		outputGobbler = new StreamGrabber(channel.getInputStream(), "OUTPUT", CoreFactory.getLogger(), buffSize);
		outputGobbler.setName(jobId + ".OutputGobbler.id." + outputGobbler.getId());

		myLogger.info(" >>" + logLabel + " icin islemin hata ve girdi akisi baslatiliyor. " + errorGobbler.getName() + " ve " + outputGobbler.getName());

		// kick them off
		errorGobbler.start();
		outputGobbler.start();
	}
	
	protected void initGrabbers(Process process, String jobId, Logger myLogger, int buffSize) throws InterruptedException, IOException {
		
		myLogger.debug(" >>" + logLabel + ">> " + "Sleeping 100 ms for error and output buffers to get ready...");
		Thread.sleep(100);
		myLogger.info(" >>" + logLabel + ">> " + " OK");
		
		//any error message?
		StreamGrabber errorGobbler = new StreamGrabber(process.getErrorStream(), "ERROR", CoreFactory.getLogger(), buffSize); //$NON-NLS-1$
		errorGobbler.setName(jobId + ".ErrorGobbler.id." + errorGobbler.getId()); //$NON-NLS-1$

		// any output?
		StreamGrabber outputGobbler = new StreamGrabber(process.getInputStream(), "OUTPUT", CoreFactory.getLogger(), buffSize); //$NON-NLS-1$
		outputGobbler.setName(jobId + ".OutputGobbler.id." + outputGobbler.getId()); //$NON-NLS-1$

		myLogger.info(" >>" + logLabel + " icin islemin hata ve girdi akisi baslatiliyor. " + errorGobbler.getName() + " ve " + buffSize);
		
		// kick them off
		errorGobbler.start();
		outputGobbler.start();
	}

	protected void stopErrorGobbler(Logger logger) {
		if (errorGobbler != null && errorGobbler.isAlive()) {
			errorGobbler.stopStreamGobbler();
			errorGobbler.interrupt();
			logger.debug("  > ExternalProgram -> errorGobbler.isAlive ->" + errorGobbler.isAlive());
			errorGobbler = null;
		}
	}

	protected void stopOutputGobbler(Logger logger) {
		if (outputGobbler != null && outputGobbler.isAlive()) {
			outputGobbler.stopStreamGobbler();
			outputGobbler.interrupt();
			logger.debug("  > ExternalProgram -> outputGobbler.isAlive ->" + outputGobbler.isAlive());
			outputGobbler = null;
		}
	}
}




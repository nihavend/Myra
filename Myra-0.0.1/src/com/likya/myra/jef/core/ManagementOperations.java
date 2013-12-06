package com.likya.myra.jef.core;

public interface ManagementOperations {

	public void start() throws Throwable;

	public void shutDown();
	
	public void suspend();

	public void resume();

	public void gracefulShutDown();

	public void forceFullShutDown();
	
	public int getExecutionState();
	
	public void setExecutionState(int executionState);

}

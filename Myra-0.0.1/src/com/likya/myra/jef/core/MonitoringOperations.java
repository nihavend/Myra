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
package com.likya.myra.jef.core;

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.collections.Predicate;

import com.likya.myra.jef.jobs.JobImpl;
import com.likya.xsd.myra.model.joblist.AbstractJobType;

public interface MonitoringOperations {
	
	public HashMap<String, JobImpl> getJobQueue();
	public Collection<AbstractJobType> getJobList(Predicate predicate);
	public boolean isThresholdOverflow();

}

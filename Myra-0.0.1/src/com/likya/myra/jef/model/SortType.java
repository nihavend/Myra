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

package com.likya.myra.jef.model;

import java.io.Serializable;



public class SortType implements Comparable<SortType>,Serializable {
	
	private static final long serialVersionUID = -6374567916153023159L;
	
	private String jobKey;
	int priortiyLevel = -1;
	
	/**
	 * Bu bölüm, sıralama yapılabilmesi amacı ile eklendi
	 * Örnek : Collections.sort(arrayList);
	 * @param jobRuntimeProperties
	 * @return
	 */
	
	public int compareTo(SortType sortType) {
		if (sortType.getPriortiyLevel() > this.getPriortiyLevel()) {
			return -1;
		} else if (sortType.getPriortiyLevel() < this.getPriortiyLevel()) {
			return 1;
		} 
		return 0;
	}

	public SortType(String jobKey, int priortiyLevel) {
		super();
		this.jobKey = jobKey;
		this.priortiyLevel = priortiyLevel;
	}

	public String getJobKey() {
		return jobKey;
	}

	public int getPriortiyLevel() {
		return priortiyLevel;
	}
	
}

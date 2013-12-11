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

import com.likya.myra.LocaleMessages;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.xsd.myra.model.xbeans.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.xbeans.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.xbeans.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.xbeans.wlagen.JobAutoRetryDocument.JobAutoRetry;

public class WatchDogTimer extends Thread {


	private Thread ownerOfTimer;
	private long timeout;

	private JobImpl jobImpl;

	public WatchDogTimer(JobImpl jobImpl, String name, Thread ownerOfTimer, long timeout) {
		super(name);
		this.ownerOfTimer = ownerOfTimer;
		this.timeout = timeout;
		// Not 1 den dolayı gerek kalmadı
		this.jobImpl = jobImpl;
	}

	/**
	 * @author serkan Taş 18.03.2011 Daha önce : 1. isAutoRetry == true ise, ilk
	 *         time-out olduğunda kill edip yeniden çalıştırmayı deniyorduk 2.
	 *         isAutoRetry == false ise, ilk time-out sonrası mesajlar
	 *         gönderiliyor, bir time-out süresi daha bekleyip işlemi stop edip
	 *         statüyü fail yapıyorduk.
	 * 
	 *         (18:03.2011) : 1. isAutoRetry == true ise, ilk time-out olduğunda
	 *         kill edip yeniden çalıştırmayı deniyoruz
	 * 
	 *         2. isAutoRetry == false ise, ilk time-out sonrası mesajlar
	 *         gönderiliyor, kullanıcı stop edene kadar time-out da kalıyor.
	 * 
	 * 
	 * @author merve 11.02.2013
	 * 
	 *         İş bir kere time-out'a düştükten sonra bir daha time-out süresini
	 *         kontrol etmiyoruz.
	 * 
	 *         1. isAutoRetry == true ise, ilk time-out olduğunda kill edip
	 *         yeniden çalıştırmayı deniyoruz.
	 * 
	 *         2. isAutoRetry == false ise, ilk time-out olduğunda işi time-out
	 *         statüsüne çekip uyarı mesajlarını gönderiyoruz. İş timeout
	 *         statüsünde çalışmaya devam ediyor.
	 */

	public void run() {
		try {
			Thread.sleep(timeout);
			if (jobImpl.getJobAbstractJobType().getCascadingConditions().getJobAutoRetry() != JobAutoRetry.YES) {
				LiveStateInfoUtils.insertNewLiveStateInfo(jobImpl.getJobAbstractJobType(), StateName.INT_RUNNING, SubstateName.INT_ON_RESOURCE, StatusName.INT_TIME_OUT);
				CoreFactory.getLogger().info(LocaleMessages.getString("WatchDogTimer.0")); //$NON-NLS-1$
			} else {
				CoreFactory.getLogger().info(LocaleMessages.getString("WatchDogTimer.1") + jobImpl.getJobAbstractJobType().getId()); //$NON-NLS-1$
				ownerOfTimer.interrupt();
			}
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
		this.jobImpl = null;
		this.ownerOfTimer = null;
	}

}

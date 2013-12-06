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
			if (jobImpl.getJobSimpleProperties().getCascadingConditions().getJobAutoRetry() != JobAutoRetry.YES) {
				LiveStateInfoUtils.insertNewLiveStateInfo(jobImpl.getJobSimpleProperties(), StateName.INT_RUNNING, SubstateName.INT_ON_RESOURCE, StatusName.INT_TIME_OUT);
				CoreFactory.getLogger().info(LocaleMessages.getString("WatchDogTimer.0")); //$NON-NLS-1$
			} else {
				CoreFactory.getLogger().info(LocaleMessages.getString("WatchDogTimer.1") + jobImpl.getJobSimpleProperties().getId()); //$NON-NLS-1$
				ownerOfTimer.interrupt();
			}
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
		this.jobImpl = null;
		this.ownerOfTimer = null;
	}

}

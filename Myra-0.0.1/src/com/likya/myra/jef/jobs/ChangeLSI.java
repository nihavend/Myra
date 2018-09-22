package com.likya.myra.jef.jobs;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.OutputStrategy;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.OutputData;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateInfosDocument.StateInfos;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

/**
 * Changes LiveStateInfo of the given {@link AbstractJobType}
 * by inserting new value to {@link StateInfos}
 * @author serkan
 *
 */
public class ChangeLSI {
	
	public static void forValue(AbstractJobType abstractJobType, StateName.Enum stateName) {
		LiveStateInfoUtils.insertNewLiveStateInfo(abstractJobType, stateName);
		sendOutputData(abstractJobType);
	}
	
	public static void forValue(AbstractJobType abstractJobType, StateName.Enum stateName, SubstateName.Enum substateName) {
		LiveStateInfoUtils.insertNewLiveStateInfo(abstractJobType, stateName, substateName);
		sendOutputData(abstractJobType);
	}

	public static void forValue(AbstractJobType abstractJobType, StateName.Enum stateName, SubstateName.Enum substateName, StatusName.Enum statusName, int returnCode, String desc) {
		LiveStateInfoUtils.insertNewLiveStateInfo(abstractJobType, stateName, substateName, statusName, returnCode, desc);
		sendOutputData(abstractJobType);
	}

	public static void forValue(AbstractJobType abstractJobType, StateName.Enum stateName, SubstateName.Enum substateName, StatusName.Enum statusName, String desc) {
		LiveStateInfoUtils.insertNewLiveStateInfo(abstractJobType, stateName, substateName, statusName, desc);
		sendOutputData(abstractJobType);
	}
	
	public static void forValue(AbstractJobType abstractJobType, StateName.Enum stateName, SubstateName.Enum substateName, StatusName.Enum statusName) {
		LiveStateInfoUtils.insertNewLiveStateInfo(abstractJobType, stateName, substateName, statusName);
		sendOutputData(abstractJobType);
	}
	
	public static void forValue(AbstractJobType abstractJobType, LiveStateInfo liveStateInfo) {
		LiveStateInfoUtils.insertNewLiveStateInfo(abstractJobType, liveStateInfo);
		sendOutputData(abstractJobType);
	}
	
	private static void sendOutputData(AbstractJobType abstractJobType) {
		OutputData outputData = OutputData.generateDefault(abstractJobType);
		if(CoreFactory.getInstance() != null) {
			OutputStrategy outputStrategy = CoreFactory.getInstance().getOutputStrategy();
			outputStrategy.sendDataObject(outputData);
		} else {
			System.err.println("Ya test modülü içinde çalıştık ya da korkarım BOMBA VARRRR !!!!!");
		}
	}
}

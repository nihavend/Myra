package com.likya.myra.jef.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

import com.likya.commons.utils.FileUtils;
import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.jef.ConfigurationManager;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.jobs.JobImpl;
import com.likya.myra.jef.model.PersistObject;
import com.likya.xsd.myra.model.config.MyraConfigDocument;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;

public class MyraPersistApi {

	public static void serializeConfig(String fileName, MyraConfigDocument myraConfigDocument) throws Exception {

		String tmpDstFile = fileName + ".tmp";

		FileOutputStream outputStream = new FileOutputStream(tmpDstFile);

		outputStream.write(myraConfigDocument.toString().getBytes());
		outputStream.close();

		// Check the newly created binary senaryo file...
		Path dstPath = FileSystems.getDefault().getPath(fileName);
		Path tmpPath = FileSystems.getDefault().getPath(tmpDstFile);
		if (FileUtils.checkFile(tmpDstFile)) {
			if (FileUtils.checkFile(fileName)) {
				// remove the old binary
				Files.delete(dstPath);
			}
			Files.move(tmpPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
		} else {
			throw new Exception(fileName + " is not created, serialization failed !");
		}
	}

	public static boolean recoverJobQueue(ConfigurationManager configurationManager, HashMap<String, JobImpl> jobQueue, ArrayList<String> messages) {

		CoreFactory.getLogger().info(CoreFactory.getMessage("JobQueueOperations.12"));

		FileInputStream fis = null;
		ObjectInputStream in = null;

		try {
			fis = new FileInputStream(configurationManager.getFileToPersist());
			in = new ObjectInputStream(fis);
			Object input = in.readObject();

			PersistObject persistObject = (PersistObject) input;

			if (!persistObject.getTlosVersion().equals(CoreFactory.getVersion())) {
				CoreFactory.getLogger().error(CoreFactory.getMessage("JobQueueOperations.13"));
				CoreFactory.getLogger().error(CoreFactory.getMessage("JobQueueOperations.14") + CoreFactory.getVersion() + CoreFactory.getMessage("JobQueueOperations.15") + persistObject.getTlosVersion() + CoreFactory.getMessage("JobQueueOperations.16")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				in.close();
				return false;
			}

			jobQueue.putAll(persistObject.getJobQueue());

			// grup listesi de recover dosyasindan okunuyor
			configurationManager.setGroupList(persistObject.getGroupList());

			in.close();

			JobQueueOperations.resetJobQueue(LiveStateInfoUtils.generateLiveStateInfo(StateName.INT_FINISHED, SubstateName.INT_COMPLETED, StatusName.INT_SUCCESS), jobQueue);

		} catch (FileNotFoundException fnf) {
			messages.add(fnf.getMessage());
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			messages.add(ex.getMessage());
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			messages.add(e.getMessage());
			return false;
		}

		// dumpJobQueue(jobQueue);

		CoreFactory.getLogger().info(CoreFactory.getMessage("JobQueueOperations.17"));

		// CoreFactory.setRecovered(true);

		return true;
	}

	public static boolean persistJobQueue(ConfigurationManager configurationManager, HashMap<String, JobImpl> jobQueue) {

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		if (jobQueue.size() == 0) {
			CoreFactory.getLogger().fatal(CoreFactory.getMessage("JobQueueOperations.10"));
			CoreFactory.getLogger().fatal(CoreFactory.getMessage("JobQueueOperations.11"));
			return true;
		}
		try {

			File fileTemp = new File(configurationManager.getFileToPersist() + ".temp");
			fos = new FileOutputStream(fileTemp);

			out = new ObjectOutputStream(fos);

			PersistObject persistObject = new PersistObject();

			persistObject.setJobQueue(jobQueue);
			persistObject.setTlosVersion(CoreFactory.getVersion());
			persistObject.setGroupList(configurationManager.getGroupList());

			out.writeObject(persistObject);
			out.close();

			File file = new File(configurationManager.getFileToPersist());

			if (file.exists()) {
				file.delete();
			}

			fileTemp.renameTo(file);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;

	}
}

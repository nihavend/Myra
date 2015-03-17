package com.likya.myra.jef.utils;

import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.likya.commons.utils.FileUtils;
import com.likya.xsd.myra.model.config.MyraConfigDocument;

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
}

package org.apodhrad.filemanager.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.FileTypeMap;

/**
 * 
 * @author apodhrad
 * 
 */
public class FileManager {

	public static final String DATA_DIR_PROPERTY = "filemanager.data.dir";
	public static final String DATA_DIR_DEFAULT = System.getProperty("user.dir") + File.separator + "data";
	public static final String DATA_DIR = System.getProperty(DATA_DIR_PROPERTY, DATA_DIR_DEFAULT);
	public static final int BUFFER_LENGTH = 4 * 1024;

	public List<File> getFiles(String path) {
		List<File> fileList = new ArrayList<File>();

		File dir = new File(DATA_DIR, path);
		File[] file = dir.listFiles();
		if (file != null) {
			for (int i = 0; i < file.length; i++) {
				fileList.add(file[i]);
			}
		}

		return fileList;
	}
	
	public File getFile(String path) {
		return new File(DATA_DIR, path);
	}

	public void saveFile(InputStream input, String path, String fileName) throws IOException {
		OutputStream out = null;

		try {
			out = new FileOutputStream(new File(DATA_DIR + File.separator + path + File.separator + fileName));
			int read = 0;
			final byte[] bytes = new byte[BUFFER_LENGTH];
			while ((read = input.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (out != null) {
				out.close();
			}
			if (input != null) {
				input.close();
			}
		}
	}

	public List<FileInfo> getFilesInfo(String path, String contextPath) {
		List<FileInfo> fileInfoList = new ArrayList<FileInfo>();

		File dir = new File(DATA_DIR, path);
		File[] file = dir.listFiles();

		if (file != null) {
			for (int i = 0; i < file.length; i++) {
				FileInfo fileInfo = createFileInfo(file[i]);
				fileInfoList.add(fileInfo);
			}
		}

		return fileInfoList;
	}

	private FileInfo createFileInfo(File file) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.setName(file.getName());
		fileInfo.setSize(file.length());
		fileInfo.setType(FileTypeMap.getDefaultFileTypeMap().getContentType(file));
		fileInfo.setType(getFileExtension(file));
		fileInfo.setDirectory(file.isDirectory());
		fileInfo.setLastModified(file.lastModified());
		return fileInfo;
	}
	

	private String getFileExtension(File file) {
		String name = file.getName();
		try {
			return name.substring(name.lastIndexOf(".") + 1);
		} catch (Exception e) {
			return "unknown";
		}
	}

}

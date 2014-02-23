package org.apodhrad.filemanager.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/rest")
public class FileManagerService {

	private final String UPLOADED_FILE_PATH = "/home/apodhrad/Temp/data/";

	@GET
	@Path("/{path:.*}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FileInfo> getFileInfo(@PathParam("path") String path) {
		List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
		File dir = new File(UPLOADED_FILE_PATH, path);
		String[] fileName = dir.list();

		if (fileName != null) {
			for (int i = 0; i < fileName.length; i++) {
				FileInfo fileInfo = new FileInfo();
				fileInfo.setFileUrl("/" + fileName[i]);
				fileInfoList.add(fileInfo);
			}
		}

		return fileInfoList;
	}

	@POST
	@Path("/{path:.*}")
	@Consumes("multipart/form-data")
	public FileInfo uploadFile(@PathParam("path") String path, MultipartFormDataInput input) {

		new File(UPLOADED_FILE_PATH, path).mkdirs();

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("uploadedFile");

		for (InputPart inputPart : inputParts) {

			try {

				MultivaluedMap<String, String> header = inputPart.getHeaders();
				String fileName = getFileName(header);

				MultivaluedMap<String, String> map = inputPart.getHeaders();
				for (String key : map.keySet()) {
					System.out.println(key + ": " + map.get(key));
				}

				InputStream inputStream = inputPart.getBody(InputStream.class, null);

				saveFile(inputStream, UPLOADED_FILE_PATH + fileName);

				// constructs upload file path
				fileName = UPLOADED_FILE_PATH + fileName;

				System.out.println("Done");

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return new FileInfo();

	}

	/**
	 * header sample { Content-Type=[image/png], Content-Disposition=[form-data;
	 * name="file"; filename="filename.extension"] }
	 **/
	// get uploaded filename, is there a easy way in RESTEasy?
	private String getFileName(MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

	// save uploaded file to a defined location on the server
	private void saveFile(InputStream uploadedInputStream, String serverLocation) {

		try {
			OutputStream outpuStream = new FileOutputStream(new File(serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			outpuStream = new FileOutputStream(new File(serverLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
}

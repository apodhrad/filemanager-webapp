package org.apodhrad.filemanager.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.FileTypeMap;
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

@Path("/api")
public class FileManagerService {

	private final String UPLOADED_FILE_PATH = "/home/apodhrad/Temp/data/";

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FileInfo> getFileInfo() {
		return getFileInfo("/");
	}

	@GET
	@Path("/{path:.*}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FileInfo> getFileInfo(@PathParam("path") String path) {
		List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
		
		File dir = new File(UPLOADED_FILE_PATH, path);

		if(new File(UPLOADED_FILE_PATH).equals(dir)) {
			System.out.println("NO PARENT");
		}
		File[] file = dir.listFiles();

		if (file != null) {
			for (int i = 0; i < file.length; i++) {
				fileInfoList.add(createFileInfo(file[i]));
			}
		}

		return fileInfoList;
	}

	@POST
	@Path("{path:.*}")
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

	private FileInfo createFileInfo(File file) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.setName(file.getName());
		fileInfo.setUrl(file.getAbsolutePath().substring(UPLOADED_FILE_PATH.length()));
		fileInfo.setSize(file.length());
		fileInfo.setType(FileTypeMap.getDefaultFileTypeMap().getContentType(file));
		fileInfo.setDirectory(file.isDirectory());
		fileInfo.setLastModified(file.lastModified());
		fileInfo.setExtension(getFileExtension(file));
		fileInfo.setIcon(getIcon(file));
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

	private String getIcon(File file) {
		String icon;
		if (file.isDirectory()) {
			icon = "folder";
		} else {
			icon = getFileExtension(file);
			if(icon.equals("txt")) {
				icon = "text";
			}
		}
		if (exists(getApacheIcon(icon))) {
			return getApacheIcon(icon);
		}
		return getApacheIcon("unknown");
	}

	private static String getApacheIcon(String icon) {
		return "http://www.apache.org/icons/" + icon + ".png";
	}

	public static boolean exists(String URLName) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

package org.apodhrad.filemanager.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.activation.FileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "FileManager", urlPatterns = { "/*" })
public class FileManagerServlet extends HttpServlet {

	private static final long serialVersionUID = -4179165806543404803L;

	private final String UPLOADED_FILE_PATH = "/home/apodhrad/Temp/data/";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String contextPath = req.getContextPath();
		String path = "/" + req.getRequestURI().replace(req.getContextPath(), "") + "/";
		path = removeDoubleSlashes(path);
		List<FileInfo> filesInfo = getFileInfo(path, contextPath);
		Collections.sort(filesInfo, new NameComparator());

		PrintWriter out = resp.getWriter();
		out.println(getHtml(path, filesInfo));
		out.flush();
		out.close();
	}

	private String removeDoubleSlashes(String s) {
		while (s.contains("//")) {
			s = s.replaceAll("//", "/");
		}
		return s;
	}

	private String getHtml(String path, List<FileInfo> filesInfo) {

		String table = "<table>";
		for (FileInfo fileInfo : filesInfo) {
			table += "<tr><td><a href=\"" + fileInfo.getUrl() + "\">" + fileInfo.getName() + "</a></td></tr>";
		}
		table += "</table>";

		String html = "<!DOCTYPE html>" + "<html>" + "<head>" + "<meta charset=\"UTF-8\">" + "<title>Index of " + path
				+ "</title>" + "</head>" + "<body><h1>Index of " + path + "</h1>" + table + "</body>" + "</html>";

		return html;
	}

	public List<FileInfo> getFileInfo(String path, String contextPath) {
		List<FileInfo> fileInfoList = new ArrayList<FileInfo>();

		File dir = new File(UPLOADED_FILE_PATH, path);

		if (new File(UPLOADED_FILE_PATH).equals(dir)) {
			System.out.println("NO PARENT");
		}
		File[] file = dir.listFiles();

		if (file != null) {
			for (int i = 0; i < file.length; i++) {
				FileInfo fileInfo = createFileInfo(file[i]);
				fileInfo.setUrl(contextPath + path + fileInfo.getName());
				fileInfoList.add(fileInfo);
			}
		}

		return fileInfoList;
	}

	// /**
	// * header sample { Content-Type=[image/png],
	// Content-Disposition=[form-data;
	// * name="file"; filename="filename.extension"] }
	// **/
	// // get uploaded filename, is there a easy way in RESTEasy?
	// private String getFileName(MultivaluedMap<String, String> header) {
	//
	// String[] contentDisposition =
	// header.getFirst("Content-Disposition").split(";");
	//
	// for (String filename : contentDisposition) {
	// if ((filename.trim().startsWith("filename"))) {
	//
	// String[] name = filename.split("=");
	//
	// String finalFileName = name[1].trim().replaceAll("\"", "");
	// return finalFileName;
	// }
	// }
	// return "unknown";
	// }

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
			if (icon.equals("txt")) {
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

	private class NameComparator implements Comparator<FileInfo> {

		private boolean ascendent;

		public NameComparator() {
			this(true);
		}

		public NameComparator(boolean ascendent) {
			this.ascendent = ascendent;
		}

		public int compare(FileInfo o1, FileInfo o2) {
			int result = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			return (ascendent ? 1 : -1) * result;
		}

	}

	private class SizeComparator implements Comparator<FileInfo> {

		public int compare(FileInfo o1, FileInfo o2) {
			if (o1.getSize() > o2.getSize()) {
				return 1;
			} else if (o1.getSize() < o2.getSize()) {
				return -1;
			} else {
				return 0;
			}
		}

	}
}

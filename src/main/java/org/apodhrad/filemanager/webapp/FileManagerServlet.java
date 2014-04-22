package org.apodhrad.filemanager.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apodhrad.filemanager.webapp.comparator.NameComparator;

/**
 * 
 * @author apodhrad
 *
 */

@WebServlet(name = "FileManager", urlPatterns = { "/*" })
@MultipartConfig
public class FileManagerServlet extends HttpServlet {

	private static final long serialVersionUID = -4179165806543404803L;

	public static final String DATA_DIR_PROPERTY = "filemanager.data.dir";
	public static final String DATA_DIR_DEFAULT = System.getProperty("user.dir") + File.separator + "data";
	public static final String DATA_DIR = System.getProperty(DATA_DIR_PROPERTY, DATA_DIR_DEFAULT);
	public static final Format DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static final int BUFFER_LENGTH = 4 * 1024;

	private FileManager fileManager;

	public FileManagerServlet() {
		super();
		fileManager = new FileManager();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String contextPath = req.getContextPath();
		String path = "/" + req.getRequestURI().replace(req.getContextPath(), "") + "/";
		path = removeDoubleSlashes(path);

		List<FileInfo> filesInfo = getFileInfo(path, contextPath);
		Collections.sort(filesInfo, new NameComparator());

		List<File> files = fileManager.getFiles(path);
		File file = null;
		if (files.size() == 1 && (file = files.get(0)).isFile()) {
			InputStream in = new FileInputStream(file);

			resp.setContentLength((int) file.length());
			resp.setContentType(new MimetypesFileTypeMap().getContentType(file));

			OutputStream out = resp.getOutputStream();
			byte[] bytes = new byte[BUFFER_LENGTH];
			int read = 0;
			while (read != -1) {
				read = in.read(bytes, 0, BUFFER_LENGTH);
				if (read != -1) {
					out.write(bytes, 0, read);
					out.flush();
				}
			}

			in.close();
			out.close();
			return;
		}

		PrintWriter out = resp.getWriter();
		out.println(getHtml(contextPath, path, filesInfo, (String) req.getAttribute("infoMessage"),
				(String) req.getAttribute("errorMessage")));
		out.flush();
		out.close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String contextPath = req.getContextPath();
		String path = "/" + req.getRequestURI().replace(contextPath, "") + "/";
		path = removeDoubleSlashes(path);

		final Part filePart = req.getPart("file");
		final String fileName = getFileName(filePart);

		try {
			fileManager.saveFile(filePart.getInputStream(), path, fileName);
			req.setAttribute("infoMessage", "File uploaded successfully.");
		} catch (IOException ioe) {
			ioe.printStackTrace();
			req.setAttribute("errorMessage", "You either did not specify a file to upload or are "
					+ "trying to upload a file to a protected or nonexistent location." + ioe.getMessage());
		}

		doGet(req, resp);
	}

	private String getFileName(final Part part) {
		final String partHeader = part.getHeader("content-disposition");
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

	private String removeDoubleSlashes(String s) {
		while (s.contains("//")) {
			s = s.replaceAll("//", "/");
		}
		return s;
	}

	private String getHtml(String context, String path, List<FileInfo> filesInfo, String infoMsg, String errorMsg) {
		StringBuffer table = new StringBuffer("<table>");
		table.append("<tr><th></th><th>Name</th><th>Size</th><th>Last Modified</th></tr>");
		table.append("<tr><th colspan=\"4\"><hr></th></tr>");
		for (FileInfo fileInfo : filesInfo) {
			table.append("<tr>");
			table.append("<td></td>");
			table.append("<td><a href=\"" + fileInfo.getUrl() + "\">" + fileInfo.getName() + "</a></td>");
			table.append("<td>" + readableFileSize(fileInfo.getSize()) + "</td>");
			table.append("<td>" + DATE_FORMAT.format(new Date(fileInfo.getLastModified())) + "</td>");
			table.append("</tr>");
		}
		table.append("<tr><th colspan=\"4\"><hr></th></tr>");
		table.append("</table>");

		String form = "<form method=\"POST\" action=\"" + context + path + "\" enctype=\"multipart/form-data\">"
				+ "File: <input type=\"file\" name=\"file\" id=\"file\"/><br>"
				+ "<input type=\"submit\" value=\"Upload\" name=\"upload\" id=\"upload\"/>" + "</form>";

		String msg = "";
		if (infoMsg != null) {
			msg += "<p>INFO: " + infoMsg + "</p>";
		}
		if (errorMsg != null) {
			msg += "<p>ERROR: " + errorMsg + "</p>";
		}

		String html = "<!DOCTYPE html>" + "<html>" + "<head>" + "<meta charset=\"UTF-8\">" + "<title>Index of " + path
				+ "</title>" + "</head>" + "<body>" + msg + "<h1>Index of " + path + "</h1>" + table + form + "</body>"
				+ "</html>";

		return html;
	}

	public static String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	public List<FileInfo> getFileInfo(String path, String contextPath) {
		List<FileInfo> fileInfoList = new ArrayList<FileInfo>();

		File dir = new File(DATA_DIR, path);

		if (new File(DATA_DIR).equals(dir)) {
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
		fileInfo.setUrl(file.getAbsolutePath().substring(DATA_DIR.length()));
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

}

package org.apodhrad.filemanager.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

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

	public static final String INFO_MESSAGE = "infoMessage";
	public static final String ERROR_MESSAGE = "errorMessage";
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

		File file = fileManager.getFile(path);
		if (file.isFile()) {
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

		List<FileInfo> filesInfo = fileManager.getFilesInfo(path, contextPath);
		Collections.sort(filesInfo, new NameComparator());
		
		String infoMsg = (String) req.getAttribute(INFO_MESSAGE);
		String errorMsg = (String) req.getAttribute(ERROR_MESSAGE);
		String html = HtmlGenerator.getHtml(contextPath, path, filesInfo, infoMsg, errorMsg);

		PrintWriter out = resp.getWriter();
		out.println(html);
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
			req.setAttribute(INFO_MESSAGE, "File uploaded successfully.");
		} catch (IOException ioe) {
			ioe.printStackTrace();
			req.setAttribute(ERROR_MESSAGE, "You either did not specify a file to upload or are "
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

}

package org.apodhrad.filemanager.webapp;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HtmlGenerator {

	public static final Format DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static String getHtml(String context, String path, List<FileInfo> filesInfo, String infoMsg, String errorMsg) {
		StringBuffer table = new StringBuffer("<table>");
		table.append("<tr><th></th><th>Name</th><th>Size</th><th>Last Modified</th></tr>");
		table.append("<tr><th colspan=\"4\"><hr></th></tr>");
		table.append("<tr>");
		table.append("<td><img src=\"" + getApacheIcon("back") + "\"/></td>");
		table.append("<td><a href=\"" + context + "/" + path + "/../.." + "\">Parent Directory</a></td>");
		table.append("<td></td>");
		table.append("<td></td>");
		table.append("</tr>");
		for (FileInfo fileInfo : filesInfo) {
			table.append("<tr>");
			table.append("<td><img src=\"" + getIcon(fileInfo) + "\"/></td>");
			table.append("<td><a href=\"" + context + "/" + path + "/" + fileInfo.getName() + "\">"
					+ fileInfo.getName() + "</a></td>");
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

	private static String getIcon(FileInfo fileInfo) {
		String icon = fileInfo.getName();
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

	private String getFileExtension(File file) {
		String name = file.getName();
		try {
			return name.substring(name.lastIndexOf(".") + 1);
		} catch (Exception e) {
			return "unknown";
		}
	}
}

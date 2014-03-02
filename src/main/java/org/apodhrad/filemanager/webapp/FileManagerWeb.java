package org.apodhrad.filemanager.webapp;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/")
public class FileManagerWeb {

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	public void getFileInfo(@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws ServletException, IOException {
		getFileInfo(request, response, "");
	}

	@GET
	@Path("/{path:.*}")
	@Produces(MediaType.TEXT_HTML)
	public void getFileInfo(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@PathParam("path") String path) throws ServletException, IOException {
		List<FileInfo> filesInfo = new FileManagerService().getFileInfo(path);
		Collections.sort(filesInfo, new NameComparator());

		request.setAttribute("path", path);

		request.setAttribute("filesInfo", filesInfo);

		RequestDispatcher dispetcher = request.getRequestDispatcher("/WEB-INF/filemanager.jsp");
		dispetcher.forward(request, response);
	}

	private class NameComparator implements Comparator<FileInfo> {

		public int compare(FileInfo o1, FileInfo o2) {
			return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
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
	
	public static void main(String[] args) {
		System.out.println("aaa".compareTo("bbb"));
		System.out.println("AAA".compareTo("bbb"));
		System.out.println("ccc".compareTo("bbb"));
	}
}

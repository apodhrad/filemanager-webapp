package org.apodhrad.filemanager.webapp;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
		request.setAttribute("name", "Andy");
		RequestDispatcher dispetcher = request.getRequestDispatcher("/WEB-INF/filemanager.jsp");
		dispetcher.forward(request, response);
	}
}

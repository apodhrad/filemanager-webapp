<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>

<html>
<head>
<title><c:out value="Index of /${path}" /></title>
</head>
<body>
	<h1>
		<c:out value="Index of /${path}" />
	</h1>

	<table border="0">
		<tr>
			<th></th>
			<th>Name</th>
			<th>Size</th>
			<th>Last Modified</th>
		</tr>
		<tr>
			<th colspan="4"><hr /></th>
		</tr>
		<tr>
			<td></td>
			<td><a href="<c:url value="/files/${fileInfo.url}/.." />">..</a></td>
			<td></td>
			<td></td>
		</tr>
		<c:forEach items="${filesInfo}" var="fileInfo">
			<tr>
				<td><img src='<c:url value="/icons_32/pdf.png"/>' /></td>
				<td><a href="<c:url value="/files/${fileInfo.url}" />"><c:out
							value="${fileInfo.name}" /></a></td>
				<td><c:out value="${fileInfo.size}" /></td>
				<td><c:out value="${fileInfo.lastModified}" /></td>
			</tr>
		</c:forEach>
		<tr>
			<th colspan="4"><hr /></th>
		</tr>
	</table>

</body>
</html>

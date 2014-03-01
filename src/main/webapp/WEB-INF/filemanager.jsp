<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<html>
<head>
<title>Index of <c:out value="${path}" /></title>
</head>
<body>
	<h1>
		Index of
		<c:out value="${path}" />
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
		<c:forEach items="${filesInfo}" var="fileInfo">
			<tr>
				<td></td>
				<td><c:out value="${fileInfo.name}" /></td>
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

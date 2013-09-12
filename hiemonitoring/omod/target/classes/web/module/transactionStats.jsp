<!-- <%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<p>Hello ${user.systemId}!</p>
<table>
    <tr>
		<td>Site FOSA ID</td>
		<td>${site}</td>
	</tr>
	<tr>
		<td>ARCHIVE QUEUE</td>
		<td>${archiveTotal}</td>
	</tr>
	<tr>
		<td>PROCESSING QUEUE</td>
		<td>${processingTotal}</td>
		<c:forEach var="entry" items="${pq}" varStatus="status">
			<td>${entry.key}</td>
			<td>${entry.value}</td>
		</c:forEach>
	</tr>
		
	
	<tr>
		<td>ERROR QUEUE</td>
		<td>${errorTotal}</td>
	</tr>
	<tr>
		<td>Maximim Response Time</td>
		<td>${max}</td>
	</tr>
	<tr>
		<td>Minimum Response Time</td>
		<td>${min}</td>
	</tr>
	<tr>
		<td>Average Response Time</td>
		<td>${avg}</td>
	</tr>
	<tr>
	<td>Message</td>
	<td><textarea rows="30" cols="35">${xml}</textarea></td>
	
	
	</tr>


</table>


<%@ include file="/WEB-INF/template/footer.jsp"%>-->
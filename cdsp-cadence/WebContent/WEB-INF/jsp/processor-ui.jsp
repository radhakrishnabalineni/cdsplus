<%@page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
 <%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<title>Processor Assignment UI</title>
<link rel="stylesheet" media="screen" href="static-resources/style.css" />
<script type="text/javascript" src="static-resources/jquery-1.11.0.min.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	window.setInterval(function(){
		window.location.reload(true);
	}, 60000);
	$("input.assign-button").click(function(){	
		var contentclass=$(this).parent().parent().find("td:first").text();
		var selectedvalue= $(this).parent().parent().find("td:nth-child(2)").find("span select").val();
		if(selectedvalue =="-1")
			alert("Please select Processor instance");
		else{
			$.get("processor/assignContentClass",{processorinstance:selectedvalue,contentclass:contentclass},function(data){
				if(data=="ok"){
					window.location.reload(true);
				}				  
			});
		}		
	});
	$("input.unassign-button").click(function(){
		var contentclass=$(this).parent().parent().find("td:first").text();
		var processorinstance = $(this).parent().parent().find("td:nth-child(2)").text();
		$.get("processor/unassignContentClass",{processorinstance:processorinstance,contentclass:contentclass},function(data){
			if(data=="ok"){
			  window.location.reload(true);
			}
		});
	});
});
</script>
</head>
<body>
<div class="container">
<h2>Processor Assignment UI</h2>
<h3>List of All Processor Instances</h3>
<table class="hor-zebra all-pi">
    <thead>
    	<tr>
        	<th scope="col">Instance Name</th>
            <th scope="col">Server Name</th>
            <th scope="col">Processor Location</th>
            <th scope="col">Status</th>
        </tr>
    </thead>
    <tbody>
	    <c:forEach var="processor" items="${it.processors}" varStatus="loopStatus">
	       <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
        	<td><c:out value="${processor._id}"/></td>
            <td><c:out value="${processor.servername}"/></td>
            <td><c:out value="${processor.installationlocation}"/></td>
            <td><c:if test="${processor.isrunning =='true'|| processor.isrunning == true}">
            		<c:out value="Running"/>
            	</c:if>
            	<c:if test="${!(processor.isrunning =='true'|| processor.isrunning == true)}">
            		<c:out value="Stopped"/>
            	</c:if>
            </td>
        	</tr>
	    </c:forEach>
    </tbody>
</table>
<br/>
<h3>List of Assigned Content Classes</h3>
<table class="hor-zebra assigned-cc">
    <thead>
    	<tr>
        	<th scope="col" width="40%">Content Class</th>
            <th scope="col" width="30%">Processor Instance</th>
            <th scope="col" width="30%"></th>           
        </tr>
    </thead>
    <tbody>
    	<c:forEach var="contentclassmap" items="${it.assignedclasses}" varStatus="loopStatus">
	       <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
		       <td><c:out value="${contentclassmap.contentClassName}"/></td>
	           <td><c:out value="${contentclassmap.processorInstanceName}"/></td>
	           <td><input type="button" class="button-style unassign-button" value="Unassign"/></td>  
           </tr>
	    </c:forEach>    	
    </tbody>
</table>
<br/>
<h3>List of Unassigned Content Classes</h3>
<table class="hor-zebra unassigned-cc">
    <thead>
    	<tr>
        	<th scope="col" width="40%">Content Class</th>
			<th scope="col" width="30%">Processor Instance</th>   
            <th scope="col" width="30%"></th>           
        </tr>
    </thead>
    <tbody>
    	<c:forEach var="contentclass" items="${it.unassignedclasses}" varStatus="loopStatus">
	       <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
		       <td><c:out value="${contentclass}"/></td>
	           <td>
				<span class="my-dropdown my-dropdown-color-2673ec">
				<select>
					<option value="-1">Select</option>
					<c:forEach var="processor" items="${it.processors}">
					<c:if test="${processor.isrunning =='true'|| processor.isrunning == true}">
						<option value="${processor._id}"><c:out value="${processor._id}"/></option>
					</c:if>
					</c:forEach>					
				</select>
				</span>
			 </td>
             <td><input type="button" class="button-style assign-button" value="Assign"/></td>    
           </tr>
	    </c:forEach>        	
    </tbody>
</table>
<c:if test="${fn:length(it.intermediateclasses)>0}">
<br/>
<div class="message-box">
<c:forEach var="intermediateclass" items="${it.intermediateclasses}">
A Processor Instance is initiated to stop processing content class <b><c:out value="${intermediateclass.contentClassName}"/>.</b><div class="progressbar"><img src="static-resources/ajax-loader.gif"/></div>
<br/>
</c:forEach>
</div>
</c:if>
<c:if test="${fn:length(it.assigningprogressclasses)>0}">
<br/>
<div class="message-box">
<c:forEach var="assigningprogressclass" items="${it.assigningprogressclasses}">
Content class <b><c:out value="${assigningprogressclass.contentClassName}"/></b> is being assigned to Processor Instance <b><c:out value="${assigningprogressclass.processorInstanceName}"/>.</b><div class="progressbar"><img src="static-resources/ajax-loader.gif"/></div>
<br/>
</c:forEach>
</div>
</c:if>
</div>
</body>
</html>
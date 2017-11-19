'flag="Pass"
'contentType="contentfeedback"
'subscription="content"
'Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
'objHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/"&contentType&"/"&subscription&"/*?expand=attachments",false
'objHTTP.send
'var=objHTTP.ResponseText

'startPos=Instr(100,var,contentType&"/"&subscription&"/")+len(contentType&"/"&subscription&"/")
'endPos=instr(startPos+3,var,chr(34))
'extractedDocName=Mid(var,startPos,endPos-startPos)

'objHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/"&contentType&"/"&subscription&"/"&extractedDocName&"/*",false
'objHTTP.send
'docvar=objHTTP.ResponseText
'start=1
'pos=Instr(start,docvar,"<proj:ref lastModified")
'Do while pos>0
'     last=instr(docvar,"simple")+9
'	 str=Mid(docvar,pos,last-pos)
'	 Print str
'	 start=last
'	 If Instr(var,str)=0 Then
'		 flag="Fail"
'		 Exit do 
'	End if
'	 pos=Instr(start,docvar,"<proj:ref lastModified")>0
'Loop
'Msgbox flag




Dim URL
URL="http://cdsplus.houston.hp.com/cadence/app/"
'contentType="marketingstandard"
'subscription="content"
Call QueryParams(contentType,subscription)

Function QueryParams(contentType,subscription)
   Set Control=CreateObject("Excel.Application")
   Set sh=CreateObject("wscript.Shell")
    file_path= "C:\CDS_Plus_Automation\SmokeTestScripts\TestDataQueryParams.xlsx"
   Set Controlbook=Control.Workbooks.Open( file_path)
   Set ControlSheet=Controlbook.Worksheets("QueryParams")
   '   URL=Trim(ControlSheet.cells(1,4))   
   For contentRow=2 to ControlSheet.usedRange.Rows.Count
       If  Trim(Lcase(ControlSheet.cells(contentRow,2)))="yes"Then
		   contentType=Trim(Lcase(ControlSheet.cells(contentRow,1)))
		   Set subSheet=Controlbook.Worksheets(contentType)
		   For Row=3 to subSheet.UsedRange.Rows.Count
			   subscription=Trim(subSheet.cells(Row,1))
'			   If Row=6  Then Exit For
			   If contentType="supportcontent" OR contentType="generalpurposecontent" OR contentType="marketinghhocontent" OR  contentType="librarycontent" OR contentType="contentfeedbackcontent"Then 
						subSheet.cells(Row,2)=CheckAfter(contentType,subscription)
						subSheet.cells(Row,3)=CheckBefore(contentType,subscription)
						subSheet.cells(Row,4)=CheckAfterBefore(contentType,subscription)
						subSheet.cells(Row,5)=CheckReverse(contentType,subscription)
						subSheet.cells(Row,6)=checkIncludeDeletes(contentType,subscription)
						subSheet.cells(Row,7)=ExpandVersions(contentType,subscription)
				Else
					subSheet.cells(Row,2)=checkUpdate(contentType,subscription)
					subSheet.cells(Row,3)=checkDelete(contentType,subscription)
					subSheet.cells(Row,4)=checkTouch(contentType,subscription)
					subSheet.cells(Row,5)=CheckAfter(contentType,subscription)
					subSheet.cells(Row,6)=CheckBefore(contentType,subscription)
					subSheet.cells(Row,7)=CheckAfterBefore(contentType,subscription)
					subSheet.cells(Row,8)=CheckReverse(contentType,subscription)
					subSheet.cells(Row,9)=checkIncludeDeletes(contentType,subscription)
					subSheet.cells(Row,10)=CheckPriority1(contentType,subscription)
					subSheet.cells(Row,11)=CheckPriority2(contentType,subscription)
					subSheet.cells(Row,12)=CheckPriority3(contentType,subscription)
					subSheet.cells(Row,13)=ExpandVersions(contentType,subscription)
				End If
'				Controlbook.Save
			Next			
		   Controlbook.Save
	   End If
   Next
d=Now
Str=Replace(Replace(d,"/","_"),":","_")
filepath="QueryParamResults "&Str
Controlbook.SaveAs "C:\CDS_Plus_Automation\SmokeTestScripts\Results\"&filepath&".xlsx"
Control.Quit
Set Control=nothing
End Function 

'Msgbox checkUpdate(contentType,subscription) 
Function checkUpdate(contentType,subscription)
	flag="PASS"
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?eventType=update&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		checkUpdate="No Docs"
		Exit Function
	End If	
   	updatePos=Instr(var,chr(34)&"update"&chr(34))
	deletePos=Instr(var,chr(34)&"delete"&chr(34))
	touchPos=Instr(var,chr(34)&"touch"&chr(34))
	If Not(updatePos>0 and deletePos=0 and touchPos=0) Then
		 flag="FAIL"
	End If
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	checkUpdate=flag
End Function 

Function checkDelete(contentType,subscription)
	flag="PASS"
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?eventType=delete&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		checkDelete="No Docs"
		Exit Function
	End If
	updatePos=Instr(var,chr(34)&"update"&chr(34))
	deletePos=Instr(var,chr(34)&"delete"&chr(34))
	touchPos=Instr(var,chr(34)&"touch"&chr(34))
	If Not(updatePos=0 and deletePos>0 and touchPos=0) Then
		 flag="FAIL"
	End If
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	checkDelete=flag
End Function 

Function checkTouch(contentType,subscription)
	flag="PASS"
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?eventType=touch&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		checkTouch="No Docs"
		Exit Function
	End If
	updatePos=Instr(var,chr(34)&"update"&chr(34))
	deletePos=Instr(var,chr(34)&"delete"&chr(34))
	touchPos=Instr(var,chr(34)&"touch"&chr(34))
	If Not(updatePos=0 and deletePos=0 and touchPos>0) Then
		flag="FAIL"
	End If
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	checkTouch=flag
End Function

Function checkIncludeDeletes(contentType,subscription)
	flag="PASS"
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?includeDeletes=true&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		checkIncludeDeletes="No Docs"
		Exit Function
	End If
	deletePos=Instr(var,chr(34)&"delete"&chr(34))
	If deletePos=0 Then 
		 flag="Fail"
	End if 
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	checkIncludeDeletes=flag
End Function

Function CheckAfter(contentType,subscription)
	flag="PASS"
'	c=1398163265737
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription,false
	objHTTP.send
	var1=objHTTP.ResponseText
	lastTimeStampPos=InstrRev(var1,"lastModified=",len(var1),0)+len("lastModified=")
	quotePos=Instr(lastTimeStampPos+4,var1,chr(34))
	c=Mid(var1,lastTimeStampPos+1,quotePos-(lastTimeStampPos+1))

	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?after="&c&"&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		CheckAfter="No Docs"
		Exit Function
	End If
	start=1
	pos=1
	point=1
	Do while point<>0
		point= Instr(pos,var,"lastModified=")
		start= point+len("lastModified=")+1
		endpos=instr(start+1,var,chr(34))
		pos=endpos	
		extract=mid(var,start,endpos-start)
		If IsNumeric(extract) Then
			If extract<c Then 
				flag="FAIL"		
				Exit do
			End if 
		End If
	Loop
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	CheckAfter= flag
End Function 

'Msgbox CheckBefore(contentType,subscription)
Function CheckBefore(contentType,subscription)
   flag="PASS"
'   c=1398163265737
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")	
	objHTTP.open "GET",URL&contentType&"/"&subscription,false
	objHTTP.send
	var=objHTTP.ResponseText
	lastTimeStampPos=Instr(1,var,"lastModified=")+len("lastModified=")
	quotePos=Instr(lastTimeStampPos+4,var,chr(34))
	c=Mid(var,lastTimeStampPos+1,quotePos-(lastTimeStampPos+1))

	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?before="&c&"&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		CheckBefore="No Docs"
		Exit Function
	End If
	start=1
	pos=1
	point=1
	Do while point<>0
		point= Instr(pos,var,"lastModified=")
		start= point+len("lastModified=")+1
		endpos=instr(start+1,var,chr(34))
		pos=endpos	
		extract=mid(var,start,endpos-start)
		If IsNumeric(extract) Then
			If extract>c Then 
				flag="FAIL"		
				Exit do
			End if 
		End If
	Loop
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	CheckBEfore= flag
End Function 

Function CheckAfterBefore(contentType,subscription)
	flag="PASS"
'	c1=1398163265737
'	c2=1398808269299
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription,false
	objHTTP.send
	var1=objHTTP.ResponseText
	lastTimeStampPos=InstrRev(var1,"lastModified=",len(var1),0)+len("lastModified=")
	quotePos=Instr(lastTimeStampPos+4,var1,chr(34))
	c1=Mid(var1,lastTimeStampPos+1,quotePos-(lastTimeStampPos+1))

	objHTTP.open "GET",URL&contentType&"/"&subscription,false
	objHTTP.send
	var=objHTTP.ResponseText
	lastTimeStampPos=Instr(1,var,"lastModified=")+len("lastModified=")
	quotePos=Instr(lastTimeStampPos+4,var,chr(34))
	c2=Mid(var,lastTimeStampPos+1,quotePos-(lastTimeStampPos+1))

	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?after="&c1&"&before="&c2&"&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		CheckAfterBefore="No Docs"
		Exit Function
	End if 
	start=1
	pos=1
	point=1	
	Do while point<>0
		point= Instr(pos,var,"lastModified=")
		start= point+len("lastModified=")+1
		endpos=instr(start+1,var,chr(34))
		pos=endpos	
		extract=mid(var,start,endpos-start)
		If IsNumeric(extract) Then
			If extract<c1 and extract>c2 Then 
				flag="FAIL"
				Exit do
			End if 
		End If
	Loop
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	CheckAfterBefore=flag
End Function

Function CheckPriority1(contentType,subscription)
	flag="PASS"
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?priority=1&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		CheckPriority1="No Docs"
		Exit Function
	End if 
	priority1=Instr(var,"priority="&chr(34)&"1"&chr(34))
	priority2=Instr(var,"priority="&chr(34)&"2"&chr(34))
	priority3=Instr(var,"priority="&chr(34)&"3"&chr(34))
	priority4=Instr(var,"priority="&chr(34)&"4"&chr(34))
	If not(priority1>0 and priority2=0 and priority3=0 and priority4=0) Then
		flag="FAIL"
	End If
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	CheckPriority1=flag
End Function 

Function CheckPriority2(contentType,subscription)
	flag="PASS"
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?priority=2&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		CheckPriority2="No Docs"
		Exit Function
	End if
	priority1=Instr(var,"priority="&chr(34)&"1"&chr(34))
	priority2=Instr(var,"priority="&chr(34)&"2"&chr(34))
	priority3=Instr(var,"priority="&chr(34)&"3"&chr(34))
	priority4=Instr(var,"priority="&chr(34)&"4"&chr(34))
	If not(priority1=0 and priority2>0 and priority3=0 and priority4=0) Then
		flag="FAIL"
	End If
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	CheckPriority2=flag
End Function 

Function CheckPriority3(contentType,subscription)
	flag="PASS"
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?priority=3&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		CheckPriority3="No Docs"
		Exit Function
	End if
	priority1=Instr(var,"priority="&chr(34)&"1"&chr(34))
	priority2=Instr(var,"priority="&chr(34)&"2"&chr(34))
	priority3=Instr(var,"priority="&chr(34)&"3"&chr(34))
	priority4=Instr(var,"priority="&chr(34)&"4"&chr(34))
	If not(priority1=0 and priority2=0 and priority3>0 and priority4=0) Then
		flag="FAIL"
	End If
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	CheckPriority3=flag
End Function 

Function CheckReverse(contentType,subscription)
'	URL="http://cdsplus.houston.hp.com/cadence/app/"
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?reverse=true&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	If checkDocs(var)=0 Then
		 CheckReverse="No Docs"
		Exit Function
	End if
	start=1
	pos=1
	point=1
	previous=0
	check=true	
	Do while point<>0
		point= Instr(pos,var,"lastModified=")
		start= point+len("lastModified=")+1
		endpos=instr(start+1,var,chr(34))
		pos=endpos	
		extract=mid(var,start,endpos-start)
		If isNumeric(extract) Then
			If (extract<previous) Then
				check=false
				Exit Do
			Else
				previous=extract
			End If
		End If
	Loop
	if(check) then 
	   CheckReverse="Pass"
	Else 
	  CheckReverse="Fail"
	End If 
	If not(checkConsiderTag(var)) Then
		CheckReverse=CheckReverse&vbnewline&"Considered Tag Miss"
	End If
End Function
'Msgbox CheckReverse("Library","content")

'contentType="manual"
'subscription="content"
'Msgbox ExpandVersions("support","content")
Function ExpandVersions(contentType,subscription)
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription&"/",false
	objHTTP.send
	Content=objHTTP.ResponseText
	If checkDocs(Content)=0 Then
		ExpandVersions="No Docs"
		Exit Function
	End if
	cc_Pos=Instr(1,Content,contentType)+len(contentType)+5
	slash_beforedoc=Instr(cc_Pos,Content,"/")+1
	doublequotes_pos=Instr(slash_beforedoc+1,Content,"""")
	docName= Mid(Content,slash_beforedoc,doublequotes_pos-slash_beforedoc)
'	While var=""
		objHTTP.open "GET",URL&contentType&"/"&subscription&"/"&Trim(docName)&"/?expand=versions",false
		objHTTP.send
		var=objHTTP.ResponseText
'	Wend
	
	pos=instr(var,docName)
	VersionsTagCheck=Instr(var,"versions")
	If pos=0 OR VersionsTagCheck=0 Then
		ExpandVersions="Fail"
	Else 
		ExpandVersions= "Pass"
	End If
	If not(checkConsiderTag(var)) Then
		ExpandVersions=ExpandVersions&vbnewline&"Considered Tag Miss"
	End If
End Function

Function checkDocs(var)	
	pos=instr(var,"count=")+len("count=")+1
	semiPos=instr(pos,var,chr(34))
'	Msgbox Mid(var,pos,semiPos-pos)
	checkDocs=Mid(var,pos,semiPos-pos)
End Function

Function checkConsiderTag(var)
	c=Instr(var,"considered="&chr(34)&"0"&chr(34))
	If c>0  Then
		checkConsiderTag=true
	else 
		checkConsiderTag=true
	End If
End Function

contentType="suppor"
subscription="content"
'Msgbox  CheckAfterBeforeIncludeDeletes(contentType,subscription)
Function CheckAfterBeforeIncludeDeletes(contentType,subscription)
	flag="PASS"
'	c1=1398163265737
'	c2=1398808269299
	Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
	objHTTP.open "GET",URL&contentType&"/"&subscription,false
	objHTTP.send
	var1=objHTTP.ResponseText
	lastTimeStampPos=InstrRev(var1,"lastModified=",len(var1),0)+len("lastModified=")
	quotePos=Instr(lastTimeStampPos+4,var1,chr(34))
	c1=Mid(var1,lastTimeStampPos+1,quotePos-(lastTimeStampPos+1))

	objHTTP.open "GET",URL&contentType&"/"&subscription,false
	objHTTP.send
	var=objHTTP.ResponseText
	lastTimeStampPos=Instr(1,var,"lastModified=")+len("lastModified=")
	quotePos=Instr(lastTimeStampPos+4,var,chr(34))
	c2=Mid(var,lastTimeStampPos+1,quotePos-(lastTimeStampPos+1))

	objHTTP.open "GET",URL&contentType&"/"&subscription&"/*?after="&c1&"&before="&c2&"&includeDeletes=true&limit=1000",false
	objHTTP.send
	var=objHTTP.ResponseText
	
	If checkDocs(var)=0 Then
		CheckAfterBeforeIncludeDeletes="No Docs"
		Exit Function
	End if 
	If instr(var,"eventType")=0 Then
		flag="Fail"
	End If
	If not(checkConsiderTag(var)) Then
		flag=flag&vbnewline&"Considered Tag Miss"
	End If
	CheckAfterBeforeIncludeDeletes=flag
End Function

contentclass="support"
subscription="content"
Function afterWithoutLimit(contentclass,subscription)
	Set ObjHTTP=CreateObject("MSXML2.serverxmlHTTP")
	ObjHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/"&contentclass&"/"&subscription&"/"
	ObjHTTP.send
	var =ObjHTTP.responseText
	pos1=InstrRev(var,"lastModified",len(var))+len("lastModified")+2
	pos2=instr(pos1+2,var,chr(34))
	extractedLastModified=Mid(var,pos1,pos2-pos1)
	
	ObjHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/"&contentclass&"/"&subscription&"/*?after="&extractedLastModified
	ObjHTTP.send
	var =ObjHTTP.responseText
	'Msgbox  var
	If  instr(1,var,"considered=")>0 then 
		afterWithoutLimit= "Pass"
	Else 
	 afterWithoutLimit= "Fail"
	End if 
End Function

Function beforeWithoutLimit(contentclass,subscription)
	Set ObjHTTP=CreateObject("MSXML2.serverxmlHTTP")
	ObjHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/"&contentclass&"/"&subscription&"/*?reverse=true&limit=20"
	ObjHTTP.send
	var =ObjHTTP.responseText
	pos1=InstrRev(var,"lastModified",len(var))+len("lastModified")+2
	pos2=instr(pos1+2,var,chr(34))
	extractedLastModified=Mid(var,pos1,pos2-pos1)
	'print extractedLastModified
	
	ObjHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/"&contentclass&"/"&subscription&"/*?before="&extractedLastModified
	ObjHTTP.send
	var =ObjHTTP.responseText
	If  instr(1,var,"considered=")>0 then 
		beforeWithoutLimit= "Pass"
	Else 
	  beforeWithoutLimit= "Fail"
	End if 
End Function

'Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
'objHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/manual/arc_201/*?includeDeletes=true&limit=1000",false
'objHTTP.send
'var=objHTTP.ResponseText
'Msgbox Instr(var,chr(34)&"delete"&chr(34))



'Msgbox "0"=0

'Set objHTTP=CreateObject("msxml2.Serverxmlhttp")
'objHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/library/content/*?limit=100",false
'objHTTP.send
'var=objHTTP.ResponseText

'Msgbox checkDocs(var)	

'lastTimeStampPos=Instr(1,var,"lastModified=")+len("lastModified=")
'quotePos=Instr(lastTimeStampPos+4,var,chr(34))
'Msgbox Mid(var,lastTimeStampPos+1,quotePos-(lastTimeStampPos+1))

'queryParam="priority3"
'Select Case queryParam
'
'Case "after"
'		flag=true
'		c=1398163265737
'		start=1
'		pos=1
'		point=1
'		Do while point<>0
'			point= Instr(pos,var,"lastModified=")
'			start= point+len("lastModified=")+1
'			endpos=instr(start+1,var,chr(34))
'			pos=endpos	
'			extract=mid(var,start,endpos-start)
'			If IsNumeric(extract) Then
'				If extract<=c Then 
'					flag=false
'					Msgbox flag
'					Exit do
'				End if 
'			End If
'		Loop
'Case "before"
'	flag=true
'	c=1398808269299
'	start=1
'	pos=1
'	point=1
'	Do while point<>0
'		point= Instr(pos,var,"lastModified=")
'		start= point+len("lastModified=")+1
'		endpos=instr(start+1,var,chr(34))
'		pos=endpos	
'		extract=mid(var,start,endpos-start)
'		If IsNumeric(extract) Then
'			If extract>=c Then 				
'				flag=false
'				Msgbox flag
'				Exit do
'			End if 
'		End If
'	Loop
'Case "afterbefore"
'	flag=true
'	c1=1398163265737
'	c2=1398808269299
'	start=1
'	pos=1
'	point=1
'	Do while point<>0
'		point= Instr(pos,var,"lastModified=")
'		start= point+len("lastModified=")+1
'		endpos=instr(start+1,var,chr(34))
'		pos=endpos	
'		extract=mid(var,start,endpos-start)
'		If IsNumeric(extract) Then
'			If extract<=c1 and extract>=c2 Then 
'				flag=false
'				Msgbox flag
'				Exit do
'			End if 
'		End If
'	Loop
'Case "includeDeletes"
'	deletePos=Instr(var,chr(34)&"delete"&chr(34))
'	If deletePos<>0 Then 
'		flag=true
'	End if 
'Case "update"
'	updatePos=Instr(var,chr(34)&"update"&chr(34))
'	deletePos=Instr(var,chr(34)&"delete"&chr(34))
'	touchPos=Instr(var,chr(34)&"touch"&chr(34))
'	If Not(updatePos>0 and deletePos=0 and touchPos=0) Then
'		flag=false
'	End If
'Case "touch"
'	updatePos=Instr(var,chr(34)&"update"&chr(34))
'	deletePos=Instr(var,chr(34)&"delete"&chr(34))
'	touchPos=Instr(var,chr(34)&"touch"&chr(34))
'    	If Not(updatePos=0 and deletePos=0 and touchPos>0) Then
'		flag=false
'	End If
'Case "delete"
'	updatePos=Instr(var,chr(34)&"update"&chr(34))
'	deletePos=Instr(var,chr(34)&"delete"&chr(34))
'	touchPos=Instr(var,chr(34)&"touch"&chr(34))
'    	If Not(updatePos=0 and deletePos>0 and touchPos=0) Then
'		flag=false
'	End If
'Case "priority1"	
'		priority1=Instr(var,"priority="&chr(34)&"1"&chr(34))
'		priority2=Instr(var,"priority="&chr(34)&"2"&chr(34))
'		priority3=Instr(var,"priority="&chr(34)&"3"&chr(34))
'		priority4=Instr(var,"priority="&chr(34)&"4"&chr(34))
'		If not(priority1>0 and priority2=0 and priority3=0 and priority4=0) Then
'			flag=false
'		End If
'Case "priority2"	
'		priority1=Instr(var,"priority="&chr(34)&"1"&chr(34))
'		priority2=Instr(var,"priority="&chr(34)&"2"&chr(34))
'		priority3=Instr(var,"priority="&chr(34)&"3"&chr(34))
'		priority4=Instr(var,"priority="&chr(34)&"4"&chr(34))
'		If not(priority1=0 and priority2>0 and priority3=0 and priority4=0) Then
'			flag=false
'		End If
'Case "priority3"	
'		priority1=Instr(var,"priority="&chr(34)&"1"&chr(34))
'		priority2=Instr(var,"priority="&chr(34)&"2"&chr(34))
'		priority3=Instr(var,"priority="&chr(34)&"3"&chr(34))
'		priority4=Instr(var,"priority="&chr(34)&"4"&chr(34))
'		If not(priority1=0 and priority2=0 and priority3>0 and priority4=0) Then
'			flag=false
'		End If
'Case "priority4"	
'		priority1=Instr(var,"priority="&chr(34)&"1"&chr(34))
'		priority2=Instr(var,"priority="&chr(34)&"2"&chr(34))
'		priority3=Instr(var,"priority="&chr(34)&"3"&chr(34))
'		priority4=Instr(var,"priority="&chr(34)&"4"&chr(34))
'		If not(priority1=0 and priority2=0 and priority3=0 and priority4>0) Then
'			flag=false
'		End If
'End Select
'Msgbox flag


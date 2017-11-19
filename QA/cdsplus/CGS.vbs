On Error Resume Next
Set xl=CreateObject("Excel.application")
Set wb=xl.Workbooks.Open("C:\CDS_Plus_Automation\SmokeTestScripts\CGS.xlsx")
Set ws=wb.Worksheets(1)
k=4
takeDocs=Cint(Trim(ws.cells(1,2)))
content_class="cgs"
subscription="content"
Set objHttp = CreateObject("Msxml2.ServerXMLHTTP") 
objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/"&content_class&"/"&subscription&"/*?limit=100", False  
'objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/"&content_class&"/"&subscription&"/*?limit=100", False       
objHttp.Send
Var = objHttp.ResponseText
set objHttp=Nothing 
startPos=1
For z=1 to takeDocs	  
	startPos=instr(startPos,Var,content_class&"/"&subscription&"/")
	EndPos=instr(startPos+1,Var,"""")
	str1=Mid(Var,startPos+len(content_class&"/"&subscription&"/"),EndPos-(startPos+len(content_class&"/"&subscription&"/")))
	str=str&str1&","
	startPos=EndPos
Next 
'str="bpd01906,c00734455"
doc=Split(str,",")
Dim a()
For each docName in doc
	Erase a
	If docName<>""  Then	   	
		cc=0
		Set objHTTP=CreateObject("MSXML2.serverxmlHTTP")
		objHTTP.open "GET","http://cdsplus-itg.austin.hp.com/cadence/app/cgs/content/"&docName&"/",false
		'objHTTP.open "GET","http://cdsplus.austin.hp.com/cadence/app/cgs/content/"&docName&"/",false
		objHTTP.send
		var=objHTTP.responseText
		pos=1
		i=1
		Do While Instr(pos,var,"<group>")>0
			c=Instr(pos,var,"<group>")+Len("<group>")
			c1=Instr(c,var,"</group>")
			pos=c1
			ReDim Preserve a(i)
			a(i)= Mid(var,c,c1-c)
			i=i+1
		Loop

		objHTTP.open "GET","http://cdsplus-itg.houston.hp.com/cadence/app/cgs/content/"&docName&"/",false
		'objHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/cgs/content/"&docName&"/",false
		objHTTP.send
		var=objHTTP.responseText
		If Ubound(a)>0 Then
			If err.number=0 Then
			For i=0 to uBound(a)
				  If inStr(var,a(i))<>0 then cc=cc+1	
			Next
	
			If UbOund(a)+1=cc Then
					ws.cells(k,1)= docName
					ws.cells(k,2)="Pass"
			Else 
					ws.cells(k,1)= docName
					ws.cells(k,2)="Fail"
			End If
			Elseif Err.number=9 then  
		         ws.cells(k,1)= docName
				ws.cells(k,2)="No Groups"
				 err.clear
		End If
	End If
	End If
	k=k+1
Next
filePath=Replace(Replace(now,"/","_"),":","_")
wb.SaveAs "C:\CDS_Plus_Automation\SmokeTestScripts\Results\CGS "&filePath&".xlsx"
'wb.Close
xl.Quit
Set xl=nothing

'Dim a()
'cc=0
'Set objHTTP=CreateObject("MSXML2.serverxmlHTTP")
'objHTTP.open "GET","http://cdsplus-itg.austin.hp.com/cadence/app/cgs/content/"&docName&"/",false
'objHTTP.send
'var=objHTTP.responseText
'pos=1
'i=1
'Do While Instr(pos,var,"<group>")>0
'	c=Instr(pos,var,"<group>")+Len("<group>")
'	c1=Instr(c,var,"</group>")
'	pos=c1
'	ReDim Preserve a(i)
'	a(i)= Mid(var,c,c1-c)
'	i=i+1
'Loop
'
'objHTTP.open "GET","http://cdsplus-itg.houston.hp.com/cadence/app/cgs/content/"&docName&"/",false
'objHTTP.send
'var=objHTTP.responseText
'For i=0 to uBound(a)
'    If inStr(var,a(i))<>0 then cc=cc+1	
'Next
'
'If UbOund(a)+1=cc Then
'	MSgbox "Pass"
'Else 
'   Msgbox "Fail"
'End If
'

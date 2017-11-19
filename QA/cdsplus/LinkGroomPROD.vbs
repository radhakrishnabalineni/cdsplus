URL="http://cdsplus"
Set xl=CreateObject("Excel.Application")
Set wb=xl.Workbooks.Open("C:\CDS_Plus_Automation\SmokeTestScripts\LinkGroom_TestData.xlsx")
Set ws=wb.Worksheets(1)
For x=2 to ws.usedRange.Rows.Count
    contentclass=Trim(ws.cells(x,1))
	subscription=Trim(ws.cells(x,2))
	docName=Trim(ws.cells(x,3))
	ws.cells(x,4)=LinkGroom(contentclass,subscription,docName)
	Ws.cells(x,5)=Now
Next
d=Now
Str=Replace(Replace(d,"/","_"),":","_")
filepath="LinkGroom "&Str
wb.SaveAs "C:\CDS_Plus_Automation\SmokeTestScripts/Results/"&filepath&".xlsx"
wb.Close
xl.Quit
Set xl=Nothing


Function LinkGroom(contentclass,subscription,docName)
		flag="Match"
		Dim address()
		Dim addr()
		Set objHTTP=CreateObject("MSXML2.ServerxmlHTTP")
		objHTTP.open "GET",URL&".houston.hp.com/cadence/app/"&contentclass&"/"&subscription&"/"&docName&"/",false
		objHTTP.send
		var=objHTTP.responseText
        i=0
		pos=1
		c=Instr(pos,var,"address=")+len("address=")+1
		Do while c>len("address=")+1
			ReDim Preserve  address(i)
			SemiPos=Instr(c,var,chr(34))
		   address(i)= Mid(var,c,SemiPos-c)
		   i=i+1
		   pos=c
			c=Instr(pos,var,"address=")+len("address=")+1
		Loop
		
		objHTTP.open "GET",URL&".austin.hp.com/cadence/app/"&contentclass&"/"&subscription&"/"&docName&"/",false
		objHTTP.send
		var=objHTTP.responseText
		j=0
		pos=1
		c=Instr(pos,var,"address=")+len("address=")+1
		Do while c>len("address=")+1
			ReDim Preserve  addr(j)
	
		SemiPos=Instr(c,var,chr(34))
	   addr(j)= Mid(var,c,SemiPos-c)
	   j=j+1
	   pos=c
		c=Instr(pos,var,"address=")+len("address=")+1
	Loop
	
	If Ubound(address)=ubound(addr) Then
		For k=0 to Ubound(address)
		   If address(k)<>addr(k) Then 
			   flag="Mismatch"
			   Exit for 
			End If
		Next
	Else 
		flag="Mismatch"
	End If
	LinkGroom= flag
End Function

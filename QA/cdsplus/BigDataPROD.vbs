Set objHTTP=CreateObject("MSXML2.serverxmlHTTP")
Set xl=CreateObject("Excel.application")
Set wb=xl.Workbooks.Open("C:\CDS_Plus_Automation\SmokeTestScripts\BigDocumentsTestData.xlsx")
Set ws=wb.Worksheets(1)
For i=2 to ws.usedRange.Rows.Count
	contentclass=Lcase(Trim(ws.cells(i,1)))
	subscription=Lcase(Trim(ws.cells(i,2)))
	docName=Lcase(Trim(ws.cells(i,3)))

	var=""
	Do while var=""
'		objHTTP.open "GET","http://cdsplus.houston.hp.com/cadence/app/"&contentclass&"/"&subscription&"/"&docName&"/"
		objHTTP.open "http://cdsplus.houston.hp.com/cadence/app/"&contentclass&"/"&subscription&"/"&docName&"/"
		objHTTP.send
		Wait(300)
		var=objHTTP.responseText
'		Msgbox var
	Loop
	If instr(var,"document xml:base")>0  Then
		ws.cells(i,4)="Opened"
	Else 
		ws.cells(i,4)="Not Opened"
	End If
Next
filePath=Replace(Replace(now,"/","_"),":","_")
wb.SaveAs "C:\CDS_Plus_Automation\SmokeTestScripts\Results\BigData "&filePath&".xlsx"
xl.Quit
Set xl=nothing

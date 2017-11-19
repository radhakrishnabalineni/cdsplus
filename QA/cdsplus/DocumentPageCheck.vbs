


'SubscriptionData_path="C:\CDSPAutomation\SubscriptionTestData.xlsx"
'SubscriptionData_path="C:\CDSPAutomation\ProductFilterTestDataPROD.xlsx"
SubscriptionData_path="C:\CDSPAutomation\ProductFilterTestData.xlsx"
Results_path="C:\CDSPAutomation\DocumentPageResults"
ControlFile_Path="C:\CDSPAutomation\ControlFile.xlsx"


Set objHttp = CreateObject("Msxml2.ServerXMLHTTP")
Set Xl=CreateObject("Excel.Application")
Set Wb2=Xl.Workbooks.Open(SubscriptionData_path)
'Set Wb3=Xl.Workbooks.Open(Results_path)
Set Wb3=Xl.Workbooks.Add()
Set Wb4=Xl.Workbooks.Open(ControlFile_Path)
Set ws4=Wb4.worksheets(1)
Set ws3=Wb3.worksheets(1)
TestURL="http://cdsplus-itg.houston.hp.com/cadence/app/"



For i=2 To  ws4.usedrange.rows.count

resRow=2

If ws4.cells(i,4) = "Yes" Then

'MsgBox ws4.cells(i,1)

worksheetName=ws4.cells(i,1)

	Set ws2=Wb2.worksheets(worksheetName)
	 Wb3.worksheets.Add().name=worksheetName
	 Set ws3=Wb3.worksheets(worksheetname)
	 Wb3.worksheets(worksheetname).activate

	 ws3.cells(1,1)="Subscription_Name"
	 ws3.cells(1,2)="Document ID"
	 ws3.cells(1,3)="Document page is opened correctly"
	 
  

		For m=2 To ws2.usedrange.rows.count

		If ws2.cells(m,2) <> "" then
		
				var=""
				While var = ""
	   					objHttp.Open "GET", TestURL&worksheetName&"/"&ws2.cells(m,1)&"/"&ws2.cells(m,2), False 
  						objHttp.Send
						var = objHttp.ResponseText
     			Wend

  
		invalidDoc=InStr(var,"doesn't exist")
		errorDoc1=InStr(var,"404 error")
		errorDoc2=InStr(var,"500 error")

		ws3.cells(resRow,1)=ws2.cells(m,1)
		ws3.cells(resRow,2)=ws2.cells(m,2)
	

		If invalidDoc > 0 Or errorDoc1 > 0 Or errorDoc2 > 0 Then
			ws3.cells(resRow,3)="Document does not exist"
			'MsgBox "doc does not exist"
		Else
			ws3.cells(resRow,3)="Yes"
			'MsgBox "doc opened"
		End if
  
		
	Else
		ws3.cells(resRow,1)=ws2.cells(m,1)
		ws3.cells(resRow,2)=ws2.cells(m,2)
		ws3.cells(resRow,3)="No Data in datasheet"
		'MsgBox "no data"
	End If
	
	resRow=resRow+1
		
  Next

  End if

  Next

  MsgBox "check done"

   d=date 
str1=Replace(d,"/","_")
'MsgBox str1

t=Time
str2=Replace(t,":","_")
'MsgBox str2

str=str1&"_"&str2
'MsgBox str
  
  Results_path=Results_path&"_ITG_"&str&".xlsx"

Wb3.saveAs(Results_path)
  
  
'Wb3.save
Wb3.close
Wb2.close
Wb4.close
Xl.quit
Set XL=nothing 
Set objHttp=nothing


















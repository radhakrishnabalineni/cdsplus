
'SubscriptionData_path="C:\CDSPAutomation\SubscriptionTestData.xlsx"
'SubscriptionData_path="C:\CDSPAutomation\ProductFilterTestDataPROD.xlsx"
SubscriptionData_path="C:\CDSPAutomation\ProductFilterTestData.xlsx"
Results_path="C:\CDSPAutomation\SubscriptionPageResults"
ControlFile_Path="C:\CDSPAutomation\ControlFile.xlsx"

Set objIE=createobject("internetexplorer.application")
                objIE.visible = True
                objIE.TheaterMode = False 
                objIE.AddressBar = True
                objIE.StatusBar = False
                objIE.MenuBar = True
                objIE.FullScreen = False 
                objIE.Navigate "about:blank"

Set Xl=CreateObject("Excel.Application")
Set Wb2=Xl.Workbooks.Open(SubscriptionData_path)
'Set Wb3=Xl.Workbooks.Open(Results_path)
Set Wb3=Xl.Workbooks.Add()
Set Wb4=Xl.Workbooks.Open(ControlFile_Path)
Set ws4=Wb4.worksheets(1)
Set ws3=Wb3.worksheets(1)



For i=2 To  ws4.usedrange.rows.count

If ws4.cells(i,2) = "Yes" Then

'MsgBox ws4.cells(i,1)

worksheetName=ws4.cells(i,1)

	Set ws2=Wb2.worksheets(worksheetName)
	 Wb3.worksheets.Add().name=worksheetName
	 Set ws3=Wb3.worksheets(worksheetname)
	 Wb3.worksheets(worksheetname).activate

	 ws3.cells(1,1)="Subscription_Name"
	 ws3.cells(1,2)="Subscription page is opened correctly"
	 ws3.cells(1,3)="All columns are displayed"
	 ws3.cells(1,4)="Cosidered tag is displayed"
	 ws3.cells(1,5)="No of documnets displayed matches with count value"
  

		For m=2 To ws2.usedrange.rows.count
		

	 objIE.Navigate  "http://cdsplus-itg.houston.hp.com/cadence/app/"&ws4.cells(i,1)&"/"&ws2.cells(m,1)&"/"

	Do Until objIE.ReadyState = 4
       WScript.Sleep 100
	Loop

  var= objIE.Document.Body.outerHTML
  'MsgBox var
  countPos=InStr(var,"count")
  consideredPos=InStr(var,"considered")
   If consideredPos > 0 Then
   ws3.cells(m,4)="Yes"
   Else
   ws3.cells(m,4)="No"
   End If
   
   errorMsg=InStr(var,"exception")

  		If countPos > 0  And errorMsg = 0 Then

		countDigits=0
		countPos=countPos+38
		countPos1=countPos
		countVal=""
		Do 
		countVal=Mid(var,countPos1,1)
		'MsgBox "count digit:" & countVal
		countPos1=countPos1+1
		countDigits=countDigits+1
		Loop While countVal <> "<"

		countDigits=countDigits-1
		countVal=Mid(var,countPos,countDigits)

		'MsgBox Len(countVal)
		' MsgBox countVal
		eventTypePos=InStr(var,"eventType")
		hasAttachmentsPos=InStr(var,"hasAttachments")
		lastModifiedPos=InStr(var,"lastModified")
		priorityPos=InStr(var,"priority")
			If countVal > 0 then
				ws3.cells(m,1)=ws2.cells(m,1)
				ws3.cells(m,2)="Yes"
	 
				If eventTypePos > 0 And  hasAttachmentsPos > 0 And lastModifiedPos > 0 And priorityPos > 0 Then 
					ws3.cells(m,3)="Yes"
				Else
					ws3.cells(m,3)="No"
				End If
				lineStartPos=1
				lineCount=0
				docLinePos=1
				Do While docLinePos <> 0
					docLinePos=InStr(lineStartPos,var,"proj:ref")
					lineCount=lineCount+1
					lineStartPos=docLinePos+8
				Loop 
				
				lineCount=lineCount-1
				'MsgBox lineCount
				If CStr(lineCount) = CStr(countVal) Then
				ws3.cells(m,5)="Yes"
				'MsgBox "No of documnets displayed matches with count value"
				Else
				ws3.cells(m,5)="No"
				'MsgBox "No of documnets displayed does not matches with count value"
				End if


   
 ' MsgBox "Subscription page is opened correctly and contains all columns"
			Else
				ws3.cells(m,1)=ws2.cells(m,1)
				ws3.cells(m,2)="Found 0 records"
			End If
  Else
	ws3.cells(m,1)=ws2.cells(m,1)
	If errorMsg > 0 Then
	ws3.cells(m,2)="Page loded with exception"
	else
	ws3.cells(m,2)="No"
	End if
  End if
    	
	

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
objIE.quit


















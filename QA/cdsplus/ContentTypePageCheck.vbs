


'SubscriptionData_path="C:\CDSPAutomation\SubscriptionTestData.xlsx"
'SubscriptionData_path="C:\CDSPAutomation\ProductFilterTestDataPROD.xlsx"
SubscriptionCount_Path="C:\CDSPAutomation\SubscriptionCount.xlsx"
Results_path="C:\CDSPAutomation\ContentTypePageResults"
ControlFile_Path="C:\CDSPAutomation\ControlFile.xlsx"

Dim URL(9),SheetName(9)

URL(1)="http://cdsplus-itg.houston.hp.com/cadence/app/"
URL(2)="http://g9t3886.houston.hp.com:9080/cadence/app/"
URL(3)="http://g9t3886.houston.hp.com:9180/cadence/app/"
URL(4)="http://g9t3886.houston.hp.com:9280/cadence/app/"
URL(5)="http://g9t3886.houston.hp.com:9380/cadence/app/"
URL(6)="http://g9t3887.houston.hp.com:9080/cadence/app/"
URL(7)="http://g9t3887.houston.hp.com:9180/cadence/app/"
URL(8)="http://g9t3887.houston.hp.com:9280/cadence/app/"
URL(9)="http://g9t3887.houston.hp.com:9380/cadence/app/"

SheetName(1)="cdsplus-itg.houston.hp.com"
SheetName(2)="g9t3886.houston.hp.com_9080"
SheetName(3)="g9t3886.houston.hp.com_9180"
SheetName(4)="g9t3886.houston.hp.com_9280"
SheetName(5)="g9t3886.houston.hp.com_9380"
SheetName(6)="g9t3887.houston.hp.com_9080"
SheetName(7)="g9t3887.houston.hp.com_9180"
SheetName(8)="g9t3887.houston.hp.com_9280"
SheetName(9)="g9t3887.houston.hp.com_9380"



Set objIE=createobject("internetexplorer.application")
                objIE.visible = True
                objIE.TheaterMode = False 
                objIE.AddressBar = True
                objIE.StatusBar = False
                objIE.MenuBar = True
                objIE.FullScreen = False 
                objIE.Navigate "about:blank"

Set Xl=CreateObject("Excel.Application")

Set Wb2=Xl.Workbooks.Open(SubscriptionCount_Path)
'Set Wb3=Xl.Workbooks.Open(Results_path)
Set Wb3=Xl.Workbooks.Add()
Set Wb4=Xl.Workbooks.Open(ControlFile_Path)
Set ws4=Wb4.worksheets(1)

For k=1 To 9

worksheetName=SheetName(k)

Wb3.worksheets.Add().name=worksheetName
Set ws3=Wb3.worksheets(worksheetName)

Set ws2=Wb2.worksheets("ITG")
	Set ws3=Wb3.worksheets(worksheetname)
	 Wb3.worksheets(worksheetname).activate

	 ws3.cells(1,1)="ContentType"
	 ws3.cells(1,2)="Content Type page opened correctly"
	 ws3.cells(1,3)="Count of Subscriptions matched"
	ws3.cells(1,4)="No of documnets displayed matches with count value"

For i=2 To  ws4.usedrange.rows.count

If ws4.cells(i,5) = "Yes" Then

'MsgBox ws4.cells(i,1)

	If ws4.cells(i,1) = "app" Then
	
		'objIE.Navigate  "http://cdsplus-itg.houston.hp.com/cadence/app/"
		objIE.Navigate URL(k)

		Do Until objIE.ReadyState = 4
			WScript.Sleep 100
		Loop

	else

		'objIE.Navigate  "http://cdsplus-itg.houston.hp.com/cadence/app/"&ws4.cells(i,1)&"/"
		objIE.Navigate URL(k)&ws4.cells(i,1)&"/"

		Do Until objIE.ReadyState = 4
			WScript.Sleep 100
		Loop

	End if

  var= objIE.Document.Body.outerHTML
  'MsgBox var
  countPos=InStr(var,"count")
  

  'Read count value from Excel sheet
  countVal_Excel=ws2.cells(i,2)
  

  If countPos > 0 Then

  ws3.cells(i,1)=ws2.cells(i,1)
  ws3.cells(i,2)="Yes"

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
	 'MsgBox "count val UI:"&countVal&",Count val excel:"&countVal_Excel
	
		If CInt(countVal) = CInt(countVal_Excel) then
			
			ws3.cells(i,3)="Yes"
 
			
			lineStartPos=1
			lineCount=0
			docLinePos=1
			Do While docLinePos <> 0
				docLinePos=InStr(lineStartPos,var,"proj:ref")
				lineCount=lineCount+1
				lineStartPos=docLinePos+8
			Loop 

			
			If ws4.cells(i,1) = "productmaster" Or ws4.cells(i,1) = "productmastercache" Or ws4.cells(i,1) = "app" then
			lineCount=lineCount-1
			'MsgBox "lineCount:"&lineCount
			Else
			lineCount=lineCount-2
			End If
			
			'MsgBox lineCount
			If CStr(lineCount) = CStr(countVal) Then
			ws3.cells(i,4)="Yes"
			'MsgBox "No of documnets displayed matches with count value"
			Else
			ws3.cells(i,4)="No"
			'MsgBox "No of documnets displayed does not matches with count value"
			End if


   
 ' MsgBox "Subscription page is opened correctly and contains all columns"
		Else
			
			ws3.cells(i,3)="No"
		End If
  Else
	ws3.cells(i,1)=ws2.cells(i,1)
	ws3.cells(i,2)="No"
  End if
   	
	
End if
  Next
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
  
  Results_path=Results_path&str&".xlsx"

Wb3.saveAs(Results_path)
Wb3.close
Wb2.close
Wb4.close
Xl.quit
Set XL=nothing 
objIE.quit


















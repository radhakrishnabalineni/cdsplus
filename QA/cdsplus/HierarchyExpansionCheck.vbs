

SubscriptionsFilter_path="C:\CDSPAutomation\SubscriptionFilterData.xlsx"
SubscriptionData_path="C:\CDSPAutomation\ProductFilterTestData.xlsx"
Results_path="C:\CDSPAutomation\FilterconditionResults"
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

Set Wb1=Xl.Workbooks.Open(SubscriptionsFilter_path)
Set Wb2=Xl.Workbooks.Open(SubscriptionData_path)
Set Wb4=Xl.Workbooks.Open(ControlFile_Path)
'Set Wb3=Xl.Workbooks.Open(Results_path)
Set Wb3=Xl.Workbooks.Add()
Set ws4=Wb4.worksheets(1)




For i=2 To  ws4.usedrange.rows.count
	resRow=2
		If ws4.cells(i,3) = "Yes" Then

			worksheetName1=ws4.cells(i,1)
			worksheetName2=worksheetName1&"_false"

			Set objHttp = CreateObject("Msxml2.ServerXMLHTTP")
			Set ws1=Wb1.worksheets(worksheetName1)
			Set ws1a=Wb1.worksheets(worksheetName2)
			Set ws2=Wb2.worksheets(worksheetName1)
			Wb3.worksheets.Add().name=worksheetName1
			Set ws3=Wb3.worksheets(worksheetName1)
			Wb3.worksheets(worksheetname1).activate

			ws3.cells(1,1)="Content"
			ws3.cells(1,1).Font.Bold=TRUE
			ws3.cells(1,2)="Subscription_Name"
			ws3.cells(1,2).Font.Bold=TRUE
			ws3.cells(1,3)="Document ID"
			ws3.cells(1,3).Font.Bold=TRUE
			ws3.cells(1,4)="Results"
			ws3.cells(1,4).Font.Bold=True
			ws3.cells(1,4).Font.Bold=True
			ws3.cells(1,5)="True Filters Mismatch"
			ws3.cells(1,5).Font.Bold=True
			ws3.cells(1,6)="False Filters Mismatch"
			ws3.cells(1,6).Font.Bold=TRUE
	
			'ws3.cells(1,1)="Results for "&ws2.name
			For m=2 To ws2.usedrange.rows.count
				For n=2 To ws2.usedrange.columns.count

					If ws2.cells(m,n) <> "" Then

						filterFlag1=""
						filterFlag2=""
						trueFilterMismatch=""
						falseFilterMismatch=""

						If worksheetName1 = "soar" then
						var=""
							While var = ""
	   							objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/soar/"&ws2.cells(m,1)&"/"&ws2.cells(m,n), False 
  								objHttp.Send
								var = objHttp.ResponseText
     						Wend

						else

						objIE.Navigate  "http://cdsplus-itg.houston.hp.com/cadence/app/"&worksheetName1&"/"&ws2.cells(m,1)&"/"&ws2.cells(m,n)

							Do Until objIE.ReadyState = 4
								WScript.Sleep 100
							Loop

							var= objIE.Document.Body.outerHTML

						End If
						
							
							feedError=InStr(var,"cannot display this feed")

							InValidDoc=InStr(var,"broken")
								If InValidDoc > 0 Then
									ws3.cells(resRow,1)=worksheetName1
									ws3.cells(resRow,2)=ws2.cells(m,1)
									ws3.cells(resRow,3)=ws2.cells(m,n)
									ws3.cells(resRow,4)="Document does not exist"
									resRow=resRow+1
									
								Elseif ws2.cells(m,1)= "content" Then
									ws3.cells(resRow,1)=worksheetName1
									ws3.cells(resRow,2)=ws2.cells(m,1)
									ws3.cells(resRow,3)=ws2.cells(m,n)
									If feedError > 0 Then
									ws3.cells(resRow,4)="Document did not open due to feed format error"
									else
									ws3.cells(resRow,4)="Document opened under content"
									End if
								Else
										  
										If worksheetName1 = "soar" then
										prodExist=InStr(var,"<products-supported>")
										'MsgBox prodExist
										Else
											If feedError > 0 Then
												prodExist=0
											else
												prodExist=InStr(var,"<SPAN class=m>&lt;</SPAN><SPAN class=t>products</SPAN><SPAN class=m>&gt;</SPAN>")
											End If
										End If
										
										If prodExist > 0 then
  
											  'check for true filters
											  For j=2 To ws1.usedrange.columns.count
											  'MsgBox ws1.cells(m,j)

											  If ws1.cells(m,j) <> "" Then
											    If worksheetName1 = "soar" Then
											    ProdFilter="<"&ws1.cells(m,j)
												else
												ProdFilter="<SPAN class=m>&lt;</SPAN><SPAN class=t>"&ws1.cells(m,j)
												End if
											  strPos1=InStr(var,ProdFilter)
											  'strProduct=Mid(var,strPos,25)

											  ' MsgBox var
											 'MsgBox ProdFilter &":true postion"&strPos1
											  If strPos1 > 0  Then
											  filterFlag1="true"
											  Else
											  filterFlag1="false"
											  trueFilterMismatch=trueFilterMismatch&","&ws1.cells(m,j)
											  End If

											  End if
											 
											 Next
										 
												'Check for false filters
												'For i=2 To ws1.usedrange.rows.count
												For k=2 To ws1a.usedrange.columns.count
												If ws1a.cells(m,k) <> "" Then
													If worksheetName1 = "soar" Then
													ProdFilter="<"&ws1a.cells(m,k)
													else
													ProdFilter="<SPAN class=m>&lt;</SPAN><SPAN class=t>"&ws1a.cells(m,k)
													End if
											  strPos2=InStr(var,ProdFilter)
											  'strProduct=Mid(var,strPos,25)

											  ' MsgBox var
											' MsgBox ProdFilter &":false postion"&strPos2
											  If strPos2 = 0 Or strPos2 = 1  Then
											  filterFlag2="true"
											  Else
											  filterFlag2="false"
											  falseFilterMismatch=falseFilterMismatch&","&ws1a.cells(m,k)
											  End If
											 
											  End If
											  Next
	 

											'MsgBox "filterfFlag1:"&filterFlag1
											'MsgBox "filterfFlag2:"&filterFlag2

											'If filterFlag1="true" Or filterFlag1=""  And filterFlag2="true"  Then
											If trueFilterMismatch = "" And falseFilterMismatch = "" then
											ws3.cells(resRow,1)=worksheetName1
											ws3.cells(resRow,2)=ws2.cells(m,1)
											ws3.cells(resRow,3)=ws2.cells(m,n)
											ws3.cells(resRow,4)="Hierarchy expansion filters match"
											resRow=resRow+1
											Else
											ws3.cells(resRow,1)=worksheetName1
											ws3.cells(resRow,2)=ws2.cells(m,1)
											ws3.cells(resRow,3)=ws2.cells(m,n)
											ws3.cells(resRow,4)="Hierarchy expansion filters does not match"
											ws3.cells(resRow,5)=trueFilterMismatch
											ws3.cells(resRow,6)=falseFilterMismatch
											resRow=resRow+1
											'MsgBox "Hierarchy expansion filters exists for "& ws2.cells(m,n)
											End If
										Else
											If feedError > 0 Then
												ws3.cells(resRow,1)=worksheetName1
												ws3.cells(resRow,2)=ws2.cells(m,1)
												ws3.cells(resRow,3)=ws2.cells(m,n)
												ws3.cells(resRow,4)="Document did not open due to feed format error"
												resRow=resRow+1
																						
											else
												ws3.cells(resRow,1)=worksheetName1
												ws3.cells(resRow,2)=ws2.cells(m,1)
												ws3.cells(resRow,3)=ws2.cells(m,n)
												ws3.cells(resRow,4)="Products do not exist in document"
												resRow=resRow+1
											End if
									End If

							End If
					End if
				Next
			Next
	 End If
 Next

MsgBox "done"

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
  
 ' Wb3.save
Wb3.close
  Wb2.close
Wb1.close
Wb4.close
Xl.quit
Set xl=nothing 
'objIE.quit

















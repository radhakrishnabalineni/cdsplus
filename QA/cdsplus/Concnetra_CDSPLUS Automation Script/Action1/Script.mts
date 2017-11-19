Function ConcentraLogin(Username,password,user)
	SystemUtil.Run "iexplore","https://concentra-itg-new.austin.hp.com/Concentra/code/actions/home.do"	
	Browser("Title:=HP SiteMinder Login.*").Page("title:=HP SiteMinder Login").WebEdit("name:=USER").Set Username
	Browser("Title:=HP SiteMinder Login.*").Page("title:=HP SiteMinder Login").WebEdit("name:=PASSWORD").SetSecure password
	Browser("Title:=HP SiteMinder Login.*").Page("title:=HP SiteMinder Login").WebButton("name:=Log on").Click
	Browser("Title:=Welcome To Concentra.*").Page("title:=Welcome To Concentra").WebRadioGroup("name:=radios").Select user
	Browser("Title:=Welcome To Concentra.*").Page("title:=Welcome To Concentra").Image("alt:=Connect to Concentra").click
'	Window("text:=Welcome To Concentra.*").Type micAltdwn+" "+"x"+micAltup
End Function
'Call ConcentraLogin("sanjeev.dm@hp.com","530ed3c227170091333708f349ad2d05aa3b75800654a5916a7988917142632e8708","test_sa")
'********************************************************************************************************************************************************
Dim str1,str2,Tag_name,docName,MatchedTags,NotMatchedTags,subscription,content_class,RowValue,documentStatus,SceanrioType,docExtractedbyCopy,docsCreated
documentStatus="Found"
RowValue=2
ConcentraDataPath="C:\Users\dmsan\Desktop\Concentra.xls"
'Call DriverCopy()

Function DriverCopy()
	Set xlcopyData=CreateObject("Excel.application")
	Set wbcopyData=xlcopyData.Workbooks.Open("C:\Users\dmsan\Desktop\CDS_Plus_Auto\ExistingDocumentData.xlsx")
	Set wscopyData=wbcopyData.Worksheets("CreatebyCopy")
	SceanrioType="CreateByCopy"
	For dataRow=2 to wscopyData.UsedRange.Rows.Count
		content_class=Trim(wscopyData.cells(dataRow,3))
		subscription=Trim(wscopyData.cells(dataRow,4))
'		If  dataRow=3 Then Exit For 
		If  Lcase(Trim(wscopyData.cells(dataRow,5)))="yes" Then
			Set objHttp = CreateObject("Msxml2.ServerXMLHTTP") 
			objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/"&content_class&"/"&subscription, False       
		   objHttp.Send
		   Var = objHttp.ResponseText
		   set objHttp=Nothing 
		   startPos=1
		  For z=1 to Trim(wscopyData.cells(dataRow,6))		  
			startPos=instr(startPos,Var,content_class&"/"&subscription&"/")
			 EndPos=instr(startPos+1,Var,"""")
			 str1=Mid(Var,startPos+len(content_class&"/"&subscription&"/"),EndPos-(startPos+len(content_class&"/"&subscription&"/")))
			str=str&str1&","
			startPos=EndPos
		Next 
	   wscopyData.cells(dataRow,8)= left(str,len(str)-1)
	   str=""
	   DocumentsForCopy=Trim(wscopyData.cells(dataRow,8))
		Else
		DocumentsForCopy=Trim(wscopyData.cells(dataRow,7))
		End If
'		Documents=Trim(wscopyData.cells(dataRow,7))
		DocName=Split(DocumentsForCopy,",")
		For Each elem In DocName
			Call SearchDocument(elem)
			Call CopyDocument()
				wscopyData.cells(dataRow,2)=wscopyData.cells(dataRow,2)&Trim(docExtractedbyCopy)&","    				
		Next 
		wscopyData.cells(dataRow,2)=left(wscopyData.cells(dataRow,2),len(wscopyData.cells(dataRow,2))-1)
'		Msgbox wscopyData.cells(dataRow,2)	
		wbcopyData.save
	Next
	For dataRow=2 to wscopyData.UsedRange.Rows.Count
'		If  dataRow=3 Then Exit For 
		Documents=Trim(wscopyData.cells(dataRow,2))
		Document=Split(Documents,",")
			For Each Elem in Document 
				docName=Elem 
				Execute Trim(wscopyData.cells(dataRow,1))	
			Next	
		Next
		
	wbcopyData.save
	wbcopyData.close
	xlcopyData.Quit
End Function

Function getFileExtension(filename)   
	First_=instr(filename,"_")	
	Second_=instr(First_+1,filename,"_")	
	getFileExtension=Mid(filename,First_+1,(Second_-First_)-1)
	If getFileExtension="jpeg" Then
        getFileExtension="jpg"
	ElseIf getFileExtension="crtext" Then
		getFileExtension="txt"
	End If
End Function

Function ValidateDocumentProperties(docName)
   If documentStatus="Not Found" Then
	   documentStatus="Found"
	   Exit Function 
   End If
	Set xl=CreateObject("Excel.application")
	Set wb=xl.Workbooks.Open("C:\Users\dmsan\Desktop\Export.xls")
	Set ws=wb.Worksheets("Global")
	
	Set Property_map=CreateObject("Scripting.Dictionary")
	For l=1 to ws.UsedRange.Rows.Count
		Property_map.Add Trim(ws.cells(l,1)),Trim(ws.cells(l,2))
	Next
	
	Set Property_mapped=CreateObject("Scripting.Dictionary")
	
	Set DocnameTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Document Name.*")
	Set AuthorsTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Authors.*","rows:=7")
	Set DisclosureTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Disclosure Level.*")
	
	For m=1 to DocnameTable.RowCount
		If Property_map.Exists(Trim(DocnameTable.GetCellData(m,1))) Then
			Property_mapped.Add Property_map.Item(Trim(DocnameTable.GetCellData(m,1))),Trim(DocnameTable.GetCellData(m,2))
		End If
	Next
	
	For m=1 to AuthorsTable.RowCount
		If Property_map.Exists(Trim(AuthorsTable.GetCellData(m,1))) Then
			Property_mapped.Add Property_map.Item(Trim(AuthorsTable.GetCellData(m,1))),Trim(AuthorsTable.GetCellData(m,2))
		End If
	Next
	
	For p=1 to DisclosureTable.RowCount   
		If Property_map.Exists(Trim(DisclosureTable.GetCellData(p,1))) Then
			Property_mapped.Add Property_map.Item(Trim(DisclosureTable.GetCellData(p,1))),Trim(DisclosureTable.GetCellData(p,2))
		End If
	Next
	
		Set xmlDoc1 = CreateObject("Msxml2.DOMDocument")
		xmlDoc1.load("C:\Users\dmsan\Desktop\"&docName&".xml")
		Set ElemList1= xmlDoc1.DocumentElement.ChildNodes
'		msgbox ElemList1.length
		MatchedTags=""
		NotMatchedTags=""
		        
		For q = 0 to ElemList1.length-1 
			Tag_name=ElemList1.item(q).TagName
					If Property_mapped.Exists(Tag_name) Then
						Tag_name=ElemList1.item(q).TagName
			             Tag_Text=ElemList1.item(q).Text
						str1=Trim(Tag_Text)
						str2=Trim(Property_mapped.Item(Tag_name))						
						
						If  Not(ItemValidate(Tag_name)) Then			
							 NotMatchedTags=NotMatchedTags&Tag_name&","						
						End If
					End If 							
		Next
		Browser("Title:=.*View Document Properties.*","creationTime:=1").Close
		If NotMatchedTags="" or NotMatchedTags="content_version_date," Then
			NotMatchedTags="No Mismatch"
		End If
	Call ExistCheckResults(sceanrioType,content_class,docName,subscription,documentStatus,NotMatchedTags)

'		Msgbox "Matched Tags--"&MatchedTags
'		Msgbox "Not Matched Tags--"&NotMatchedTags	
	wb.Save
	wb.Close
	xl.Quit
	Set xmlDoc1=nothing
	Set xl=nothing
End Function

Function BrowseNSave(docName,content_class)
	Dim objHttp
	 Set objHttp = CreateObject("Msxml2.ServerXMLHTTP")
      objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/"&content_class&"/content/"&docName, False       
	  objHttp.Send
	  Var = objHttp.ResponseText
	If Trim(Var)="The Entry "&docName&" doesn't exist" Then Exit Function
	Set fso=createObject("Scripting.filesystemObject")
	Set f=fso.Createtextfile("C:\Users\dmsan\Desktop\"&docName&".xml")
	f.write(Var)
	f.close
	Set objHttp=nothing 
End Function 

'Call BrowseNSave("c50331127","library")

Function Test()
	Set xl=CreateObject("Excel.application")
	Set wb=xl.Workbooks.Open("C:\Users\dmsan\Desktop\Export.xls")
	Set ws=wb.Worksheets("Global")
	
	Set Property_map=CreateObject("Scripting.Dictionary")
	For i=1 to ws.UsedRange.Rows.Count
		Property_map.Add Trim(ws.cells(i,1)),Trim(ws.cells(i,2))
	Next
	
	Set Property_mapped=CreateObject("Scripting.Dictionary")
	
	Set DocNameTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Name/TitleDocument.*")
	Set GeneralPropTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=General properties.*")
	
	For m=1 to DocNameTable.RowCount
		If DocNameTable.ColumnCount(m)>1 Then
			If Property_map.Exists(Trim(DocnameTable.GetCellData(m,1))) Then
				Property_mapped.Add Property_map.Item(Trim(DocnameTable.GetCellData(m,1))),Trim(DocnameTable.GetCellData(m,2))
			End If
		End If
	Next
	
	For n=1 to GeneralPropTable.RowCount
		If GeneralPropTable.ColumnCount(n)>2 Then
			If Property_map.Exists(Trim(GeneralPropTable.GetCellData(n,1))) Then
				Property_mapped.Add Property_map.Item(Trim(GeneralPropTable.GetCellData(n,1))),Trim(GeneralPropTable.GetCellData(n,2))
			End If
		End If
	Next
	
	
		Set xmlDoc1 = CreateObject("Msxml2.DOMDocument")
		xmlDoc1.load("C:\Users\dmsan\Desktop\c50332171.xml")
		Set ElemList1= xmlDoc1.DocumentElement.ChildNodes
'		msgbox ElemList1.length
	k=1	

		For i = 0 to ElemList1.length-1   
					If Property_mapped.Exists(ElemList1.item(i).TagName) Then
					ws.cells(k,3)=ElemList1.item(i).TagName
					ws.cells(k,4)=ElemList1.item(i).Text
					ws.cells(k,5)=Property_mapped.Item(ElemList1.item(i).TagName)
					k=k+1
				End If
		Next
	
	wb.Save
	wb.Close
	xl.Quit
	Set xmlDoc1=nothing
	Set xl=nothing
End Function
'Call Test()

Function ItemValidate(Tagname)  
   If Tagname="regions" Or Tagname="smartflow_content_types" Then
	    str1=Trim(str1)
		str2=Trim(str2)&space(1)
		lastPos=Len(str2)
			For i= Len(str2) To 1 Step -1 
		'If InStrRev(str2," ",i,0)>0 Then 
				FirstLetter=Mid(str2,i,1)
				If ( i-1)<>0  Then 
						SecondLetter=Mid(str2,i-1,1)
				Else 
						SecondLetter="a"
				End if
	
				If (Asc(FirstLetter)>=65 And Asc(FirstLetter)<=90)  And (Asc(SecondLetter)>=Asc("a") And Asc(SecondLetter)<=Asc("z")) Then
					firstPos=i
					extracted=Mid(str2,i,lastPos-i)
					str=str&extracted&" "
					lastPos=i
					
				ElseIf (Asc(FirstLetter)>=65 And Asc(FirstLetter)<=90)  And (Asc(SecondLetter)=Asc(")"))  Then
					firstPos=i
					extracted=Mid(str2,i,lastPos-i)
					str=str&extracted&" "
					lastPos=i
					
				End If
		'End If
			Next
		
			d=Split(str1)

		For i=0 To ubound(d)
			str=Replace(str,d(i),"")
		Next
		str=Replace(str," ","")
		If Len(str)<=3 Then
			ItemValidate=True
		Else
			ItemValidate=False
		End if

	elseif Tagname= "document_type" or Tagname= "project_name" or Tagname= "description" or Tagname="disclosure_level" or Tagname="feedback_address" or Tagname="object_name" or Tagname="crm_asset_code" or Tagname="full_title" or Tagname="organization" or Tagname="content_url" or Tagname="information_source" or Tagname="content_version_date" Then
		 If Trim(str1)=Trim(str2) Then 
			ItemValidate=True
		Else
			ItemValidate=False
		End If 

		elseif Tagname="content_version" Then 
		If Instr(str2,str1)>0 Then
			ItemValidate=True
		Else
			ItemValidate=False
		End If 
		

	elseif Tagname="authors" or Tagname= "search_keywords" Then
		str2=Replace(str2,", ","")
		str1split=Split(str1)
		For i=0 To ubound(str1split)
			str2=Replace(str2,str1split(i),"")
		Next 
        str2=Replace(str2," ","")
		If Len(str2)=0 Then 
			ItemValidate=True
		Else
			ItemValidate=False
		End If 

	elseIf Tagname="cmg_name" Then
		 If InStr(str2,str1) Then 
				ItemValidate=True
			Else
				ItemValidate=False
			End If 
	 elseIf Tagname="content_version_date" Then       
        If isEmpty(str1) And IsEmpty(str2) Then 
			ItemValidate=True
			Exit Function 
		 str1=Left(str1,10)
		Elseif dateDiff("d",str1,str2)=0 Then
			ItemValidate=True
			Exit Function 
		Else
			ItemValidate=False
		End If 		
     End If	
End Function

Function SearchDocument(docName)
	Browser("Concentra").Page("HomePage").WebEdit("name:=quickSearchDocId").set docName
	Browser("Concentra").Page("HomePage").WebTable("name:=quickSearchDocId").Image("name:=Image").Click
	Browser("Concentra").Page("SearchResults").Sync
	If Browser("Concentra").Page("SearchResults").WebElement("innertext:=Your search did not return any objects.*","index:=0").Exist(5) Then
		documentStatus="Not Found"
		Call ExistCheckResults(SceanrioType,content_class,docName,subscription,documentStatus,NotMatchedTags)
		Exit Function 
	End If
	FInalDocumentRow=Browser("Concentra").Page("SearchResults").WebTable("name:=selectAll").GetRowWithCellText("FINAL")
	If (FInalDocumentRow<=1)Then
		documentStatus="Not Found"
		Call ExistCheckResults(SceanrioType,content_class,docName,subscription,documentStatus,NotMatchedTags)
		Exit Function
	End If
	Browser("Concentra").Page("SearchResults").WebTable("name:=selectAll").ChildItem(FInalDocumentRow,4,"Link",0).Click
	Wait(10)
	Window("text:=.*View Document Properties.*","index:=1").Activate
	Window("text:=.*View Document Properties.*","index:=1").Type micAltdwn+" "+"x"+micAltup
End Function



Function ValidateDocProp_lib_man(docName)
   If documentStatus="Not Found" Then
	   documentStatus="Found"
	   Exit Function 
   End If
'   Msgbox docName
	Set xl=CreateObject("Excel.application")
	Set wb=xl.Workbooks.Open("C:\Users\dmsan\Desktop\Export.xls")
	Set ws=wb.Worksheets("Global")
	
	Set Property_map=CreateObject("Scripting.Dictionary")
	For l=1 to ws.UsedRange.Rows.Count
		Property_map.Add Trim(ws.cells(l,1)),Trim(ws.cells(l,2))
	Next
	
	Set Property_mapped=CreateObject("Scripting.Dictionary")
	Set DocNameTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Name/TitleDocument.*")
	Set GeneralPropTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=General properties.*")
	
	For m=1 to DocNameTable.RowCount
		If DocNameTable.ColumnCount(m)>1 Then
			If Property_map.Exists(Trim(DocnameTable.GetCellData(m,1))) Then
				Property_mapped.Add Property_map.Item(Trim(DocnameTable.GetCellData(m,1))),Trim(DocnameTable.GetCellData(m,2))
			End If
		End If
	Next
	
	For n=1 to GeneralPropTable.RowCount
		If GeneralPropTable.ColumnCount(n)>2 Then
			If Property_map.Exists(Trim(GeneralPropTable.GetCellData(n,1))) Then
				Property_mapped.Add Property_map.Item(Trim(GeneralPropTable.GetCellData(n,1))),Trim(GeneralPropTable.GetCellData(n,2))
			End If
		End If
	Next
	
		Set xmlDoc1 = CreateObject("Msxml2.DOMDocument")
		xmlDoc1.load("C:\Users\dmsan\Desktop\"&docName&".xml")
		Set ElemList1= xmlDoc1.DocumentElement.ChildNodes
'		msgbox ElemList1.length		
		        
		For p = 0 to ElemList1.length-1 
			Tag_name=ElemList1.item(p).TagName
					If Property_mapped.Exists(Tag_name) Then
						Tag_name=ElemList1.item(p).TagName
			             Tag_Text=ElemList1.item(p).Text
						str1=Trim(Tag_Text)
						str2=Property_mapped.Item(Tag_name)
						
						If Not ItemValidate(Tag_name) Then
							NotMatchedTags=NotMatchedTags&Tag_name&"," 
						End If
					End If 							
		Next
		If NotMatchedTags="" or NotMatchedTags="content_version_date," Then
			NotMatchedTags="No Mismatch"
		End If
		Browser("Title:=.*View Document Properties.*","creationTime:=1").Close
		Call ExistCheckResults(sceanrioType,content_class,docName,subscription,documentStatus,NotMatchedTags)
	NotMatchedTags=""
	Matched=""
	wb.Save
	wb.Close
	xl.Quit
	Set xmlDoc1=nothing
	Set xl=nothing
End Function

Function ValidateDocProp_general(docName)
   If documentStatus="Not Found" Then
	   documentStatus="Found"
	   Exit Function 
   End If
	Set xl=CreateObject("Excel.application")
	Set wb=xl.Workbooks.Open("C:\Users\dmsan\Desktop\Export.xls")
	Set ws=wb.Worksheets("Global")
	
	Set Property_map=CreateObject("Scripting.Dictionary")
	For x=1 to ws.UsedRange.Rows.Count
		Property_map.Add Trim(ws.cells(x,1)),Trim(ws.cells(x,2))
	Next
	
	Set Property_mapped=CreateObject("Scripting.Dictionary")
	Set GeneralPropTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=General properties.*")	
	For y=1 to GeneralPropTable.RowCount
		If GeneralPropTable.ColumnCount(y)>1 Then
			If Property_map.Exists(Trim(GeneralPropTable.GetCellData(y,1))) Then
				Property_mapped.Add Property_map.Item(Trim(GeneralPropTable.GetCellData(y,1))),Trim(GeneralPropTable.GetCellData(y,2))
			End If
		End If
	Next
	
		Set xmlDoc1 = CreateObject("Msxml2.DOMDocument")
		xmlDoc1.load("C:\Users\dmsan\Desktop\"&docName&".xml")
		Set ElemList1= xmlDoc1.DocumentElement.ChildNodes
'		msgbox ElemList1.length		
		        
		For z= 0 to ElemList1.length-1 
			Tag_name=ElemList1.item(z).TagName
					If Property_mapped.Exists(Tag_name) Then
						Tag_name=ElemList1.item(z).TagName
			             Tag_Text=ElemList1.item(z).Text
						str1=Trim(Tag_Text)
						str2=Property_mapped.Item(Tag_name)
						
						If  Not ItemValidate(Tag_name) Then
							NotMatchedTags=NotMatchedTags&Tag_name&"," 
						Else 
							Matched=Matched&Tag_name&"," 
						End If
					End If 							
		Next
		If NotMatchedTags="" or NotMatchedTags="content_version_date," Then
			NotMatchedTags="No Mismatch"
		End If
		Call ExistCheckResults(sceanrioType,content_class,docName,subscription,documentStatus,NotMatchedTags)
		Browser("Title:=.*View Document Properties.*","creationTime:=1").Close

		NotMatchedTags=""
	    Matched=""
	wb.Save
	wb.Close
	xl.Quit
	Set xmlDoc1=nothing
	Set xl=nothing
End Function

Function ValidateDocProp_contentfeedback(docName)
   If documentStatus="Not Found" Then
	   documentStatus="Found"
	   Exit Function 
   End If
	Set xl=CreateObject("Excel.application")
	Set wb=xl.Workbooks.Open("C:\Users\dmsan\Desktop\Export.xls")
	Set ws=wb.Worksheets("Global")
	
	Set Property_map=CreateObject("Scripting.Dictionary")
	For i=1 to ws.UsedRange.Rows.Count
		Property_map.Add Trim(ws.cells(i,1)),Trim(ws.cells(i,2))
	Next
	
	Set Property_mapped=CreateObject("Scripting.Dictionary")
	Set ContentInfoTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Concentra Object Info.*")
	For i=1 to ContentInfoTable.RowCount
		If ContentInfoTable.ColumnCount(i)>1 Then
			If Property_map.Exists(Trim(ContentInfoTable.GetCellData(i,1))) Then
				Property_mapped.Add Property_map.Item(Trim(ContentInfoTable.GetCellData(i,1))),Trim(ContentInfoTable.GetCellData(i,2))
			End If
		End If
	Next
	
		Set xmlDoc1 = CreateObject("Msxml2.DOMDocument")
		xmlDoc1.load("C:\Users\dmsan\Desktop\"&docName&".xml")
		Set ElemList1= xmlDoc1.DocumentElement.ChildNodes
'		msgbox ElemList1.length		
		        
		For i = 0 to ElemList1.length-1 
			Tag_name=ElemList1.item(i).TagName
					If Property_mapped.Exists(Tag_name) Then
						Tag_name=ElemList1.item(i).TagName
			             Tag_Text=ElemList1.item(i).Text
						str1=Trim(Tag_Text)
						str2=Property_mapped.Item(Tag_name)
						
						If  Not ItemValidate(Tag_name) Then
							NotMatchedTags=NotMatchedTags&Tag_name&"," 
						End If
					End If 							
		Next
		If NotMatchedTags="" or NotMatchedTags="content_version_date," Then
			NotMatchedTags="No Mismatch"
		End If
		Call ExistCheckResults(sceanrioType,content_class,docName,subscription,documentStatus,NotMatchedTags)
		Browser("Title:=.*View Document Properties.*","creationTime:=1").Close	
		NotMatchedTags=""
		Matched=""
	wb.Save
	wb.Close
	xl.Quit
	Set xmlDoc1=nothing
	Set xl=nothing
End Function

Function ValidateDocProp_marketinghho_nacons(docName)
   If documentStatus="Not Found" Then
	   documentStatus="Found"
	   Exit Function 
   End If
	Set xl=CreateObject("Excel.application")
	Set wb=xl.Workbooks.Open("C:\Users\dmsan\Desktop\Export.xls")
	Set ws=wb.Worksheets("Global")
	
	Set Property_map=CreateObject("Scripting.Dictionary")
	For i=1 to ws.UsedRange.Rows.Count
		Property_map.Add Trim(ws.cells(i,1)),Trim(ws.cells(i,2))
	Next
	
	Set Property_mapped=CreateObject("Scripting.Dictionary")
	Set marketinghhoTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Names/TitleDocument.*")
	For i=1 to marketinghhoTable.RowCount
		If marketinghhoTable.ColumnCount(i)>1 Then
			If Property_map.Exists(Trim(marketinghhoTable.GetCellData(i,1))) Then
				Property_mapped.Add Property_map.Item(Trim(marketinghhoTable.GetCellData(i,1))),Trim(marketinghhoTable.GetCellData(i,2))
			End If
		End If
	Next
	
		Set xmlDoc1 = CreateObject("Msxml2.DOMDocument")
		xmlDoc1.load("C:\Users\dmsan\Desktop\"&docName&".xml")
		Set ElemList1= xmlDoc1.DocumentElement.ChildNodes
'		msgbox ElemList1.length		
		        
		For i = 0 to ElemList1.length-1 
			Tag_name=ElemList1.item(i).TagName
					If Property_mapped.Exists(Tag_name) Then
						Tag_name=ElemList1.item(i).TagName
			             Tag_Text=ElemList1.item(i).Text
						str1=Trim(Tag_Text)
						str2=Property_mapped.Item(Tag_name)
						
						If  Not ItemValidate(Tag_name) Then
							NotMatchedTags=NotMatchedTags&Tag_name&"," 
						Else 
							Matched=Matched&Tag_name&"," 
						End If
					End If 							
		Next
		If NotMatchedTags="" or NotMatchedTags="content_version_date," Then
			NotMatchedTags="No Mismatch"
		End If
		Call ExistCheckResults(sceanrioType,content_class,docName,subscription,documentStatus,NotMatchedTags)
		Browser("Title:=.*View Document Properties.*","creationTime:=1").Close	
		NotMatchedTags=""
	Matched=""
	wb.Save
	wb.Close
	xl.Quit
	Set xmlDoc1=nothing
	Set xl=nothing
End Function

'**********************************Scenarios Existing Check *************************************************
Function MarketingstandardExistingCheck()
Call SearchDocument(docName)
Call BrowseNSave(docName,content_class)
Call ValidateDocumentProperties(docName)
End Function

Function supportExistingCheck()
Call SearchDocument(docName)
Call BrowseNSave(docName,content_class)
Call ValidateDocProp_lib_man(docName)
End Function

Function LibraryExistingCheck()
Call SearchDocument(docName)
Call BrowseNSave(docName,content_class)
Call ValidateDocProp_lib_man(docName)
End Function

Function ManualExistingCheck()
Call SearchDocument(docName)
Call BrowseNSave(docName,content_class)
Call ValidateDocProp_lib_man(docName)
End Function

Function GeneralPurposeExistingCheck()
Call SearchDocument(docName)
Call BrowseNSave(docName,content_class)
Call ValidateDocProp_general(docName)
End Function

Function ContentfeedbackExistingCheck()
Call SearchDocument(docName)
Call BrowseNSave(docName,content_class)
Call ValidateDocProp_contentfeedback(docName)
End Function

Function marketinghhoExistingCheck()
Call SearchDocument(docName)
Call BrowseNSave(docName,content_class)
Call ValidateDocProp_marketinghho_nacons(docName)
End Function

Function marketingNAConsExistingCheck()
Call SearchDocument(docName)
Call BrowseNSave(docName,content_class)
Call ValidateDocProp_marketinghho_nacons(docName)
End Function

Function productsetupExistingCheck()
Call SearchDocument(docName)
Call BrowseNSave(docName,content_class)
Call ValidateDocProp_marketinghho_nacons(docName)
End Function

Call DriverExistingCheck()

Function DriverExistingCheck()
	Set ExistingSearchTestData=CreateObject("Excel.Application")
	Set DataSheetWb=ExistingSearchTestData.workbooks.Open("C:\Users\dmsan\Desktop\CDS_Plus_Auto\ExistingDocumentData.xlsx")
	Set DataSheet=DataSheetWb.Worksheets(1)
    SceanrioType="ExistingDocumentCheck"
	
	For k=2 to 5'DataSheet.usedRange.Rows.Count
		content_class=Trim(DataSheet.cells(k,3))
		subscription=Trim(DataSheet.cells(k,4))
		'Documents=Trim(DataSheet.cells(k,2))
		If  Lcase(Trim(DataSheet.cells(k,5)))="yes" Then
			Set objHttp = CreateObject("Msxml2.ServerXMLHTTP") 
			objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/"&content_class&"/"&subscription, False       
		   objHttp.Send
		   Var = objHttp.ResponseText
		   set objHttp=Nothing 
		   startPos=1
		  For z=1 to Trim(DataSheet.cells(k,6))		  
			startPos=instr(startPos,Var,content_class&"/"&subscription&"/")
			 EndPos=instr(startPos+1,Var,"""")
			 str1=Mid(Var,startPos+len(content_class&"/"&subscription&"/"),EndPos-(startPos+len(content_class&"/"&subscription&"/")))
			str=str&str1&","
			startPos=EndPos
		Next 
	   DataSheet.cells(k,7)= left(str,len(str)-1)
	   DataSheetWb.Save
	   str=""
	   Documents=Trim(DataSheet.cells(k,7))
		Else
		Documents=Trim(DataSheet.cells(k,2))
		End If
		Document=Split(Documents,",")
		For Each Elem in Document 
			docName=Elem 
	'		Msgbox docName&"--"&content_class&"--"&subscription
	'		marketingNAConsExistingCheck()
		
			Execute Trim(DataSheet.cells(k,1))	
		Next
	Next
	DataSheetWb.Save
	DataSheetWb.close 
	ExistingSearchTestData.Quit
	Set ExistingSearchTestData=Nothing
	
End Function

Function ExistCheckResults(scenarioType,Contentclass,DocumentName,Subscription,docStatusinConcentra,PropertyNotMatched)
	Set objExcel=CreateObject("Excel.Application")
   Set ResWorkbook=objExcel.Workbooks.open("C:\Users\dmsan\Desktop\CDS_Plus_Auto\CreateNCheckProperies.xlsx")
   Set ResWorksheet=ResWorkbook.Worksheets("ExistingDocumentResult") 
  
   ResWorksheet.cells(RowValue,1)=Contentclass
   ResWorksheet.cells(RowValue,2)=sceanrioType
   ResWorksheet.cells(RowValue,3)=DocumentName
   ResWorksheet.cells(RowValue,4)=Subscription
   ResWorksheet.cells(RowValue,5)=docStatusinConcentra
   ResWorksheet.cells(RowValue,6)=PropertyNotMatched   
   ResWorksheet.cells(RowValue,7)=Now
   If Trim(ResWorksheet.cells(RowValue,6).value)="No Mismatch" Then
		ResWorksheet.cells(RowValue,6).font.bold=true
		ResWorksheet.cells(RowValue,6).interior.colorindex=4
	Else 
		ResWorksheet.cells(RowValue,6).font.bold=true
		ResWorksheet.cells(RowValue,6).interior.colorindex=3	    
   End If
  
   RowValue=RowValue+1
   ResWorkbook.save
   ResWorkbook.close
   objExcel.quit
 Set objExcel=Nothing
End Function

'**************************************************Attachments Related********************************************
'Set IE=CreateObject("InternetExplorer.Application")
'IE.Visible=True
'Wait(10)
'IE.Navigate "http://cdsplus-itg.houston.hp.com/cadence/app/marketingstandard/content/c50331154/*"
'Wait(10)
'IE.Navigate  "http://cdsplus-itg.houston.hp.com/cadence/app/marketingstandard/content/c50331154/c50331154.rtf"
'Browser("Title:=http://cdsplus-itg.houston.hp.com.*").Dialog("Text:=File Download").WinButton("Text:=Cancel").Click
'IE.Quit

Function ContentAttachedCheck(contentclass,subscription,docName)
   AttachCount=0
   opened=0
	Set ContentTable=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Content and structure details.*")
	If  (ContentTable.RowCount<2)  Then
		Msgbox "No Attachments Found"
		Exit Function 
	End If
	Set objHttp = CreateObject("Msxml2.ServerXMLHTTP")
	objHttp.Open "GET", "http://cdsplus-itg.austin.hp.com/cadence/app/"&contentclass&"/"&subscription&"/"&docName&"/*", False
	objHttp.Send
	Var = objHttp.ResponseText

'	Set IE=CreateObject("InternetExplorer.Application")
'	IE.Visible=True
'	Wait(10)

	For i=2 to ContentTable.RowCount		
		Extracted_docName=Trim(ContentTable.GetcellData(i,1))
		Msgbox Extracted_docName
		If i=2 Then
			ExtractedFile=Extracted_docName			
	    Else		
		b=Split(Trim(Extracted_docName))
			ExtractedFile=b(0)		
		End If 		
			fileExtension=ContentTable.ChildItem(i,2,"Image",0).GetRoProperty("file name")
			ContentUrlExtracted=ExtractedFile&"."&getFileExtension(fileExtension)
		If Instr(Var,ContentUrlExtracted)>0 Then
			AttachCount=AttachCount+1
		End If 	
	Next
	Msgbox AttachCount
'	Msgbox opened 
'	IE.Quit
	If AttachCount=ContentTable.RowCount-1 Then
		Msgbox "Same Number Of Attachments"
	Else 
		Msgbox "Attachments Not Same"
	End If
'	Set IE=Nothing 
End Function
'Call SearchDocument("c50332415")
'Call ContentAttachedCheck("marketingstandard","content","c50332415")
'**********************************************Copy Existing Check************************************************
Function CopyDocument()
    docExtractedbyCopy=""
	If Not Validate(Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").Image("alt:=Copy Document"),"","Copy Document Option") Then
	 documentStatus="Not Found"
	 Call ExistCheckResults(sceanrioType,content_class,docName,subscription,documentStatus,NotMatchedTags)
	 documentStatus="Found"
		Exit Function 
	End If
	Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").Sync
	Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").Image("alt:=Copy Document").Click
'	Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").Sync
	Wait(15)
	If Not Validate(Browser("Title:=Copy Document.*","CreationTime:=1").Page("Title:=Copy Document").Image("alt:=Edit Document Properties"),"","Copy Document Page") Then
	CopyDocument="Copy Document option not found"
	Call ExistCheckResults(sceanrioType,content_class,docName,subscription,CopyDocument,NotMatchedTags)
		Exit Function
	End If 
	Browser("Title:=Copy Document.*","CreationTime:=1").Page("Title:=Copy Document").WebList("name:=selectWfId").Select "3 step wf"
	Browser("Title:=Copy Document.*","CreationTime:=1").Page("Title:=Copy Document").Image("alt:=Edit Document Properties").Click
    Wait(15)
	If Validate(Browser("Title:=.*Edit Document.*","CreationTime:=1").Page("Title:=.*Edit Document").WebElement("class:=error_2","innertext:=WARNING!.*"),"","Edit Document Propetirs Page") Then
		Call ExistCheckResults(sceanrioType,content_class,docName,subscription,"Found Error in Document taken for copy" ,NotMatchedTags)
			Exit Function 	
	End If 
	Wait(5)
	Browser("Title:=.*Edit Document.*","CreationTime:=1").Page("Title:=.*Edit Document").Image("alt:=Save and go to workflow page").Click
	Wait(15)
	Browser("Title:=Document Workflow.*","CreationTime:=1").Page("Title:=Document Workflow.*").WebRadioGroup("html id:=CHKRADIO1").Select "completeWorkflow"
	Browser("Title:=Document Workflow.*","CreationTime:=1").Page("Title:=Document Workflow.*").Image("alt:=Submit").Click
	If Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Document Name.*").Exist(5) Then
		docExtractedbyCopy=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Document Name.*").GetCellData(1,2)
		Browser("Title:=.*View Document Properties.*","creationTime:=1").Close
		Exit Function 
	End If
	If Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Name/TitleDocument.*").Exist(5) Then
		docExtractedbyCopy=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Name/TitleDocument.*").GetCelldata(2,2)
		Browser("Title:=.*View Document Properties.*","creationTime:=1").Close
		Exit Function 
	End If
	If Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=General properties.*").Exist(1) Then
		docExtractedbyCopy=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=General properties.*").GetCellData(2,2)
		Browser("Title:=.*View Document Properties.*","creationTime:=1").Close
		Exit Function 
	End If
	If Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Concentra Object Info.*").Exist(5) Then
		docExtractedbyCopy=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Concentra Object Info.*").GetCellData(2,2)
		Browser("Title:=.*View Document Properties.*","creationTime:=1").Close
		Exit Function 
	End If
	Browser("Title:=.*View Document Properties.*","creationTime:=1").Close	
'	Msgbox docExtractedbyCopy
End Function 

'******************************************CDS+ Create and Check Stuff******************************************
Function LibraryCDS()
	Call CreateNewDocument()
	Call ContentClass("Library")
	Call CreateMethod("Import File")
	Call UploadFile("C:\Users\dmsan\Documents\My Received Files\test1 - Copy (4).jpg")
	Call AssignProperties_Library( )
	Call SaveDocument("WorkFlow")
	Call ToCompeteWorkFlow()
End Function

'Call LibraryCDS()
'Call SupportCDS()
'Call MarketingstandardCDS()
'Call marketinghhoCDS()
'Call GeneralpurposeCDS()
'Call CeateMarketingNAConsumerDocument()	
'Call Productsetup()

Function SupportCDS()
	Call CreateNewDocument()
	Call ContentClass("Support")
	Call CreateMethod("Import File")
	Call UploadFile("C:\Users\dmsan\Documents\My Received Files\Support-content.xml")
	Call AssignProperties_Support( )
	Call SaveDocument("WorkFlow")
	Call ToCompeteWorkFlow()
End Function

Function MarketingstandardCDS()
	Call CreateNewDocument()
	Call ContentClass("Marketing-Standard")
	Call CreateMethod("Import File")
	Call UploadFile("C:\Users\dmsan\Documents\My Received Files\test.pdf")
	Call  AssignProperties_MarketingStandard()
	Call SaveDocument("WorkFlow")
	Call ToCompeteWorkFlow()
End Function

Function marketinghhoCDS()
	Call CreateNewDocument()
	Call ContentClass("Marketing-HHO")
	Call CreateMethod("Select Template")
	Call SelectTemplate("#0")
	Call AssignProperties_HHOMarketing()
	Call SaveDocument("View Document" )
    Call Checkout("#0","#1")
	Call Checkin("","","#0")
	Call CompleteWorkFlow()
End Function

Function GeneralpurposeCDS()
		Call CreateNewDocument()
		Call ContentClass("General Purpose")
		Call CreateMethod("Import File")
		Call UploadFile("C:\Users\dmsan\Documents\My Received Files\test.pdf")
		Call AssignProperties_GeneralPurpose()
		Call SaveDocument("WorkFlow")
		Call ToCompeteWorkFlow()
End Function

Function Productsetup()
	Call CreateNewDocument()
		Call ContentClass("Product setup")
		Call CreateMethod("Next")
		Call AssignProperties_ProductSetup()
		Call SaveDocument("View Document" )
		Call CompleteWorkFlow() 
End Function

Function CeateMarketingNAConsumerDocument()	
	Call CreateNewDocument()
	Call ContentClass("Marketing-NA Consumer")
	Call CreateMethod("Import File")
	Call UploadFile("C:\Users\dmsan\Documents\My Received Files\test.pdf")
	Call AssignProperties_MarketingNAConsumer()
	Call SaveDocument("View Document")	
	Call CompleteWorkFlow()     	
End Function

'********************************************Import From Concentra Functions ***********************************
FilePath="C:\Users\dmsan\Desktop\Concentra.xls"

Function Validate(obj,StrToCheck,CheckingFor)
	 Validate="True"

	if not obj.Exist(1) then
		Validate="False"
		Reporter.reportEvent micFail,CheckingFor&"--Check","Failed"
		Exit Function
	End If 

	If Len(StrToCheck)=0 Then
		Reporter.ReportEvent micPass,CheckingFor&"--Check","Succesful"
		Exit Function
	End if

	if instr(obj.GetROPROperty("innertext"),StrToCheck)=0 Then 
		validate="False"
		Reporter.ReportEvent micFail,CheckingFor&"--Does not Contain "&StrToCheck,"Failed"
	Else
		 Reporter.ReportEvent micPass,CheckingFor&"--Check","Succesful"
	End If 
           
End Function

'Msgbox  Validate(Browser("Concentra").Page("HomePage").WebElement("HomePage_WebElement"),"","Concentra Home Page")

'******************************Function to validate Document Status and Name***********************************
Function DocumentValidate(CheckingFor,Status)
   DocumentValidate="True"
   If Not Validate(Browser("Concentra").Page("ViewDocumentProperty").WebTable("innertext:=Document State.*"),"","View Document Properties Page") Then
'	   Call Save_Data("Document State Check","Fail")
	   Exit  Function 
	End If
   Select Case CheckingFor
    Case "DocState"
		
			If instr(Browser("Concentra").Page("ViewDocumentProperty").WebTable("innertext:=Document State.*").Getcelldata(1,2),Status)=0 Then
					DocumentValidate="False"
					Reporter.ReportEvent micFail,"Document State is Not "&Status,"Failed"
					Exit Function
			End If
		      Reporter.ReportEvent micPass,"Document State is  "&Status,"Passed"
		Case "DocName"
				DocumentName=Browser("Concentra").Page("DocumentProperty").WebTable("Text:=Name/TitleDocument Name.*").GetcellData(2,2)
				If IsEmpty( DocumentName) Then
						DocumentValidate="False"
						Reporter.ReportEvent micFail,CheckingFor&"--Check Document Name Not Generated ","Failed"
						Exit Function
				End If

            If Not Left(DocumentName,1)="c" and IsNumeric(Mid(DocumentName,2)) Then 
					DocumentValidate="False"
					Reporter.ReportEvent micFail,CheckingFor&"--Check Document Name  Generated is Not Valid","Failed"
					Exit Function
			End If

			Reporter.ReportEvent micPass,CheckingFor&"--Check Document Name  Generated is Valid","Succesful"

   End Select
End Function

Function SelectTemplate(Template)
	If Not Validate(Browser("Concentra").Page("CreateNewDocument").WebElement("SelectTemplate_Label"),"Select Template","Select Template Page") Then
'		Call Save_Data("Create Document","Failed")
		ExitAction
	End If 
	DocName=Browser("Concentra").Page("CreateNewDocument").WebTable("column names:=Document summary").GetCellData(2,2)
'	Call Save_Data("Document Created ["&DocName&"]","Pass")
	Browser("Concentra").Page("CreateNewDocument").WebRadioGroup("SelectTemplate").Select Template
	Browser("Concentra").Page("CreateNewDocument").Image("Next_image").Click
	Wait(10)
    Browser("Concentra").Page("CreateNewDocument").Sync
	
End Function
'Function Save_Data(TCName,status)
'
'	Dim  File_Path, rowValue, currentDate,currentTime
'	File_Path="C:\Users\dmsan\Desktop\Results.xlsx"
'	Set objExcel=CreateObject("Excel.Application")
'	set objFso=CreateObject("Scripting.FileSystemObject")
'	'objExcel.Visible=True
'	rowValue=Environment.Value("Rowcount")
'	'msgbox rowValue
'    If Not objFso.FileExists(FilePath) Then
'		objExcel.Workbooks.Add
'		objExcel.Cells(1,1).value="Testcase"
'		objExcel.Cells(1,2).value="Status"
'		Environment.Value("Rowcount")= rowValue + 1
'		'msgbox "row value in if" + CStr(rowValue)
'		objExcel.ActiveWorkbook.SaveAs (FilePath)
'    Else
'		set myFile= objExcel.Workbooks.Open (File_Path)
'		Set mySheet=myFile.Worksheets("Sheet1")
'		mySheet.cells(rowValue,1).value=TCName
'		mySheet.cells(rowValue,2).value=status
'		If mid(status,1,4) = "Pass" Then
'			mySheet.cells(rowValue,2).interior.colorIndex=4
'		elseif status = "Fail" then
'			mySheet.cells(rowValue,2).interior.colorIndex=3
'		else
'			mySheet.cells(rowValue,1).Font.Bold=TRUE
'			mySheet.cells(rowValue,1).Font.Size= 14
'			mySheet.cells(rowValue,1).interior.colorIndex=8
'			mySheet.cells(rowValue,2).Font.Bold=TRUE
'			mySheet.cells(rowValue,2).Font.Size= 14
'			mySheet.cells(rowValue,2).interior.colorIndex=8
'			mySheet.cells(rowValue,3).Font.Bold=TRUE
'			mySheet.cells(rowValue,3).Font.Size= 14
'			mySheet.cells(rowValue,3).interior.colorIndex=8
'			mySheet.cells(rowValue,4).Font.Bold=TRUE
'			mySheet.cells(rowValue,4).Font.Size= 14
'			mySheet.cells(rowValue,4).interior.colorIndex=8
'		End If
'		currentDate=Date
'		currentTime=Time
'		mySheet.cells(rowValue,3).value=currentDate
'		mySheet.cells(rowValue,4).value=currentTime
'		Environment.Value("Rowcount")= rowValue + 1
'		'msgbox "row value in else" + CStr(rowValue)
'		objExcel.ActiveWorkbook.Save
'	End If
'		objExcel.Quit
'		Set objExcel=Nothing 
'End Function

'***********************Navigate From Home Page To Create Document Page******************************* 
Function CreateNewDocument()
	If not Validate(Browser("Concentra").Page("HomePage").Link("Document"),"","Document Tab") Then 
		Exit Function 
	End if 
	Browser("Concentra").Page("HomePage").Link("Document").FireEvent "onmouseover"
	Browser("Concentra").Page("HomePage").Link("CreateNew").Click
End Function

'***********************Select Content Class,CMG and WorkFlow****************************************************************************
Function ContentClass(DocumentType)
	If not validate(Browser("Concentra").Page("CreateNewDocument").WebElement("innerhtml:=Create New Document","index:=1"),"Create New","Create New Document Page")Then
			Exit Function
	End if 
	Browser("Concentra").Page("CreateNewDocument").WebList("objectType").Select DocumentType
	Browser("Concentra").Page("CreateNewDocument").WebList("cmgId").Select "developers_playground (cmg101)"
	Browser("Concentra").Page("CreateNewDocument").WebList("selectWfId").Select "3 step wf"
End Function

'Call ContentClass("Support")
'*******************************Select Method **********************************************************************************************
Function CreateMethod(Opertion)
	Select Case Opertion
	  case "Import File"
		 If not validate(Browser("Concentra").Page("CreateNewDocument").Image("ImportFile"),"","Import File Button") Then
			 ExitAction
		 End if 
		 Browser("Concentra").Page("CreateNewDocument").Image("ImportFile").Click
		 Browser("Concentra").Page("CreateNewDocument").Sync
		  Wait(10)
	 Case "Select Template"	
		 If not validate(Browser("Concentra").Page("CreateNewDocument").Image("SelectTemplate"),"","Select Template Button") Then
			 ExitAction
		 End If
		 Browser("Concentra").Page("CreateNewDocument").Image("SelectTemplate").Click

	  Case "Next"
		 If not validate( Browser("Concentra").Page("CreateNewDocument").Image("Next"),"","Next Button") Then
			 ExitAction
		 End If
		 Browser("Concentra").Page("CreateNewDocument").Image("Next").Click
		 Browser("Concentra").Page("CreateNewDocument").Sync
		 Wait(10)
	Case Else
		Exit Function 

		
	End Select
End Function

'**************************Upload Files******************************************************************************
Function UploadFile(FilePath)
	 If Not validate(Browser("Concentra").Page("CreateNewDocument").WebElement("Document_Name"),"","Document Name") Then
'		 Call Save_Data("Create Document","Fail")
		 Exit Function 
	 End if 
	    DocName=Browser("Concentra").Page("CreateNewDocument").WebElement("Document_Name").GetROProperty("innertext")
		Msgbox  DocName
'		Call Save_Data("Document Created ["&DocName&"]","Pass")
		Browser("Concentra").Page("CreateNewDocument").WebRadioGroup("UploadLocalFile").Select "#0"
		Browser("Concentra").Page("CreateNewDocument").WebFile("File").Click
		Browser("Concentra").Dialog("Upload_Window").WinEdit("Filename_WinEdit").Set FilePath
		Browser("Concentra").Dialog("Upload_Window").WinButton("Open_WinButton").Click
		Browser("Concentra").Page("CreateNewDocument").Image("BeginUpload").Click
		Wait(60)
'		Do until State = "Complete"
'				State = Browser("Title:=Initial Content Upload.*").Page("Title:=Initial Content Upload").Object.readystate
'		Loop
		Browser("Concentra").Page("Title:=Initial Content Upload").Sync
'		Browser("Concentra").Page("CreateNewDocument").Sync
End Function

'Call UploadFile("C:\Users\dmsan\Documents\My Received Files\test1 - Copy (4).jpg")


Public Function AssignProperties_Library( ) 
   If not validate(Browser("Concentra").Page("DocumentProperty").WebElement("innerhtml:=Assign Properties.*"),"Assign","Assign Properies Page")Then
			Exit Function
   End If
   DataTable.ImportSheet ConcentraDataPath,"Library","Global"
	Browser("Concentra").Page("DocumentProperty").WebEdit("Title"). Set DataTable("Title")
	Browser("Concentra").Page("DocumentProperty").WebList("DisclosureLevel").Select DataTable("DisclosureLevel")
'	Browser("Concentra").Page("DocumentProperty").WebEdit("name:=contentVersion").Set Trim(DataTable("Content Version"))
	Browser("Concentra").Page("DocumentProperty").WebList("Regions").Select DataTable("Regions")
	Browser("Concentra").Page("DocumentProperty").WebList("ParentDocumentType").Select DataTable("ParentDocumentType")
End Function
'Call AssignProperties_Library()

'*************************************To Select View Document Or WorkFlow*****************************************
Function SaveDocument(SaveType)
		Select Case SaveType
		   Case "WorkFlow"
			   If Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties").Image("alt:=Save and go to workflow page").Exist(1) Then
							Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties").Image("alt:=Save and go to workflow page").Click
'							Call Save_Data("Assign Properties to Document","Pass")
				Elseif  Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties","index:=1").Image("alt:=Save and go to workflow page").Exist(1) Then
							Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties","index:=1").Image("alt:=Save and go to workflow page").Click
'							Call Save_Data("Assign Properties to Document","Pass")
				Else
							Call Validate(Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties").Image("alt:=Save and go to workflow page"),"","Save and Go To WorkFlow Button")
'							Call Save_Data("Assign Properties to Document","Fail")
							Exit Function 
			   End If
		
			Case "View Document"
				If  Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties").Image("alt:=Save and view parent document properties").Exist(1) Then
                   Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties").Image("alt:=Save and view parent document properties").Click
'				   Call Save_Data("Assign Properties to Document","Pass")
				   Browser("Concentra").Page("DocumentProperty").Sync
				Elseif  Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties","index:=0").Image("alt:=Save and view parent document properties").Exist(1) Then
					Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties","index:=0").Image("alt:=Save and view parent document properties").Click
'					Call Save_Data("Assign Properties to Document","Pass")
					Browser("Concentra").Page("DocumentProperty").Sync
				Else
				   Call Validate(Browser("Concentra").Page("DocumentProperty").WebTable("name:=Save and view parent document properties").Image("alt:=Save and view parent document properties"),"","Save And Go to View Document")
'					Call Save_Data("Assign Properties to Document","Fail")
				   ExitAction
				End If
				
		End Select
End Function
'SaveDocument("View Document")

'**********************To Complete WorkFlow After Selcting Save And Go To WorkFlow***********************************************
Function ToCompeteWorkFlow()
	 If not Validate(Browser("Concentra").Page("DocumentWorkflow").WebElement("WorkFlow_Label"),"","Complete WorkFlow Page") Then
		Exit Function 
	 End If 
	If Not Browser("Concentra").Page("DocumentWorkflow").WebRadioGroup("CompleteWorkflow").CheckProperty("disabled",0,1000) Then
		Reporter.ReportEvent micFail,"Mandatory Properties Are Not Assigned To the Document","Failed"
		Exit Function 
	End If
	 Browser("Concentra").Page("DocumentWorkflow").WebRadioGroup("CompleteWorkflow").Select "#0"
	 Browser("Concentra").Page("DocumentWorkflow").Image("Submit").Click
	 Browser("Concentra").Page("DocumentWorkflow").Sync
	 If  DocumentValidate("DocState","FINAL") Then
'		 Call Save_Data("Complete Work Flow","Pass")
	Else
'			Call Save_Data("Complete Work Flow","Fail")
	End If 
End Function

Public Function AssignProperties_Support( )

   If not validate(Browser("Concentra").Page("DocumentProperty").WebElement("innerhtml:=Assign Properties.*"),"Assign","Assign Properties Page")Then
	   Exit Function
   End If
   DataTable.ImportSheet ConcentraDataPath,"Support","Global"
     Browser("Concentra").Page("DocumentProperty").WebEdit("Title").Set DataTable("Title")
	Browser("Concentra").Page("DocumentProperty").WebList("Priority").Select DataTable("Priority")
	Browser("Concentra").Page("DocumentProperty").WebList("DisclosureLevel").Select DataTable("DisclosureLevel")
'	Browser("Concentra").Page("DocumentProperty").WebEdit("name:=contentVersion").Set DataTable("contentVersion")
	Browser("Concentra").Page("DocumentProperty").WebList("LanguageCode").Select DataTable("LanguageCode")
	Browser("Concentra").Page("DocumentProperty").WebList("ParentDocumentType").Select DataTable("ParentDocumentType")
	Browser("Concentra").Page("DocumentProperty").Frame("Products_Frame").WebElement("Accelarator_Product").Click
	Browser("Concentra").Page("DocumentProperty").Image("Products_Add").Click
	Browser("Concentra").Page("DocumentProperty").WebList("ContentTopic").Select DataTable("ContentTopic")
End Function

Function AssignProperties_GeneralPurpose()
	 If not validate(Browser("Concentra").Page("DocumentProperty").WebElement("innerhtml:=Assign Properties.*"),"Assign","Assign Properties Page")Then
		   Exit Function
	   End If
	DataTable.ImportSheet ConcentraDataPath,"General Purpose","Global"
	Browser("Concentra").Page("DocumentProperty").WebList("ParentDocumentType").Select DataTable("ParentDocumentType")
	Browser("Concentra").Page("DocumentProperty").WebEdit("Title").Set DataTable("Title")
	Browser("Concentra").Page("DocumentProperty").WebList("DisclosureLevel").Select DataTable("DisclosureLevel")
	Browser("Concentra").Page("DocumentProperty").WebList("LanguageCode").Select DataTable("LanguageCode")
	Browser("Concentra").Page("DocumentProperty").WebList("Regions").Select DataTable("Regions")
	Browser("Concentra").Page("DocumentProperty").Image("Regions_Add").Click
	Browser("Concentra").Page("DocumentProperty").Frame("Products_Frame").WebElement("Accelarator_Product").Click
	Browser("Concentra").Page("DocumentProperty").Image("Product_GenealPurpose_Add").Click
	Browser("Concentra").Page("DocumentProperty").WebList("Priority").Select DataTable("Priority")
End Function

Function AssignProperties_Manual()
	 If not validate(Browser("Concentra").Page("DocumentProperty").WebElement("innerhtml:=Assign Properties.*"),"Assign","Assign Properties Page Check")Then
		Exit Function
	 End If
	DataTable.ImportSheet ConcentraDataPath,"Manual","Global"
'	Browser("Concentra").Page("ViewDocumentProperty").Weblist("name:=allLanguages").Select DataTable("Languages")
'	Browser("Concentra").Page("DocumentProperty").Image("Add_Languages").Click
	Browser("Concentra").Page("DocumentProperty").WebEdit("Titles").Set DataTable("Titles")
	Browser("Concentra").Page("DocumentProperty").WebList("Priority").Select DataTable("Priority")
	Browser("Concentra").Page("DocumentProperty").WebList("DisclosureLevel").Select DataTable("DisclosureLevel")
	Browser("Concentra").Page("DocumentProperty").WebList("ParentDocumentType").Select DataTable("ParentDocumentType")
'	Browser("Concentra").Page("ViewDocumentProperty").WebEdit("name:=contentVersion").Set Trim(DataTable("ContentVersion"))
	Browser("Concentra").Page("DocumentProperty").Frame("Products_Frame").WebElement("Accelarator_Product").Click
	Browser("Concentra").Page("DocumentProperty").Image("Add_ManualProduct").Click
End Function

Function AssignProperties_HHOMarketing()
	If not validate(Browser("Concentra").Page("DocumentProperty").WebElement("innerhtml:=Assign Properties.*"),"Assign","Assign Properties Page")Then
			Exit Function
		 End If
	DataTable.ImportSheet ConcentraDataPath,"Marketing HHO","Global"
	Browser("Concentra").Page("DocumentProperty").WebEdit("Title").Set DataTable("Title")
	Browser("Concentra").Page("DocumentProperty").WebEdit("SearchKeyword").Set DataTable("SearchKeyword")
	Browser("Concentra").Page("DocumentProperty").Image("Add_keyword").Click
	Browser("Concentra").Page("DocumentProperty").WebList("ParentDocumentType").Select DataTable("ParentDocumentType")
	Browser("Concentra").Page("DocumentProperty").WebList("LanguageCode").Select DataTable("LanguageCode")
	Browser("Concentra").Page("DocumentProperty").WebList("Contacts").Select DataTable("Contacts")
	Browser("Concentra").Page("DocumentProperty").WebEdit("ContactName").Set DataTable("ContactName")
	Browser("Concentra").Page("DocumentProperty").Image("Contacts_Add").Click
	Browser("Concentra").Page("DocumentProperty").WebList("DisclosureLevel").Select DataTable("DisclosureLevel")
	Browser("Concentra").Page("DocumentProperty").WebEdit("Day").Set Trim(DataTable("Day"))
	Browser("Concentra").Page("DocumentProperty").WebList("Month").Select DataTable("Month")
	Browser("Concentra").Page("DocumentProperty").WebEdit("Years").Set Trim(DataTable("Years"))
	Browser("Concentra").Page("DocumentProperty").WebEdit("To_Day").Set Trim(DataTable("To_Day"))
	Browser("Concentra").Page("DocumentProperty").WebList("To_Month").Select DataTable("To_Month")
	Browser("Concentra").Page("DocumentProperty").WebEdit("To_Years").Set Trim(DataTable("To_Years"))
	Browser("Concentra").Page("DocumentProperty").WebList("ActivityType").Select DataTable("ActivityType")
	Browser("Concentra").Page("DocumentProperty").WebList("MarketWindow").Select DataTable("MarketWindow")

End Function

Function AssignProperties_ProductSetup()
   If not validate(Browser("Concentra").Page("DocumentProperty").WebElement("innerhtml:=Assign Properties.*"),"Assign Properties to Product setup Document","Assign Properties Page to ProductSetUp")Then
			Exit Function
	 End If
   DataTable.ImportSheet ConcentraDataPath,"Product SetUp","Global"
	Browser("Concentra").Page("DocumentProperty").WebEdit("Title").Set DataTable("Title")
	Browser("Concentra").Page("DocumentProperty").WebList("ParentDocumentType").Select DataTable("DocumentType")
	Browser("Concentra").Page("DocumentProperty").Frame("Products_Frame").Link("Accelarator_Expand").Click
	Browser("Concentra").Page("DocumentProperty").Sync
	Browser("Concentra").Page("DocumentProperty").Frame("Products_Frame").Link("Computational_Expand").Click
	Browser("Concentra").Page("DocumentProperty").Sync
	Browser("Concentra").Page("DocumentProperty").Frame("Products_Frame").Link("AMD_Expand").Click
	Browser("Concentra").Page("DocumentProperty").Sync
	Browser("Concentra").Page("DocumentProperty").Frame("Products_Frame").Link("AMD_HP_ProductExpand").Click
	Browser("Concentra").Page("DocumentProperty").Sync
	Browser("Concentra").Page("DocumentProperty").Frame("Products_Frame").WebElement("Product").Click
	Browser("Concentra").Page("DocumentProperty").Image("Product_ProductSetup_Add").Click
	Browser("Concentra").Page("DocumentProperty").WebList("Regions").Select DataTable("Regions")
	Browser("Concentra").Page("DocumentProperty").WebList("Regions").Select "#"&RandomNumber(0,241)
	Browser("Concentra").Page("DocumentProperty").Image("Regions_Add").Click
'	Browser("Concentra").Page("DocumentProperty").WebEdit("name:=contentVersion").Set Trim(DataTable("contentVersion"))
	Browser("Concentra").Page("DocumentProperty").WebList("DisclosureLevel").Select DataTable("DisclosureLevel")
	Browser("Concentra").Page("DocumentProperty").WebEdit("name:=showAsNewDuration").Set Trim(DataTable("showAsNewDuration"))
	Browser("Concentra").Page("DocumentProperty").Frame("Accounts_Frame").Link("AllAccounts_Expand").Click
	Browser("Concentra").Page("DocumentProperty").Sync
	Browser("Concentra").Page("DocumentProperty").Frame("Accounts_Frame").WebElement("Apple_Account").Click
	Browser("Concentra").Page("DocumentProperty").Image("Add_Accounts").Click
	Browser("Concentra").Page("DocumentProperty").Frame("Marketing_Teams").Link("MarketingTeams_Expand").Click
	Browser("Concentra").Page("DocumentProperty").Sync
	Browser("Concentra").Page("DocumentProperty").Frame("Marketing_Teams").WebElement("Marketing_Team").Click
	Browser("Concentra").Page("DocumentProperty").Image("MarketingTerms_Add").Click
End Function

Public Function AssignProperties_MarketingStandard( )
   If not validate(Browser("Concentra").Page("DocumentProperty").WebElement("innerhtml:=Assign Properties.*"),"Assign","Assign Properites Page")Then
			Exit Function
       End If

	DataTable.ImportSheet  ConcentraDataPath,"Marketing Standard","Global"
	Browser("Concentra").Page("DocumentProperty").WebEdit("Title").Set DataTable("Title")
	Browser("Concentra").Page("DocumentProperty").WebEdit("Description").Set DataTable("Description")
	Browser("Concentra").Page("DocumentProperty").WebEdit("SearchKeyword").Set DataTable("SearchKeyword")
	Browser("Concentra").Page("DocumentProperty").Image("Add_keyword").Click
	Browser("Concentra").Page("DocumentProperty").WebList("DocumentType").Select DataTable("DocumentType")
    Browser("Concentra").Page("DocumentProperty").WebList("LanguageCode").Select DataTable("LanguageCode")
	Browser("Concentra").Page("DocumentProperty").WebList("Regions").Select  DataTable("Regions")
	Browser("Concentra").Page("DocumentProperty").Image("Regions_Add").Click
	Browser("Concentra").Page("DocumentProperty").Image("PublishingDisclosure_Enlarge").Click
	Browser("Concentra").Page("DocumentProperty").WebList("DisclosureLevel").Select DataTable("DisclosureLevel")
	Browser("Concentra").Page("DocumentProperty").WebEdit("Day").Set trim(DataTable("Day"))
	Browser("Concentra").Page("DocumentProperty").WebList("Month").Select  Trim(DataTable("Month"))
	Browser("Concentra").Page("DocumentProperty").WebEdit("Years").Set  Trim(DataTable("Years"))
	Browser("Concentra").Page("DocumentProperty").WebEdit("To_Day").Set Trim(DataTable("To_Day"))
	Browser("Concentra").Page("DocumentProperty").WebList("To_Month").Select Trim(DataTable("To_Month"))
     Browser("Concentra").Page("DocumentProperty").WebEdit("To_Years").Set Trim(DataTable("To_Years"))
	
End Function
'Call AssignProperties_MarketingStandard()

Function Checkout(DownloadFile,OpenClientFile)
			 If not Validate(Browser("Concentra").Page("ViewDocumentProperty").Image("alt:=Create new WIP version"),"","CheckOut Button") Then 
				 ExitAction
			End If
			Browser("Concentra").Page("ViewDocumentProperty").Image("alt:=Create new WIP version").Click
			Browser("Concentra").Page("ViewDocumentProperty").Sync
            If Not Validate(Browser("Concentra").Page("Checkout/Export_Content").WebElement("Checkout/Export_Content"),"Checkout / Export Content","Checkout Page") Then
'				Call Save_Data("Check Out","Fail")
				ExitAction
			End If 
			Browser("Concentra").Page("Checkout/Export_Content").WebRadioGroup("DownloadFile").Select DownloadFile
			Browser("Concentra").Page("Checkout/Export_Content").WebRadioGroup("OpenClientFile").Select OpenClientFile
			If Browser("Concentra").Page("Checkout/Export_Content").WebList("WorkflowId").Exist(1) Then
				If Browser("Concentra").Page("Checkout/Export_Content").WebList("WorkflowId").CheckProperty("disabled","0",1000) Then 
						Browser("Concentra").Page("Checkout/Export_Content").WebList("WorkflowId").Select "3 step wf"
				End If 
			End If
			Browser("Concentra").Page("Checkout/Export_Content").Image("Next").Click
			Browser("Concentra").Page("Checkout/Export_Content").Sync
			Wait(20)
            If Window("AppletFile_Download").WinObject("SaveAll").Exist(1) Then
				Window("AppletFile_Download").WinObject("SaveAll").Click
			End If 
           	Wait(20)
'		 Call Save_Data("Check out","Pass")
End Function

Function Checkin(Doc,FilePath,NextAction)
	If Not Validate(Browser("Concentra").Page("ViewDocumentProperty").Image("alt:=Save modified content"),"","Checkin Button") Then
		Call Save_Data("Check in","Fail")
		ExitAction
	End If
	Browser("Concentra").Page("ViewDocumentProperty").Image("alt:=Save modified content").Click
'	Browser("Concentra").Page("ViewDocumentProperty").Sync
	Wait(10)
	If Not Validate(Browser("Concentra").Page("Checkin").WebElement("Checkin_Content"),"Checkin","Checkin Page") Then
		ExitAction
	End if
	Browser("Concentra").Page("Checkin").WebRadioGroup("name:=nextAction").Select NextAction
	Browser("Concentra").Page("CreateNewDocument").WebRadioGroup("UploadLocalFile").Select "#0"
	
	Select Case Doc
	Case "DiffDoc"
		Browser("Concentra").Page("CreateNewDocument").WebFile("File").Click
		Browser("Concentra").Dialog("Upload_Window").WinEdit("Filename_WinEdit").Set Filepath
	Case ""
		CellContent=Browser("Concentra").Page("Checkin").WebTable("name:=addContentAuthor").GetCellData(11,2)
		File_Path=Trim(Mid(CellContent,Instr(CellContent,"c:\"),36))
		Browser("Concentra").Page("CreateNewDocument").WebFile("File").Click
		Browser("Concentra").Dialog("Upload_Window").WinEdit("Filename_WinEdit").Set File_Path
	End Select
	Browser("Concentra").Dialog("Upload_Window").WinButton("Open_WinButton").Click 
	If Not Validate(Browser("Concentra").Page("Checkin").Image("BeginUpload_Selected"),"","Begin Upload Image") Then
		ExitAction
	End if
	Browser("Concentra").Page("Checkin").Image("BeginUpload_Selected").Click
	If Browser("Concentra").Dialog("Dialog_ReplaceFile").WinButton("OK").Exist(1) Then
		Browser("Concentra").Dialog("Dialog_ReplaceFile").WinButton("OK").Click
	End If 
	Browser("Concentra").Page("CreateNewDocument").Sync
'	Call Save_Data("Check in","Pass")
	Wait(10)
End Function

'Call Checkin("","","#2")


Function CompleteWorkFlow()
	If  Not right(Trim(Browser("Concentra").Page("ViewDocumentProperty").WebTable("innertext:=Document State.*").GetcellData(1,2)),3)="WIP" Then
		ExitAction
	End If
	Browser("Concentra").Page("ViewDocumentProperty").Image("Workflow").Click
	If Not Browser("Concentra").Page("DocumentWorkflow").WebRadioGroup("CompleteWorkflow").CheckProperty("disabled",0,1000) Then
		Reporter.ReportEvent micFail,"Mandatory Properties Are Not Assigned To the Document","Failed"
		Exit Function 
	End If
	Browser("Concentra").Page("DocumentWorkflow").WebRadioGroup("CompleteWorkflow").Select "#0"
	Browser("Concentra").Page("DocumentWorkflow").Image("Submit").Click
	 If Right(Trim(Browser("Concentra").Page("ViewDocumentProperty").WebTable("innertext:=Document State.*").GetcellData(1,2)),5)="FINAL" Then
		 Reporter.ReportEvent micPass,"Create Document","SuccesFul"
'		 Call Save_Data("Complete Work Flow","Pass")
	 Else
		Reporter.ReportEvent micPass,"Create Document","Not SuccesFul"
'		Call Save_Data("Complete WorkFlow","Fail")
	End If 
	
End Function

Function AssignProperties_MarketingNAConsumer()
	If not validate(Browser("Concentra").Page("DocumentProperty").WebElement("innerhtml:=Assign Properties.*"),"Assign","Assign Properties Page Check")Then
			Exit Function
	End If
	 DataTable.ImportSheet ConcentraDataPath,"Marketing NA Consumer","Global"
	Browser("Concentra").Page("DocumentProperty").WebEdit("Title").Set DataTable("Title")
	Browser("Concentra").Page("DocumentProperty").WebList("ParentDocumentType").Select DataTable("DocumentType")
	Browser("Concentra").Page("DocumentProperty").WebList("Regions").Select DataTable("Regions")
	Browser("Concentra").Page("DocumentProperty").Image("Regions_Add").Click
'	Browser("Concentra").Page("ViewDocumentProperty").WebEdit("name:=contentVersion").Set Trim(DataTable("ContentVersion"))
	Browser("Concentra").Page("DocumentProperty").WebList("DisclosureLevel").Select DataTable("DisclosureLevel")
	Browser("Concentra").Page("DocumentProperty").WebList("ChildDocumentTypeDetails").Select "#2"
	Browser("Concentra").Page("DocumentProperty").Image("Add_NAConsumer").Click
	Browser("Concentra").Page("DocumentProperty").Frame("Accounts_Frame").Link("AllAccounts_Expand").Click
	Browser("Concentra").Page("DocumentProperty").Frame("Accounts_Frame").WebElement("Apple_Account").Click
	Browser("Concentra").Page("DocumentProperty").Image("Accounts_Add").Click
	Browser("Concentra").Page("DocumentProperty").Frame("Marketing_Teams").Link("MarketingTeams_Expand").Click
	Browser("Concentra").Page("DocumentProperty").Frame("Marketing_Teams").WebElement("Marketing_Team").Click
	Browser("Concentra").Page("DocumentProperty").Image("MarketingTerms_Add").Click
End Function
'**********************************************************************************************************************************************************

Function A()
	Set CreateNew=CreateObject("Excel.application")
	Set CreateNewWb=CreateNew.Workbooks.Open("C:\Users\dmsan\Desktop\CDS_Plus_Auto\ExistingDocumentData.xlsx")
	Set CreateNewSheet=CreateNewWb.Worksheets("CreateNew")
	SceanrioType="CreateDocumentCheck"
	For CreateRow=2  to  4 'CreateNewSheet.UsedRange.Rows.Count	
		For Numbers=1 to Trim(CreateNewSheet.cells(CreateRow,5))
			Execute Trim(CreateNewSheet.cells(CreateRow,6))
		For p=1 to 1
			If Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Document Name.*").Exist(2) Then
				docsCreated=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Document Name.*").GetCellData(1,2) 
				Exit For
			End If
			If Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Name/TitleDocument.*").Exist(2) Then
				docsCreated=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Name/TitleDocument.*").GetCelldata(2,2) 
				Exit For
			End If
			If Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=General properties.*").Exist(2) Then
				docsCreated=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=General properties.*").GetCellData(2,2)		 
				Exit For
			End If

			If Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Concentra Object Info.*").Exist(2) Then
				docsCreated=Browser("Title:=.*View Document Properties.*","CreationTime:=1").Page("Title:=.*View Document Properties").WebTable("innertext:=Concentra Object Info.*").GetCellData(2,2)
				Exit for
			End If
		Next
		Msgbox docsCreated
		CreateNewSheet.cells(CreateRow,2)=CreateNewSheet.cells(CreateRow,2)&Trim(docsCreated)&","
		CreateNewWb.save
		Next
		docs=Trim(CreateNewSheet.cells(CreateRow,2))
		CreateNewSheet.cells(CreateRow,2)=Left(docs,len(docs)-1)	
		CreateNewWb.save	
	Next 

    For Iterator=2 to 4'CreateNewSheet.usedRange.Rows.count
		content_class=Trim(CreateNewSheet.cells(Iterator,3))
		subscription=Trim(CreateNewSheet.cells(Iterator,4))
        CreatedDocs=Trim(CreateNewSheet.cells(Iterator,2))
		Docs=Split(CreatedDocs,",")
		For Each Elem in Docs
			Execute(Trim(CreateNewSheet.cells(Iterator,1)))
		Next
	Next   
	CreateNew.Quit
	Set CreateNew=Nothing	
End Function

'Call A()

















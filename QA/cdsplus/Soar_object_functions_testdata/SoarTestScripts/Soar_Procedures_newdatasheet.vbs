' ******************************************************************************************************************************************************
'                   This is the library file which consists of all the sub procedures used in different scenarios                                                          *
'                                                                                                                                                                                                                                                         *
' ******************************************************************************************************************************************************

'*****************************************************LoginSOAR**************************************************************

Sub LoginSOAR

	'Browser("HP Employee Portal").Page("Certificate Error: Navigation").Link("Continue to this website").Click
'	Browser("HP Employee Portal").Page("Certificate Error: Navigation").Link("Continue to this website").Click
	'wait(5)
    Browser("HP Employee Portal").Page("HP Employee Portal").Sync
	Browser("HP Employee Portal").Page("HP Employee Portal").WebEdit("USER").Set(mySheet1.cells(DSRowNum,1).value)
	Browser("HP Employee Portal").Page("HP Employee Portal").WebEdit("PASSWORD").SetSecure mySheet1.cells(DSRowNum,2).value
	'Browser("HP Employee Portal").Page("HP Employee Portal").Link("Log on").Click
'	Browser("HP Employee Portal").Page("HP Employee Portal").Image("Log_on_SSO").Click
	Browser("HP Employee Portal").Page("HP Employee Portal").WebButton("Log-on").Click
	Browser("Welcome To SOAR").Page("Welcome To SOAR").Sync
	Browser("Welcome To SOAR").Page("Welcome To SOAR").WebRadioGroup("userName").Select mySheet1.cells(DSRowNum,3).value
	Browser("Welcome To SOAR").Page("Welcome To SOAR").Image("Connect to SOAR").Click
	
	'Retreive welcome message string
	HPageLabel = Browser("QA / TEST Home Page").Page("QA / TEST Home Page").WebElement("WebTable_1").GetTOProperty("innertext")
	HPageLabel=  mid(HPageLabel,1,15)
	'msgbox "Home page Label  " + HPageLabel
	If HPageLabel = "Welcome to SOAR" Then
			'msgbox "Opened SOAR Home Page successfully"
			 Environment.Value("Checkflag")="pass"
			 call Save_Data("Login","Pass")
	else
			 call Save_Data("Login","Fail")
	End If
End Sub


'*****************************************************Add_collection_dataonly**************************************************************

 Sub Add_Collection_DO
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Add New").Click
	Browser("Create Collection").Page("Create Collection").WebList("submittalGroupOid").Select(mySheet2.cells(DSRowNum,7).value)
	Browser("Create Collection").Page("Create Collection").WebList("submittalTypeOid").Select("Data Only")
	Browser("Create Collection").Page("Create Collection").Image("Save").Click
	Browser("Create Collection").Page("Create Collection").WebEdit("title").Set(mySheet2.cells(DSRowNum,1).value)
	Browser("Create Collection").Page("Create Collection").WebEdit("description").Set(mySheet2.cells(DSRowNum,2).value)
	Browser("Create Collection").Page("Create Collection").WebCheckBox("updateTypeOids").Click
	Browser("Create Collection").Page("Create Collection").WebList("submittalPriorityOid").Select(mySheet2.cells(DSRowNum,3).value)
    Browser("Create Collection").Page("Create Collection").WebList("softwareTypeOid").Select(mySheet2.cells(DSRowNum,4).value)
	Browser("Create Collection").Page("Create Collection").WebList("compressionUtilityOid").Select(mySheet2.cells(DSRowNum,5).value)
	Browser("Create Collection").Page("Create Collection").Image("Save_Collection").Click
End Sub

'*****************************************************Add_collection_Phy media**************************************************************

Sub Add_Collection_PM
   submitalType="PM"
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Add New").Click
	Browser("Create Collection").Page("Create Collection").WebList("submittalGroupOid").Select(mySheet2.cells(DSRowNum,7).value)
	Browser("Create Collection").Page("Create Collection").WebList("submittalTypeOid").Select("Physical Media")
	Browser("Create Collection").Page("Create Collection").Image("Save").Click
	Browser("Create Collection").Page("Create Collection").WebEdit("title").Set(mySheet2.cells(DSRowNum,1).value)
	Browser("Create Collection").Page("Create Collection").WebEdit("description").Set(mySheet2.cells(DSRowNum,2).value)
	Browser("Create Collection").Page("Create Collection").WebCheckBox("updateTypeOids").Click
	Browser("Create Collection").Page("Create Collection").WebList("submittalPriorityOid").Select(mySheet2.cells(DSRowNum,3).value)
	Browser("Create Collection").Page("Create Collection").WebList("softwareTypeOid").Select(mySheet2.cells(DSRowNum,4).value)
	Browser("Create Collection").Page("Create Collection").WebEdit("copyrightAcknowledgement").Set(mySheet2.cells(DSRowNum,6).value)
	Browser("Create Collection").Page("Create Collection").WebList("compressionUtilityOid").Select(mySheet2.cells(DSRowNum,5).value)
	Browser("Create Collection").Page("Create Collection").Image("Save_Collection").Click
End Sub

'*****************************************************Add_collection_electronic**************************************************************

Sub Add_Collection_EL
   submitalType="EL"
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Add New").Click
	Browser("Create Collection").Page("Create Collection").WebList("submittalGroupOid").Select(mySheet2.cells(DSRowNum,7).value)
	Browser("Create Collection").Page("Create Collection").WebList("submittalTypeOid").Select("Electronic")
	Browser("Create Collection").Page("Create Collection").Image("Save").Click
	Browser("Create Collection").Page("Create Collection").WebEdit("title").Set(mySheet2.cells(DSRowNum,1).value)
	Browser("Create Collection").Page("Create Collection").WebEdit("description").Set(mySheet2.cells(DSRowNum,2).value)
'	Browser("ColnandItemProps").Page("CIProps").WebCheckBox("updateTypeOids_10").Click
	Browser("Create Collection").Page("Create Collection").WebCheckBox("updateTypeOids").Click
	Browser("Create Collection").Page("Create Collection").WebList("submittalPriorityOid").Select(mySheet2.cells(DSRowNum,3).value)
	Browser("Create Collection").Page("Create Collection").WebList("softwareTypeOid").Select(mySheet2.cells(DSRowNum,4).value)
	Browser("Create Collection").Page("Create Collection").WebList("compressionUtilityOid").Select(mySheet2.cells(DSRowNum,5).value)
	Browser("Create Collection").Page("Create Collection").Image("Save_Collection").Click
End Sub

'********************************************Validating in view collection page*******************************************************************
Sub VerifyNewCollection
	CollectionID=Browser("View Collection_3").Page("View Collection").WebTable("innertext:=The state of.*").GetCellData(2,2)
	CIDLabel=Browser("View Collection_3").Page("View Collection").WebTable("innertext:=Collection.*","index:=1").GetCellData(1,1)
	CIDLabel=trim(Mid(CIDLabel,12))
	Cstate=Browser("View Collection_3").Page("View Collection").WebTable("innertext:=The state of.*").GetCellData(7,2)
	If CollectionID = CIDLabel and trim(Cstate) = "DRAFT" Then
		Call Save_Data("Create Collection", "Pass and collection ID is "+CollectionID)
	else
		Call Save_Data("Create Collection", "Fail")

 End If
End Sub

'******************************************validate and approve the collection*******************************************************************
Sub Validate_Approve_Collection
	Browser("View Software Item").Page("View Software Item").Image("View The Collection").Click
	colnValidity=Browser("View Collection_3").Page("View Collection").WebTable("innertext:=The state of.*").GetCellData(9,2)
    If trim(colnValidity) = "This collection is valid." Then
		Call Save_Data("validation of collection", "Pass")
		colnValidationState="TRUE"
	else
		Call Save_Data("validation of collection", "Fail")
	End If
	If colnValidationState ="TRUE" Then
		Browser("View Collection").Page("View Collection").Image("Send For Review then Approve").Click
		Cstate=Browser("View Collection_3").Page("View Collection").WebTable("innertext:=The state of.*").GetCellData(7,2)
		wait(4)
		If trim(Cstate) = "APPROVED" Then
			Call Save_Data("collection approval", "Pass")
			'Enter collection ID into CDSPdata file

		mySheet7.cells(CDSPDataRow,1)=CollectionID
		CDSPDataRow=CDSPDataRow+1
		else
			Call Save_Data("collection approval", "Fail")
		End If
	End if
End Sub

'***************************************************************Search collection*****************************************************************
Sub Search_Collection
	Browser("View Collection").Page("View Collection").Link("by Collection ID").Click
	Browser("Search by Collection").Page("Search by Collection").WebEdit("collectionId").Set(CollectionID)
	Browser("Search by Collection").Page("Search by Collection").Image("Search_2").Click
	SearchResult=Mid(Browser("Search by Collection").Page("Collection Search Results").WebTable("innertext:=Your search returned.*").GetCellData(1,1),23,1)
	If SearchResult = "1" Then
		Call Save_Data("Search Collection", "Pass")
	else
		Call Save_Data("Search Collection", "Fail")
	End If
End Sub

'***************************************************************Delete collection*******************************************************************
Sub Delete_Collection
	set colobj=Browser("Search by Collection").Page("Collection Search Results").WebTable("innertext:=COL.*","index:=1").ChildItem(1,1,"Link",0)
	colobj.Click
	Browser("View Collection_4").Page("View Collection").Image("Delete This Collection").Click
	Browser("View Collection_4").Page("Confirm Delete Collection").Image("Delete This Collection").Click
End Sub

'**************************************************************Search for deleted collection******************************************************

Sub Search_Deleted_Collection
	Browser("View Collection").Page("View Collection").Link("by Collection ID").Click
	Browser("Search by Collection").Page("Search by Collection").WebEdit("collectionId").Set(CollectionID)
	Browser("Search by Collection").Page("Search by Collection").Image("Search_2").Click
	SearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("Your search did not return").GetCellData(1,1),1,42)
	If SearchResult = "Your search did not return any collections" Then
		Call Save_Data("Delete Collection", "Pass")
	else
		Call Save_Data("Delete Collection", "Fail")
	End If
End Sub

'************************************************************Add new English doc at collection level*********************************************
Sub AddNewEngDoc
	Browser("View Collection").Page("View Collection").Image("Add New").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("title").Set(mySheet1.cells(DSRowNum,4).value)
	Browser("Edit Documents").Page("Edit Documents").WebList("availDocumentTypes").Select(mySheet1.cells(DSRowNum,6).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add").Click
	Browser("Edit Documents").Page("Edit Documents").WebList("availLanguageCharSets").Select(mySheet1.cells(DSRowNum,8).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add_2").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("objectName").Set(mySheet1.cells(DSRowNum,7).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Save Document Properties_2").Click
	'Add document content
	Browser("Edit Document Content").Page("Edit Document Content").WebEdit("contentAsString").Set(mySheet1.cells(DSRowNum,9).value)
	Browser("Edit Document Content").Page("Edit Document Content").Image("save the attachment details").Click
	'Check for newly added english under view collection page
	If Browser("View Collection").Page("View Collection_2").WebCheckBox("attachmentString").Exist Then
		Call Save_Data("Add new english doc from view collection page", "Pass")
	else
		Call Save_Data("Add new english doc from view collection page", "Fail")
	End If
End Sub

'**************************************************Edit  the english doc attributes at collection level**********************************************
Sub EditEngDoc
	'TitleOld=Browser("View Collection").Page("View Collection_2").WebTable("innertext:=You.*").GetCellData(2,2)
	Browser("View Collection").Page("View Collection_2").Image("view document attributes.").Click
	TitleOld=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(2,2)
	'TitleOld=Mid(TitleOld,34,11)
	'Msgbox TitleOld
	Filesize1=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(16,2)
	Filesize1=Mid(Filesize1,1,2)
   	'Edit the content of english doc
	Browser("View Document Details").Page("View Document Details").Image("Edit Document Content").Click
	Browser("Edit Document Content").Page("Edit Document Content").WebEdit("contentAsString").Set(mySheet1.cells(DSRowNum,10).value)
	Browser("Edit Document Content").Page("Edit Document Content").Image("save the attachment details").Click
	Browser("View Collection").Page("View Collection_2").Image("view document attributes.").Click
	Filesize2=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(16,2)
	Filesize2=Mid(Filesize2,1,2)
    If CInt(Filesize1) <> CInt(Filesize2) Then
		Call Save_Data("Edit  content of english doc from view collection page", "Pass")
	else
		Call Save_Data("Edit content of english doc from view collection page", "Fail")
	End If
    'Edit attributes of english doc
    Browser("View Document Details").Page("View Document Details").Image("Edit").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("title").Set(mySheet1.cells(DSRowNum,5).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Save Document Properties").Click
	Browser("View Collection").Page("View Collection_2").Image("view document attributes.").Click
	'TitleNew=Browser("View Collection").Page("View Collection").WebTable("You can create or upload").GetCellData(2,2)
	TitleNew=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(2,2)
	'TitleNew=Mid(TitleNew,34,11)
	'Msgbox TitleNew
    If strcomp(trim(TitleOld),trim(TitleNew)) = 0 Then
		'msgbox "edit properties Fail"
		Call Save_Data("Edit  english doc properties", "Fail")
	else
		'msgbox "edit properties Pass"
		Call Save_Data("Edit  english doc properties", "Pass")
	End If
	Browser("View Document Details").Page("View Document Details").Image("Back To View Collection").Click
End Sub

'**********************************************Remove English collection document************************************************************
Sub RemoveEngDoc
	Browser("View Collection").Page("View Collection_2").WebCheckBox("attachmentString").Click
	Browser("View Collection").Page("View Collection_2").Image("Remove").Click
	Browser("View Collection").Page("Remove").Image("remove the attachments").Click
	EngDocExist= Browser("View Collection").Page("Remove").WebTable("innertext:=You can create.*").GetCellData(2,1)
	'Msgbox EngDocExist
	If strcomp(EngDocExist,"This collection has no English documents.")= 0 then
		'msgbox "Remove collection doc Pass"
		Call Save_Data("Remove english doc", "Pass")
	else
		'msgbox "Remove collection doc Fail"
		Call Save_Data("Remove english doc", "Fail")
	end if
End Sub

'***********************************************Validate collection with no item**************************************************************
Sub Validate_Collection_with_Noitem
	Browser("View Collection").Page("View Collection").Image("Validate This Collection").Click
	if Browser("View Collection").Page("View Collection").WebElement("** No items have been").Exist then
		Call Save_Data("Collection_validate_with_Noitem", "Pass")
	else
		Call Save_Data("Collection_validate_with_Noitem", "Fail")
	 End If
End Sub

'***************************************************upload Eng document*******************************************************************
Sub UploadEngDoc
	Browser("View Collection").Page("View Collection").Image("Add New").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("title").Set(mySheet1.cells(DSRowNum,4).value)
	Browser("Edit Documents").Page("Edit Documents").WebList("availDocumentTypes").Select(mySheet1.cells(DSRowNum,6).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add").Click
	Browser("Edit Documents").Page("Edit Documents").WebList("availLanguageCharSets").Select(mySheet1.cells(DSRowNum,8).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add_2").Click
	Browser("Edit Documents").Page("Edit Documents").Image("Upload Document Content_2").Click
	Browser("Upload Document").Page("Upload Document").WebFile("attachment").Click
'    set WshShell =CreateObject("WScript.Shell")
'    'WshShell.Sendkeys "C:\ATMN\upld1.htm"
'	WshShell.Sendkeys UploadFile
	Browser("Upload Document").Dialog("Choose File to Upload").WinEdit("File name:").Set(UploadFile(0))
	Browser("Upload Document").Dialog("Choose File to Upload").WinButton("Open").Click
	Browser("Upload Document").Page("Upload Document").Image("save the attachment details").Click
	FileExtnPos=Instr(UploadFile(0),".")
	FileExtnPos = FileExtnPos+1
	FileExtn=Mid(UploadFile(0),FileExtnPos)
	If FileExtn = "htm" or FileExtn="html" Then
		Browser("Upload Document").Page("Upload Document").Image("save the attachment details").Click
	End If
	If Browser("View Collection").Page("View Collection_2").WebCheckBox("attachmentString").Exist Then
		Call Save_Data("Upload english document ", "Pass")
	else
		Call Save_Data("Upload english document", "Fail")
	End If
End Sub

'**********************************************************Add collection level Non Eng doc**************************************************
Sub AddNonEngDoc
	Browser("View Collection").Page("View Collection_3").Image("Add New").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("title").Set(mySheet1.cells(DSRowNum,11).value)
	Browser("Edit Documents").Page("Edit Documents").WebList("availDocumentTypes").Select(mySheet1.cells(DSRowNum,6).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add").Click
	Browser("Edit Documents").Page("Edit Documents").WebList("availLanguageCharSets").Select(mySheet1.cells(DSRowNum,14).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add_2").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("objectName").Set(mySheet1.cells(DSRowNum,13).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Save Document Properties_2").Click
    'Add document content
	Browser("Edit Document Content").Page("Edit Document Content").WebEdit("contentAsString").Set(mySheet1.cells(DSRowNum,15).value)
	Browser("Edit Document Content").Page("Edit Document Content").Image("save the attachment details").Click
    'Check for newly added english under view collection page
	NonEngDocExist=Browser("View Collection").Page("View Collection_3").WebTable("Non-English Documents").GetCellData(3,1)
	NonEngDocExist=Mid(NonEngDocExist,11,2)
	If  NonEngDocExist = "no" Then
		Call Save_Data("Add new non english doc from view collection page", "Fail")
	else
		Call Save_Data("Add new non english doc from view collection page", "Pass")
	End If
End Sub

'**********************************************************Edit non english document***************************************************************
Sub EditNonEngDoc
   Browser("View Collection").Page("View Collection_3").Image("View Non-English Documents").Click
	'TitleOld=Browser("Non-English Documents").Page("Non-English Documents").Frame("currentTranslation").WebTable("innertext:=Attached to collection.*","index:=1").GetCellData(4,2)
	'msgbox "title old: "+TitleOld
	'Filesize1=Mid(TitleOld,26,2)
	'msgbox "size1:"+Filesize1
	'TitleOld=Mid(TitleOld,37,11)
	'msgbox "title old: "+TitleOld

	'Edit the content of non englisg doc
	Browser("Non-English Documents").Page("Non-English Documents").Frame("currentTranslation").Image("view document attributes.").Click
	TitleOld=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(2,2)
	Filesize1=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(16,2)
	Filesize1=Mid(Filesize1,1,2)
'	msgbox Filesize1
	Browser("View Document Details").Page("View Document Details").Image("Edit Document Content").Click
	Browser("Edit Document Content").Page("Edit Document Content").WebEdit("contentAsString").Set(mySheet1.cells(DSRowNum,10).value)
	Browser("Edit Document Content").Page("Edit Document Content").Image("save the attachment details").Click
	Browser("Non-English Documents").Page("Non-English Documents").Frame("currentTranslation").Image("view document attributes.").Click
	
	'Edit the attributes of non english doc
	Browser("View Document Details").Page("View Document Details").Image("Edit").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("title").Set(mySheet1.cells(DSRowNum,12).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Save Document Properties").Click
	'TitleNew=Browser("Non-English Documents").Page("Non-English Documents").Frame("currentTranslation").WebTable("innertext:=Attached to collection.*","index:=1").GetCellData(4,2)
	'Filesize2=Mid(TitleNew,26,2)
	'msgbox "size2 : "+Filesize2
	'TitleNew=Mid(TitleNew,37,11)
	'msgbox "title new: "+TitleNew
	Browser("Non-English Documents").Page("Non-English Documents").Frame("currentTranslation").Image("view document attributes.").Click
	TitleNew=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(2,2)
	Filesize2=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(16,2)
	Filesize2=Mid(Filesize2,1,2)
'	msgbox Filesize2
	If CInt(Filesize1) <> CInt(Filesize2) Then
		Call Save_Data("Edit  content of non english doc", "Pass")
	else
		Call Save_Data("Edit content of non english doc", "Fail")
	End If
	If strcomp(trim(TitleOld),trim(TitleNew)) = 0 Then
		Call Save_Data("Edit non english doc properties", "Fail")
	else
    	Call Save_Data("Edit non english doc properties", "Pass")
	End If
	Browser("View Document Details").Page("View Document Details").Image("Back To View Non-English").Click
	Browser("Non-English Documents").Page("Non-English Documents").Image("View Collection").Click
End Sub

'*********************************************************Remove non english doc*****************************************************************
Sub RemoveNonEngDoc
	Browser("View Collection").Page("View Collection_3").Image("View Non-English Documents").Click
	Browser("Non-English Documents").Page("Non-English Documents").Frame("currentTranslation").WebCheckBox("attachmentString").Click
	Browser("Non-English Documents").Page("Non-English Documents").Image("Remove").Click
	Browser("Non-English Documents").Page("Remove Translations").Image("remove the attachments").Click
    If Browser("Non-English Documents").Page("Non-English Documents").Frame("currentTranslation").Image("view document attributes.").Exist then
		Call Save_Data("Remove  non english doc", "Fail")
	else
		Call Save_Data("Remove non english doc", "Pass")
	end if
	Browser("Non-English Documents").Page("Non-English Documents").Image("View Collection").Click
	'If submitalType = "EL" Then
		'	NonEngDocExist=Browser("View Collection").Page("View Collection_4").WebTable("Non-English Documents").GetCellData(3,1)
	'else
		'NonEngDocExist=Browser("View Collection").Page("View Collection_3").WebTable("Non-English Documents").GetCellData(3,1)
	'End If
    'NonEngDocExist=Mid(NonEngDocExist,11,2)
    'If  NonEngDocExist = "no" Then
		'Call Save_Data("Remove  non english doc", "Pass")
	'else
		'Call Save_Data("Remove non english doc", "Fail")
	'end if
End Sub

'**************************************************************Add new item for Phy med collection**********************************************
Sub AddNewItem_PM
	submitalType = "PM"
	 Browser("View Collection").Page("View Collection").Image("Add a New Software Item").Click
	Browser("Edit Item").Page("Edit Item_2").Sync
	Browser("Edit Item").Page("Edit Item_2").WebEdit("orderablePartId").Set(mySheet3.cells(DSRowNum,1).value)
	Browser("Edit Item").Page("Edit Item_2").WebEdit("bomSequence").Set(mySheet3.cells(DSRowNum,2).value)
	Browser("Edit Item").Page("Edit Item_2").WebEdit("bomQuantity").Set(mySheet3.cells(DSRowNum,3).value)
	Browser("Edit Item").Page("Edit Item_2").WebEdit("bomValue").Set(mySheet3.cells(DSRowNum,4).value)
	Browser("Edit Item").Page("Edit Item_2").WebList("levelOid").Select(mySheet3.cells(DSRowNum,5).value)
	Browser("Edit Item").Page("Edit Item_2").WebList("checksumTypeOid").Select(mySheet3.cells(DSRowNum,6).value)
	Browser("Edit Item").Page("Edit Item_2").WebButton("Add BOM").Click
	Browser("Edit Item").Page("Edit Item").WebList("severityOid").Select(mySheet3.cells(DSRowNum,7).value)
	Browser("Edit Item").Page("Edit Item_2").WebCheckBox("mediaTypeOids").Click
	Browser("Edit Item").Page("Edit Item").WebEdit("softwareVersion").Set(mySheet3.cells(DSRowNum,8).value)
	Browser("Edit Item").Page("Edit Item_2").WebList("availDistributionRegion").Select(mySheet3.cells(DSRowNum,9).value)
	Browser("Edit Item").Page("Edit Item_2").Image("Add").Click
	Browser("Edit Item").Page("Edit Item").WebList("priceTypeOid").Select(mySheet3.cells(DSRowNum,10).value)
	For each  x in itemLang
		Browser("Edit Item").Page("Edit Item").WebList("availLanguageCharSets").Select(x)
		Browser("Edit Item").Page("Edit Item_2").Image("Add_4").Click
	Next
	 set WshShell =CreateObject("WScript.Shell")
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").Link("+_3").Click
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebEdit("PM_SEARCH").Click
	WshShell.Sendkeys "2342"
	wait(5)
	WshShell.Sendkeys "{ENTER}"
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("WebTable").WaitProperty "text","Up.*"
	'Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("358960-B72(Compaq Armada").Click
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("234219-B21(Compaq 1700").Click
	'Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("innertext:=3589.*","index:=3").Click
	'Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("innertext:=3589.*","index:=itemIndex").Click
	'Set prodVal=Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("innertext:=3589.*","index=itemIndex")
	'prodVal.object.index=itemIndex
	'prodVal.object.Click
	Browser("Edit Item").Page("Edit Item_2").Image("Add_3").Click
	'Browser("Edit Item").Page("Edit Item").WebList("environmentsParent").Select("Windows-English")
	'Browser("Edit Item").Page("Edit Item").WebList("envDetails").Select("Microsoft Windows 3.1")
	Browser("Edit Item").Page("Edit Item").WebList("environmentsParent").Select(mySheet3.cells(DSRowNum,11).value)
	Browser("Edit Item").Page("Edit Item").WebList("envDetails").Select(mySheet3.cells(DSRowNum,12).value)
	Browser("Edit Item").Page("Edit Item_2").Image("Add_5").Click
	Browser("Edit Item").Page("Edit Item_2").WebList("availOrderLinks").Select(mySheet3.cells(DSRowNum,13).value)
	Browser("Edit Item").Page("Edit Item_2").Image("Add_2").Click
	Browser("Edit Item").Page("Edit Item").Image("Save_2").Click
End Sub

'**************************************************************Add new item for electronic collection**********************************************
Sub AddNewItem_EL
    submitalType = "EL"
	Browser("View Collection").Page("View Collection").Image("Add a New Software Item").Click
	Browser("Edit Item").Page("Edit Item").Sync
	Browser("Edit Item").Page("Edit Item").WebList("severityOid").Select(mySheet3.cells(DSRowNum,7).value)
	Browser("Edit Item").Page("Edit Item").WebEdit("softwareVersion").Set(mySheet3.cells(DSRowNum,8).value)
	Browser("Edit Item").Page("Edit Item").WebList("priceTypeOid").Select(mySheet3.cells(DSRowNum,10).value)
	
	For each  x in itemLang
		Browser("Edit Item").Page("Edit Item").WebList("availLanguageCharSets").Select(x)
		 Browser("Edit Item").Page("Edit Item_4").Image("Add").Click
	Next
	 set WshShell =CreateObject("WScript.Shell")
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").Link("+_3").Click
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebEdit("PM_SEARCH").Click
	WshShell.Sendkeys "2342"
	wait(5)
	WshShell.Sendkeys "{ENTER}"
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("WebTable").WaitProperty "text","Up.*"
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("234219-B21(Compaq 1700").Click
	'Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("358960-B72(Compaq Armada").Click
	Browser("Edit Item").Page("Edit Item_4").Image("Add_2").Click
	Browser("Edit Item").Page("Edit Item").WebList("environmentsParent").Select(mySheet3.cells(DSRowNum,11).value)
	Browser("Edit Item").Page("Edit Item").WebList("envDetails").Select(mySheet3.cells(DSRowNum,12).value)
	Browser("Edit Item").Page("Edit Item_4").Image("Add_3").Click

    Browser("Edit Item").Page("Edit Item").Image("Save_2").Click
End Sub

'***************************************************************Add new item for data only collection********************************************
Sub AddNewItem
	Browser("View Collection").Page("View Collection").Image("Add a New Software Item").Click
	Browser("Edit Item").Page("Edit Item").Sync
	Browser("Edit Item").Page("Edit Item").WebList("severityOid").Select(mySheet3.cells(DSRowNum,7).value)
	Browser("Edit Item").Page("Edit Item").WebEdit("softwareVersion").Set(mySheet3.cells(DSRowNum,8).value)
	Browser("Edit Item").Page("Edit Item").WebList("priceTypeOid").Select(mySheet3.cells(DSRowNum,10).value)
	For each  y in itemLang
		Browser("Edit Item").Page("Edit Item").WebList("availLanguageCharSets").Select(y)
		 Browser("Edit Item").Page("Edit Item").Image("Add_3").Click
	Next
	set WshShell =CreateObject("WScript.Shell")
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").Link("+_3").Click
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebEdit("PM_SEARCH").Click
	WshShell.Sendkeys "2342"
	wait(5)
	WshShell.Sendkeys "{ENTER}"
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("WebTable").WaitProperty "text","Up.*"
'	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("358960-B72(Compaq Armada").Click
	Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("234219-B21(Compaq 1700").Click
	'Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("innertext:=3589.*","index:=3").Click
	'Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("innertext:=3589.*","index:=itemIndex").Click
	'Set prodVal=Browser("Edit Item").Page("Edit Item").Frame("pmproductGroupsSimple").WebElement("innertext:=3589.*","index=itemIndex")
	'prodVal.object.index=itemIndex
	'prodVal.object.Click
	Browser("Edit Item").Page("Edit Item").Image("Add_2").Click
	'Browser("Edit Item").Page("Edit Item").WebList("environmentsParent").Select("Windows-English")
	'Browser("Edit Item").Page("Edit Item").WebList("envDetails").Select("Microsoft Windows 3.1")
	Browser("Edit Item").Page("Edit Item").WebList("environmentsParent").Select(mySheet3.cells(DSRowNum,11).value)
	Browser("Edit Item").Page("Edit Item").WebList("envDetails").Select(mySheet3.cells(DSRowNum,12).value)
	Browser("Edit Item").Page("Edit Item").Image("Add_4").Click
	wait(5)
	Browser("Edit Item").Page("Edit Item").Image("Save_2").Click
End Sub

'***********************************************************Add Software_File to item*************************************************************
Sub Add_Software_File
	Browser("View Software Item").Page("View Software Item").Image("Add New_3").Click
	Browser("Upload Software Files").Page("Upload Software Files").WebFile("firstFile").Click
	'set WshShell =CreateObject("WScript.Shell")
	'WshShell.Sendkeys SoftwareFile
	Browser("Upload Software Files").Dialog("Choose File to Upload").WinEdit("File name:").Set(mySheet3.cells(DSRowNum,14).value)
	Browser("Upload Software Files").Dialog("Choose File to Upload").WinButton("Open").Click
    Browser("Upload Software Files").Page("Upload Software Files").WebButton("Upload Files").Click
	Browser("Upload Software Files").Page("Upload Software Files").WebElement("File uploaded and attached").WaitProperty "innertext","File uploaded and attached to collection/item successfully"
	Browser("Upload Software Files").Page("Upload Software Files").Image("Organize Files").Click
	Browser("Upload Software Files").Page("Organize Uploaded Files").WebList("pageFileTypeOid").Select(mySheet3.cells(DSRowNum,15).value)
	Browser("Upload Software Files").Page("Organize Uploaded Files").WebEdit("fileCheckvalue").Set(mySheet3.cells(DSRowNum,16).value)
	Browser("Upload Software Files").Page("Organize Uploaded Files").Image("Attach").Click
    'If Browser("View Software Item").Page("View Software Item").Link("text:=*").Exist then
	If Browser("View Software Item_2").Page("View Software Item").WebCheckBox("name:=attachmentString","index:=1").Exist Then
		Call Save_Data("Add software file","Pass")
	else
		Call Save_Data("Add software file","Fail")
    end if
End Sub

'*****************************************************************Verify new ly added  item********************************************************
Sub VerifyNewItem
	Browser("View Software Item").Page("View Software Item").Sync
	If submitalType = "PM" Then
        itemValidity=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(13,2)
		'itemState=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(10,2)
		itemValidity=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(13,2)
		itemID=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(2,2)
	elseif submitalType = "EL" Then
        itemValidity=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(11,2)
		'itemState=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(8,2)
		itemValidity=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(11,2)
		itemID=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(2,2)
	else
        itemValidity=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(11,2)
		'itemState=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(8,2)
		itemValidity=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(11,2)
		itemID=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(2,2)
    End If
	'msgbox itemValidity
	'msgbox itemID
   	If  trim(itemValidity) = "This item has not yet passed validation." Then
		call Save_Data("Add new item","Pass and item id is "+itemID)
	else
		call Save_Data("Add new item","Fail")
	End If
End Sub

'*******************************************************Validate newly added item************************************************************
Sub ValidateItem
	Browser("View Software Item").Page("View Software Item").Image("Display product / environment").Click
	If Browser("Products/Environments").Page("Products/Environments").Image("blue_isvalid").Exist then
		call Save_Data("Product to environment mapping","Pass")
		Browser("Products/Environments").Page("Products/Environments").Image("blue_close").Click
		Browser("View Software Item").Page("View Software Item").Image("Validate This Item").Click
		 If submitalType = "PM" Then
			itemValidity=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(13,2)
		else
			itemValidity=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=The details.*","index:=1").GetCellData(11,2)
		End If
		 If trim(itemValidity) = "This item is valid." Then
			Call Save_Data("Validate an item", "Pass")
		else
			Call Save_Data("Validate an item", "Fail")
		End If
	else
		call Save_Data("Product to environment mapping","Fail")
	end if
End Sub

'*****************************************************************************Remove an item************************************************************
Sub RemoveItem
	Browser("View Software Item").Page("View Software Item").Image("View The Collection_2").Click
	Browser("View Collection").Page("View Collection").WebCheckBox("objectIds").Set("ON")
	Browser("View Collection").Page("View Collection").Image("Delete Selected Items").Click
	Browser("Confirm Delete Item").Page("Confirm Delete Item").Image("delete the item").Click
	If  Browser("View Collection").Page("View Collection").Image("Delete Selected Items").Exist Then
		Call Save_Data("Remove an item", "Fail")
	else
		Call Save_Data("Remove an  item", "Pass")
	End If
End Sub

'*********************************************************Add new itel level english doc*****************************************************
Sub Add_ItemEngDoc
	Browser("View Software Item").Page("View Software Item").Image("Add New").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("title").Set(mySheet1.cells(DSRowNum,4).value)
	Browser("Edit Documents").Page("Edit Documents").WebList("availDocumentTypes").Select(mySheet1.cells(DSRowNum,6).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add").Click
	Browser("Edit Documents").Page("Edit Documents").WebList("availLanguageCharSets").Select(mySheet1.cells(DSRowNum,8).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add_2").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("objectName").Set(mySheet1.cells(DSRowNum,7).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Save Document Properties_2").Click
    'Add document content
	Browser("Edit Document Content").Page("Edit Document Content").WebEdit("contentAsString").Set(mySheet1.cells(DSRowNum,9).value)
	Browser("Edit Document Content").Page("Edit Document Content").Image("save the attachment details").Click
    'Check for newly added english under view collection page
	'If Browser("View Collection").Page("View Collection_2").WebCheckBox("attachmentString").Exist Then
	if	Browser("View Software Item").Page("View Software Item").Image("view document attributes.").Exist Then
		Call Save_Data("Add new english doc from view item page", "Pass")
	else
		Call Save_Data("Add new english doc from view item page", "Fail")
	End If
End Sub

'**********************************************************Edit item level english doc content and attributes**************************************************
Sub Edit_ItemEngDoc
	 Browser("View Software Item").Page("View Software Item").Image("view document attributes.").Click
	 TitleOld=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(2,2)
	'TitleOld=Mid(TitleOld,34,11)
	'Msgbox TitleOld
	 Filesize1=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(16,2)
	 Filesize1=Mid(Filesize1,1,2)
    'TitleOld=Browser("View Software Item").Page("View Software Item").WebTable("innertext:=You.*").GetCellData(2,2)
	'TitleOld=Mid(TitleOld,34,11)
	''Msgbox TitleOld
	'Browser("View Software Item").Page("View Software Item").Image("view document attributes.").Click
	'Filesize1=Browser("View Document Details").Page("View Document Details").WebTable("Document Details").GetTOProperty("innerText")
	'Filesize1=Mid(Filesize1,360,2)
    'Edit the content of english doc
	Browser("View Document Details").Page("View Document Details").Image("Edit Document Content").Click
	Browser("Edit Document Content").Page("Edit Document Content").WebEdit("contentAsString").Set(mySheet1.cells(DSRowNum,10).value)
	Browser("Edit Document Content").Page("Edit Document Content").Image("save the attachment details").Click
	Browser("View Software Item").Page("View Software Item").Image("view document attributes.").Click
	Filesize2=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(16,2)
	Filesize2=Mid(Filesize1,1,2)
    If CInt(Filesize1) <> CInt(Filesiz2) Then
		Call Save_Data("Edit  content of english doc from view item page", "Pass")
	else
		Call Save_Data("Edit content of english doc from view item page", "Fail")
	End If
    'Edit attributes of english doc
    Browser("View Document Details").Page("View Document Details").Image("Edit").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("title").Set(mySheet1.cells(DSRowNum,5).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Save Document Properties").Click
    Browser("View Software Item").Page("View Software Item").Image("view document attributes.").Click
	TitleNew=Browser("View Document Details").Page("View Document Details").WebTable("innertext:=Document Details.*").GetCellData(2,2)
	'Msgbox TitleNew
    If strcomp(trim(TitleOld),trim(TitleNew)) = 0 Then
		'msgbox "edit properties Fail"
		Call Save_Data("Edit  item level english doc properties", "Fail")
	else
		'msgbox "edit properties Pass"
		Call Save_Data("Edit  item level english doc properties", "Pass")
	End If
    Browser("View Document Details").Page("View Document Details").Image("Back To View Item Details").Click
End Sub

'*****************************************************************Remove item level english document************************************************************
Sub RemoveItemEngDoc

	Browser("View Software Item").Page("View Software Item").WebCheckBox("name:=attachmentString","index:=0").Click
	Browser("View Software Item").Page("View Software Item").Image("Remove").Click
	Browser("View Software Item").Page("Remove").Image("remove the attachments").Click
	EngDocExist= Browser("View Software Item").Page("View Software Item").WebTable("innertext:=You can create.*").GetCellData(2,1)
	If strcomp(trim(EngDocExist),"Currently there are no documents attached to this item.")= 0 then
		'msgbox "Remove itemlevel Eng  doc Pass"
		Call Save_Data("Remove item level english doc", "Pass")
	else
		'msgbox "Remove itemlevel Eng doc Fail"
		Call Save_Data("Remove item level english doc", "Fail")
	end if
End Sub

'*****************************************************************Upload item level english document************************************************************
Sub UploadItemEngDoc

   For i=0 to 5

	Browser("View Software Item").Page("View Software Item").Image("Add New").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("title").Set(mySheet1.cells(DSRowNum,4).value)
	Browser("Edit Documents").Page("Edit Documents").WebList("availDocumentTypes").Select(mySheet1.cells(DSRowNum,6).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add").Click
	Browser("Edit Documents").Page("Edit Documents").WebList("availLanguageCharSets").Select(mySheet1.cells(DSRowNum,8).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add_2").Click
	Browser("Edit Documents").Page("Edit Documents").Image("Upload Document Content_2").Click
	Browser("Upload Document").Page("Upload Document").WebFile("attachment").Click
	Browser("Upload Document").Dialog("Choose File to Upload").WinEdit("File name:").Set(UploadFile(i))
	Browser("Upload Document").Dialog("Choose File to Upload").WinButton("Open").Click
	Browser("Upload Document").Page("Upload Document").Image("save the attachment details").Click
	FileExtnPos=Instr(UploadFile(i),".")
	FileExtnPos = FileExtnPos+1
	FileExtn=Mid(UploadFile(i),FileExtnPos)
	If FileExtn = "htm" or FileExtn="html" Then
		Browser("Upload Document").Page("Upload Document").Image("save the attachment details").Click
	End If
	If Browser("View Software Item").Page("View Software Item").WebCheckBox("name:=attachmentString","index:="&i).Exist Then
		UploadStatus="1"
    End If
	If  UploadStatus = "0" Then
		Call Save_Data("Upload Item level English document failed for" + UploadFile(i))
		UploadFailure="TRUE"
	End If
    Next
	If UploadFailure = "FALSE"  Then
    		Call Save_Data("Upload Item level English document ", "Pass")
		'msgbox "upload item level eng doc :pass"
	else
		Call Save_Data("Upload Item level English document", "Fail")
		'msgbox "upload item level eng doc :fail"
	End If

	For i=0 to 5
	Browser("View Software Item").Page("View Software Item").WebCheckBox("name:=attachmentString","index:="&i).Click
	Next
	Browser("View Software Item").Page("View Software Item").Image("Remove").Click
	Browser("View Collection").Page("Remove").Image("remove the attachments").Click

	Browser("View Software Item").Page("View Software Item").Image("Add New").Click
	Browser("Edit Documents").Page("Edit Documents").WebEdit("title").Set(mySheet1.cells(DSRowNum,4).value)
	Browser("Edit Documents").Page("Edit Documents").WebList("availDocumentTypes").Select(mySheet1.cells(DSRowNum,6).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add").Click
	Browser("Edit Documents").Page("Edit Documents").WebList("availLanguageCharSets").Select(mySheet1.cells(DSRowNum,8).value)
	Browser("Edit Documents").Page("Edit Documents").Image("Add_2").Click
	Browser("Edit Documents").Page("Edit Documents").Image("Upload Document Content_2").Click
	Browser("Upload Document").Page("Upload Document").WebFile("attachment").Click
	Browser("Upload Document").Dialog("Choose File to Upload").WinEdit("File name:").Set(UploadFile(0))
	Browser("Upload Document").Dialog("Choose File to Upload").WinButton("Open").Click
	Browser("Upload Document").Page("Upload Document").Image("save the attachment details").Click

End Sub

'*****************************************************************Search collection by title************************************************************
Sub Search_collection_byTitle

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("by Title").Click
	'msgbox CollectionTitle
	Browser("Search by Collection Title").Page("Search by Collection Title").WebEdit("title").Set(CTitle)
	Browser("Search by Collection Title").Page("Search by Collection Title").Image("Search").Click
	Browser("Collection Search Results").Page("Collection Search Results").Sync
	SearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("innertext:=Your search.*","index:=0").GetCellData(1,1),13,7)
	If SearchResult = "did not" Then
		'Call Save_Data("Search Collection by invalid title", "Pass")
		Select Case CollectionTitleType
			Case "TWS"
				Call Save_Data("Search Collection by invalid  title with space", "Pass")
			Case "TWU"
				Call Save_Data("Search Collection by invalid  title with uppercase", "Pass")
			Case "TWL"
				Call Save_Data("Search Collection by invalid  title with lowercase", "Pass")
			Case "TWP"
				Call Save_Data("Search Collection by partial invalid title", "Pass")
			 Case Else
				 Call Save_Data("Search Collection by invalid title", "Pass")
			End Select

	Else
		SearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("innertext:=Your search.*","index:=0").GetCellData(1,1),23,1)
  ' msgbox SearchResult
		If Cint(SearchResult) > 0 Then
            Select Case CollectionTitleType
			Case "TWS"
				Call Save_Data("Search Collection by vallid title with space", "Pass")
			Case "TWU"
				Call Save_Data("Search Collection by valid title with uppercase", "Pass")
			Case "TWL"
				Call Save_Data("Search Collection by valid title with lowercase", "Pass")
			Case "TWP"
				Call Save_Data("Search Collection by partial valid title", "Pass")
			 Case Else
				 Call Save_Data("Search Collection by valid title", "Pass")
			End Select
        else
			Call Save_Data("Search Collection by valid title", "Fail")
		End If
	End If
End Sub

'*****************************************************************Search collection by ID*******************************************************************

Sub Search_collection_ById

    Browser("View Collection").Page("View Collection").Link("by Collection ID").Click
	Browser("Search by Collection").Page("Search by Collection").WebEdit("collectionId").Set(CId)
	Browser("Search by Collection").Page("Search by Collection").Image("Search_2").Click
	Browser("Collection Search Results").Page("Collection Search Results").Sync
    SearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("innertext:=Your search.*","index:=0").GetCellData(1,1),13,7)
	If SearchResult = "did not" Then
		'Call Save_Data("Search Collection by invalid title", "Pass")
		Select Case CollectionTitleType
			Case "IWS"
				Call Save_Data("Search Collection by invalid  ID with space", "Pass")
			Case "IWU"
				Call Save_Data("Search Collection by invalid  ID with uppercase", "Pass")
			Case "IWL"
				Call Save_Data("Search Collection by invalid  ID with lowercase", "Pass")
			Case "IWP"
				Call Save_Data("Search Collection by partial invalid ID", "Pass")
			 Case Else
				 Call Save_Data("Search Collection by invalid ID", "Pass")
			End Select

	Else
		SearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("innertext:=Your search.*","index:=0").GetCellData(1,1),23,1)
  ' msgbox SearchResult
		If Cint(SearchResult) > 0 Then
            Select Case CollectionTitleType
			Case "IWS"
				Call Save_Data("Search Collection by vallid ID with space", "Pass")
			Case "IWU"
				Call Save_Data("Search Collection by valid ID with uppercase", "Pass")
			Case "IWL"
				Call Save_Data("Search Collection by valid ID with lowercase", "Pass")
			Case "IWP"
				Call Save_Data("Search Collection by partial valid  ID", "Pass")
			 Case Else
				 Call Save_Data("Search Collection by valid ID", "Pass")
			End Select
        else
			Call Save_Data("Search Collection by valid ID", "Fail")
		End If
	End If
End Sub

'*****************************************************************Search Item by ID**************************************************************************
Sub Search_Item_ById

    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("by Item ID").Click
	Browser("Search by Collection").Page("Search by Item").WebEdit("itemId").Set(ItemId)
	Browser("Search by Collection").Page("Search by Item").Image("Search").Click
	Browser("Collection Search Results").Page("Collection Search Results").Sync
    SearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("innertext:=Your search.*","index:=0").GetCellData(1,1),13,7)
	If SearchResult = "did not" Then
		'Call Save_Data("Search Collection by invalid title", "Pass")
		Select Case ItemIdType
			Case "IWS"
				Call Save_Data("Search Item by invalid  ID with space", "Pass")
			Case "IWU"
				Call Save_Data("Search Item by invalid  ID with uppercase", "Pass")
			Case "IWL"
				Call Save_Data("Search Item by invalid  ID with lowercase", "Pass")
			Case "IWP"
				Call Save_Data("Search Item by partial invalid ID", "Pass")
			 Case Else
				 Call Save_Data("Search Item by invalid ID", "Pass")
			End Select

	Else
		SearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("innertext:=Your search.*","index:=0").GetCellData(1,1),22,1)
  ' msgbox SearchResult
		If Cint(SearchResult) > 0 Then
            Select Case ItemIdType
			Case "IWS"
				Call Save_Data("Search Item by vallid ID with space", "Pass")
			Case "IWU"
				Call Save_Data("Search Item by valid ID with uppercase", "Pass")
			Case "IWL"
				Call Save_Data("Search Item by valid ID with lowercase", "Pass")
			Case "IWP"
				Call Save_Data("Search Item by partial valid ID", "Pass")
			 Case Else
				 Call Save_Data("Search Item by valid ID", "Pass")
			End Select
        else
			Call Save_Data("Search Item by valid ID", "Fail")
		End If
	End If
End Sub

'*****************************************************************Search Item by Filename**************************************************************************
Sub Search_Item_ByFilename

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("by Filename").Click
	Browser("Search by Collection").Page("Search by File Name").WebEdit("fileName").Set(Filename)
	Browser("Search by Collection").Page("Search by File Name").Image("Search").Click
	Browser("Collection Search Results").Page("Collection Search Results").Sync
   SearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("innertext:=Your search.*","index:=0").GetCellData(1,1),13,7)
   	If SearchResult = "did not" Then
		'Call Save_Data("Search Collection by invalid title", "Pass")
		Select Case FilenameType
			Case "FWS"
				Call Save_Data("Search Item by invalid  Filename with space", "Pass")
			Case "FWU"
				Call Save_Data("Search Item by invalid  Filename with uppercase", "Pass")
			Case "FWL"
				Call Save_Data("Search Item by invalid  Filename with lowercase", "Pass")
			Case "FWP"
				Call Save_Data("Search Item by partial invalid Filename", "Pass")
			 Case Else
				 Call Save_Data("Search Item by invalid Filename", "Pass")
			End Select

	Else
		SearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("innertext:=Your search.*","index:=0").GetCellData(1,1),22,1)
  ' msgbox SearchResult
		If Cint(SearchResult) > 0 Then
            Select Case FilenameType
			Case "FWS"
				Call Save_Data("Search Item by vallid Filename with space", "Pass")
			Case "FWU"
				Call Save_Data("Search Item by valid Filename with uppercase", "Pass")
			Case "FWL"
				Call Save_Data("Search Item by valid Filename with lowercase", "Pass")
			Case "FWP"
				Call Save_Data("Search Item by partial valid Filename", "Pass")
			 Case Else
				 Call Save_Data("Search Item by valid Filename", "Pass")
			End Select
        else
			Call Save_Data("Search Item by valid Filename", "Fail")
		End If
	End If
End Sub

'*****************************************************************Last search results--immediate login************************************************************
Sub Last_Search_Results_immediateLogin

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Last Search Results").Click
	SearchResult=Mid(Browser("Search by Collection").Page("SOAR Error page").WebTable("Return to the SOAR homepage").GetCellData(1,1),47,43)
 
	If SearchResult = "You do not have any previous search results" Then
		Call Save_Data("Last Search Results immediately after login","Pass")
		else
		Call Save_Data("Last Search Results immediately after login","Fail")
	End If

End Sub

'*****************************************************************Last search results************************************************************
Sub Last_Search_Results

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Last Search Results").Click
	LSearchResult=Mid(Browser("Collection Search Results").Page("Collection Search Results").WebTable("innertext:=Your search.*","index:=0").GetCellData(1,1),23,1)
 
	If SearchResult = LSearchResult Then
		Call Save_Data("Last Search Results","Pass")
		else
		Call Save_Data("Last Search Results","Fail")
	End If

End Sub

'*****************************************************************Search collection by blank title************************************************************
Sub Search_Collection_byBlankTitle
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("by Title").Click
    Browser("Search by Collection Title").Page("Search by Collection Title").Image("Search").Click
	SearchResult=Mid(Browser("Search by Collection Title").Page("Search by Collection Title").WebTable("Search Criteria_2").GetCellData(3,2),33,8)
	'msgbox SearchResult
	If SearchResult = "You must" Then
		Call Save_Data("Search Collection by blank title", "Pass")
	else
		Call Save_Data("Search Collection by blank title", "Fail")
	end if
End Sub

'*****************************************************************Search collection by blank CollectionID************************************************************
Sub Search_Collection_byBlankColnID

	Browser("View Collection").Page("View Collection").Link("by Collection ID").Click
    Browser("Search by Collection").Page("Search by Collection").Image("Search_2").Click
	SearchResult=Mid(Browser("Search by Collection").Page("Search by Collection").WebTable("Search Criteria_2").GetCellData(3,2),85,8)
	'msgbox SearchResult
	If SearchResult = "You must" Then
		Call Save_Data("Search Collection by blank collection ID", "Pass")
	else
		Call Save_Data("Search Collection by blank collection ID", "Fail")
	end if
End Sub

'*****************************************************************Search item by blank item ID************************************************************
Sub Search_Item_byBlankItemID

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("by Item ID").Click
    Browser("Search by Collection").Page("Search by Item").Image("Search").Click
	SearchResult=Mid(Browser("Search by Collection").Page("Search by Item").WebTable("Search Criteria").GetCellData(3,2),79,8)
	'msgbox SearchResult
	If SearchResult = "You must" Then
		Call Save_Data("Search item by blank item ID", "Pass")
	else
		Call Save_Data("Search item by blank item ID", "Fail")
	end if
End Sub

'*****************************************************************Search item  by blank Filename************************************************************
Sub Search_Item_byBlankFilename

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("by Filename").Click
    Browser("Search by Collection").Page("Search by File Name").Image("Search").Click
	SearchResult=Mid(Browser("Search by Collection").Page("Search by File Name").WebTable("Search Criteria").GetCellData(3,2),116,8)
	If SearchResult = "You must" Then
		Call Save_Data("Search item by blank Filename", "Pass")
	else
		Call Save_Data("Search item by blank Filename", "Fail")
	end if
End Sub


'******************************************************************Add reference tables data**********************************************************
Sub AddRefTables

'Add new country
	
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Countries").Click
	Browser("QA / TEST Home Page").Page("Countries").Image("Add a new Country type").Click
   Browser("QA / TEST Home Page").Page("Create Country").WebEdit("countryName").Set(CountryName)
	Browser("QA / TEST Home Page").Page("Create Country").WebEdit("isoCountryCode").Set(CountryCode)
	Browser("QA / TEST Home Page").Page("Create Country").WebCheckBox("activeFlag").Set("ON")
	Browser("QA / TEST Home Page").Page("Create Country").Image("Save Country Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Country").WebElement("This form has errors,").Exist (0) Then
		Browser("QA / TEST Home Page").Page("Create Country").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Country","Pass")
	Else
    Browser("QA / TEST Home Page").Page("Countries").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add country")
	'msgbox pos1
	pos2=instr(RefValidate,ucase(CountryName))
	'msgbox pos2
    If pos1>0 and pos2>0 Then
		'msgbox "Add country pass"
		call Save_Data("Add Country","Pass")
	else
		'msgbox "Add country fail"
		call Save_Data("Add Country","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
	End If

'	Add new currency

	Browser("QA / TEST Home Page").Page("Currencies").Link("Currencies").Click
	Browser("QA / TEST Home Page").Page("Currencies").Image("Add a new compression").Click
	Browser("Create Currency").Page("Create Currency").WebEdit("currencyDescription").Set(CurrencyName)
	Browser("Create Currency").Page("Create Currency").WebEdit("currencyNewCode").Set(CurrencyCode)
	Browser("Create Currency").Page("Create Currency").Image("Save Currency Properties").Click
	If Browser("Create Currency").Page("Create Currency").WebElement("This form has errors,").Exist(0)Then
	Browser("Create Currency").Page("Create Currency").Image("Cancel and go back to").Click
	Call Save_Data("Add Duplicate Currency","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Currencies").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add Currency")
	'msgbox pos1
	pos2=instr(RefValidate,CurrencyName)
	'msgbox pos2
    If pos1>0 and pos2>0 Then
        call Save_Data("Add Currency","Pass")
	else
        call Save_Data("Add Currency","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
	End If

'Add new  Disclosure level

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Disclosure Levels").Click
	Browser("QA / TEST Home Page").Page("Disclosure Levels").Image("Add a new disclosure level").Click
    Browser("QA / TEST Home Page").Page("Create Disclosure Level").WebEdit("disclosureLevel").Set(DisclosureLevel)
	Browser("QA / TEST Home Page").Page("Create Disclosure Level").WebEdit("sortOrder").Set(DlevelSortOrder)
	Browser("QA / TEST Home Page").Page("Create Disclosure Level").WebCheckBox("usage").Set "ON"
	Browser("QA / TEST Home Page").Page("Create Disclosure Level").Image("Save Disclosure Level").Click
	If Browser("QA / TEST Home Page").Page("Create Disclosure Level").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Disclosure Level").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Disclosure level","Pass")
	Else
    Browser("QA / TEST Home Page").Page("Disclosure Levels").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add disclosure level")
	'msgbox pos1
	pos2=instr(RefValidate,DisclosureLevel)
	'msgbox pos2
    If pos1>0 and pos2>0 Then
		call Save_Data("Add Disclosure Level","Pass")
	Else
		call Save_Data("Add Disclosure Level","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add  Document type

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Document Types").Click
	Browser("QA / TEST Home Page").Page("Document Types").Image("Add a new document type").Click
    Browser("QA / TEST Home Page").Page("Create Document Type").WebEdit("documentType").Set(DocType)
	Browser("QA / TEST Home Page").Page("Create Document Type").WebCheckBox("isActive").Set "ON"
    Browser("QA / TEST Home Page").Page("Create Document Type").Image("Save Document Type Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Document Type").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Document Type").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Document type","Pass")
	Else
    Browser("QA / TEST Home Page").Page("Document Types").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add document type")
	'msgbox pos1
	pos2=instr(RefValidate,DocType)
	'msgbox pos2
    If pos1>0 and pos2>0 Then
		call Save_Data("Add Document Type","Pass")
	Else
		call Save_Data("Add Document Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'	Add  Document type_DisclosureLevels

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Document Types - Disclosure").Click
	Browser("QA / TEST Home Page").Page("Document Types - Disclosure").Image("Add a new document type").Click
    Browser("QA / TEST Home Page").Page("Create Document Type -").WebList("disclosureLevelNewOid").Select(DisclosureLevel)
	Browser("QA / TEST Home Page").Page("Create Document Type -").WebList("docTypeNewOid").Select(DocType)
	Browser("QA / TEST Home Page").Page("Create Document Type -").Image("Save Properties").Click
    If Browser("QA / TEST Home Page").Page("Create Document Type -").WebElement("This form has errors,").Exist(0) Then
		Browser("QA / TEST Home Page").Page("Create Document Type -").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Document type_Disclosure Level","Pass")
	Else
    Browser("QA / TEST Home Page").Page("Document Types - Disclosure").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new doctype disclosure")
	'msgbox pos1
     If pos1>0  Then
		call Save_Data("Add Document Type_Disclosure Level","Pass")
	Else
		call Save_Data("Add Document Type_Disclosure Level","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add Driver model

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Driver Models").Click
	Browser("QA / TEST Home Page").Page("Driver Models").Image("Add a new driver model").Click
    Browser("QA / TEST Home Page").Page("Create Driver Model").WebEdit("driverModelName").Set(DriverModel)
	Browser("QA / TEST Home Page").Page("Create Driver Model").WebCheckBox("isActive").Set "ON"
    Browser("QA / TEST Home Page").Page("Create Driver Model").Image("Save Driver Model Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Driver Model").WebElement("** This driver method").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Driver Model").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Driver model","Pass")
	Else
     Browser("QA / TEST Home Page").Page("Document Types").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add driver model")
	'msgbox pos1
	pos2=instr(RefValidate,DriverModel)
	'msgbox pos2
    If pos1>0 and pos2>0 Then
		call Save_Data("Add Driver model","Pass")
	Else
		call Save_Data("Add Driver model","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add  File type

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("File Types").Click
	Browser("QA / TEST Home Page").Page("File Types").Image("Add a new file type").Click
    Browser("QA / TEST Home Page").Page("Create File Type").WebEdit("fileType").Set(FileType)
	Browser("QA / TEST Home Page").Page("Create File Type").WebCheckBox("displayed").Set "ON"
    Browser("QA / TEST Home Page").Page("Create File Type").Image("Save File Type Properties").Click
	If Browser("QA / TEST Home Page").Page("Create File Type").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create File Type").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate File Type","Pass")
	Else
     Browser("QA / TEST Home Page").Page("File Types").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new file type")
	'msgbox pos1
	pos2=instr(RefValidate,FileType)
	'msgbox pos2
    If pos1>0 and pos2>0 Then
		call Save_Data("Add File Type","Pass")
	Else
		call Save_Data("Add File Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add Flag

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Flags").Click
	Browser("QA / TEST Home Page").Page("Flags").Image("Add a new flag").Click
    Browser("QA / TEST Home Page").Page("Create Flag").WebEdit("name").Set(Flag)
	Browser("QA / TEST Home Page").Page("Create Flag").WebEdit("description").Set("test flag")
	Browser("QA / TEST Home Page").Page("Create Flag").WebCheckBox("value:=COLLECTION").Set "ON"
	Browser("QA / TEST Home Page").Page("Create Flag").WebCheckBox("value:=ITEM").Set "ON"
    Browser("QA / TEST Home Page").Page("Create Flag").Image("Save Flag Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Flag").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Flag").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Flag","Pass")
	Else
     Browser("QA / TEST Home Page").Page("Flags").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new flag")
	'msgbox pos1
	pos2=instr(RefValidate,FileType)
	'msgbox pos2
    If pos1>0 and pos2>0 Then
		call Save_Data("Add Flag","Pass")
	Else
		call Save_Data("Add Flag","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add Install format

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Install Formats").Click
	Browser("QA / TEST Home Page").Page("Install Format").Image("Add a new install format").Click
    Browser("QA / TEST Home Page").Page("Create Install Format").WebEdit("installFormat").Set(InstallFormat)
    Browser("QA / TEST Home Page").Page("Create Install Format").WebEdit("description").Set("test install format")
	Browser("QA / TEST Home Page").Page("Create Install Format").WebCheckBox("isActive").Set "ON"
    Browser("QA / TEST Home Page").Page("Create Install Format").Image("Save Install Format Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Install Format").WebElement("** This install format").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Install Format").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Install format","Pass")
	Else
     Browser("QA / TEST Home Page").Page("Install Format").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new install format")
	'msgbox pos1
	pos2=instr(RefValidate,InstallFormat)
	'msgbox pos2
    If pos1>0 and pos2>0 Then
		call Save_Data("Add install format","Pass")
	Else
		call Save_Data("Add install format","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add Language

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Languages").Click
	Browser("QA / TEST Home Page").Page("Languages").Image("Add a new language").Click
    Browser("QA / TEST Home Page").Page("Create Language").WebEdit("SOARLanguage").Set(Language)
    Browser("QA / TEST Home Page").Page("Create Language").WebEdit("localLanguage").Set(Language)
	Browser("QA / TEST Home Page").Page("Create Language").WebEdit("languageIso").Set("KN")
    Browser("QA / TEST Home Page").Page("Create Language").Image("Save Language Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Language").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Language").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Language","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Languages").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new language")
	'msgbox pos1
	pos2=instr(RefValidate,Language)
	'msgbox pos2
    If pos1>0 and pos2>0 Then
		call Save_Data("Add Language","Pass")
	Else
		call Save_Data("Add Language","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If


'Add Language_country mapping

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Language-Countries").Click
	Browser("QA / TEST Home Page").Page("Languages-Country").Image("Add a new Country-language").Click
    Browser("QA / TEST Home Page").Page("Create Country-Language").WebList("countryNewOid").Select("INDIA")
    Browser("QA / TEST Home Page").Page("Create Country-Language").WebList("languageNewOid").Select(Language)
    Browser("QA / TEST Home Page").Page("Create Country-Language").Image("Save Country-Language").Click
	If Browser("QA / TEST Home Page").Page("Create Country-Language").WebElement("**This language-country").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Country-Language").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Country_Language","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Languages-Country").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new language country map")
	'msgbox pos1
      If pos1>0 Then
		call Save_Data("Add Country_Language mapping","Pass")
	Else
		call Save_Data("Add Country_Language mapping","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'	Add media type

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Media Types").Click
	Browser("QA / TEST Home Page").Page("Media Types").Image("Add a new media type").Click
    Browser("QA / TEST Home Page").Page("Create Media Type").WebEdit("mediaType").Set(MediaType)
    Browser("QA / TEST Home Page").Page("Create Media Type").Image("Save Media Type Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Media Type").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Media Type").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate media type","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Media Types").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new media type")
	'msgbox pos1
	pos2=instr(RefValidate,MediaType)
'	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Media Type","Pass")
	Else
		call Save_Data("Add Media Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add Order link

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Order Links").Click
	Browser("QA / TEST Home Page").Page("Order Links").Image("Add a new Order Link").Click
    Browser("QA / TEST Home Page").Page("Create Order Link").WebEdit("orderLinkDescription").Set(OrderLink)
    Browser("QA / TEST Home Page").Page("Create Order Link").WebEdit("orderLinkURL").Set(OrderLinkURL)
	Browser("QA / TEST Home Page").Page("Create Order Link").Image("Save Order Link Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Order Link").WebElement("**This order Link already").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Order Link").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Order Link","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Order Links").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new order link")
	'msgbox pos1
	pos2=instr(RefValidate,OrderLinkURL)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Order Link","Pass")
	Else
		call Save_Data("Add Order Link","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add price type

	 Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Price Types").Click
	Browser("QA / TEST Home Page").Page("Price Types").Image("Add a new price type").Click
    Browser("QA / TEST Home Page").Page("Create Price Type").WebEdit("priceType").Set(PriceType)
	Browser("QA / TEST Home Page").Page("Create Price Type").WebEdit("priceDefault").Set("5")
    Browser("QA / TEST Home Page").Page("Create Price Type").WebList("currencyCodeDefault").Select("Indian Rupee")
	Browser("QA / TEST Home Page").Page("Create Price Type").Image("Save Price Type Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Price Type").WebElement("** This price type already").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Price Type").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate price type","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Price Types").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new price type")
	'msgbox pos1
	pos2=instr(RefValidate,PriceType)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Price Type","Pass")
	Else
		call Save_Data("Add Price Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add  project name

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Project Names").Click
	Browser("QA / TEST Home Page").Page("ProjectNames").Image("Add a new compression").Click
    Browser("QA / TEST Home Page").Page("Create Project Name").WebEdit("projectName").Set(ProjectName)
	Browser("QA / TEST Home Page").Page("Create Project Name").WebEdit("projectDescription").Set("test project")
    Browser("QA / TEST Home Page").Page("Create Project Name").Image("Save Project Name Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Project Name").WebElement("** This project already").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Project Name").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Project name","Pass")
	Else
	Browser("QA / TEST Home Page").Page("ProjectNames").Image("View Event Log").Click
	RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new project name")
	'msgbox pos1
	pos2=instr(RefValidate,ProjectName)
'	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Project Name","Pass")
	Else
		call Save_Data("Add Project Name","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add Region

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Regions").Click
	Browser("QA / TEST Home Page").Page("Regions").Image("Add a new region").Click
    Browser("QA / TEST Home Page").Page("Create Region").WebEdit("regionNewOid").Set(Region)
    Browser("QA / TEST Home Page").Page("Create Region").WebEdit("regionDescription").Set(Region)
    Browser("QA / TEST Home Page").Page("Create Region").Image("Save Region Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Region").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Region").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Region","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Regions").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new region")
	'msgbox pos1
	pos2=instr(RefValidate,Region)
'	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Region","Pass")
	Else
		call Save_Data("Add Region","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add  Region_Language

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Regions-Languages").Click
	Browser("QA / TEST Home Page").Page("Regions-Languages").Image("Add a new region-language").Click
    Browser("QA / TEST Home Page").Page("Create Region-Language").WebList("languageNewOid").Select(Language)
    Browser("QA / TEST Home Page").Page("Create Region-Language").WebList("regionNewOid").Select(Region)
    Browser("QA / TEST Home Page").Page("Create Region-Language").Image("Save Region-Language").Click
	If Browser("QA / TEST Home Page").Page("Create Region-Language").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Region-Language").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Region_Language","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Regions-Languages").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new region language")
	'msgbox pos1
     If pos1>0 Then
		call Save_Data("Add Region_Language","Pass")
	Else
		call Save_Data("Add Region_Language","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add  Relationship type

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Relationship Types").Click
	Browser("QA / TEST Home Page").Page("Relationship Types").Image("Add a new relationship").Click
    Browser("QA / TEST Home Page").Page("Create Relationship Type").WebEdit("relationshipType").Set(RelationShipType)
    Browser("QA / TEST Home Page").Page("Create Relationship Type").Image("Save Relationship Type").Click
	If Browser("QA / TEST Home Page").Page("Create Relationship Type").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Relationship Type").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Relationship Type","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Relationship Types").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new relationship type")
	'msgbox pos1
	pos2=instr(RefValidate,RelationShipType)
'	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add RelationShip Type","Pass")
	Else
		call Save_Data("Add RelationShip Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
    End If

'Add Server

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Server Names").Click
	Browser("QA / TEST Home Page").Page("Server Names").Image("Add a new server name").Click
    Browser("QA / TEST Home Page").Page("Create Server Name").WebEdit("serverName").Set(ServerName)
    Browser("QA / TEST Home Page").Page("Create Server Name").WebEdit("protocol").Set(Protocol)
	Browser("QA / TEST Home Page").Page("Create Server Name").WebEdit("description").Set("test server")
	Browser("QA / TEST Home Page").Page("Create Server Name").WebCheckBox("displayOnUI").Set "ON"
    Browser("QA / TEST Home Page").Page("Create Server Name").Image("Save Server Name Properties").Click
	Browser("QA / TEST Home Page").Page("Server Names").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new server feed")
	'msgbox pos1
	pos2=instr(RefValidate,ServerName)
'	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Server Name","Pass")
	Else
		call Save_Data("Add Server Name","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click

'	Add severities

	 Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Severities").Click
	Browser("QA / TEST Home Page").Page("Severities").Image("Add a new severity").Click
    Browser("QA / TEST Home Page").Page("Create Severity").WebEdit("severity").Set(Severity)
    Browser("QA / TEST Home Page").Page("Create Severity").WebEdit("description").Set("test severity")
	Browser("QA / TEST Home Page").Page("Create Severity").WebCheckBox("isActive").Set "ON"
    Browser("QA / TEST Home Page").Page("Create Severity").Image("Save Severity Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Severity").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Severity").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Severity","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Severities").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new severities")
	'msgbox pos1
	pos2=instr(RefValidate,Severity)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Severity","Pass")
	Else
		call Save_Data("Add Severity","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
   End If

'Add Software Type

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Software Types").Click
	Browser("QA / TEST Home Page").Page("Software Types").Image("Add a new software type").Click
    Browser("QA / TEST Home Page").Page("Create Software Type").WebEdit("softwareType").Set(SoftwareType)
    Browser("QA / TEST Home Page").Page("Create Software Type").WebEdit("softwareTypeHoverText").Set("Test software type")
    Browser("QA / TEST Home Page").Page("Create Software Type").Image("Save Software Type Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Software Type").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Software Type").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Software Type","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Software Types").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new sw type")
	'msgbox pos1
	pos2=instr(RefValidate,SoftwareType)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Software Type","Pass")
	Else
		call Save_Data("Add Software Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
   End If

'Add Software sub type

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Software SubTypes").Click
	Browser("QA / TEST Home Page").Page("Software SubTypes").Image("Add a new software type").Click
    Browser("QA / TEST Home Page").Page("Create Software SubType").WebEdit("softwareSubType").Set(SoftwareSubType)
    Browser("QA / TEST Home Page").Page("Create Software SubType").WebEdit("softwareSubTypeHoverText").Set("Test Software SubType")
    Browser("QA / TEST Home Page").Page("Create Software SubType").Image("Save Software SubType").Click
	If Browser("QA / TEST Home Page").Page("Create Software SubType").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Software SubType").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Software Sub Type","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Software Types").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new sw sub type")
	'msgbox pos1
	pos2=instr(RefValidate,SoftwareSubType)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Software Sub Type","Pass")
	Else
		call Save_Data("Add Software Sub Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
   End If

'   Add SoftwareType_Software SubType mapping

    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Software Type - SubTypes").Click
	Browser("QA / TEST Home Page").Page("Software Type-Software").Image("Add a new software type-sub-ty").Click
    Browser("QA / TEST Home Page").Page("Create Software Type-Subtype").WebList("softwareSubTypeNewOid").Select(SoftwareSubType)
    Browser("QA / TEST Home Page").Page("Create Software Type-Subtype").WebList("softwareTypeNewOid").Select(SoftwareType)
    Browser("QA / TEST Home Page").Page("Create Software Type-Subtype").Image("Save Software Type-Subtype").Click
	If Browser("QA / TEST Home Page").Page("Create Software Type-Subtype").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Software Type-Subtype").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Software Type_Software Sub Type","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Software Type-Software").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new sw type - sw sub type map")
	'msgbox pos1
     If pos1>0 Then
		call Save_Data("Add Software Type_Software Sub Type","Pass")
	Else
		call Save_Data("Add Software Type_Software Sub Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
   End If

'Add  Submittal Priorities

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Submittal Priorities").Click
	Browser("QA / TEST Home Page").Page("Submittal Priorities").Image("Add a new submittal priority").Click
    Browser("QA / TEST Home Page").Page("Create Submittal Priority").WebEdit("priorityLabel").Set(SubmittalPriority)
    Browser("QA / TEST Home Page").Page("Create Submittal Priority").Image("Save Submittal Priority").Click
	If Browser("QA / TEST Home Page").Page("Create Submittal Priority").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Submittal Priority").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Submittal Priority","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Submittal Priorities").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new submittal priorities")
	'msgbox pos1
	pos2=instr(RefValidate,SubmittalPriority)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Submittal Priority","Pass")
	Else
		call Save_Data("Add Submittal Priority","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
   End If

'Add  Submittal types

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Submittal Types").Click
	Browser("QA / TEST Home Page").Page("Submittal Types").Image("Add a new submittal type").Click
    Browser("QA / TEST Home Page").Page("Create Submittal Type").WebEdit("submittalType").Set(SubmittalType)
    Browser("QA / TEST Home Page").Page("Create Submittal Type").Image("Save Submittal Type Properties").Click
	If Browser("QA / TEST Home Page").Page("Create Submittal Type").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Submittal Type").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Submittal Type","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Submittal Types").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new submittal type")
	'msgbox pos1
	pos2=instr(RefValidate,SubmittalType)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Submittal Type","Pass")
	Else
		call Save_Data("Add Submittal Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
   End If

'Add Update types

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Update Types").Click
	Browser("QA / TEST Home Page").Page("Update Types").Image("Add a new update type").Click
    Browser("QA / TEST Home Page").Page("Create Update Type").WebEdit("updateType").Set(UpdateType)
	Browser("QA / TEST Home Page").Page("Create Update Type").WebCheckBox("isActive").Set "ON"
    Browser("QA / TEST Home Page").Page("Create Update Type").Image("Save Update Type Properties").Click
    If Browser("QA / TEST Home Page").Page("Create Update Type").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Update Type").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Update Type","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Update Types").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new update type")
	'msgbox pos1
	pos2=instr(RefValidate,UpdateType)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Update Type","Pass")
	Else
		call Save_Data("Add Update Type","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
   End If

'   Add Environment

   Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Environments").Click
	Browser("QA / TEST Home Page").Page("Environments").Image("Add a new environment").Click
    Browser("QA / TEST Home Page").Page("Create Environment").WebEdit("pmOId").Set("20554")
	Browser("QA / TEST Home Page").Page("Create Environment").WebEdit("environment").Set(Envmnt)
	Browser("QA / TEST Home Page").Page("Create Environment").WebEdit("environmentDetail").Set("This is sample test environment")
	Browser("QA / TEST Home Page").Page("Create Environment").WebEdit("environmentDetailShort").Set("test")
	Browser("QA / TEST Home Page").Page("Create Environment").WebEdit("availableDateDay").Set("20")
	Browser("QA / TEST Home Page").Page("Create Environment").WebEdit("availableDateMonth").Set("09")
	Browser("QA / TEST Home Page").Page("Create Environment").WebEdit("availableDateYear").Set("2012")
	Browser("QA / TEST Home Page").Page("Create Environment").WebCheckBox("usage").Set "ON"
	Browser("QA / TEST Home Page").Page("Create Environment").WebCheckBox("active").Set "ON"
    Browser("QA / TEST Home Page").Page("Create Environment").Image("Save Environment Properties").Click
    If Browser("QA / TEST Home Page").Page("Create Environment").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Environment").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Environment","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Environments").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add environment")
	'msgbox pos1
	pos2=instr(RefValidate,Envmnt)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Environment","Pass")
	Else
		call Save_Data("Add Environment","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
   End If

'Add Fulfillment method

	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Fulfillment Methods").Click
	Browser("QA / TEST Home Page").Page("Fulfillment Methods").Image("Add a new fulfillment").Click
    Browser("QA / TEST Home Page").Page("Create Fulfillment Method").WebEdit("fulfillmentMethod").Set(FulfillmentMethod)
    Browser("QA / TEST Home Page").Page("Create Fulfillment Method").Image("Save Fulfillment Method").Click
    If Browser("QA / TEST Home Page").Page("Create Fulfillment Method").WebElement("This form has errors,").Exist(0) then
		Browser("QA / TEST Home Page").Page("Create Fulfillment Method").Image("Cancel and go back to").Click
		Call Save_Data("Add Duplicate Fulfillment Method","Pass")
	Else
	Browser("QA / TEST Home Page").Page("Fulfillment Methods").Image("View Event Log").Click
    RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
	pos1=instr(RefValidate,"Add new FulfillmentMethod")
	'msgbox pos1
	pos2=instr(RefValidate,FulfillmentMethod)
	'msgbox pos2
      If pos1>0 and pos2>0Then
		call Save_Data("Add Fulfillment Method","Pass")
	Else
		call Save_Data("Add Fulfillment Method","Fail")
	End If
	Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
   End If

End Sub


'***************************************************************Edit reference tables data**********************************************************
Sub EditRefTables

'Edit country

	NewRefName=CountryName+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Countries").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
   For i=2 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),CountryName,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,6,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Country").WebEdit("countryName").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Country").Image("Save Country Properties").Click
		Browser("QA / TEST Home Page").Page("Countries").Image("View Event Log").Click
		countryValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(countryValidate,"Update country")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
        If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
               call Save_Data("Update Country","Pass")
                else
               call Save_Data("Update Country","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Currency

	NewRefName=CurrencyName+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Currencies").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),CurrencyName,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,4,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Currency").WebEdit("currencyDescription").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Currency").Image("Save Currency Properties").Click
		Browser("QA / TEST Home Page").Page("Currencies").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update Currency")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                call Save_Data("Update Currency","Pass")
                else
               call Save_Data("Update Currency","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Disclosure level

	NewRefName=DisclosureLevel+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Disclosure Levels").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
    For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
    If strcomp(trim(TitleOld),DisclosureLevel,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,6,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Disclosure Level").WebEdit("disclosureLevel").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Disclosure Level").Image("Save Disclosure Level").Click
		Browser("QA / TEST Home Page").Page("Disclosure Levels").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update disclosure level")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		 If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                'msgbox "Update country pass"
				call Save_Data("Update Disclosure Level","Pass")
                else
				'msgbox "Update country fail"
				call Save_Data("Update Disclosure Level","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Document types

	NewRefName=DocType+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Document Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),DocType,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,6,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Document Type").WebEdit("documentType").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Document Type").Image("Save Document Type Properties").Click
		Browser("QA / TEST Home Page").Page("Document Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update document type")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                call Save_Data("Update Document Type","Pass")
                else
				call Save_Data("Update Document Type","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit  Document Type_Disclosure Level

	NewRefName="Public"
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Document Types - Disclosure").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),DocType+RefEdit,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,7,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Document Type - Disclosur").WebList("disclosureLevelNewOid").Select(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Document Type - Disclosur").Image("Save Properties").Click
		Browser("QA / TEST Home Page").Page("Document Types - Disclosure").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update doctype disclosure level")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
       If pos1>0 Then
                call Save_Data("Update Document Type_Disclosure Level","Pass")
		Else
				call Save_Data("Update Document Type_Disclosure Level","Fail")
      End if
		Exit For
		End If
		Next

'Edit  Driver Model

	NewRefName=DriverModel+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Driver Models").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),DriverModel,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,5,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Driver Model").WebEdit("driverModelName").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Driver Model").Image("Save Driver Model Properties").Click
		Browser("QA / TEST Home Page").Page("Driver Models").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update driver model")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Driver Model","Pass")
                else
				call Save_Data("Update Driver Model","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit FileType

	NewRefName=FileType+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("File Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),FileType,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,8,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit File Type").WebEdit("fileType").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit File Type").Image("Save File Type Properties").Click
		Browser("QA / TEST Home Page").Page("File Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update file type")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update File Type","Pass")
                else
				call Save_Data("Update File Type","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Flag

	NewRefName=Flag+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Flags").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),Flag,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,7,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Flag").WebEdit("name").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Flag").Image("Save Flag Properties").Click
		Browser("QA / TEST Home Page").Page("Flags").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update flag")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Flag","Pass")
                else
				call Save_Data("Update Flag","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Fulfillment methods

	NewRefName=FulfillmentMethod+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Fulfillment Methods").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),FulfillmentMethod,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,4,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Fulfillment Method").WebEdit("fulfillmentMethod").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Fulfillment Method").Image("Save Fulfillment Method").Click
		Browser("QA / TEST Home Page").Page("Fulfillment Methods").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update FulfillmentMethod")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Fulfillment Method","Pass")
                else
				call Save_Data("Update Fulfillment Method","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit  Install formats

	NewRefName=InstallFormat+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Install Formats").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),InstallFormat,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,5,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Install Format").WebEdit("installFormat").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Install Format").Image("Save Install Format Properties").Click
		Browser("QA / TEST Home Page").Page("Install Format").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update install format")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Install Format ","Pass")
                else
				call Save_Data("Update Install Format ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Language

	NewRefName=Language+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Languages").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),Language,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,8,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Language").WebEdit("SOARLanguage").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Language").Image("Save Language Properties").Click
		Browser("QA / TEST Home Page").Page("Languages").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update language")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Language ","Pass")
                else
				call Save_Data("Update Language ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit  Language_Country

	NewRefName=Language+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Language-Countries").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
    For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,5)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,6,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Country-Language").WebList("countryNewOid").Select("NEPAL")
		Browser("QA / TEST Home Page").Page("Edit Country-Language").Image("Save Country-Language").Click
		Browser("QA / TEST Home Page").Page("Languages-Country").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update language country map")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		If pos1>0 Then
                 call Save_Data("Update Language_Country ","Pass")
         Else
				call Save_Data("Update Language_Country ","Fail")
         End if
		Exit For
		End If
		Next

'Edit  media types

	NewRefName=MediaType+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Media Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),MediaType,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,4,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Media Type").WebEdit("mediaType").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Media Type").Image("Save Media Type Properties").Click
		Browser("QA / TEST Home Page").Page("Media Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update media type")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Media Type ","Pass")
                else
				call Save_Data("Update Media Type ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Order Link

	NewRefName=OrderLink+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Order Links").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),OrderLink,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,5,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Order Link").WebEdit("orderLinkDescription").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Order Link").Image("Save Order Link Properties").Click
		Browser("QA / TEST Home Page").Page("Order Links").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update order link")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update order link","Pass")
                else
				call Save_Data("Update order link ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit  Price types

	NewRefName=PriceType+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Price Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),PriceType,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,6,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Price Type").WebEdit("priceType").Set(Cstr(NewRefName))
		Browser("QA / TEST Home Page").Page("Edit Price Type").Image("Save Price Type Properties").Click
		Browser("QA / TEST Home Page").Page("Price Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update price type")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Price Type ","Pass")
                else
				call Save_Data("Update Price Type ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit  project name

	NewRefName=ProjectName+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Project Names").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),ProjectName,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,5,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Project Name").WebEdit("projectName").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Project Name").Image("Save Project Name Properties").Click
		Browser("QA / TEST Home Page").Page("ProjectNames").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update project name")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update  Project Name","Pass")
                else
				call Save_Data("Update Project Name ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Region

	NewRefName=Region+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Regions").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),Region,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,4,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Region").WebEdit("regionNewOid").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Region").Image("Save Region Properties").Click
		Browser("QA / TEST Home Page").Page("Regions").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update region")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Region","Pass")
                else
				call Save_Data("Update Region ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Region_Language

	NewRefName="Hindi"
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Regions-Languages").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),Region+RefEdit,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,6,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Region-Language").WebList("languageNewOid").Select(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Region-Language").Image("Save Region-Language").Click
		Browser("QA / TEST Home Page").Page("Regions-Languages").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update region language")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
       If pos1>0 Then
                call Save_Data("Update Region_Language","Pass")
		Else
				call Save_Data("Update Region_language","Fail")
      End if
		Exit For
		End If
		Next

'Edit Relationship Type

	NewRefName=RelationShipType+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Relationship Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),RelationShipType,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,4,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Relationship Type").WebEdit("relationshipType").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Relationship Type").Image("Save Relationship Type").Click
		Browser("QA / TEST Home Page").Page("Relationship Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update relationship type")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Relationship type","Pass")
                else
				call Save_Data("Update Relationship type ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit  server name

	NewRefName=ServerName+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Server Names").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),ServerName,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,9,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Server Name").WebEdit("serverName").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Server Name").Image("Save Server Name Properties").Click
		Browser("QA / TEST Home Page").Page("Server Names").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update server feed")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Server Name","Pass")
                else
				call Save_Data("Update Server Name ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Severity

	NewRefName=Severity+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Severities").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),Severity,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,5,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Severity").WebEdit("severity").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Severity").Image("Save Severity Properties").Click
		Browser("QA / TEST Home Page").Page("Severities").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update severities")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Severity","Pass")
                else
				call Save_Data("Update Severity ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit  software type

	NewRefName=SoftwareType+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Software Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),SoftwareType,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,5,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Software Type").WebEdit("softwareType").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Software Type").Image("Save Software Type Properties").Click
		Browser("QA / TEST Home Page").Page("Software Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update sw type")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Software Type","Pass")
                else
				call Save_Data("Update Software Type ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Software subtype

	NewRefName=SoftwareSubType+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Software SubTypes").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),SoftwareSubType,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,5,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Software SubType").WebEdit("softwareSubType").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Software SubType").Image("Save Software SubType").Click
		Browser("QA / TEST Home Page").Page("Software SubTypes").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update sw sub type")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Software Sub Type","Pass")
                else
				call Save_Data("Update Software Sub Type","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit  Software Type_Software Sub Type

NewRefName="DVD"
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Software Type - SubTypes").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),SoftwareType+RefEdit,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,6,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Software Type-Subtype").WebList("softwareSubTypeNewOid").Select(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Software Type-Subtype").Image("Save Software Type-Subtype").Click
		Browser("QA / TEST Home Page").Page("Software Type-Software").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update sw type - sw sub type map")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
       If pos1>0 Then
                call Save_Data("Update Software Type_Software Sub Type","Pass")
		Else
				call Save_Data("Update Software Type_Software Sub Type","Fail")
      End if
		Exit For
		End If
		Next

'Edit Submittal Priority

	NewRefName=SubmittalPriority+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Submittal Priorities").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),SubmittalPriority,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,4,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Submittal Priority").WebEdit("priorityLabel").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Submittal Priority").Image("Save Submittal Priority").Click
		Browser("QA / TEST Home Page").Page("Submittal Priorities").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update submittal priorities")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Submittal Priority","Pass")
                else
				call Save_Data("Update Submittal Priority ","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit  Submittal Type

	NewRefName=SubmittalType+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Submittal Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),SubmittalType,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,4,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Submittal Type").WebEdit("submittalType").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Submittal Type").Image("Save Submittal Type Properties").Click
		Browser("QA / TEST Home Page").Page("Submittal Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update submittal type")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Submittal Type","Pass")
                else
				call Save_Data("Update Submittal Type","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Update Type

	NewRefName=UpdateType+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Update Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),UpdateType,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,5,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Update Type").WebEdit("updateType").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Update Type").Image("Save Update Type Properties").Click
		Browser("QA / TEST Home Page").Page("Update Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"update the update type")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update the Update Type","Pass")
                else
				call Save_Data("Update the Update Type","Fail")
				End if
			End if
		Exit For
		End If
		Next

'Edit Environment

	NewRefName=Envmnt+RefEdit
    Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Environments").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + Cstr(rows)
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
	'msgbox TitleOld
	If strcomp(trim(TitleOld),Envmnt,1) = 0Then
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		set colobj=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").ChildItem(i,9,"Image",0)
		colobj.click
        Browser("QA / TEST Home Page").Page("Edit Environment").WebEdit("environment").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Environment").WebEdit("environmentDetailShort").Set(NewRefName)
		Browser("QA / TEST Home Page").Page("Edit Environment").Image("Save Environment Properties").Click
		Browser("QA / TEST Home Page").Page("Environments").Image("View Event Log").Click
        RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		pos1=instr(RefValidate,"Update environment")
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefNameUpd1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
		'msgbox RefNameUpd1
			If pos1>0 Then
				If strcomp(trim(RefNameUpd1),NewRefName,1) = 0 Then
                 call Save_Data("Update Environment","Pass")
                else
				call Save_Data("Update Environment","Fail")
				End if
			End if
		Exit For
		End If
		Next


End Sub

'***************************************************************Delete reference tables data**********************************************************
Sub DeleteRefTables


'Remove country

	NewRefName=CountryName+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Countries").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=2 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		'Msgbox "cell" +Cstr( i) + ","+"3" +": "+TitleOld
		Browser("QA / TEST Home Page").Page("Countries").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Countries").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Countries").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete country")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
        	'msgbox "Delete country pass"
			call Save_Data("Delete Country","Fail")
			Else
			'msgbox "Delete country fail"
			call Save_Data("Delete Country","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove currency

	NewRefName=CurrencyName+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Currencies").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
        RefId1=trim(RefId1)
        Browser("QA / TEST Home Page").Page("Currencies").WebCheckBox("value:="&RefId1).Click
        Browser("QA / TEST Home Page").Page("Currencies").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Currencies").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete Currency")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Currency","Fail")
			Else
            call Save_Data("Delete Currency","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove DocumentType_DisclosureLevel

	NewRefName=DocType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Document Types - Disclosure").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		'Browser("QA / TEST Home Page").Page("Document Types - Disclosure").WebCheckBox("value:="&RefId1).Click
        Refid2=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
        Browser("QA / TEST Home Page").Page("Document Types - Disclosure").WebCheckBox("value:="&(trim(RefId1)+","+trim(RefId2))).Click
		Browser("QA / TEST Home Page").Page("Document Types - Disclosure").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Document Types - Disclosure").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete doctype disclosure level")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete DocumentType_DisclosureLevel","Fail")
			Else
            call Save_Data("Delete  DocumentType_DisclosureLevel","Pass")
			End If
		End If
	Exit For
	End If
	Next
	
'Remove Disclosure Level

	NewRefName=DisclosureLevel+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Disclosure Levels").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Disclosure Levels").WebCheckBox("value:="&RefId1).Click
        Browser("QA / TEST Home Page").Page("Disclosure Levels").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Disclosure Levels").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete disclosure level")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete DisclosureLevel","Fail")
			Else
            call Save_Data("Delete DisclosureLevel","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Document Type

	NewRefName=DocType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Document Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Document Types").WebCheckBox("value:="&RefId1).Click
       	Browser("QA / TEST Home Page").Page("Document Types").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Document Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete document type")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete DocumentType","Fail")
			Else
            call Save_Data("Delete  DocumentType","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Driver model

	NewRefName=DriverModel+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Driver Models").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Driver Models").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Driver Models").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Driver Models").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete driver model")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete  driver model ","Fail")
			Else
            call Save_Data("Delete  driver model ","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove environment

	NewRefName=Envmnt+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Environments").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Environments").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Environments").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Environments").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
		pos1=instr(RefValidate,"Delete environment")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete  Environment ","Fail")
			Else
            call Save_Data("Delete  Environment ","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove FileTypes

	NewRefName=FileType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("File Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("File Types").WebCheckBox("value:="&RefId1).Click
        Browser("QA / TEST Home Page").Page("File Types").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("File Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete file type")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete file type ","Fail")
			Else
            call Save_Data("Delete file type ","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Flags

	NewRefName=Flag+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Flags").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Flags").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Flags").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Flags").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete flag")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete flag ","Fail")
			Else
            call Save_Data("Delete flag ","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Install Format

NewRefName=InstallFormat+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Install Formats").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("install Format").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Install Format").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Install Format").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete install format")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Install Format ","Fail")
			Else
            call Save_Data("Delete Install Format ","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Language_Country

	NewRefName=Language+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Language-Countries").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,5)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Refid2=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
		Browser("QA / TEST Home Page").Page("Languages-Country").WebCheckBox("value:="&(trim(RefId1)+","+trim(RefId2))).Click
		'Browser("QA / TEST Home Page").Page("install Format").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Languages-Country").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Languages-Country").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,5)
		pos1=instr(RefValidate,"Delete lang country map")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Language_Country ","Fail")
			Else
            call Save_Data("Delete Language_Country ","Pass")
			End If
		End If
	Exit For
	End If
	Next

'	Remove Region_Language

	NewRefName=Region+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Regions-Languages").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Refid2=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
		Browser("QA / TEST Home Page").Page("Regions-Languages").WebCheckBox("value:="&(trim(RefId1)+","+trim(RefId2))).Click
		'Browser("QA / TEST Home Page").Page("ProjectNames").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Regions-Languages").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Regions-Languages").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		'msgbox RefName
		pos1=instr(RefValidate,"Delete region language")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Region_Language","Fail")
			Else
            call Save_Data("Delete Region_Language","Pass")
			End If
		End If
	Exit For
	End If
	Next


'Remove Language

	NewRefName=Language+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Languages").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Languages").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Languages").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Languages").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete language")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Language","Fail")
			Else
            call Save_Data("Delete Language","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Media Type

	NewRefName=MediaType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Media Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
       Browser("QA / TEST Home Page").Page("Media Types").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Media Types").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Media Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete media type")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Media type","Fail")
			Else
            call Save_Data("Delete Media type","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove order link

	NewRefName=OrderLink+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Order Links").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
'	msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
        Browser("QA / TEST Home Page").Page("Order Links").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Order Links").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Order Links").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
        pos1=instr(RefValidate,"Delete order link")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Order Links","Fail")
			Else
            call Save_Data("Delete Order Links","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Price Type

	NewRefName=PriceType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Price Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Price Types").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Price Types").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Price Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
        pos1=instr(RefValidate,"Delete price type")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Price Types","Fail")
			Else
            call Save_Data("Delete Price Types","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Project Name

	NewRefName=ProjectName+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Project Names").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("ProjectNames").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("ProjectNames").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("ProjectNames").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
        pos1=instr(RefValidate,"Delete project name")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Project name","Fail")
			Else
            call Save_Data("Delete Project name","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Region

	NewRefName=Region+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Regions").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
    	Browser("QA / TEST Home Page").Page("Regions").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Regions").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Regions").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
        pos1=instr(RefValidate,"Delete region")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Region","Fail")
			Else
            call Save_Data("Delete Region","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Relationship Type

	NewRefName=RelationShipType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Relationship Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
        Browser("QA / TEST Home Page").Page("Relationship Types").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Relationship Types").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Relationship Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
        pos1=instr(RefValidate,"Delete relationship type")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Relationship type","Fail")
			Else
            call Save_Data("Delete Relationship type","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Server Name

	NewRefName=ServerName+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Server Names").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
        Browser("QA / TEST Home Page").Page("Server Names").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Server Names").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Server Names").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
        pos1=instr(RefValidate,"Delete server feed")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Server Name","Fail")
			Else
            call Save_Data("Delete Server Name","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Severity

	NewRefName=Severity+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Severities").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Severities").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Severities").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Severities").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
        pos1=instr(RefValidate,"Delete severities")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Severity","Fail")
			Else
            call Save_Data("Delete Severity","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Software Type_Software Sub Type

	NewRefName=SoftwareType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Software Type - SubTypes").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Refid2=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,4)
		Browser("QA / TEST Home Page").Page("Software Type-Software").WebCheckBox("value:="&(trim(RefId1)+","+trim(RefId2))).Click
       Browser("QA / TEST Home Page").Page("Software Type-Software").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Software Type-Software").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
       pos1=instr(RefValidate,"Delete sw type - sw sub type map")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Software Type_Software SubType","Fail")
			Else
            call Save_Data("Delete Software Type_Software SubType","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Software Type

	NewRefName=SoftwareType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Software Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Software Types").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Software Types").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Software Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete sw type")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Software Type","Fail")
			Else
            call Save_Data("Delete Software Type","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Software SubType

	NewRefName=SoftwareSubType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Software SubTypes").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Software SubTypes").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Software SubTypes").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Software SubTypes").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete sw sub type")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Software SubType","Fail")
			Else
            call Save_Data("Delete Software SubType","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Submittal  Priority

	NewRefName=SubmittalPriority+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Submittal Priorities").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Submittal Priorities").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Submittal Priorities").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Submittal Priorities").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete submittal priorities")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Submittal priorities","Fail")
			Else
            call Save_Data("Delete Submittal priorities","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Submittal Type

	NewRefName=SubmittalType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Submittal Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Submittal Types").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Submittal Types").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Submittal Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
		pos1=instr(RefValidate,"Delete submittal type")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Submittal type","Fail")
			Else
            call Save_Data("Delete submittal type","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove UpdateType

	NewRefName=UpdateType+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Update Types").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Update Types").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Update Types").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Update Types").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
        pos1=instr(RefValidate,"Delete update types")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Update type","Fail")
			Else
            call Save_Data("Delete Update type","Pass")
			End If
		End If
	Exit For
	End If
	Next

'Remove Fulfillment method

	NewRefName=FulfillmentMethod+RefEdit
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Fulfillment Methods").Click
    rows=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").RowCount
	'msgbox "rows:" + rows
	For i=3 to rows
	TitleOld=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
	If strcomp(trim(TitleOld),NewRefName,1) = 0Then
		RefId1=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,2)
		Browser("QA / TEST Home Page").Page("Fulfillment Methods").WebCheckBox("value:="&RefId1).Click
		Browser("QA / TEST Home Page").Page("Fulfillment Methods").Image("Delete selected records").Click
        Browser("QA / TEST Home Page").Page("Fulfillment Methods").Image("View Event Log").Click
		RefValidate=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(2,6)
		Browser("QA / TEST Home Page").Page("View Event Log").Image("Go back to previous page.").Click
		RefName=Browser("QA / TEST Home Page").Page("View Event Log").WebTable("Event Id").GetCellData(i,3)
        pos1=instr(RefValidate,"Delete FulfillmentMethod")
		If pos1>0 Then
			If strcomp(trim(RefName),NewRefName,1) = 0 Then
            call Save_Data("Delete Fulfillment method","Fail")
			Else
            call Save_Data("Delete Fulfillment method","Pass")
			End If
		End If
	Exit For
	End If
	Next


End Sub

 
Sub ColnSearchUsingID

'CollectionID="col63136"

   Browser("View Collection").Page("View Collection").Link("by Collection ID").Click
	Browser("View Collection").Page("View Collection").Sync
	Browser("Search by Collection").Page("Search by Collection").WebEdit("collectionId").Set(CollectionID)
	Browser("Search by Collection").Page("Search by Collection").Image("Search_2").Click
	Browser("Welcome To SOAR").Page("Collection Search Results").Sync

	If  Browser("Welcome To SOAR").Page("Collection Search Results").Link("text:="&CollectionID).Exist Then
    	Browser("Welcome To SOAR").Page("Collection Search Results").WebTable("name:="&CollectionID).childItem(1,1,"Link",0).click
	else
			isValidColn="false"
	End If

End Sub

Sub ColnMultipleItemsSearchUsingID

'CollectionID="col60172"

   Browser("View Collection").Page("View Collection").Link("by Collection ID").Click
	Browser("View Collection").Page("View Collection").Sync
	Browser("Search by Collection").Page("Search by Collection").WebEdit("collectionId").Set(CollectionID)
	Browser("Search by Collection").Page("Search by Collection").Image("Search_2").Click
	Browser("Welcome To SOAR").Page("Collection Search Results").Sync

	If  Browser("Welcome To SOAR").Page("Collection Search Results").Link("text:="&CollectionID).Exist Then
		itemCount=Browser("Welcome To SOAR").Page("Collection Search Results").WebTable("name:="&CollectionID).GetCellData(1,3)
		itemCount=mid(itemCount,1,1)
'		msgbox itemCount
	Browser("Welcome To SOAR").Page("Collection Search Results").WebTable("name:="&CollectionID).childitem(1,3,"Link",0).click
		For i=1 to itemCount
			ItemId=Browser("Welcome To SOAR").Page("Collection Search Results").WebTable("innerhtml:=<!-- many.*").GetCellData(i,2)
			itemPos=Instr(ItemId,":")
			itemIds(i)=Mid(ItemId,1,itemPos-1)
'			msgbox itemIds(i)

		Next

    	Browser("Welcome To SOAR").Page("Collection Search Results").WebTable("name:="&CollectionID).childItem(1,1,"Link",0).click
		
		
	else
			isValidColn="false"
	End If

End Sub

'*******************************************************Read collection and item properties***********************************************
Sub ReadColnPropsNew

   Set mySheet5=myFile1.Worksheets(1)
   rownum=2

'   	CollectionID="col60172"
colnPropsNotFound=""
TotalItemEngAttachCount=0
nonEngAttachCount=0
TotalAttachCount=0


'Read collection properties

	Browser("Welcome To SOAR").Page("View Collection").Sync
	rowVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The state of this collection.").RowCount
	

	For p=1 to rowVal
		colVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The state of this collection.").ColumnCount(p)
		For q=1 to colVal

			propVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The state of this collection.").GetCellData(p,q)
			'msgbox propVal
			If q=1 Then
				mySheet5.cells(rownum,1)=propVal
				else
				mySheet5.cells(rownum,2)=propVal
			End If
	
		Next
		rownum=rownum+1

	Next

	rowVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The properties specified").RowCount

	For p=1 to rowVal
		colVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The properties specified").ColumnCount(p)
		For q=1 to colVal

			propVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The properties specified").GetCellData(p,q)
			'msgbox propVal
				If q=1 Then
					mySheet5.cells(rownum,1)=propVal
				else
					mySheet5.cells(rownum,2)=propVal
			End If

			If  Instr(mySheet5.cells(rownum,1),"Flags")>0 Then
				mySheet5.cells(rownum,2)=""
                        Rc=Browser("Welcome To SOAR").Page("View Collection_2").WebTable("innertext:=BlackMagic.*").RowCount
							For i=1 to Rc
								collectionFlagImage=  Browser("Welcome To SOAR").Page("View Collection_2").WebTable("innertext:=BlackMagic.*").ChildItem(i,2,"Image",0).GetROProperty("file name")
							  If  collectionFlagImage="blue_isvalid.gif" Then
								  collectionFlagVal=Browser("Welcome To SOAR").Page("View Collection_2").WebTable("innertext:=BlackMagic.*").GetCellData(i,1)
								   mySheet5.cells(rownum,2)=trim(collectionFlagVal)&", "&mySheet5.cells(rownum,2)
							  End If
                            Next
							
				End If
		Next
rownum=rownum+1
	Next

'**************************Find out collection level eng atttachments****************************************
        		
				set docLinks=description.Create
				docLinks("MicClass").value="Link"
                	Set colnEngdocs= Browser("Welcome To SOAR").Page("View Collection").WebTable("innertext:=You can create or upload.*").ChildObjects(docLinks)
				colnEngAttachCount=colnEngdocs.count
                				
			'msgbox "coln level eng doc count:"&colnEngAttachCount



	'open item page

	For x=1 to itemCount
		itemPropsNotFound=""

'	colVal=Browser("Welcome To SOAR").Page("View Collection_2").WebTable("name:=ObjectIds").ColumnCount(1)
'    Browser("Welcome To SOAR").Page("View Collection_2").WebTable("name:=ObjectIds").ChildItem(1,colVal,"Link",0).click
'	If isNewCollection = "true" Then
		'msgbox itemIds(x)
'		Browser("Welcome To SOAR").Page("View Collection_5").Link("name:="&itemIds(x)).Click
'	else
	Browser("Welcome To SOAR").Page("View Collection_3").Link("text:="&itemIds(x)).Click
'	End If

	'read item properties
	Set  mySheet5=myFile1.Worksheets(2)
	rownum=2

	Browser("Welcome To SOAR").Page("View Software Item").Sync
	rowVal=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=The details of this software.*").RowCount
	

	For p=1 to rowVal
		colVal=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=The details of this software.*").ColumnCount(p)
		For q=1 to colVal

			propVal=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=The details of this software.*").GetCellData(p,q)
			'msgbox propVal
			If q=1 Then
				If x=1  Then
                		mySheet5.cells(rownum,1)=propVal
				End If
				else
				mySheet5.cells(rownum,x+1)=propVal
			End If

			If  Instr(mySheet5.cells(rownum,1),"Flags")>0 Then
				mySheet5.cells(rownum,x+1)=""
                        Rc=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=Basic.*").RowCount
							For i=1 to Rc
								collectionFlagImage=  Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=Basic.*").ChildItem(i,2,"Image",0).GetROProperty("file name")
							  If  collectionFlagImage="blue_isvalid.gif" Then
								  collectionFlagVal=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=Basic.*").GetCellData(i,1)
								   mySheet5.cells(rownum,x+1)=trim(collectionFlagVal)&", "&mySheet5.cells(rownum,x+1)
							  End If
                            Next
							
				End If
	
		Next
		rownum=rownum+1

	Next

'**************************Read item level eng attachments***********************************
    
			set docLinks=description.Create
				docLinks("MicClass").value="Link"

				Set itemEngdocs=Browser("View Collection_4").Page("View Software Item").WebTable("innertext:=You can create or upload.*").ChildObjects(docLinks)
				itemEngAttachCount=itemEngdocs.count
                				
			'msgbox "item level eng doc count:"&itemEngAttachCount
			

		'msgbox itemEngAttachCount
		TotalItemEngAttachCount=TotalItemEngAttachCount+itemEngAttachCount

		Browser("View Software Item").Page("View Software Item").Image("View The Collection").Click
		Browser("Welcome To SOAR").Page("View Collection_3").Sync
	

Next

		'msgbox "Total item level eng docs count:"&TotalItemEngAttachCount

'Read non english docs

		'Browser("Welcome To SOAR").Page("View Software Item_2").Image("View Non-English Documents").Click
		Browser("View Collection").Page("View Collection_3").Image("View Non-English Documents").Click
		Browser("Title:=Non-English Documents.*").Page("title:=Non-English Documents").Sync
		Wait(2)
		Browser("Title:=Non-English Documents.*").Page("title:=Non-English Documents").Sync

        Set d=Description.Create
		d("micclass").value="WebTable"
		Set a=Browser("Title:=Non-English Documents.*").Page("title:=Non-English Documents").Frame("name:=currentTranslation").ChildObjects(d)

		For i=1 to a.count-1
            		For j=1 to a(i).RowCount
						If a(i).columnCount(j)=4 Then
							If a(i).ChildItemCount(j,4,"Image")=0 Then nonEngAttachCount=nonEngAttachCount+1
						'msgbox "Image not found:"&i&","&j
						End If
                Next
			Next
		'Msgbox "count of non eng attachments"&nonEngAttachCount
		Browser("Welcome To SOAR").Page("Non-English Documents").Image("View Collection").Click
		TotalAttachCount=colnEngAttachCount + TotalItemEngAttachCount+nonEngAttachCount

		'msgbox "all attchments count:"&TotalAttachCount


Set mySheet5=Nothing

End Sub


Sub CheckPropsInCdspNew

   mySheet6.cells(cdspResultRowVal,9)=Date&" "&Time

'   CollectionID="COL63135"

'PropsFilePath="C:\CDSPAutomation\CollectionProperties.xlsx"
'	Set objExcel=CreateObject("Excel.Application")
'	set myFile= objExcel.Workbooks.Open (PropsFilePath)
Set  mySheet5=myFile1.Worksheets(1)
	  myFile1.Worksheets(1).activate

       var=""
	   While var = ""
	   
        objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/soar/content/"&Lcase(CollectionID), False 
  
		objHttp.Send
		var = objHttp.ResponseText
     	Wend

		cdspContent=Instr(var,"doesn't exist")

		If cdspContent = 0 Then
        		
'		msgbox var
'		 print var

  'Check for existance of collection properties in CDSP
propFound="false"
propPos=0
propCount=0

'msgbox  mySheet5.usedrange.rows.count

For i=2 to mySheet5.usedrange.rows.count

	'msgbox  mySheet5.cells(i,1)

	If  mySheet5.cells(i,1) ="Submittal Type" Then
				colnType= mySheet5.cells(i,2)
'				msgbox colnType
			End If

validProp="false"


	For each  elem in  propNeedCheckList

		If  trim(mysheet5.cells(i,1)) = elem  Then
			'msgbox  "Inside for each loop"&mySheet5.cells(i,1)
			validProp="true"
			Exit for
		End If

	Next

	If validProp = "true" Then

			propVal=mySheet5.cells(i,2)

            'msgbox propVal

			If propVal <> "" Then

				'msgbox mySheet5.cells(i,1)

				If  mySheet5.cells(i,1) = "SOAR Business Lead" Then

                    pos=Instr(mySheet5.cells(i,2),"(")
					pos=pos+1
                    propVal=Mid(mySheet5.cells(i,2),pos)
					propLen=Len(propVal)
					'msgbox propLen
					propLen=propLen-2
					'msgbox propLen
					propVal=Mid(propVal,1,propLen)
					'msgbox propVal
					CheckProp
					pos=Instr(mySheet5.cells(i,2),"(")
					pos=pos-2
                    propVal=Mid(mySheet5.cells(i,2),1,pos)
					'msgbox propVal
					CheckProp
					elseif mySheet5.cells(i,1) = "SOAR Collection Contact" Then
					 pos=Instr(mySheet5.cells(i,2),"(")
					pos=pos+1
                    propVal=Mid(mySheet5.cells(i,2),pos)
					propLen=Len(propVal)
					'msgbox propLen
					propLen=propLen-2
					'msgbox propLen
					propVal=Mid(propVal,1,propLen)
					'msgbox propVal
					CheckProp
					pos=Instr(mySheet5.cells(i,2),"(")
					pos=pos-2
                    propVal=Mid(mySheet5.cells(i,2),1,pos)
					'msgbox propVal
					CheckProp
					elseif mySheet5.cells(i,1) = "Update Types" or trim(mySheet5.cells(i,1)) = "Flags" Then
					startPos=1
					mySheet5.cells(i,2)=mySheet5.cells(i,2)&","
					pos=Instr(mySheet5.cells(i,2),",")
					propPos=pos
					propPos=propPos-1
					'msgbox pos
					Do
						propVal=mid(mySheet5.cells(i,2),startPos,propPos)
						'msgbox propVal
						CheckProp
						startPos=pos+2
					pos=Instr(startPos,mySheet5.cells(i,2),",")
					propPos=pos-(startPos-1)
					propPos=propPos-1
					'msgbox  pos
					'msgbox propPos
					Loop while pos <> 0
                    				
					else
					CheckProp
				End If
       	
			End If

	End If

      
Next

	mySheet6.cells(cdspResultRowVal,1)=CollectionID
	mySheet6.cells(cdspResultRowVal,2)=colnType

		If propCount > 0 Then
'			msgbox "Some collection properties are missing in CDSP"
			mySheet6.cells(cdspResultRowVal,3)="No"
			
'			For j=0 to propCount
'
'                propsNotMatched=propsNotMatched&","&propNFList(j)
'			Next
'			msgbox propsNotMatched
			mySheet6.cells(cdspResultRowVal,4)=colnPropsNotFound
		else
'			msgbox "All collection properties are displayed in CDSP"
			mySheet6.cells(cdspResultRowVal,3)="Yes"
		end if
		


'check for item properties

Set  mySheet5=myFile1.Worksheets(2)
	  myFile1.Worksheets(2).activate
'	  Set objFSO=CreateObject("Scripting.FileSystemObject")

'
For x=1 to itemCount

		propFound="false"
		propPos=0
		propCount=0

	itemPos=Instr(var,itemIds(x))
	itemVar=mid(var,itemPos)
' ' How to write file
'outFile="c:\cdspautomation\output.txt"
'Set objFile = objFSO.CreateTextFile(outFile,True)
'objFile.Write itemvar
'objFile.Close

	  For i=2 to mySheet5.usedrange.rows.count

	validProp="false"

	'msgbox "property in newfunction:"&mySheet5.cells(i,1)

		For each  elem in  propItemNeedCheckList

			If  trim(mysheet5.cells(i,1)) = elem  Then
				'msgbox "inside loop:"&mysheet5.cells(i,1)
				validProp="true"
				Exit for
			End If

		Next

	If validProp = "true" Then

			propVal=mySheet5.cells(i,x+1)

'			msgbox propVal

			If propVal <> "" Then

				Select Case trim(mySheet5.cells(i,1))

					Case "Suspended"

						'tagString="<SPAN class=t>is-suspended</SPAN><SPAN class=m>&gt;</SPAN><SPAN class=tx>"
						tagString="<is-suspended>"
						propVal=tagString&trim(mySheet5.cells(i,x+1))
'						 msgbox propVal
						CheckItemProp

					Case "Version"

'						tagString="<SPAN class=t>version</SPAN><SPAN class=m>&gt;</SPAN><SPAN class=tx>"
						tagString="<version>"
						propVal=tagString&trim(mySheet5.cells(i,x+1))
'						 msgbox propVal
						CheckItemProp

					Case "Price"

						tagString=".00"
						propVal=trim(mySheet5.cells(i,x+1))&tagString
'						 msgbox propVal
						CheckItemProp

					Case "Item-ID"
						propVal=trim(mySheet5.cells(i,x+1))
						'msgbox propVal
						supercedeItemLoc=Instr(propVal,"superseded")
						If  supercedeItemLoc > 0 Then
                    		'msgbox supercedeItemLoc
						ItemIDLen=supercedeItemLoc-2
						'msgbox "ItemIDLen="&ItemIDLen
						propVal=Mid(propVal,1,ItemIDLen)
						'msgbox propVal
						End If
						CheckItemProp


						Case  "Update Types" 
							       ExtractMultipleItemVal
						Case "Flags"
							ExtractMultipleItemVal
						Case "Media Types"
							ExtractMultipleItemVal
						Case "Install Formats"
							ExtractMultipleItemVal
						Case "Environments"
							ExtractMultipleItemVal
						Case "Languages"
							ExtractMultipleItemVal

					Case else
						CheckItemProp


				End Select

'				If  mySheet5.cells(i,1) = "Suspended" Then
'
'                    tagString="<SPAN class=t>is-suspended</SPAN><SPAN class=m>&gt;</SPAN><SPAN class=tx>"
'                     propVal=tagString&trim(mySheet5.cells(i,2))
'                    msgbox propVal
'					CheckItemProp
'                    else
'					CheckItemProp
'				End If
       	
			End If

	End If

      
Next


	propsNotMatched=""

'	msgbox "propCount:"& propCount
		mySheet6.cells(cdspResultRowVal,5)=itemIds(x)

		If propCount > 0 Then
'			msgbox "Some item properties are missing in CDSP"
			mySheet6.cells(cdspResultRowVal,6)="No"
'			For j=0 to propCount
''				msgbox propItemNFList(j)
'				propsNotMatched=propsNotMatched&","&propNFList(j)
'			Next
			mySheet6.cells(cdspResultRowVal,7)=itemPropsNotFound
'			msgbox itemPropsNotFound
'			msgbox propsNotMatched
            else
'			msgbox "All item properties are displayed in CDSP"
			mySheet6.cells(cdspResultRowVal,6)="Yes"
		end if

		If itemCount > 1 Then
			cdspResultRowVal=cdspResultRowVal+1
		End If

		Next

		else

'		msgbox "collection not reached CDSP"
		mySheet6.cells(cdspResultRowVal,1)=CollectionID
		mySheet6.cells(cdspResultRowVal,8)="collection not reached CDSP"
		
End If


End Sub

Sub ExtractMultipleItemVal

							startPos=1
							mySheet5.cells(i,x+1)=mySheet5.cells(i,x+1)&","
							pos=Instr(mySheet5.cells(i,x+1),",")
							propPos=pos
							propPos=propPos-1
							'msgbox pos
							Do
								propVal=mid(mySheet5.cells(i,x+1),startPos,propPos)
'								msgbox propVal
								propBracketPos=Instr(propVal,"(")
								If  propBracketPos > 0 Then
									propVal=mid(propVal,1,propBracketPos-1)
                                End If
								CheckItemProp
								startPos=pos+2
								pos=Instr(startPos,mySheet5.cells(i,x+1),",")
								propPos=pos-(startPos-1)
								propPos=propPos-1
								'msgbox  pos
								'msgbox propPos
							Loop while pos <> 0       

End Sub          	

Sub CompareAttachmentCount

   'CollectionID="col60017"
   countDigits=0

   Set objHttp = CreateObject("Msxml2.ServerXMLHTTP")

				var=""
				While var = ""
	   					objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/soar/content/"&Lcase(CollectionID)&"/*", False 
  						objHttp.Send
						var = objHttp.ResponseText
     			Wend
				'print var
			countPos=Instr(var,"count")
			countPos=countPos+7
			countPos1=countPos
			'msgbox "count pos:"&countPos
			Do
			countVal=mid(var,countPos1,1)
			'msgbox countVal
			countPos1=countPos1+1
			countDigits=countDigits+1
			Loop while countVal <> """"

			countDigits=countDigits-1
			CDSPCountVal= mid(var,countPos,countDigits)
			'msgbox "cdsp count val:"&CDSPCountVal

			mySheet6.cells(cdspResultRowVal,10)=TotalAttachCount
			mySheet6.cells(cdspResultRowVal,11)=CDSPCountVal

			If Cstr(TotalAttachCount) =  Cstr(CDSPCountVal) Then
				'msgbox "attachment count matched"
				mySheet6.cells(cdspResultRowVal,12)="Yes"
				else
				'msgbox "attachment count did not match"
				mySheet6.cells(cdspResultRowVal,12)="No"
			End If

End Sub

'********************************read top collections from CDSP to excel sheet

Sub RetreiveCDSPCollection
   
'Set Xl=CreateObject("Excel.application")
'Set wb=Xl.workbooks.open("C:\CDSPAutomation\CDSPDataFile.xlsx")
'Set ws1=wb.worksheets(1)

Set objHttp = CreateObject("Msxml2.ServerXMLHTTP")

Set objFileToWrite = CreateObject("Scripting.FileSystemObject").OpenTextFile("C:\CDSPAutomation\SoarCollectiondata.txt",2,true)

limitVal=mySheet7.cells(2,3)

var=""
	While var = ""
			objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/soar/content/*?limit="&limitVal, False 
  			objHttp.Send
			var = objHttp.ResponseText
     Wend
   				
objFileToWrite.WriteLine(var)

'MsgBox "Copied soar collections records to text file"

objFileToWrite.Close
Set objFileToWrite = Nothing


'***************************Extract collection ID to excel sheet******************************



Set objFileToRead = CreateObject("Scripting.FileSystemObject")
set fil=objFileToRead.opentextfile("C:\CDSPAutomation\SoarCollectiondata.txt",1,true)

j=2

CdspData=fil.ReadAll
fil.close

startPos=1
IDPosn=8
SubscriptionName="content/"
EndPos=Len(CdspData)
'msgbox EndPos

Do 
CollectionPos=instr(startPos,CdspData,SubscriptionName)

CollectionLen=0
'msgbox "collection position" & CollectionPos

	If CollectionPos = 0 Then
		Exit Do
	Else
		CollectionPos=CollectionPos+ IDPosn
		CollectionCharPos=CollectionPos
		Do
		CollectionChar=mid(CdspData,CollectionCharPos,1)
'		msgbox CollectionChar
		CollectionCharPos=CollectionCharPos +1
		CollectionLen=CollectionLen + 1
		Loop While(CollectionChar <> """" )
	End If
	CollectionLen=CollectionLen - 1
'	msgbox "Length of collection ID" & CollectionLen
Extract=mid(CdspData,CollectionPos,CollectionLen)
'msgbox Extract
startPos=CollectionPos+CollectionLen
'msgbox "StartPosition" & StartPos
'ws1.cells(j,1)="http://cdsplus-itg.houston.hp.com/cadence/app/productsetup/gpp_201/"+Extract
mySheet7.cells(j,4)=Extract
 j=j+1
Loop while StartPos<EndPos
myFile3.save

'MsgBox "done"


Set objFileToRead = Nothing

End Sub


Sub CheckProp
		 
        		   propPos=Instr(var,trim(propVal))
'				   msgbox propVal
'				   msgbox var
				If propPos = 0 Then
'					propNFList(propCount)=mySheet5.cells(i,1)
					colnPropsNotFound=mySheet5.cells(i,1)&","&colnPropsNotFound
					propCount=propCount + 1
				End if
End Sub

Sub CheckItemProp
		 
        		   propPos=Instr(itemVar,trim(propVal))
                   '  msgbox var
				If propPos = 0 Then
'					msgbox propVal
'					msgbox "value of i=" & i
'					msgbox mySheet5.cells(i,1)
'					propItemNFList(propCount)=mySheet5.cells(i,1)
					itemPropsNotFound=mySheet5.cells(i,1)&","&itemPropsNotFound
											propCount=propCount + 1
'                    
				End if
End Sub

Sub CheckForExistingCollections

   For rcount=2 to mySheet7.usedrange.rows.count
'CollectionID="col63134"
isValidColn = "true"

		If  mySheet7.cells(rcount,2) <> "" Then

			CollectionID=mySheet7.cells(rcount,2)
'			ColnSearchUsingID
			ColnMultipleItemsSearchUsingID

			If isValidColn = "true" Then
'				ReadColnProps
'				CheckPropsInCdsp

				ReadColnPropsNew
                CheckPropsInCdspNew
				CompareAttachmentCount
				
				
			else
			mySheet6.cells(cdspResultRowVal,1)=CollectionID
				mySheet6.cells(cdspResultRowVal,7)="Collection does not exist"
				
			End If
	cdspResultRowVal=cdspResultRowVal+1
		End If

Next

End Sub

Sub CheckForTopCollections

   For rcount=2 to mySheet7.usedrange.rows.count

isValidColn = "true"

		If  mySheet7.cells(rcount,4) <> "" Then

			CollectionID=mySheet7.cells(rcount,4)
'			ColnSearchUsingID
			ColnMultipleItemsSearchUsingID

			If isValidColn = "true" Then

				ReadColnPropsNew
                CheckPropsInCdspNew
				CompareAttachmentCount
				
				
			else
			mySheet6.cells(cdspResultRowVal,1)=CollectionID
				mySheet6.cells(cdspResultRowVal,7)="Collection does not exist"
				
			End If
	cdspResultRowVal=cdspResultRowVal+1
		End If

Next

End Sub

Sub CheckForNewCollections

   For rcount=2 to mySheet7.usedrange.rows.count
isValidColn = "true"
'CollectionID="col63134"
	If  mySheet7.cells(rcount,1) <> "" Then

		CollectionID=mySheet7.cells(rcount,1)
'		ColnSearchUsingID
		ColnMultipleItemsSearchUsingID
		

			If isValidColn = "true" Then
				isNewCollection="true"
				ReadColnPropsNew
				CheckPropsInCdspNew
				CompareAttachmentCount
				
				
			else
			mySheet6.cells(cdspResultRowVal,1)=CollectionID
				mySheet6.cells(cdspResultRowVal,7)="Collection does not exist"
				
			End If
		cdspResultRowVal=cdspResultRowVal+1
	End If

Next

End Sub

Sub CreateCollections

    '     msgbox "Total no of rows in data sheet:" & Cstr(TotalRows)
'	 For i=1 to 17
		For j=2 to 3 'TotalDataRows
		 DSRowNum=j
        	For k=1 to ScenarioRows
			If objWorkSheet.cells(k,2) = "Yes" then
				Execute objWorkSheet.cells(k,1)
'				msgbox objWorkSheet.cells(k,1)
			End if
		 Next
	 Next
''	Next

End Sub

'****************************************************************** Logoff  from SOARUI****************************************************************
Sub Logoff
	Browser("QA / TEST Home Page").Page("QA / TEST Home Page").Link("Disconnect").Click
	logoutMsg=Browser("Logout").Page("Logout").WebElement("You have Successfully").GetTOProperty("innertext")
	If logoutMsg = "You have Successfully logged off......" Then
		Call Save_Data("Logout", "Pass")
	else
		Call Save_Data("Logout","Fail")
	End If
End Sub

'***********************************************************************Duplicate functions**********************************************************************************

'*******************************************************Read collection and item properties*************************************************

Sub ReadColnProps

   Set mySheet5=myFile1.Worksheets(1)
   rownum=2

'   	CollectionID="col63134"
colnPropsNotFound=""

'Read collection properties
	rowVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The state of this collection.").RowCount
	

	For p=1 to rowVal
		colVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The state of this collection.").ColumnCount(p)
		For q=1 to colVal

			propVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The state of this collection.").GetCellData(p,q)
			'msgbox propVal
			If q=1 Then
				mySheet5.cells(rownum,1)=propVal
				else
				mySheet5.cells(rownum,2)=propVal
			End If
	
		Next
		rownum=rownum+1

	Next

	rowVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The properties specified").RowCount

	For p=1 to rowVal
		colVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The properties specified").ColumnCount(p)
		For q=1 to colVal

			propVal=Browser("Welcome To SOAR").Page("View Collection").WebTable("The properties specified").GetCellData(p,q)
			'msgbox propVal
				If q=1 Then
					mySheet5.cells(rownum,1)=propVal
				else
					mySheet5.cells(rownum,2)=propVal
			End If

			If  Instr(mySheet5.cells(rownum,1),"Flags")>0 Then
				mySheet5.cells(rownum,2)=""
                        Rc=Browser("Welcome To SOAR").Page("View Collection_2").WebTable("innertext:=Basic.*").RowCount
							For i=1 to Rc
								collectionFlagImage=  Browser("Welcome To SOAR").Page("View Collection_2").WebTable("innertext:=Basic.*").ChildItem(i,2,"Image",0).GetROProperty("file name")
							  If  collectionFlagImage="blue_isvalid.gif" Then
								  collectionFlagVal=Browser("Welcome To SOAR").Page("View Collection_2").WebTable("innertext:=Basic.*").GetCellData(i,1)
								   mySheet5.cells(rownum,2)=collectionFlagVal&","&mySheet5.cells(rownum,2)
							  End If
                            Next
							
				End If
		Next
rownum=rownum+1
	Next

	'open item page

	
itemPropsNotFound=""

	colVal=Browser("Welcome To SOAR").Page("View Collection_2").WebTable("name:=ObjectIds").ColumnCount(1)
    Browser("Welcome To SOAR").Page("View Collection_2").WebTable("name:=ObjectIds").ChildItem(1,colVal,"Link",0).click

	'read item properties
	Set  mySheet5=myFile1.Worksheets(2)
	rownum=2

	rowVal=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=The details of this software.*").RowCount
	

	For p=1 to rowVal
		colVal=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=The details of this software.*").ColumnCount(p)
		For q=1 to colVal

			propVal=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=The details of this software.*").GetCellData(p,q)
			'msgbox propVal
			If q=1 Then
				mySheet5.cells(rownum,1)=propVal
				else
				mySheet5.cells(rownum,2)=propVal
			End If

			If  Instr(mySheet5.cells(rownum,1),"Flags")>0 Then
				mySheet5.cells(rownum,2)=""
                        Rc=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=Basic.*").RowCount
							For i=1 to Rc
								collectionFlagImage=  Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=Basic.*").ChildItem(i,2,"Image",0).GetROProperty("file name")
							  If  collectionFlagImage="blue_isvalid.gif" Then
								  collectionFlagVal=Browser("Welcome To SOAR").Page("View Software Item").WebTable("innertext:=Basic.*").GetCellData(i,1)
								   mySheet5.cells(rownum,2)=collectionFlagVal&","&mySheet5.cells(rownum,2)
							  End If
                            Next
							
				End If
	
		Next
		rownum=rownum+1

	Next

Set mySheet5=Nothing
'	myFile1.Save
'	myFile1.Close


End Sub

'************************************************Check collection and item properties in CDSP UI************************************************

Sub CheckPropsInCdsp

'   CollectionID="COL63135"

'PropsFilePath="C:\CDSPAutomation\CollectionProperties.xlsx"
'	Set objExcel=CreateObject("Excel.Application")
'	set myFile= objExcel.Workbooks.Open (PropsFilePath)
Set  mySheet5=myFile1.Worksheets(1)
	  myFile1.Worksheets(1).activate


'    Set objIE=createobject("internetexplorer.application")
'                objIE.visible = True
'                objIE.TheaterMode = False 
'                objIE.AddressBar = True
'                objIE.StatusBar = False
'                objIE.MenuBar = True
'                objIE.FullScreen = False 
'                objIE.Navigate "about:blank"
'
'                objIE.Navigate  "http://cdsplus-itg.houston.hp.com/cadence/app/soar/content/"&Lcase(CollectionID)
'
'	Do Until objIE.ReadyState = 4
'       wait(5)
'     Loop

'    	 var= objIE.Document.Body.outerHTML
'	   	cdspPageVal=Instr(var,"collection-ID")
'
'	 Do Until cdspPageVal <> 0
'		 objIE.Navigate  "http://cdsplus-itg.houston.hp.com/cadence/app/soar/content/"&Lcase(CollectionID)
'		objIE.Refresh
'			Do Until objIE.ReadyState = 4
'			wait(10)
'			Loop
'     	 var= objIE.Document.Body.outerHTML
'		cdspPageVal=Instr(var,"collection-ID")
'	Loop


       var=""
	   While var = ""
	   
        objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/soar/content/"&Lcase(CollectionID), False 
  
		objHttp.Send
		var = objHttp.ResponseText
     	Wend

		cdspContent=Instr(var,"doesn't exist")

		If cdspContent = 0 Then
        		
'		msgbox var
'		 print var

  'Check for existance of collection properties in CDSP
propFound="false"
propPos=0
propCount=0

'msgbox  mySheet5.usedrange.rows.count

For i=2 to mySheet5.usedrange.rows.count

'	msgbox  mySheet5.cells(i,1)

	If  mySheet5.cells(i,1) ="Submittal Type" Then
				colnType= mySheet5.cells(i,2)
'				msgbox colnType
			End If

validProp="false"

	For each  elem in  propNeedCheckList

		If  mysheet5.cells(i,1) = elem  Then
			validProp="true"
			Exit for
		End If

	Next

	If validProp = "true" Then

			propVal=mySheet5.cells(i,2)

            'msgbox propVal

			If propVal <> "" Then

				If  mySheet5.cells(i,1) = "SOAR Business Lead" Then

                    pos=Instr(mySheet5.cells(i,2),"(")
					pos=pos+1
                    propVal=Mid(mySheet5.cells(i,2),pos)
					propLen=Len(propVal)
					'msgbox propLen
					propLen=propLen-2
					'msgbox propLen
					propVal=Mid(propVal,1,propLen)
					'msgbox propVal
					CheckProp
					pos=Instr(mySheet5.cells(i,2),"(")
					pos=pos-2
                    propVal=Mid(mySheet5.cells(i,2),1,pos)
					'msgbox propVal
					CheckProp
					elseif mySheet5.cells(i,1) = "SOAR Collection Contact" Then
					 pos=Instr(mySheet5.cells(i,2),"(")
					pos=pos+1
                    propVal=Mid(mySheet5.cells(i,2),pos)
					propLen=Len(propVal)
					'msgbox propLen
					propLen=propLen-2
					'msgbox propLen
					propVal=Mid(propVal,1,propLen)
					'msgbox propVal
					CheckProp
					pos=Instr(mySheet5.cells(i,2),"(")
					pos=pos-2
                    propVal=Mid(mySheet5.cells(i,2),1,pos)
					'msgbox propVal
					CheckProp
					
					else
					CheckProp
				End If
       	
			End If

	End If

	

      
Next

	mySheet6.cells(cdspResultRowVal,1)=CollectionID
	mySheet6.cells(cdspResultRowVal,2)=colnType

		If propCount > 0 Then
'			msgbox "Some collection properties are missing in CDSP"
			mySheet6.cells(cdspResultRowVal,3)="No"
			
'			For j=0 to propCount
'
'                propsNotMatched=propsNotMatched&","&propNFList(j)
'			Next
'			msgbox propsNotMatched
			mySheet6.cells(cdspResultRowVal,4)=colnPropsNotFound
		else
'			msgbox "All collection properties are displayed in CDSP"
			mySheet6.cells(cdspResultRowVal,3)="Yes"
		end if
		


'check for item properties

Set  mySheet5=myFile1.Worksheets(2)
	  myFile1.Worksheets(2).activate
'	  Set objFSO=CreateObject("Scripting.FileSystemObject")

'


propFound="false"
propPos=0
propCount=0

itemPos=Instr(var,"software-item")
itemVar=mid(var,itemPos)
' ' How to write file
'outFile="c:\cdspautomation\output.txt"
'Set objFile = objFSO.CreateTextFile(outFile,True)
'objFile.Write itemvar
'objFile.Close

	  For i=2 to mySheet5.usedrange.rows.count

validProp="false"

	msgbox "property"&mySheet5.cells(i,1)

	For each  elem in  propItemNeedCheckList

		If  mysheet5.cells(i,1) = elem  Then
			validProp="true"
			Exit for
		End If

	Next

	If validProp = "true" Then

			propVal=mySheet5.cells(i,2)

'			msgbox propVal

			If propVal <> "" Then

				Select Case mySheet5.cells(i,1)

					Case "Suspended"

						'tagString="<SPAN class=t>is-suspended</SPAN><SPAN class=m>&gt;</SPAN><SPAN class=tx>"
						tagString="<is-suspended>"
						propVal=tagString&trim(mySheet5.cells(i,2))
'						 msgbox propVal
						CheckItemProp

					Case "Version"

'						tagString="<SPAN class=t>version</SPAN><SPAN class=m>&gt;</SPAN><SPAN class=tx>"
						tagString="<version>"
						propVal=tagString&trim(mySheet5.cells(i,2))
'						 msgbox propVal
						CheckItemProp

					Case "Price"

						tagString=".00"
						propVal=trim(mySheet5.cells(i,2))&tagString
'						 msgbox propVal
						CheckItemProp

					Case else
						CheckItemProp


				End Select

'				If  mySheet5.cells(i,1) = "Suspended" Then
'
'                    tagString="<SPAN class=t>is-suspended</SPAN><SPAN class=m>&gt;</SPAN><SPAN class=tx>"
'                     propVal=tagString&trim(mySheet5.cells(i,2))
'                    msgbox propVal
'					CheckItemProp
'                    else
'					CheckItemProp
'				End If
       	
			End If

	End If

      
Next
	propsNotMatched=""

	msgbox "propCount:"& propCount

		If propCount > 0 Then
'			msgbox "Some item properties are missing in CDSP"
			mySheet6.cells(cdspResultRowVal,5)="No"
'			For j=0 to propCount
''				msgbox propItemNFList(j)
'				propsNotMatched=propsNotMatched&","&propNFList(j)
'			Next
			mySheet6.cells(cdspResultRowVal,6)=itemPropsNotFound
'			msgbox itemPropsNotFound
'			msgbox propsNotMatched
            else
'			msgbox "All item properties are displayed in CDSP"
			mySheet6.cells(cdspResultRowVal,5)="Yes"
		end if

		else

		msgbox "collection not reached CDSP"
		mySheet6.cells(cdspResultRowVal,1)=CollectionID
		mySheet6.cells(cdspResultRowVal,7)="collection not reached CDSP"
		
End If

End Sub




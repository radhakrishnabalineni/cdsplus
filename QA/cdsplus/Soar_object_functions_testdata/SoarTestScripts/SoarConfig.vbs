'Declaring variables

Dim Login_mail,errorMsg,HPageLabel, CheckFlag,num,CollectionID,CIDLabel,Cstate,SearchResult,colobj,WshShell,itemIndex,prodVal,colnValidity,ColnValidationState
Dim itemLang,itemState,itemValidity,itemValidationState,x,y,z,submitalType,partNo,UploadFile,SoftwareFile,MD5Value,TotalDataRows
Dim CollectionTitle,CollectionTitleType,CTitle,CollectionInvalidTitle,CId,CollectionInvalidId,colnProp_Title,colnProp_Desc,colnProp_SubPriority,colnProp_SoftType,colnProp_CUtility
Dim ItemId,ValidItemId,InvalidItemId,ItemIdType,UploadStatus,UploadFailure,ItemProp_Severity,ItemProp_Version,ItemProp_PriceType,ItemProp_DRegion,ItemProp_OrderLink
Dim Filename,ValidFilename,InvalidFilename,FilenameType
Dim objExcel,objExce1, DataFilePath,myFile,myFile1,mySheet1,mySheet2,mySheet3,mySheet4,mySheet5,DriverFilePath,PropsFilePath,CDSPResultsFilePath,objExcel2,mySheet6,myFile2
Dim CDSPDataFilePath,objExcel3,myFile3,mySheet7,CDSPDataRow,objIE,cdspResultRowVal
Dim SendTo,Subject,Message,FilePath
Dim objExcelApp,objWorkBook,objWorkSheet,ScenarioRows,DSRowNum,isValidColn,isNewCollection
Dim rownum,propPos,propVal,propCount
Dim propNeedCheckList,i,var,propItemNeedCheckList,itemVar,itemCount,itemIds(5),itemPos,startPos,propBracketPos
Dim propNFList(5),propItemNFList(5),objFSO,outFile,objFile,colnType,propsNotMatched,objHttp,itemPropsNotFound,colnPropsNotFound
Dim colnEngAttachCount,TotalItemEngAttachCount,itemEngAttachCount,TotalAttachCount,nonEngAttachCount,CDSPCountVal
Dim EndPos,CollectionPos,CollectionChar,CollectionLen,CollectionCharPos

'propNeedCheckList=Array("Collection ID","SOAR Business Lead","Update Types","SOAR Collection Contact","Title","Description","Software Type","Compression Utility")
'propItemNeedCheckList=Array("Item-ID","Severity","Current State","Update Types","Suspended","Media Types","Disclosure Level","Version","Price Type","Price","Currency")
propNeedCheckList=Array("Collection ID","Update Types","Flags")
propItemNeedCheckList=Array("Item-ID","Suspended","Flags","Update Types","Media Types","Install Formats","Environments","Languages")


'Variables for reference tables
Dim RefName,CountryName,CountryCode,NewRefName,RefEdit,RefId1,RefId2,CurrencyName,CurrencyCode,RefValidate,DisclosureLevel,DlevelSortOrder,Release,DocType
Dim DriverModel,FileType,Flag,InstallFormat,Language,MediaType,PriceType,ProjectName,Region,RelationShipType,ServerName,Protocol,Severity,SoftwareType
Dim SubmittalPriority,SoftwareSubType,SubmittalType,UpdateType,Envmnt,FulfillmentMethod,OrderLink,OrderLinkURL


cdspResultRowVal=2
isValidColn="true"
CDSPDataRow=2
propsNotMatched=""
Login_mail="prashanth.hc@hp.com"
Doc_type="Blind Installation"
Lang="English (American)"  'not added in datasheet
Filename="testengdoc_Atmn6.htm"
nonEngLang="Arabic"
nonEngFilename="testNonEngdoc_ATMN1.txt"
UploadFile=Array("C:\SoarAutomation\SoarTestData\ATMN\upld.txt","C:\SoarAutomation\SoarTestData\ATMN\upld5.xsd","C:\SoarAutomation\SoarTestData\ATMN\upld2.html","C:\SoarAutomation\SoarTestData\ATMN\upld3.gif","C:\SoarAutomation\SoarTestData\ATMN\upld4.xml","C:\SoarAutomation\SoarTestData\ATMN\upld1.htm")
UploadStatus="0" 				'not added in datasheet
UploadFailure="FALSE" 	'not added in datasheet
SoftwareFile=" C:\Windows\System32\calc.exe"
MD5Value="60b7c0fead45f2066e5b805a91f4f0fc"
itemIndex="4" 'please select the index value from 1 to 10 only for selecting product
itemLang=Array("German")
colnValidationState="FALSE"
colnProp_Title="Automation test collection"
colnProp_Desc="test purpose"
colnProp_SubPriority="Medium"
colnProp_SoftType="Driver"
colnProp_CUtility="PKZip"
ItemProp_Severity="Extract"
ItemProp_Version="1.1"
ItemProp_PriceType="Special"
ItemProp_DRegion="Europe"
ItemProp_OrderLink="Cust-Care"
'submitalType="PM"
partNo="455766522" 'increase the part no value by 1 every time you add new item for phy med collection
'CollectionTitle="test_collection_ATMN"
CollectionTitle="test coln hcp"         'Assign valid collection title
CollectionInvalidTitle="test8978"
CollectionInvalidId="coln7865"
CollectionID="COL59676"               ' Assign valid collection ID
ValidItemId="LJ-5921-1"                ' Assign valid  Item ID
InvalidItemId="LP-67876-5"
ValidFilename="notepad"
InvalidFilename="noteabd"
Release="cds"         'Assign only 3 digit number
SendTo="prashanth.hc@hp.com;arun.chandrashekar@hp.com;shalini.khosla@hp.com;sanil@hp.com;Balaji.N.Viswanath@hp.com"
Subject="Automation test results for SOAR"
Message="Please find attached SOAR automation test results doc"
FilePath="C:\Results\Ref_tables.xlsx"

'Reference table declarations
CountryName="testcountry"		'Assign different value or remove this before running the script
RefEdit="New"
CountryCode="hb"							'Assign different value or remove this before running the script


CurrencyCode="PS"
CurrencyName="Dev"+Release

DisclosureLevel="Dev"+Release
DlevelSortOrder="112"

DocType="Dev"+Release

DriverModel="Dev"+Release

FileType="Dev"+Release

Flag="Dev"+Release

InstallFormat="Dev"+Release

Language="Dev"+Release

MediaType="Dev"+Release

OrderLink="Dev"+Release
OrderLinkURL="www.hp2.com" 'change this value before adding new order link

PriceType="Dev"+Release

ProjectName="Dev"+Release

Region="Dev"+Release

RelationShipType="Dev"+Release

ServerName="Dev"+Release
Protocol="http"

Severity="Dev"+Release

SoftwareType="Dev"+Release

SoftwareSubType="Dev"+Release

SubmittalPriority="Dev"+Release

SubmittalType="Dev"+Release

UpdateType="Dev"+Release

Envmnt="Dev"+Release

FulfillmentMethod="Dev"+Release

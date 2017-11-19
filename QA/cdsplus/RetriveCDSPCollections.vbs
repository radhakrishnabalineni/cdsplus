
Dim objHttp,var
Dim StartPos,EndPos,CollectionPos,CollectionChar,CollectionLen,CollectionCharPos

Set Xl=CreateObject("Excel.application")
Set wb=Xl.workbooks.open("C:\CDSPAutomation\CDSPDataFile.xlsx")
Set ws1=wb.worksheets(1)

Set objHttp = CreateObject("Msxml2.ServerXMLHTTP")

Set objFileToWrite = CreateObject("Scripting.FileSystemObject").OpenTextFile("C:\CDSPAutomation\SoarCollectiondata.txt",2,true)

limitVal=ws1.cells(2,3)

var=""
	While var = ""
			objHttp.Open "GET", "http://cdsplus-itg.houston.hp.com/cadence/app/soar/content/*?limit="&limitVal, False 
  			objHttp.Send
			var = objHttp.ResponseText
     Wend
   				
objFileToWrite.WriteLine(var)

MsgBox "Copied soar collections records to text file"

objFileToWrite.Close
Set objFileToWrite = Nothing


'***************************Extract collection ID to excel sheet******************************



Set objFileToRead = CreateObject("Scripting.FileSystemObject")
set fil=objFileToRead.opentextfile("C:\CDSPAutomation\SoarCollectiondata.txt",1,true)

j=2

CdspData=fil.ReadAll
fil.close

StartPos=1
IDPosn=8
SubscriptionName="content/"
EndPos=Len(CdspData)
'msgbox EndPos

Do 
CollectionPos=instr(StartPos,CdspData,SubscriptionName)

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
msgbox Extract
StartPos=CollectionPos+CollectionLen
'msgbox "StartPosition" & StartPos
'ws1.cells(j,1)="http://cdsplus-itg.houston.hp.com/cadence/app/productsetup/gpp_201/"+Extract
ws1.cells(j,4)=Extract
 j=j+1
Loop while StartPos<EndPos
wb.save
Xl.quit
Set Xl=Nothing
MsgBox "done"


Set objFileToRead = Nothing




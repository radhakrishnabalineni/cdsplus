Function Exract_Ids_CdspLegacy (SubscriptionName, IDPosn,SubSourceFile)

Dim StartPos,EndPos,CollectionPos,CollectionChar,CollectionLen,CollectionCharPos
Set Xl=CreateObject("Excel.application")
'Set wb=Xl.workbooks.open("C:\SubscriptionEval\test.xlsx")
Set wb=Xl.workbooks.open("C:\SubscriptionEval\test.csv")
Set ws1=wb.worksheets(1)
Set ws2=wb.worksheets(2)
Set Fso=Createobject("Scripting.filesystemobject")
'set fil=Fso.opentextfile("C:\copyIds\colnIDS1.txt",1)
set fil=Fso.opentextfile(SubSourceFile,1)

j=1

CdspData=fil.ReadAll
fil.close
Set Fso=Nothing

StartPos=1
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
'msgbox Extract
StartPos=CollectionPos+CollectionLen
'msgbox "StartPosition" & StartPos
'ws1.cells(j,1)="http://cdsplus-itg.houston.hp.com/cadence/app/productsetup/gpp_201/"+Extract
ws1.cells(j,1)=Extract
 j=j+1
Loop while StartPos<EndPos
wb.save
Xl.quit
Set Xl=Nothing
MsgBox "done"

End Function

Call Exract_Ids_CdspLegacy ("content/",8,"C:\SubscriptionEval\lib_content.txt")

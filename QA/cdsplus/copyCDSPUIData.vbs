
Dim objHttp,var,lastmodtime,pos,charVal,stringlen,origPos,lmtimePrev,strPos

lmtimePrev=""
Set objHttp = CreateObject("Msxml2.ServerXMLHTTP")

lastmodtime="1396395445489"
Set objFileToWrite = CreateObject("Scripting.FileSystemObject").OpenTextFile("C:\SubscriptionEval\lib_content.txt",8,true)
strPos=0


Do Until strPos <> 0
objHttp.Open "GET", "http://cdsplus.austin.hp.com/cadence/app/library/content/*?limit=1000&includeDeletes=true&before="&lastmodtime, False 
  
		objHttp.Send
		var = objHttp.ResponseText
		strPos=InStr(var,"count=""0""")
		'MsgBox strPos



		
objFileToWrite.WriteLine(var)

pos=InstrRev(var,"lastModified=")
'MsgBox pos
stringlen=0
charVal=""
pos=pos+14
origPos=pos
Do Until charVal = " "
charVal=Mid(var,pos,1)
'MsgBox charVal
pos=pos+1
stringlen=stringlen+1
Loop
stringlen=stringlen-2
lastmodtime=Mid(var,origPos,stringlen)
'MsgBox lastmodtime

Loop

MsgBox "done"


objFileToWrite.Close
Set objFileToWrite = Nothing
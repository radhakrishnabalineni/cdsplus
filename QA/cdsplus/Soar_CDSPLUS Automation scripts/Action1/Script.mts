

DriverFilePath="C:\SoarAutomation\SoarDriverScript\ControlFile.xlsx"
	Set objExcelApp=CreateObject("Excel.Application")
	set objWorkBook= objExcelApp.Workbooks.Open (DriverFilePath)
    Set objWorkSheet=objWorkBook.Worksheets("TestSet")
     ScenarioRows=objWorkSheet.usedrange.rows.count
'	 msgbox ScenarioRows

'open data sheets
	DataFilePath="C:\SoarAutomation\SoarTestData\SoarTestDataNew.xlsx"
	Set objExcel=CreateObject("Excel.Application")
	set myFile= objExcel.Workbooks.Open (DataFilePath)
	Set mySheet1=myFile.Worksheets("General")
	Set mySheet2=myFile.Worksheets("Collection")
	Set mySheet3=myFile.Worksheets("Item")
	Set mySheet4=myFile.Worksheets("Search")
	TotalDataRows=mySheet1.usedrange.rows.count

	PropsFilePath="C:\CDSPAutomation\CollectionProperties1.xlsx"
	Set objExcel1=CreateObject("Excel.Application")
	Set myFile1= objExcel1.Workbooks.Open (PropsFilePath)
	

	CDSPDataFilePath="C:\CDSPAutomation\CDSPDataFile.xlsx"
	Set objExcel3=CreateObject("Excel.Application")
	Set myFile3= objExcel3.Workbooks.Open (CDSPDataFilePath)
	Set mySheet7=myFile3.Worksheets(1)

	CDSPResultsFilePath="C:\CDSPAutomation\CheckPropsInCDSPResults.xlsx"
	Set objExcel2=CreateObject("Excel.Application")
	Set myFile2= objExcel2.Workbooks.Open (CDSPResultsFilePath)
	Set mySheet6=myFile2.Worksheets(1)

	rownum=2
'	msgbox TotalDataRows
'	cols=mySheet1.usedrange.columns.count
'	msgbox cols
	DSRowNum=2
	LoginSOAR

Set objHttp = CreateObject("Msxml2.ServerXMLHTTP")    

CreateCollections
RetreiveCDSPCollection

mySheet6.cells(cdspResultRowVal,1)="Existing Collections"
cdspResultRowVal=cdspResultRowVal+1
CheckForExistingCollections

mySheet6.cells(cdspResultRowVal,1)="Existing Top Collections"
cdspResultRowVal=cdspResultRowVal+1
CheckForTopCollections

mySheet6.cells(cdspResultRowVal,1)="New Collections"
cdspResultRowVal=cdspResultRowVal+1
CheckForNewCollections


	myFile1.Save
	myFile1.Close()
	myFile2.Save
	myFile2.Close()
	myFile3.Save
	myFile3.Close()
'	myFile.Close()
	
	msgbox "done"

	 Set objWorkSheet=Nothing
	 Set objWorkBook=Nothing
	 
'	
'	
	Set mySheet1=Nothing
	Set mySheet2=Nothing
	Set mySheet3=Nothing
	Set mySheet4=Nothing
	Set mySheet5=Nothing
	Set mySheet6=Nothing
	Set mySheet7=Nothing
	Set myFile=Nothing
    Set myFile1=Nothing
	Set myFile2=Nothing
	Set myFile3=Nothing
	objExcelApp.quit
     objExcel1.quit
	objExcel2.quit
	objExcel3.quit
'	objExcel.quit

	Set objExcelApp=Nothing
     Set objExcel=Nothing   
	Set objExcel1=Nothing
	Set objExcel2=Nothing
	Set objExcel3=Nothing
	Set objHttp=Nothing
	
'	objIE.Quit
'	Set objIE=Nothing

	











































































































































































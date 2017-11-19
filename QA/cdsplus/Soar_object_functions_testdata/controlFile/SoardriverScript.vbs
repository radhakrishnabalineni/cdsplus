'********************************************************************
'*                                                                  *
'* This driver script is used open qtp test and run it.             *
'*                                                                  *
'*                                                                  *
'*                                                                  *
'*                                                                  *
'********************************************************************



'**************Create a QTP Objects*********************************

Dim qtApp
Dim qtTest
Dim qtpRepositories,qtpResult


'*************Create the Application object*************************

Set qtApp = CreateObject("QuickTest.Application") 	


'*************Now Launch the QTP, allow it to be visible*************

qtApp.Launch ' Launch QuickTest
qtApp.Visible = True ' Set QuickTest to be visible
'qtApp.Visible = False


'************Set QuickTest run options*******************************

qtApp.Options.Run.ImageCaptureForTestResults = "Always"
qtApp.Options.Run.RunMode = "Fast"
qtApp.Options.Run.ViewResults = TRUE

QTPpath = "C:\SoarAutomation\SoarRegressionAutomation"


'*************Open a Script to execute the Automation test cases.****
qtApp.Open QTPpath 'True


'**************Create the Run Results Options object*****************
Set qtpResult = CreateObject("QuickTest.RunResultsOptions")


'**************Set the results location******************************
qtpResult.ResultsLocation = "C:\SoarAutomation\SoarTestResults\SoarQTPResults"


'***************Run the test and save results************************
Set qtTest = qtApp.Test
qtTest.Run qtpResult


'****************Close the test**************************************
qtTest.Close


'****************Now Close the QTP***********************************
qtApp.Quit ' Quit QuickTest


'*****************Free the object holders***************************
Set qtResultsOpt = Nothing
Set qtTest = Nothing
Set qtApp = Nothing
Set qtRepositories = Nothing
Set qtApp = Nothing
Set qtpResult=Nothing



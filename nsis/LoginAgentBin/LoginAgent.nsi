
!include "LogicLib.nsh"
!include "EnvVarUpdate.nsh"
!include "FileFunc.nsh"
; UI를 Modern 으로설정한다.
!include "MUI.nsh"
!insertmacro MUI_LANGUAGE "Korean"
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_RESERVEFILE_LANGDLL
!insertmacro Locate

Var /GLOBAL switch_overwrite
!include "MoveFileFolder.nsh"
; The name of the installer
Name "LoginAgent Installer"

; The file to write
OutFile "setup-LA.exe"

; The default installation directory
InstallDir $PROGRAMFILES\LoginAgent

; Registry key to check for directory (so if you install again, it will
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\LoginAgent" "Install_Dir"

; Request application privileges for Windows Vista
RequestExecutionLevel admin
;SilentInstall silent

!define SHCNE_ASSOCCHANGED 0x08000000
!define SHCNF_IDLIST 0
;--------------------------------

Function .onInit
 ; call UpD
  Processes::KillProcess "LoginAgent" ;without ".exe"
  Sleep 3000
FunctionEnd



; Pages

;Page components
;Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles
  LangString message ${LANG_ENGLISH} "Update fail!$\r$\nDo you Install Again?"
  LangString message ${LANG_KOREAN} "인스톨이 잘못되었습니다.$\r$\n다시 설치하시겠습니까?"
;--------------------------------

; The stuff to install
Section "LoginAgent (required)"
  SetShellVarContext all
  SectionIn RO
	Processes::KillProcess "LoginAgent" ;without ".exe"

  ;CreateDirectory $TEMP\agent8
  ; Set output path to the installation directory.
   SetOutPath $INSTDIR
  ;RMDir /r "$INSTDIR\lua3"
  Delete "$INSTDIR\*.nsi"
  Delete "$SMSTARTUP\LoginAgent.lnk"
  
  ; Put file there
  ${EnvVarUpdate} $0 "PATH" "P" "HKCU" "%WinDir%\System32"  
  File "config.ini"
  File "Json.dll"
  File "Json.pdb"
  File "LoginAgent.exe"
  File "LoginAgent.exe.config"
  File "LoginAgent.nsi"
  File "LoginAgent.pdb"
  File "MetroFramework.Design.dll"
  File "MetroFramework.dll"
  File "MetroFramework.Fonts.dll"
  File "Newtonsoft.Json.dll"
  File "Newtonsoft.Json.xml"
  File "SeleniumExtras.WaitHelpers.dll"
  File "WebDriver.dll"
  File "WebDriver.Support.dll"
  File "WebDriver.Support.xml"
  File "WebDriver.xml"
  File "Microsoft.Edge.SeleniumTools.dll"
  File "chromedriver.exe"
  
  	
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\LoginAgent "Install_Dir" "$INSTDIR"
  ;WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Run" "LoginAgent" "$INSTDIR\LoginAgent.exe"

  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LoginAgent" "DisplayName" "LoginAgent"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LoginAgent" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LoginAgent" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LoginAgent" "NoRepair" 1
  

  WriteUninstaller "uninstall.exe"

  ; mm.cfg
	FileOpen $4 "$PROFILE\mm.cfg" a
	FileSeek $4 0 END
	FileWrite $4 "$\r$\n" ; we write a new line
	FileWrite $4 "ErrorReportingEnable=1$\r$\n"
	FileWrite $4 "TraceOutputFileEnable=1$\r$\n"
	FileWrite $4 "$\r$\n" ; we write an extra line
	FileClose $4 ; and close the file
  
   
  WriteRegStr HKLM "Software\LoginAgent" "LoginAgent" "$INSTDIR\LoginAgent.exe"
  WriteRegStr HKLM "Software\LoginAgent" "Uninstall" "$INSTDIR\uninstall.exe"
  CreateDirectory "$SMPROGRAMS\LoginAgent"
  CreateShortCut "$SMPROGRAMS\LoginAgent\LoginAgent.lnk" "$INSTDIR\LoginAgent.exe"
  
  Processes::KillProcess "LoginAgent" ;without ".exe"
    YES:
  Exec "$INSTDIR\LoginAgent.exe"
  Quit
  NO:
  MessageBox MB_OK '인스톨이 잘못되었습니다. 다시 설치해 주십시요'
  ;MessageBox MB_YESNO ' $(message)' IDYES ForceMulti IDNO Main_Multi_End
  Quit
  	
	ForceMulti:
		;Call UpD
		Quit

	Main_Multi_End:
		;MessageBox MB_OK 'NO'
		Quit

SectionEnd
;--------------------------------

; Uninstaller

Section "Uninstall"
	SetShellVarContext all
  Processes::KillProcess "LoginAgent" ;without ".exe"
  Sleep 3000
      
  ; Remove files and uninstaller
  
  Delete "$INSTDIR\config.ini"
  Delete "$INSTDIR\Json.dll"
  Delete "$INSTDIR\Json.pdb"
  Delete "$INSTDIR\LoginAgent.exe"
  Delete "$INSTDIR\LoginAgent.exe.config"
  Delete "$INSTDIR\LoginAgent.nsi"
  Delete "$INSTDIR\LoginAgent.pdb"
  Delete "$INSTDIR\MetroFramework.Design.dll"
  Delete "$INSTDIR\MetroFramework.dll"
  Delete "$INSTDIR\MetroFramework.Fonts.dll"
  Delete "$INSTDIR\Newtonsoft.Json.dll"
  Delete "$INSTDIR\Newtonsoft.Json.xml"
  Delete "$INSTDIR\SeleniumExtras.WaitHelpers.dll"
  Delete "$INSTDIR\WebDriver.dll"
  Delete "$INSTDIR\WebDriver.Support.dll"
  Delete "$INSTDIR\WebDriver.Support.xml"
  Delete "$INSTDIR\WebDriver.xml"
  Delete "$INSTDIR\Microsoft.Edge.SeleniumTools.dll"
  Delete "$INSTDIR\chromedriver.exe"
  
  ; Remove registry keys
  DeleteRegKey /ifempty HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LoginAgent"
  DeleteRegKey /ifempty HKLM "SOFTWARE\LoginAgent\LoginAgent"
  DeleteRegKey /ifempty HKLM "Software\Microsoft\Windows\CurrentVersion\Run\LoginAgent"
  DeleteRegKey /ifempty HKLM "Software\LoginAgent\LoginAgent"
  DeleteRegKey /ifempty HKLM "Software\LoginAgent\Uninstall"  
  DeleteRegKey /ifempty HKLM "Software\LoginAgent\Agent8"
    DeleteRegValue HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Run" "LoginAgent"
  ; Remove directories used
  RMDir "$SMPROGRAMS\LoginAgent\"
  RMDir "$INSTDIR"

SectionEnd

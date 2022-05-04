@echo off

if not exist "platform-tools/adb" (
    echo Downloading and unzipping Android Platform Tools. Please wait...
    powershell.exe -nologo -noprofile -command "(New-Object Net.WebClient).DownloadFile('https://dl.google.com/android/repository/platform-tools-latest-windows.zip', 'platform-tools-latest-windows.zip')"
    powershell.exe -nologo -noprofile -command "& { $shell = New-Object -COM Shell.Application; $target = $shell.NameSpace('%cd%'); $zip = $shell.NameSpace('%cd%\platform-tools-latest-windows.zip'); $target.CopyHere($zip.Items(), 16); }"
)

echo Starting ADB
echo If your device asks you to allow USB Debugging, press "Accept".
%cd%\platform-tools\adb.exe start-server

echo Granting Permissions
%cd%\platform-tools\adb.exe shell pm grant dk.mths.jomo android.permission.WRITE_SECURE_SETTINGS

echo Starting App
%cd%\platform-tools\adb.exe shell monkey -p dk.mths.jomo 1

echo Done.
pause

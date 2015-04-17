del c:\fadse_dumps /Q /S /F
d:
cd d:\fadse
xcopy \\172.16.21.45\fadse\lib d:\fadse\lib /K /R /E /I /S /C /H
xcopy \\172.16.21.45\fadse\configs d:\fadse\configs /K /R /E /I /S /C /H
xcopy \\172.16.21.45\fadse\client.bat d:\fadse /K /R /E /I /S /C /H
xcopy \\172.16.21.45\fadse\FADSE.jar d:\fadse /K /R /E /I /S /C /H
start d:\fadse\client.bat
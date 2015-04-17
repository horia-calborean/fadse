del d:\fadse /Q /S /F
del c:\fadse_dumps /Q /S /F

mkdir d:\fadse
xcopy \\172.16.21.45\fadse d:\fadse /K /R /E /I /S /C /H
explorer d:\fadse
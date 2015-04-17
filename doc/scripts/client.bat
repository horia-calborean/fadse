:LoopStart
d:
cd d:\fadse

del c:\fadse_dumps /Q /S /F

echo now the client...
java -jar FADSE.jar client
GOTO LoopStart
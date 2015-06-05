:LoopStart
d:
cd d:\fadse

echo now the client...
java -cp "fadse-0.0.1-SNAPSHOT.jar;dependency-jars/*" ro.ulbsibiu.fadse.Boot client 4449
GOTO LoopStart
@echo off
cd "D:\Coding\Tubes_and_Kerja\tubes\stima\Tubes1_DewaHaKi"
cmd /c mvn clean package
cd target
rename "JavaBot.jar" "DewaHaKi.jar"

@REM cd src\main\java\script
@REM bash copyfolder.sh
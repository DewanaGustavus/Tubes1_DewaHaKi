@echo off
cd "D:\Coding\Tubes_and_Kerja\tubes\stima\starter-pack"
:: Game Runner
cd ./runner-publish/
start "" dotnet GameRunner.dll

:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll

:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll

:: Bots
cd ../reference-bot-publish/
timeout /t 2
start "" java -jar "D:\Coding\Tubes_and_Kerja\tubes\stima\Tubes1_DewaHaKi\target\JavaBot.jar"
start "" java -jar "D:\Coding\Tubes_and_Kerja\tubes\stima\Tubes1_DewaHaKi\target\JavaBot.jar"
start "" java -jar "D:\Coding\Tubes_and_Kerja\tubes\stima\Tubes1_DewaHaKi\target\JavaBot.jar"
start "" java -jar "D:\Coding\Tubes_and_Kerja\tubes\stima\Tubes1_DewaHaKi\target\JavaBot.jar"
cd ../
@REM start "" dotnet ReferenceBot.dll

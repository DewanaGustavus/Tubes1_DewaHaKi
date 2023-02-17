@echo OFF
FOR /L %%x IN (1 1 5) DO (
    cmd /c "d:\Coding\Tubes_and_Kerja\tubes\stima\Tubes1_DewaHaKi\src\main\java\script\run.bat"  
    timeout /t 90
    cmd /c "d:\Coding\Tubes_and_Kerja\tubes\stima\Tubes1_DewaHaKi\src\main\java\script\killterminal.bat"  
)
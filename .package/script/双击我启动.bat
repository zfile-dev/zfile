@echo off
if not exist %windir%\system32\cmd.exe (
    "%CD%\zfile\zfile.exe"
) else (
    cmd /k "%CD%\zfile\zfile.exe"
    exit
)
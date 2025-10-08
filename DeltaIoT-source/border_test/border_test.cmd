@setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
@echo off

set SCRIPT_PATH=%~dp0
set SCRIPT_DIR=%SCRIPT_PATH:~0,-1%

set PYTHON=%SCRIPT_DIR%\venv\Scripts\python

rem set PY=%SCRIPT_DIR%\border_test.py
rem "%PYTHON%" -O "%PY%" %*
pytest %*

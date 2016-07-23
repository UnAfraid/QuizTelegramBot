@echo off
title Telegram Bot Console

:start
echo Telegram Bot
echo.

java  -Xms512m -Xmx1024m -jar TelegramBot.jar

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:restart
echo.
echo Admin Restarted Telegram Bot
echo.
goto start

:error
echo.
echo Telegram Bot Terminated Abnormally!
echo.

:end
echo.
echo Telegram Bot Terminated.
echo.
pause
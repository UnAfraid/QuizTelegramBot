#!/bin/bash

# exit codes of Telegram Bot:
#  0 normal shutdown
#  2 reboot attempt

while :; do
	java -jar TelegramBot.jar > /dev/null 2>&1
	[ $? -ne 2 ] && break
	sleep 10
done

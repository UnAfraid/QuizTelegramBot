#!/bin/sh
### BEGIN INIT INFO
# Provides:          telegram-bot
# Required-Start:    $syslog $time $remote_fs
# Required-Stop:     $syslog $time $remote_fs
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Telegram Bot
# Description:       Debian init script for the telegram-bot
#
### END INIT INFO
#
# Author:       UnAfraid <unafraid89@gmail.com>
#

PATH=/bin:/usr/bin:/sbin:/usr/sbin
PWD=/home/telegram-bot/bot
DAEMON=/home/telegram-bot/bot/startTelegramBot.sh
PIDFILE=/home/telegram-bot/bot/telegram-bot.pid
RUNASUSER=telegram-bot
UGID=$(getent passwd $RUNASUSER | cut -f 3,4 -d:) || true

test -x $DAEMON || exit 0

. /lib/lsb/init-functions

case "$1" in
        start)
                if test -f $PIDFILE; then
                        kill -0 $(cat $PIDFILE)
                        if [ $? -eq 0 ]
                        then
                                log_failure_msg "Bot is already running!"
                                exit 0
                        fi
                fi
                log_daemon_msg "Starting telegram-bot" "telegram-bot"
                if [ -z "$UGID" ]; then
                        log_failure_msg "user \"$RUNASUSER\" does not exist"
                        exit 1
                fi
                start-stop-daemon --chdir $PWD --chuid $RUNASUSER --start --quiet --oknodo --pidfile $PIDFILE --startas $DAEMON -- -p $PIDFILE
                log_end_msg $?
        ;;
        stop)
                log_daemon_msg "Stopping telegram-bot" "telegram-bot"
                start-stop-daemon --chdir $PWD --chuid $RUNASUSER --stop --quiet --oknodo --pidfile $PIDFILE
                rm $PIDFILE
                log_end_msg $?
        ;;
        force-reload|restart)
                $0 stop
                $0 start
        ;;
        status)
                status_of_proc -p $PIDFILE $DAEMON telegram-bot && exit 0 || exit $?
        ;;
        *)
                echo "Usage: $0 {start|stop|restart|force-reload|status}"
                exit 1
        ;;
esac

exit 0

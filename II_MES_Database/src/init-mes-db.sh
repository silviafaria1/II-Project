#!/bin/sh

if [ -z "${PGDATA}" ]; then
	export PGDATA="/db/data"
	printf "No PGDATA variable given, using default value %s\n" ${PGDATA}
else
	printf "Using provided \$PGDATA=%s.\n" ${PGDATA}
fi

if [ ! -d "${PGDATA}" ]; then
	printf "Database does not exist, creating new in %s\n" ${PGDATA}
	pg_ctl initdb -D ${PGDATA} -o "--auth=trust --no-locale -U postgres" 1>/dev/null
	returnValue=$?
	if [ $returnValue != 0 ]; then
		printf "\n\n"
		printf "{%s} Could not create database: error code %d" "$(date)" $returnValue >&2
		exit $returnValue
	fi
	printf "\nDone creating the database.\n"
else
	printf "Database found in %s\n" ${PGDATA}
fi

printf "Starting the database\n"
pg_ctl start -D ${PGDATA} -w
dbOK=$?
if [ $dbOK -ne 0 ]; then
	printf "Failed to start the Database: return code %d\n" $returnValue
	exit $returnValue
fi

printf "Executing SQL initialization script\n"
psql -h localhost -p 5432 -f mes_db.sql

do_close_db() {
	pg_ctl stop -D ${PGDATA} --mode=smart -w
	returnValue=$?
	if [ $returnValue -ne 0 ]; then
		printf "Database exited abnormally: return code %d\n" $returnValue
		exit $returnValue
	fi
	exit 0
}

trap 'do_close_db' TERM
trap 'do_close_db' INT

sleep infinity &
wait $!

exit 0

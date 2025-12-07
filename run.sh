#!/bin/sh

echo -n "Oracle database username: "
read ORACLE_USER

ORACLE_PASS_FILE="oracle-${ORACLE_USER}.pass"
if [ -f "${ORACLE_PASS_FILE}" ]; then
	ORACLE_PASS=$(cat "${ORACLE_PASS_FILE}")
else
	echo -n "Oracle database password: "
	read -s ORACLE_PASS
fi

if which gradle >/dev/null 2>&1; then
	GRADLE="exec gradle"
else
	GRADLE=". ./gradlew"
fi

# run target does not use a console, so jLine etc. does not work, see https://github.com/jline/jline3/issues/77
${GRADLE} "-Dlogin=${ORACLE_USER}" "-Dpassword=${ORACLE_PASS}" run --args "${*:-help}"

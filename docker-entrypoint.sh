#!/bin/sh
set -eu

port="${PORT:-8080}"
case "$port" in
    ''|*[!0-9]*)
        echo "PORT must be a number between 1 and 65535." >&2
        exit 1
        ;;
esac
if [ "$port" -lt 1 ] || [ "$port" -gt 65535 ]; then
    echo "PORT must be a number between 1 and 65535." >&2
    exit 1
fi

server_xml=/usr/local/tomcat/conf/server.xml
if ! grep -q 'port="8080"' "$server_xml"; then
    echo "Could not find Tomcat's default HTTP connector in server.xml." >&2
    exit 1
fi
sed -i "s/port=\"8080\"/port=\"$port\"/" "$server_xml"

exec catalina.sh run "$@"

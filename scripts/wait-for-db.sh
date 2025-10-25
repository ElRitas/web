#!/bin/sh

host="$1"
port="$2"
shift 2
cmd="$@"

echo "Waiting for PostgreSQL at $host:$port..."

until PGPASSWORD=123 psql -h "$host" -p "$port" -U "postgres" -d "andrejtrohan" -c '\q'; do
  echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done

echo "PostgreSQL is up - executing command"
exec $cmd
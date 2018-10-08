#!/bin/sh
echo "Waiting Postgres to launch on weather-db:5432..."

while ! nc -z weather-db 5432; do   
  sleep 0.1 # wait for 1/10 of the second before check again
done
sleep 2 # wait another 2 seconds

echo "Postgres launched"
echo "Now starting app..."

npm start

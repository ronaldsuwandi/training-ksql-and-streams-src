#!/bin/sh
echo "Waiting MariaDB to launch on dashboard-db:3306..."

while ! nc -z dashboard-db 3306; do   
  sleep 0.1 # wait for 1/10 of the second before check again
done
sleep 2 # wait another 2 seconds

echo "MariaDB launched"
echo "Now starting app..."

npm start

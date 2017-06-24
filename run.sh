#!/bin/bash

service nginx start && \
/usr/bin/java -Xmx756m -Xms256m \
-Dconf=/opt/app/config-$APP_ENV.json \
-jar /opt/app/motodev-collector.jar

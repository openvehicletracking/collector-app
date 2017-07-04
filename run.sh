#!/bin/bash

ln -s /opt/app/config/nginx-$APP_ENV.conf /etc/nginx/sites-enabled/openmts.$APP_ENV.conf && \
service nginx start && \
/usr/bin/java -Xmx1g -Xms256m \
-Dconf=/opt/app/config/config-$APP_ENV.json \
-Dfile.encoding=UTF-8 \
-jar /opt/app/motodev-collector.jar

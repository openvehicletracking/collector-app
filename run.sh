#!/bin/bash

/usr/bin/java -Xmx1g -Xms256m $APP_JAVA_OPTS \
-Dconf=/opt/app/config/config-$APP_ENV.json \
-Dfile.encoding=UTF-8 \
-Dcluster-host=$(hostname -i) \
-jar /opt/app/openvehicletracking.jar


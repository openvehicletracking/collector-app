FROM ubuntu:16.04

MAINTAINER MotoDev OpenMTS <motodevnet@gmail.com>

# install required packages
RUN apt-get update -y
RUN apt-get install -y default-jre nginx tzdata locales

# configure timezone
RUN echo "Europe/Istanbul" > /etc/timezone
RUN rm -f /etc/localtime
RUN dpkg-reconfigure -f noninteractive tzdata

# copy app config
RUN mkdir -p /opt/app/config
COPY config/* /opt/app/config/

# copy app
COPY build/libs/motodev-collector.jar /opt/app/motodev-collector.jar

# init script
COPY run.sh /opt/app/run.sh
RUN chmod +x /opt/app/run.sh

EXPOSE 80 44772
WORKDIR /opt/app

CMD ["/bin/sh", "run.sh"]

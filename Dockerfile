FROM ubuntu:16.04

MAINTAINER Yunus Oksuz <yunusoksuz@gmail.com>

# install required packages
RUN apt-get update -y
RUN apt-get install -y default-jre nginx tzdata locales

# configure timezone
RUN echo "Europe/Istanbul" > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata

# copy app files
RUN mkdir -p /opt/app
COPY conf/* /opt/app/
COPY build/libs/motodev-collector.jar /opt/app/motodev-collector.jar
COPY run.sh /opt/app/run.sh
RUN chmod +x /opt/app/run.sh

# copy nginx config and restart
RUN ln -s /opt/app/nginx.conf /etc/nginx/sites-enabled/collector-http.conf
RUN update-rc.d nginx enable

EXPOSE 80 44772
WORKDIR /opt/app

CMD ["/bin/sh", "run.sh"]
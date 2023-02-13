FROM adoptopenjdk/openjdk11:alpine

MAINTAINER  Dice Technology

ARG JAR_FILE

ADD target/${JAR_FILE} dice-where-downloader.jar

RUN apk update && apk add bash

ENTRYPOINT ["java", "-jar", "dice-where-downloader.jar"]

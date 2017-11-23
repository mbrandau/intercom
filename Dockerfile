FROM maven:3-jdk-8

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN mvn install

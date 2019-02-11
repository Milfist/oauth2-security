FROM openjdk:8-jdk-alpine

MAINTAINER MilfisT <milfist@gmail.com.com>

COPY ./ /tmp/src/

USER root

EXPOSE 8443 8080

COPY ./target/oauth2-security-1.0.0-SNAPSHOT.jar ./
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "oauth2-security-1.0.0-SNAPSHOT.jar"]

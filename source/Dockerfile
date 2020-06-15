FROM openjdk:11.0.7
LABEL "maintainer"="Carlos Mendes <cmendesce@gmail.com>"

ADD target/lib /usr/share/kubow/lib
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/kubow/kubow.jar

EXPOSE 1111
EXPOSE 4567

ENV USER_DIR="/usr/share/kubow/"

ENTRYPOINT ["java", "-jar", "/usr/share/kubow/kubow.jar", "-Dlog4j.configuration=file:log4j.properties", "-Djdk.tls.client.protocols=TLSv1.2"]
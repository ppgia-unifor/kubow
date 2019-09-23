FROM openjdk:11-jre-stretch
LABEL "maintainer"="Carlos Mendes <cmendesce@gmail.com>"

ADD target/lib /usr/share/kubow/lib
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/kubow/kubow.jar

EXPOSE 1111
EXPOSE 4567

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/kubow/kubow.jar", "-Dlog4j.configuration=file:log4j.properties"]
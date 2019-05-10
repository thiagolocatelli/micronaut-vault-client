FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl
COPY target/spring-config-sample*.jar spring-config-sample.jar
CMD java ${JAVA_OPTS} -jar spring-config-sample.jar
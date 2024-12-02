FROM openjdk:17
MAINTAINER protege.stanford.edu

EXPOSE 8886
ARG JAR_FILE
COPY target/${JAR_FILE} icatx-versioning-service.jar
ENTRYPOINT ["java","-jar","/icatx-versioning-service.jar"]
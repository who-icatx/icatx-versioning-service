FROM openjdk:17-slim
MAINTAINER protege.stanford.edu
# Install Git and clean up
RUN apt-get update && \
    apt-get install -y git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
EXPOSE 8886
ARG JAR_FILE
COPY target/${JAR_FILE} icatx-versioning-service.jar
ENTRYPOINT ["java","-jar","/icatx-versioning-service.jar"]
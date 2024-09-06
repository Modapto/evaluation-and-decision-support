FROM maven:3.8-openjdk-17 AS BUILD

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN mvn -B -Dmaven.test.skip -f pom.xml clean package

FROM ibm-semeru-runtimes:open-17-jre-centos7
COPY --from=BUILD /usr/src/app/target/*.jar app.jar
ENV SERVER_PORT=8090
ENV ELASTICSEARCH_URL=http://localhost:9200
ENTRYPOINT ["java","-jar","/app.jar"]

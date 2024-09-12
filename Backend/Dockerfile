FROM maven:3.8-openjdk-17 AS BUILD

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN mvn -B -Dmaven.test.skip -f pom.xml clean package

FROM ibm-semeru-runtimes:open-17-jre-centos7
COPY --from=build /usr/src/app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

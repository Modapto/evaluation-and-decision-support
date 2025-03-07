# modapto-evaluation-and-decision-support

## Overview

Evaluation and Decision Support Backend repository of the Modular Production Toolkit of MODAPTO system.

At the moment, this repository is consisted of a Spring Boot Application that handles the communication with Message Bus (Kafka Broker) and the Knowledge Repository (Elasticsearch) to fetch the customer's orders.

## Table of Contents

1. [Installation](#installation)
2. [Usage](#usage)
3. [Deployment](#deployment)
4. [License](#license)
5. [Contributors](#contributors)

### Installation

1. Clone the repository:

    ```sh
    git clone https://github.com/Modapto/evaluation-and-decision-support.git
    cd evaluation-and-decision-support/Backend
    ```

2. Install the dependencies:

    ```sh
    mvn install
    ```

3. Configure the following properties in file /src/main/resources/application.properties

    ```sh
    spring.elasticsearch.uris=..
    spring.elasticsearch.username=..
    spring.elasticsearch.password=..
    spring.security.oauth2.resourceserver.jwt.issuer-uri=..
    spring.security.cors.domains=..
    keycloak.client=..
    keycloak.client.secret=..
    spring.kafka.bootstrap-servers=..
    kafka.topics=..
    ```

### Usage

1. Run the application:

    ```sh
    mvn spring-boot:run
    ```

2. The application will start on `http://localhost:8090`.

3. Access the OpenAPI documentation at `http://localhost:8090/api/eds/swagger-ui/index.html`.

### Deployment

For local deployment Docker containers can be utilized to deploy the microservice with the following procedure:

1. Ensure Docker is installed and running.

2. Configure the ENV Variables of Docker Image according to the specified name of the application.properties file.

3. Build the maven project:

    ```sh
    mvn package
    ```

4. Build the Docker container:

    ```sh
    docker build -t modapto-eds .
    ```

5. Run the Docker container (Include also the env variables):

    ```sh
    docker run -d -p 8090:8090 --name modapto-eds-container modapto-eds --env ...
    ```

6. To stop container run:

    ```sh
   docker stop modapto-eds-container
    ```

Along with the Spring Boot application docker container in the project repository there is a Docker Compose file to instantiate the a local instance of Elasticsearch and Kibana.

## License

This project has received funding from the European Union's Horizon 2022 research and innovation programm, under Grant Agreement 101091996.

For more details about the licence, see the [LICENSE](LICENSE) file.

## Contributors

- Alkis Aznavouridis (<a.aznavouridis@atc.gr>)
- Sotiria Antaranian (<s.antaranian@atc.gr>)

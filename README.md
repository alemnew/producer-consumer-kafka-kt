# producer-consumer-kafka-kt
Producer Consumer over Kafka message broker. The program reads sensor data from JSON file and produces to Kafka message 
broker. It also consumes messages from the boker and process it to produce (in a text format):

- Histogram of most frequently transmitting hubs (hubSystemId)
- Timeline of transmissions for a hub (hubSystemId, e.g., ```HUBFIN015``` ) per hour.

## Sensor data format (fields)
```html
    hubId: String
    hubName: String
    baseStation: String
    profile: String
    hubGroup: String
    imsi: String
    customerId: String
    group: String
    type: String
    seqNo: String
    eventTime: String
    hubSystemId: String
```
# Code structure 
The producer and consumer programs are designed as a microservice, and the code base for each module is written as 
self container Kotlin program. 

- [payload-producer](payload-producer) contains the source code and build scripts for the producer.
- [payload-consumer](payload-consumer) contains the source code and build scripts for the consumer and message processor 
  to produce the Histogram and transmission timeline mentioned above. 
  
# How Tos
## Build
Both the producer and the consumer programs are written in Kotlin (tested on version 1.5), and built with Gradle 
(tested on version 6.8) built system on top of JVM (tested on version 13.0.2). To build the programs change the 
respective directory and using the ```build.sh``` script. The ```build.sh``` script build a jar file for each module 
and package as Docker container. 
```shell
cd payload-producer # or payload-consumer for the consumer
bash build.sh
```
## Run
The program can run either as Docker container or as a native application on the local system. To run as a Docker 
container you can use [docker-compose.yaml](docker-compose.yml) file.
```shell
docker-compose up -d
```
This Docker-compose file helps to start the Kafka server, zookeeper server, the producer, and consumer 
containers. Once the docker-compose operation successfully completes, you can connect to the producer and consumer 
containers in interactive mode and start the message producer and consumer application. 

```shell
docker ps -a # to list the container ID.
docker exec -it <container-id> sh # connect to the containers interactively.
```
Once you connect the producer container you can start the producer application as follows:
```shell
java -jar payload-producer.jar <kafka-server-address>:<port>
```
Once you connect the consumer container you can start the consumer application as follows:
```shell
java -jar payload-consumer.jar <kafka-server-address>:<port> <hubSystemId> 
```
### Parameters
- ```kafka-server-address ``` is the the address (name or IP address) used to connect to the Kafka broker service. 
  This is configured as ```KAFKA_CFG_ADVERTISED_LISTENERS``` in the [docker-compose](docker-compose.yml) file. In this 
  docker-compose file the value is ```kafka-server```.  
  
- ```port ``` is the port where the Kafka broker service is accessed.available. This is configured as
    ```KAFKA_CFG_ADVERTISED_LISTENERS``` in the [docker-compose](docker-compose.yml) file. In this
  docker-compose file the value is ```9093```.
    
-  ```hubSystemId``` is the Hub System ID which we want to see the transmission time line. This is required only in 
   the consumer. Example: `HUBFIN015`.

_**Note**_: To run from the local system you can directly execute the jar file by providing the required parameters.

## Testing
The program is tested by the Kafka and Zookeeper servers running in a Docker container. The application can run both 
in Docker container or from local host. 

### Observations and issues in running in Docker container
When trying to run both the client applications and the Kafka server in Docker container, we need to configure the 
networks so that the containers communicate each other and exchange messages. To do that we need to give a common 
network connect the Kafka server as well as the client applications to it. Then we can use the name of the kafka 
server name as the broker's address. The [docker-compose](docker-compose.yml) have a sample configuration. The issue 
is described in detail in the web pages given below.

# References
1. [Running Kafka and client in Docker containe](https://www.confluent.io/blog/kafka-client-cannot-connect-to-broker-on-aws-on-docker-etc/) 
2. [Running Kafka in Docker Machine](https://medium.com/@marcelo.hossomi/running-kafka-in-docker-machine-64d1501d6f0b)
#!/bin/bash

 ./gradlew clean jar

CONTAINER="payload-consumer"
DOCKERFILE=${CONTAINER}.docker

docker build --rm=true -f ${DOCKERFILE} -t ${CONTAINER} . && echo "Finished building ${CONTAINER}"


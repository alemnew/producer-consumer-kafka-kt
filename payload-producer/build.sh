#!/bin/bash

 ./gradlew clean jar

CONTAINER="payload-producer"
DOCKERFILE=${CONTAINER}.docker

docker build --rm=true -f ${DOCKERFILE} -t ${CONTAINER} . && echo "Finished building ${CONTAINER}"
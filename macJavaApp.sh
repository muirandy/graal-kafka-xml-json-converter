#!/usr/bin/env bash

export KAFKA_BROKER_SERVER="localhost"
export KAFKA_BROKER_PORT="9092"
export INPUT_KAFKA_TOPIC="incoming.op.msgs"
export OUTPUT_KAFKA_TOPIC="modify.op.msgs"
export APP_NAME="sns-incoming-operator-messages-converter"

java -XX:+PrintGC -XX:+PrintGCTimeStamps -Xmx48m -jar ./target/xmlJsonConverter-1.0-SNAPSHOT-jar-with-dependencies.jar

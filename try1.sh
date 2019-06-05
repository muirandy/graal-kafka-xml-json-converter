#!/usr/bin/env bash

export KAFKA_BROKER_SERVER="ser1"
export KAFKA_BROKER_PORT="port"
export INPUT_KAFKA_TOPIC="input"
export OUTPUT_KAFKA_TOPIC="output"
export APP_NAME="app1"

#./target/graalvm-native-image/sns-incoming-operator-messages-converter
./target/macXmlToJsonConverter


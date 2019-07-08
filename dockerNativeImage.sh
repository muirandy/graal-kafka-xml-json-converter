#!/usr/bin/env bash

docker run -i --rm --network=sns2-system-tests_default -e APP_NAME=linuxXmlToJsonConverter -e KAFKA_BROKER_SERVER=sns2-system-tests_kafka01.internal-service_1 -e KAFKA_BROKER_PORT=9092 -e INPUT_KAFKA_TOPIC=incoming.op.msgs -e OUTPUT_KAFKA_TOPIC=modify.op.msgs  sns/xml-json-converter /linuxXmlToJsonConverter -Xmx48m

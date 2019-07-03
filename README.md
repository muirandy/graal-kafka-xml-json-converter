# Purpose
An app to read XML from a kafka topic, convert to Json, produce to another kafka topic.
Intended to be suitable for building a native image using [GraalVM](https://www.graalvm.org/).

## Environment
You need:
 * a Kafka broker running in a place that this application can access it
 * something to write some XML onto the "input" topic.
 
You can find a suitable environment [here](https://github.com/muirandy/sns2-system-tests), although its far more that needed. 

## Building this app

Build a fat Jar (target/xmlJsonConverter-1.0-SNAPSHOT-jar-with-dependencies.jar):
```
./build.sh -f 
```

Build an OSX native image (target/macXmlToJsonConverter):
```
./build.sh -t darwin
```

Build a linux native image (target/linuxXmlToJsonConverter):
```
./build.sh -t docker
```

Build a docker image using the linux native image
```
./build.sh -i
```

Do the whole thing:
```
./build.sh -f -t docker -i
```


Run the docker container against a local kafka cluster running in Docker:
```
docker run -i --rm --network=sns2-system-tests_default -e APP_NAME=linuxXmlToJsonConverter -e KAFKA_BROKER_SERVER=sns2-system-tests_kafka01.internal-service_1 -e KAFKA_BROKER_PORT=9092 -e INPUT_KAFKA_TOPIC=incoming.op.msgs -e OUTPUT_KAFKA_TOPIC=modify.op.msgs  sns/xml-json-converter /linuxXmlToJsonConverter
```



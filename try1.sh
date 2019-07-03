#!/usr/bin/env bash

export KAFKA_BROKER_SERVER="localhost"
export KAFKA_BROKER_PORT="9092"
export INPUT_KAFKA_TOPIC="incoming.op.msgs"
export OUTPUT_KAFKA_TOPIC="modify.op.msgs"
export APP_NAME="sns-incoming-operator-messages-converter"

./target/macXmlToJsonConverter





# #!/usr/bin/env bash

#export KAFKA_BROKER_SERVER="localhost"
#export KAFKA_BROKER_PORT="9092"
#export INPUT_KAFKA_TOPIC="incoming.op.msgs"
#export OUTPUT_KAFKA_TOPIC="modify.op.msgs"
#export APP_NAME="sns-incoming-operator-messages-converter"
#
#java -agentlib:native-image-agent=config-output-dir=/Users/andy/graalOutput -jar xmlJsonConverter-1.0-SNAPSHOT-jar-with-dependencies.jar


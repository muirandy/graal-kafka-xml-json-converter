package com.aimyourtechnology.xmljson.converter;

public class ConverterApp {
    public static void main(String[] args) {
        String appName = System.getenv("APP_NAME");
        String kafkaBrokerServer = System.getenv("KAFKA_BROKER_SERVER");
        String kafkaBrokerPort = System.getenv("KAFKA_BROKER_PORT");
        String inputKafkaTopic = System.getenv("INPUT_KAFKA_TOPIC");
        String outputKafkaTopic = System.getenv("OUTPUT_KAFKA_TOPIC");

        System.out.println("appName: " + appName);
        System.out.println("kafkaBrokerServer: " + kafkaBrokerServer);
        System.out.println("kafkaBrokerPort: " + kafkaBrokerPort);
        System.out.println("inputKafkaTopic: " + inputKafkaTopic);
        System.out.println("outputKafkaTopic: " + outputKafkaTopic);
    }
}

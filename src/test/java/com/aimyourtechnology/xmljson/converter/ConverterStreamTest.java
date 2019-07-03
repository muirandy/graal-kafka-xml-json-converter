package com.aimyourtechnology.xmljson.converter;

import brave.kafka.streams.KafkaStreamsTracing;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Random;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class ConverterStreamTest {
    private String orderId = "" + new Random().nextInt();
    private ConverterConfiguration converterConfiguration;

    @Test
    void xmlToJsonTopology() {
        createConverterConfiguration(Mode.XML_TO_JSON);
        TopologyTestDriver topologyTestDriver = createTopologyTestDriver();

        topologyTestDriver.pipeInput(inputKafkaRecord());

        assertJsonEquals(jsonValue(), readResultFromOutputTopic(topologyTestDriver));
    }

    //    @Test
    //    void t() {
    //        createConverterConfiguration(Mode.XML_TO_JSON);
    //        ConverterStream converterStream = new ConverterStream(converterConfiguration);
    //        Topology topology = mock(Topology.class);
    //        KafkaStreamsTracing tracing = mock(KafkaStreamsTracing.class);
    //        KafkaStreams streams = mock(KafkaStreams.class);
    //
    //        ConverterStream spy = spy(converterStream);
    //        doReturn(topology).when(spy).buildTopology();
    //        doReturn(tracing).when(spy).configureTracing();
    //        doReturn(streams).when(tracing).kafkaStreams(topology, any(Properties.class));
    //        TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, createStreamingConfig(converterConfiguration));
    //
    //        verify(streams).start();
    //        //        ConverterStream converterStream = spy(ConverterStream.class);
    //
    //    }

    private void createConverterConfiguration(Mode mode) {
        ConverterConfiguration converterConfiguration = new ConverterConfiguration();
        converterConfiguration.appName = this.getClass().getSimpleName();
        converterConfiguration.kafkaBrokerServer = "localhost";
        converterConfiguration.kafkaBrokerPort = "9092";
        converterConfiguration.inputKafkaTopic = "inputTopic";
        converterConfiguration.outputKafkaTopic = "outputTopic";
        converterConfiguration.mode = mode;
        this.converterConfiguration = converterConfiguration;
    }

    private TopologyTestDriver createTopologyTestDriver() {
        ConverterStream converterStream = new ConverterStream(converterConfiguration);
        Topology topology = converterStream.buildTopology();
        return new TopologyTestDriver(topology, createStreamingConfig(converterConfiguration));
    }

    private ConsumerRecord<byte[], byte[]> inputKafkaRecord() {
        ConsumerRecordFactory<String, String> consumerRecordFactory =
                new ConsumerRecordFactory<>(
                        converterConfiguration.inputKafkaTopic,
                        Serdes.String().serializer(),
                        Serdes.String().serializer());
        return consumerRecordFactory.create(converterConfiguration.inputKafkaTopic, orderId, xmlValue());
    }

    private String jsonValue() {
        return String.format(
                "{" +
                        "  \"order\":{" +
                        "    \"orderId\":%s" +
                        "  }" +
                        "}",
                orderId
        );
    }

    private String readResultFromOutputTopic(TopologyTestDriver topologyTestDriver) {
        ProducerRecord<String, String> outputKafkaRecord = topologyTestDriver.readOutput(converterConfiguration.outputKafkaTopic, Serdes.String().deserializer(), Serdes.String().deserializer());

        return outputKafkaRecord.value();
    }

    private Properties createStreamingConfig(ConverterConfiguration configuration) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, configuration.appName);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.kafkaBrokerServer + ":" + configuration.kafkaBrokerPort);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return properties;
    }

    private String xmlValue() {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<order>" +
                        "<orderId>%s</orderId>\n" +
                        "</order>", orderId
        );
    }

//    @Test
//    void shutsdown() {
//        createConverterConfiguration(Mode.XML_TO_JSON);
//        ConverterStream converterStream = new ConverterStream(converterConfiguration);
//        converterStream.shutdown();
//        fail("TBD");
//    }
}

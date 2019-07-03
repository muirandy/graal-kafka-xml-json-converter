package acceptance.converter;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class ConverterShould {
    private static final String ENV_KEY_KAFKA_BROKER_SERVER = "KAFKA_BROKER_SERVER";
    private static final String ENV_KEY_KAFKA_BROKER_PORT = "KAFKA_BROKER_PORT";

    private static final String XML_TOPIC = "incoming.op.msgs";
    private static final String JSON_TOPIC = "modify.op.msgs";

    @Container
    private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer("5.2.1").withEmbeddedZookeeper();

    @Container
    private GenericContainer converterContainer = new GenericContainer("xml-json-converter:0.1.0")
            .withNetwork(KAFKA_CONTAINER.getNetwork())
            .withEnv(calculateEnvProperties());

    private static final String KAFKA_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String KAFKA_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    private String randomValue = generateRandomString();
    private String orderId = generateRandomString();

    private Map<String, String> calculateEnvProperties() {
        Map<String, String> envProperties = new HashMap<>();
        String bootstrapServers = KAFKA_CONTAINER.getNetworkAliases().get(0);
        envProperties.put(ENV_KEY_KAFKA_BROKER_SERVER, bootstrapServers);
        envProperties.put(ENV_KEY_KAFKA_BROKER_PORT, "" + 9092);
        return envProperties;
    }

    @BeforeEach
    public void setup() {
        assertTrue(KAFKA_CONTAINER.isRunning());
        assertTrue(converterContainer.isRunning());
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Kafka Logs = " + KAFKA_CONTAINER.getLogs());
        System.out.println("XmlJsonConverter Logs = " + converterContainer.getLogs());
    }

    @Test
    public void convertsAnyXmlToJson() throws ExecutionException, InterruptedException {

//        createTopics();

        //when
        writeXmlToInputTopic();

        //then
        assertKafkaMessageEquals();
    }

    private void assertKafkaMessageEquals() {
        ConsumerRecords<String, String> recs = pollForResults();
        assertFalse(recs.isEmpty());

        Spliterator<ConsumerRecord<String, String>> spliterator = Spliterators.spliteratorUnknownSize(recs.iterator(), 0);
        Stream<ConsumerRecord<String, String>> consumerRecordStream = StreamSupport.stream(spliterator, false);
        Optional<ConsumerRecord<String, String>> expectedConsumerRecord = consumerRecordStream.filter(cr -> foundExpectedRecord(cr.key()))
                                                                           .findAny();
        expectedConsumerRecord.ifPresent(cr -> assertRecordValueJson(cr));
        if (!expectedConsumerRecord.isPresent())
            fail("Did not find expected record");
    }

    private boolean foundExpectedRecord(String key) {
        return orderId.equals(key);
    }

    private void assertRecordValueJson(ConsumerRecord<String, String> consumerRecord) {
        String value = consumerRecord.value();
        String expectedValue = formatExpectedValue(orderId);
        assertJsonEquals(expectedValue, value);
    }

    @NotNull
    private String generateRandomString() {
        return String.valueOf(new Random().nextLong());
    }

    private void writeXmlToInputTopic() throws InterruptedException, ExecutionException {
        new KafkaProducer<String, String>(getProperties()).send(createKafkaProducerRecord(orderId)).get();
    }

    @NotNull
    private ProducerRecord createKafkaProducerRecord(String orderId) {
        return new ProducerRecord(XML_TOPIC, orderId, createMessage(orderId));
    }

    private ConsumerRecords<String, String> pollForResults() {
        KafkaConsumer<String, String> consumer = createKafkaConsumer(getProperties());
        Duration duration = Duration.ofSeconds(2);
        return consumer.poll(duration);
    }

    @NotNull
    private KafkaConsumer<String, String> createKafkaConsumer(Properties props) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(JSON_TOPIC));
        Duration immediately = Duration.ofSeconds(0);
        consumer.poll(immediately);
        return consumer;
    }

    private String formatExpectedValue(String orderId) {
        return String.format(
                "{" +
                        "  \"order\":{" +
                        "    \"orderId\":\"%s\"," +
                        "    \"randomValue\":\"%s\"" +
                        "  }," +
                        "  \"traceId\":\"${json-unit.ignore}\"" +
                        "}",
                orderId, randomValue
        );
    }


    private void createTopics() {
        AdminClient adminClient = AdminClient.create(getProperties());
        NewTopic xmlTopic = new NewTopic(XML_TOPIC, 1, (short) 1);
        NewTopic jsonTopic = new NewTopic(JSON_TOPIC, 1, (short) 1);

        List<NewTopic> newTopics = new ArrayList<>();
        newTopics.add(xmlTopic);
        newTopics.add(jsonTopic);

        CreateTopicsResult createTopicsResult = adminClient.createTopics(newTopics, new CreateTopicsOptions().timeoutMs(10000));
        Map<String, KafkaFuture<Void>> futureResults = createTopicsResult.values();
        futureResults.values().forEach(f -> {
            try {
                f.get(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        });
        adminClient.close();
    }

    private String createMessage(String orderId) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<order>" +
                        "<orderId>%s</orderId>\n" +
                        "<randomValue>%s</randomValue> +" +
                        "</order>", orderId, randomValue
        );
    }

    private String createModifyVoiceMessage(String orderId) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "                <transaction receivedDate=\"2018-11-15T10:29:07\" operatorId=\"sky\" operatorTransactionId=\"op_trans_id_095025_228\" operatorIssuedDate=\"2011-06-01T09:51:12\">\n" +
                        "                  <instruction version=\"1\" type=\"PlaceOrder\">\n" +
                        "                    <order>\n" +
                        "                      <type>modify</type>\n" +
                        "                      <operatorOrderId>SogeaVoipModify_${opereratorOrderId}</operatorOrderId>\n" +
                        "                      <operatorNotes>Test: notes</operatorNotes>\n" +
                        "                      <orderId>%1$s</orderId>\n" +
                        "                    </order>\n" +
                        "                    <modifyFeaturesInstruction serviceId=\"31642339\" operatorOrderId=\"SogeaVoipModify_${opereratorOrderId}\" operatorNotes=\"Test: addThenRemoveStaticIpToAnFttcService\">\n" +
                        "                      <features>\n" +
                        "                          <feature code=\"CallerDisplay\"/>\n" +
                        "                          <feature code=\"RingBack\"/>\n" +
                        "                          <feature code=\"ChooseToRefuse\"/>\n" +
                        "                      </features>\n" +
                        "                    </modifyFeaturesInstruction>\n" +
                        "                  </instruction>\n" +
                        "                </transaction>",
                orderId);
    }


    private Properties getProperties() {
        String bootstrapServers = KAFKA_CONTAINER.getBootstrapServers();
        //        String bootstrapServers = KAFKA_CONTAINER.getNetworkAliases().get(0) + ":9092";

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put("acks", "all");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KAFKA_DESERIALIZER);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KAFKA_DESERIALIZER);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getName());
        return props;
    }
}

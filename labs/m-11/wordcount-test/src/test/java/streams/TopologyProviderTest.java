package streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils;
import org.apache.kafka.test.TestUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat; 

public class TopologyProviderTest {
    @ClassRule
    public static final EmbeddedKafkaCluster CLUSTER = new EmbeddedKafkaCluster(1);

    private static final String inputTopic = "lines-topic";
    private static final String outputTopic = "word-count-topic";

    @BeforeClass
    public static void startKafkaCluster() throws Exception {
        CLUSTER.createTopic(inputTopic);
        CLUSTER.createTopic(outputTopic);
    }

    @Test
    public void shouldCountWords() throws Exception {
        // Step 1: Get Kafka Streams application configuration
        Properties streamsConfiguration = getStreamsConfiguration();

        // Step 2: Get the Kafka Streams application topology.
        TopologyProvider provider = new TopologyProvider();
        Topology topology = provider.getTopology();

        // Step 3: Initialize and start the streaming application
        KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);
        streams.start();

        // Step 4: Produce some input data to the input topic.
        produceInputData();

        // Step 5: Verify the application's output data.
        verifyOutputData(streams);
    }

    private Properties getStreamsConfiguration() {
      // TODO: add code
    }

    private void produceInputData() throws Exception {
      // TODO: add code
    }

    private void verifyOutputData(KafkaStreams streams) throws Exception {
      // TODO: add code
    }
}

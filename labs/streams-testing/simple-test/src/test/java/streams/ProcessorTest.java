package streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProcessorTest {
    private TopologyTestDriver testDriver;
    private KeyValueStore<String, Long> store;

    private Serde<String> stringSerde = new Serdes.StringSerde();
    private Serde<Long> longSerde = new Serdes.LongSerde();
    private TestInputTopic<String, Long> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;

    @Before
    public void setup() {
        TopologyProvider provider = new TopologyProvider();
        Topology topology = provider.getTopology();

        ConfigProvider configProvider = new ConfigProvider();
        Properties config = configProvider.getConfig("dummy-bootstrap-server:9092");

        testDriver = new TopologyTestDriver(topology, config);

        // setup test topics
        inputTopic = testDriver.createInputTopic("input-topic", stringSerde.serializer(), longSerde.serializer());
        outputTopic = testDriver.createOutputTopic("result-topic", stringSerde.deserializer(), longSerde.deserializer());

        // pre-populate store
        store = testDriver.getKeyValueStore("aggStore");
        store.put("a", 21L);
    }

    @After
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void shouldFlushStoreForFirstInput() {
        // TODO: add test code here
        inputTopic.pipeInput("a", 1L);
        assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("a", 21L)));
        assertThat(outputTopic.isEmpty(), is(true));
    }

    @Test
    public void shouldNotUpdateStoreForSmallerValue() {
        // TODO: add test code here
        inputTopic.pipeInput("a", 10L);
        assertThat(store.get("a"), equalTo(21L));
        assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("a", 21L)));
        assertThat(outputTopic.isEmpty(), is(true));
    }

    @Test
    public void shouldUpdateStoreForLargerValue() {
        // TODO: add test code here
        inputTopic.pipeInput("a", 100L);
        assertThat(store.get("a"), equalTo(100L));
        assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("a", 100L)));
        assertThat(outputTopic.isEmpty(), is(true));
    }
    
    @Test
    public void shouldUpdateStoreForNewKey() {
        // TODO: add test code here
        inputTopic.pipeInput("new", 7L);
        assertThat(store.get("a"), equalTo(21L));
        assertThat(store.get("new"), equalTo(7L));
        assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("a", 21L)));
        assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("new", 7L)));
        assertThat(outputTopic.isEmpty(), is(true));
    }
    
    @Test
    public void shouldPunctuateIfEventTimeAdvances() {
        // TODO: add test code here
        var eventTime = Instant.now();
        inputTopic.pipeInput("a", 1L, eventTime);
        assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("a", 21L)));

        inputTopic.pipeInput("a", 1L, eventTime);
        assertThat(outputTopic.isEmpty(), is(true));

        inputTopic.pipeInput("a", 1L, eventTime.plusSeconds(11));
        assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("a", 21L)));
        assertThat(outputTopic.isEmpty(), is(true));
    }
    
    @Test
    public void shouldPunctuateIfWallClockTimeAdvances() {
        // TODO: add test code here
        // if we don't advance wall clock time, punctuator won't be called and output topic will still be empty
        testDriver.advanceWallClockTime(Duration.ofSeconds(61));
        assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("a", 21L)));
        assertThat(outputTopic.isEmpty(), is(true));
    }
}

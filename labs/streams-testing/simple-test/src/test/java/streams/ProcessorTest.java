package streams;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.Topology;

import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.hamcrest.MatcherAssert;

import java.util.*;

import org.junit.*;
import static org.hamcrest.CoreMatchers.equalTo;

public class ProcessorTest {
    private TopologyTestDriver testDriver;
    private KeyValueStore<String, Long> store;
    
    private StringDeserializer stringDeserializer = new StringDeserializer();
    private LongDeserializer longDeserializer = new LongDeserializer();
    private ConsumerRecordFactory<String, Long> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new LongSerializer());
    
    @Before
    public void setup() {
        TopologyProvider provider = new TopologyProvider();
        Topology topology = provider.getTopology();
    
        ConfigProvider configProvider = new ConfigProvider();
        Properties config = configProvider.getConfig("dummy-bootstrap-server:9092");

        testDriver = new TopologyTestDriver(topology, config);
    
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

    }

    @Test
    public void shouldNotUpdateStoreForSmallerValue() {
        testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, 9999L));
        MatcherAssert.assertThat(store.get("a"), equalTo(21L));
        OutputVerifier.compareKeyValue(testDriver.readOutput(
            "result-topic", stringDeserializer, longDeserializer), "a", 21L);
        Assert.assertNull(testDriver.readOutput(
            "result-topic", stringDeserializer, longDeserializer));
    }

    
    @Test
    public void shouldUpdateStoreForLargerValue() {
        // TODO: 
    }
    
    @Test
    public void shouldUpdateStoreForNewKey() {
        // TODO:
    }
    
    @Test
    public void shouldPunctuateIfEventTimeAdvances() {
        // TODO:
    }
    
    @Test
    public void shouldPunctuateIfWallClockTimeAdvances() {
        // TODO:
    }
}

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
        Properties config = configProvider.getConfig("dummy:1234");

        testDriver = new TopologyTestDriver(topology, config);
    
        // pre-populate store
        store = testDriver.getKeyValueStore("aggStore");
        store.put("a", 21L);
    }
    
    @After
    public void tearDown() {
        testDriver.close();
    }
}
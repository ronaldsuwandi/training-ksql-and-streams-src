package streams;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;


public class MapSample {
    public static void main(String[] args) {
        System.out.println("*** Starting Map Sample Application ***");
	
        Properties settings = new Properties();
        // TODO: Configure application.id and bootstrap.servers properties using the StreamsConfig class

        
        // TODO: Define the processor topology using the StreamsBuilder class


        // TODO: Create the KafkaStreams app


        // TODO: Add a shutdown hook for graceful termination and start the app

    }
}


package streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;


public class MapSample {
    /*
This application reads data from a topic whose keys are integers and whose values are sentence strings.
The input values are transformed to lower-case and output to a new topic.
*/
    public static void main(String[] args) {
        System.out.println("*** Starting Map Sample Application ***");


        // TODO: Configure application.id and bootstrap.servers properties using the StreamsConfig class
        var settings = new Properties();
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-sample-v0.1.0");
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");

        // TODO: Define the processor topology using the StreamsBuilder class
        final Serde<String> stringSerde = Serdes.String();
        var builder = new StreamsBuilder();
        KStream<String, String> lines = builder
                .stream("lines-topic", Consumed.with(stringSerde, stringSerde));
        KStream<String, String> transformed = lines
                .mapValues(value -> value.toLowerCase());
        transformed.to("lines-lower-topic", Produced.with(stringSerde, stringSerde));
        Topology topology = builder.build();

        // TODO: Create the KafkaStreams app
        KafkaStreams streams = new KafkaStreams(topology, settings);


        // TODO: Add a shutdown hook for graceful termination and start the app
        final var latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("### Stopping Map Sample Application ###");
            streams.close();
            latch.countDown();
        }));

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}


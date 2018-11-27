package streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import java.util.Properties;

public class WordCountSample {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("*** Starting Word Count Sample Application ***");

        Properties config = new ConfigProvider().getConfig("kafka:9092");
        Topology topology = new TopologyProvider().getTopology();
        KafkaStreams streams = new KafkaStreams(topology, config);
        streams.start();

        System.out.println("*** Word Count Sample Application started ***");

        new MetricsReporter().reportMetrics(streams);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("### Stopping Word Count Sample Application ###");
            streams.close();
        }));
    }
}

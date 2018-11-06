package streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

public class SecureAppSample {
    public static void main(String[] args) throws Exception {
        Properties config = new ConfigProvider().getConfig();
        Topology topology = new TopologyProvider().getTopology();
        final KafkaStreams streams = new KafkaStreams(topology, config);

        streams.cleanUp();

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("### Stopping the Security Sample Application ###");
            streams.close();
        }));
    }
}

package streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

public class SecureAppSample {
    public static void main(String[] args) throws Exception {
        Properties config = new ConfigProvider().getConfig();
        Topology topology = new TopologyProvider().getTopology();
        final KafkaStreams streams = new KafkaStreams(topology, config);

        // Always (and unconditionally) clean local state prior to starting the processing topology.
        // We opt for this unconditional call here because this will make it easier for you to play around with the example
        // when resetting the application for doing a re-run (via the Application Reset Tool,
        // https://docs.confluent.io/current/streams/developer-guide/app-reset-tool.html).
        //
        // The drawback of cleaning up local state prior is that your app must rebuilt its local state from scratch, which
        // will take time and will require reading all the state-relevant data from the Kafka cluster over the network.
        // Thus in a production scenario you typically do not want to clean up always as we do here but rather only when it
        // is truly needed, i.e., only under certain conditions (e.g., the presence of a command line flag for your app).
        // See `ApplicationResetExample.java` for a production-like example:
        // https://github.com/confluentinc/kafka-streams-examples/blob/5.3.0-post/src/main/java/io/confluent/examples/streams/ApplicationResetExample.java
        
        streams.cleanUp();

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("### Stopping the Security Sample Application ###");
            streams.close();
        }));
    }
}

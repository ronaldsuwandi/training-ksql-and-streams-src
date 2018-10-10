package streams;

import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.StreamsBuilder;

import java.util.Properties;

public class TopologyProvider {
    public Topology getTopology() {
        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream("secure-input").to("secure-output");
        return builder.build();
    }
}
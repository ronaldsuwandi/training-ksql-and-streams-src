package streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class SecureAppSample {
    public static void main(String[] args) throws Exception {
        Properties config = new ConfigProvider().getConfig();
        Topology topology = new TopologyProvider().getTopology();
        final KafkaStreams streams = new KafkaStreams(topology, config);


        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("### Stopping Security Sample Application ###");
            streams.close();
            latch.countDown();
        }));

        try{
           streams.start(); 
           latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}

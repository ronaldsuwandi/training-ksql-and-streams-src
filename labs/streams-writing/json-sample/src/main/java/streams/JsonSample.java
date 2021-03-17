package streams;

import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import java.util.HashMap;
import java.util.Properties;

public class JsonSample {

    /*
    This application reads temperature data from an input topic,
    filters for teperatures higher than 25 degrees,
    and outputs that data to a new output topic.
    */

    final static String APPLICATION_ID = "json-sample-v0.1.0";
    final static String APPLICATION_NAME = "JSON Sample";

    // POJO class
    static public class TempReading {
        public String station;
        public Double temperature;
        public Long timestamp;
    }

    public static void main(String[] args) {
        System.out.printf("*** Starting %s Application ***%n", APPLICATION_NAME);

        Properties config = getConfig();
        Topology topology = getTopology();
        KafkaStreams streams =  startApp(config, topology);

        setupShutdownHook(streams);
    }

    private static Topology getTopology(){
        StreamsBuilder builder = new StreamsBuilder();

        final Serde<String> stringSerde = Serdes.String();
        final Serde<TempReading> temperatureSerde = getJsonSerde();

        // TODO: here we construct the Kafka Streams topology
        builder.stream("temperatures-topic", Consumed.with(stringSerde, temperatureSerde))
                .filter((key, value) -> value.temperature > 25)
                .to("high-temperatures-topic", Produced.with(stringSerde, temperatureSerde));
        return builder.build();
    }

    private static Serde<TempReading> getJsonSerde(){
        
        // TODO: create a JSON serde for the TempReading class using KafkaJson serdes
        var serdeProps = new HashMap<String, Object>();
        serdeProps.put("json.value.type", TempReading.class);

        final var temperatureSerializer = new KafkaJsonSerializer<TempReading>();
        temperatureSerializer.configure(serdeProps, false);

        final var temperatureDeserializer = new KafkaJsonDeserializer<TempReading>();
        temperatureDeserializer.configure(serdeProps, false);;

        return Serdes.serdeFrom(temperatureSerializer, temperatureDeserializer);
    }

    private static Properties getConfig(){
        Properties settings = new Properties();
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        return settings;        
    }

    private static KafkaStreams startApp(Properties config, Topology topology){
        KafkaStreams streams = new KafkaStreams(topology, config);
        streams.start();
        return streams;
    }

    private static void setupShutdownHook(KafkaStreams streams){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.printf("### Stopping %s Application ###%n", APPLICATION_NAME);
            streams.close();
        }));
    }
}

package io.confluent.training.streams;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

public class StreamsApp {
    final static String APPLICATION_ID = "streams-app-v0.1.0";
    final static String APPLICATION_NAME = "Kafka Streams App";
    final static String INPUT_TOPIC = "temperature-readings";
    final static String OUTPUT_TOPIC = "max-temperatures";

    public static void main(String[] args) {
        System.out.printf("*** Starting %s Application ***%n", APPLICATION_NAME);

        StreamsConfig config = getConfig();
        Topology topology = getTopology();
        KafkaStreams streams =  startApp(config, topology);

        setupShutdownHook(streams);
    }

    private static StreamsConfig getConfig(){
        Properties settings = new Properties();
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");

        // Interceptor configuration
        settings.put(StreamsConfig.PRODUCER_PREFIX + ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
            "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor");
        settings.put(StreamsConfig.CONSUMER_PREFIX + ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
            "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor");

        StreamsConfig config = new StreamsConfig(settings);
        return config;        
    }

    private static Topology getTopology() {
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Integer> integerSerde = Serdes.Integer();
        final Serde<Windowed<String>> windowedStringSerde = new WindowedSerde<>(stringSerde);
        final StreamsBuilder builder = new StreamsBuilder();
        builder
            .stream(INPUT_TOPIC, Consumed.with(stringSerde, integerSerde))
            .groupByKey(Serialized.with(stringSerde, integerSerde))
            .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(60)))
            .reduce((aggValue, newValue) -> newValue > aggValue ? newValue : aggValue)
            .toStream()
            .to(OUTPUT_TOPIC, Produced.with(windowedStringSerde, integerSerde));
        return builder.build();
    }

    private static KafkaStreams startApp(StreamsConfig config, Topology topology){
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
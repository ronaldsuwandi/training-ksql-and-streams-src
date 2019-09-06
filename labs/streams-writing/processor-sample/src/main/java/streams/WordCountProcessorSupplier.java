package streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.util.Locale;

public class WordCountProcessorSupplier implements ProcessorSupplier<String, String> {
    public class WordCountProcessor implements Processor<String, String> {
        private ProcessorContext context;
        private KeyValueStore<String, Integer> kvStore;

        @Override
        @SuppressWarnings("unchecked")
        public void init(final ProcessorContext context) {
            // TODO: Create a ProcessorContext, schedule a Punctuator that pushes the kv store downstream every second, and retrieve the "Counts" state store
            this.context = context;
            this.context.schedule(Duration.ofMillis(1000), PunctuationType.STREAM_TIME, new Punctuator() {
                @Override
                public void punctuate(long timestamp) {
                    try (KeyValueIterator<String, Integer> iter = kvStore.all()) {
                        System.out.println("----------- " + timestamp + " ----------- ");
                        while (iter.hasNext()) {
                            KeyValue<String, Integer> entry = iter.next();
                            System.out.println("[" + entry.key + ", " + entry.value + "]");
                            context.forward(entry.key, entry.value.toString());
                        }
                    }
                }
            });
            this.kvStore = (KeyValueStore<String, Integer>) context.getStateStore("Counts");
        }

        @Override
        public void process(String recordKey, String recordValue) {
            // TODO: Process each record to update the state store's word counts


            /*
            Requesting a commit. This will flush state to local state stores
            and commit consumer offsets for input topics.
            */
            context.commit();
        }
    
        @Override
        public void close() {}
    }

    @Override
    public Processor<String, String> get() {
        return new WordCountProcessor();
    }
}

package streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Locale;
import java.util.Properties;

public class WordCountProcessorSupplier implements ProcessorSupplier<String, String> {
    public class WordCountProcessor implements Processor<String, String> {
        private ProcessorContext context;
        private KeyValueStore<String, Integer> kvStore;

        @Override
        @SuppressWarnings("unchecked")
        public void init(final ProcessorContext context) {
            // TODO
        }

        @Override
        public void process(String dummy, String line) {
            // TODO
        }
    
        @Override
        public void close() {}
    }

    @Override
    public Processor<String, String> get() {
        return new WordCountProcessor();
    }
}
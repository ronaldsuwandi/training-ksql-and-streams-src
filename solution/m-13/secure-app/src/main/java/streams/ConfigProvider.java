package streams;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProvider {
    public Properties getConfig() throws Exception {
        final Properties props = new Properties();
        InputStream input = new FileInputStream("/etc/kafka/secrets/secureapp-sample.properties");
        props.load(input);
        return props;
    }
}

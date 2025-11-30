package dora.messenger.server.chat.file;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ChatStorageConfiguration {

    @Bean
    public Path storageDirectory() {
        return Paths.get("storage");
    }
}

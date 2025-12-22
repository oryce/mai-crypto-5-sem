package dora.messenger.server.chat.file;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ChatStorageConfiguration {

    @Bean
    public Path storageDirectory() throws IOException {
        Path storageDirectory = Paths.get("storage");
        Files.createDirectories(storageDirectory);
        return storageDirectory;
    }
}

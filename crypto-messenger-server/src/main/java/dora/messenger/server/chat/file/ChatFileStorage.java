package dora.messenger.server.chat.file;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class ChatFileStorage {

    private final Path storageDirectory;

    public ChatFileStorage(Path storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public void put(ChatFile file, InputStream fileStream) throws IOException {
        Path filePath = filePath(file);
        Files.copy(fileStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public Resource get(ChatFile file) {
        Path filePath = filePath(file);
        return new FileSystemResource(filePath);
    }

    public void delete(ChatFile file) throws IOException {
        Path filePath = filePath(file);
        Files.deleteIfExists(filePath);
    }

    private Path filePath(ChatFile file) {
        String fileName = file.getId().toString();
        return storageDirectory.resolve(fileName);
    }
}

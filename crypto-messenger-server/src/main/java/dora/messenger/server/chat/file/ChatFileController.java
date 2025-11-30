package dora.messenger.server.chat.file;

import dora.messenger.protocol.chat.file.ChatFileDto;
import dora.messenger.server.chat.Blob;
import dora.messenger.server.chat.file.ChatFileService.StreamBlob;
import dora.messenger.server.user.User;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
public class ChatFileController {

    private final ChatFileService files;
    private final ChatFile.Mapper mapper;

    public ChatFileController(ChatFileService files, ChatFile.Mapper mapper) {
        this.files = files;
        this.mapper = mapper;
    }

    /**
     * @param file       encrypted file
     * @param fileIv     file initialization vector (Base64-encoded)
     * @param filename   encrypted filename (Base64-encoded)
     * @param filenameIv filename initialization vector (Base64-encoded)
     */
    public record UploadFile(
        MultipartFile file,
        String fileIv,
        String filename,
        String filenameIv
    ) {
    }

    @Operation(summary = "Upload File")
    @PutMapping(
        value = "/sessions/{sessionId}/files",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ChatFileDto uploadFile(
        @PathVariable("sessionId") UUID sessionId,
        @AuthenticationPrincipal User uploader,
        @ModelAttribute UploadFile uploadFile
    ) throws IOException {
        ChatFile file = files.putFile(
            sessionId,
            uploader,
            new StreamBlob(uploadFile.fileIv(), uploadFile.file().getInputStream()),
            new Blob(uploadFile.filenameIv(), uploadFile.filename())
        );
        return mapper.map(file);
    }

    @Operation(summary = "Get File")
    @GetMapping(
        value = "/sessions/{sessionId}/files/{fileId}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public Resource getFile(
        @PathVariable("sessionId") UUID sessionId,
        @PathVariable("fileId") UUID fileId,
        @AuthenticationPrincipal User user
    ) {
        return files.getFile(sessionId, fileId, user);
    }

    @Operation(summary = "Delete File")
    @DeleteMapping("/sessions/{sessionId}/files/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(
        @PathVariable("sessionId") UUID sessionId,
        @PathVariable("fileId") UUID fileId,
        @AuthenticationPrincipal User user
    ) throws IOException {
        files.deleteFile(sessionId, fileId, user);
    }
}

package dora.messenger.server.chat;

import dora.messenger.protocol.chat.BlobDto;
import jakarta.persistence.Embeddable;
import org.mapstruct.MappingConstants.ComponentModel;

/**
 * @param iv         initialization vector (Base64-encoded)
 * @param ciphertext ciphertext (Base64-encoded)
 */
@Embeddable
public record Blob(String iv, String ciphertext) {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        BlobDto map(Blob blob);

        Blob map(BlobDto blobDto);
    }
}

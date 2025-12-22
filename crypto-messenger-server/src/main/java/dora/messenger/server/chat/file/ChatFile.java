package dora.messenger.server.chat.file;

import dora.messenger.protocol.chat.file.ChatFileDto;
import dora.messenger.server.chat.Blob;
import dora.messenger.server.chat.session.ChatSession;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.UUID;

@Entity
@Table(name = "chat_files")
@EntityListeners(ChatFileEntityListener.class)
public class ChatFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "session_id")
    @ManyToOne
    private ChatSession session;

    @Column(name = "iv")
    private String iv;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "iv", column = @Column(name = "filename_iv")),
        @AttributeOverride(name = "ciphertext", column = @Column(name = "filename_ciphertext"))
    })
    private Blob filename;

    //region Accessors
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession chatSession) {
        this.session = chatSession;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public Blob getFilename() {
        return filename;
    }

    public void setFilename(Blob filename) {
        this.filename = filename;
    }
    //endregion

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING, uses = { Blob.Mapper.class })
    public interface Mapper {

        ChatFileDto map(ChatFile file);
    }
}

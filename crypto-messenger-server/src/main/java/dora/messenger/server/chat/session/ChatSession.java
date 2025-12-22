package dora.messenger.server.chat.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dora.messenger.protocol.chat.session.ChatSessionDto;
import dora.messenger.server.chat.Chat;
import dora.messenger.server.chat.file.ChatFile;
import dora.messenger.server.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "chat_sessions",
    indexes = @Index(name = "idx_chat_session_chat_id", columnList = "chat_id", unique = true)
)
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "chat_id")
    @OneToOne
    private Chat chat;

    @JoinColumn(name = "initiator_id")
    @ManyToOne
    private User initiator;

    @JoinColumn(name = "responder_id")
    @ManyToOne
    private User responder;

    @Column(name = "established")
    private boolean established;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // HACK (~oryce): Events should have separate entities.
    private List<ChatFile> files;

    public boolean isResponder(User user) {
        return user.getId().equals(responder.getId());
    }

    public boolean involvesUser(User user) {
        return user.getId().equals(initiator.getId())
            || user.getId().equals(responder.getId());
    }

    //region Accessors
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    public User getResponder() {
        return responder;
    }

    public void setResponder(User responder) {
        this.responder = responder;
    }

    public boolean isEstablished() {
        return established;
    }

    public void setEstablished(boolean established) {
        this.established = established;
    }

    public List<ChatFile> getFiles() {
        return files;
    }

    public void setFiles(List<ChatFile> files) {
        this.files = files;
    }
    //endregion

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        @Mapping(target = "sessionId", source = "id")
        @Mapping(target = "chatId", source = "chat.id")
        ChatSessionDto toDto(ChatSession chatSession);
    }
}

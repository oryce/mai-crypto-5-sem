package dora.messenger.server.chat;

import dora.messenger.protocol.chat.ChatDto;
import dora.messenger.server.chat.session.ChatSession;
import dora.messenger.server.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.UUID;

@Entity
@Table(
    name = "chats",
    indexes = {
        @Index(name = "idx_chat_first_user_id", columnList = "first_user_id"),
        @Index(name = "idx_chat_second_user_id", columnList = "second_user_id")
    }
)
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "first_user_id")
    @ManyToOne
    private User firstUser;

    @JoinColumn(name = "second_user_id")
    @ManyToOne
    private User secondUser;

    @Column(name = "dh_group")
    @Enumerated(EnumType.STRING)
    private DiffieHellmanGroup dhGroup;

    @Column(name = "algorithm")
    @Enumerated(EnumType.STRING)
    private Algorithm algorithm;

    @Column(name = "cipher_mode")
    @Enumerated(EnumType.STRING)
    private CipherMode cipherMode;

    @Column(name = "padding")
    @Enumerated(EnumType.STRING)
    private Padding padding;

    @OneToOne(mappedBy = "chat", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private ChatSession session;

    public User getOtherUser(User user) {
        if (user.getId().equals(firstUser.getId())) return secondUser;
        if (user.getId().equals(secondUser.getId())) return firstUser;
        throw new IllegalArgumentException("User is not a chat participant");
    }

    public String getName(User user) {
        User otherUser = getOtherUser(user);
        return "%s %s".formatted(otherUser.getFirstName(), otherUser.getLastName());
    }

    public boolean involvesUser(User user) {
        return user.getId().equals(firstUser.getId())
            || user.getId().equals(secondUser.getId());
    }

    //region Accessors
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getFirstUser() {
        return firstUser;
    }

    public void setFirstUser(User firstUser) {
        this.firstUser = firstUser;
    }

    public User getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(User secondUser) {
        this.secondUser = secondUser;
    }

    public DiffieHellmanGroup getDhGroup() {
        return dhGroup;
    }

    public void setDhGroup(DiffieHellmanGroup dhGroup) {
        this.dhGroup = dhGroup;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public CipherMode getCipherMode() {
        return cipherMode;
    }

    public void setCipherMode(CipherMode encryptionMode) {
        this.cipherMode = encryptionMode;
    }

    public Padding getPadding() {
        return padding;
    }

    public void setPadding(Padding paddingMode) {
        this.padding = paddingMode;
    }

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
    }
    //endregion

    public enum DiffieHellmanGroup {

        FFDHE2048,
        FFDHE3072,
        FFDHE4096,
        FFDHE6144,
        FFDHE8192
    }

    public enum Algorithm {

        RC5,
        RC6
    }

    public enum CipherMode {

        CBC,
        CFB,
        CTR,
        ECB,
        OFB,
        PCBC,
        RANDOM_DELTA
    }

    public enum Padding {

        ANSI_X923,
        ISO_10126,
        PKCS7,
        ZEROS
    }

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        @Mapping(target = "name", source = "name")
        ChatDto toDto(Chat chat, String name);

        Chat.DiffieHellmanGroup dhGroupFromDto(ChatDto.DiffieHellmanGroup dhGroup);

        Chat.Algorithm algorithmFromDto(ChatDto.Algorithm algorithm);

        CipherMode cipherModeFromDto(ChatDto.CipherMode cipherMode);

        Padding paddingFromDto(ChatDto.Padding padding);
    }
}

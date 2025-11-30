package dora.messenger.client.store.chat;

import dora.crypto.SymmetricCipher.CipherModeType;
import dora.crypto.SymmetricCipher.PaddingType;
import dora.crypto.block.BlockCipher;
import dora.crypto.block.rc5.Rc5BlockCipher;
import dora.crypto.block.rc5.Rc5Parameters;
import dora.crypto.block.rc6.Rc6BlockCipher;
import dora.crypto.block.rc6.Rc6Parameters;
import dora.crypto.dh.DiffieHellmanGroup;
import dora.messenger.protocol.chat.ChatDto;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;

import java.util.Comparator;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @param id         chat ID
 * @param name       chat name
 * @param dhGroup    Diffie-Hellman group
 * @param algorithm  encryption algorithm type
 * @param cipherMode cipher mode type
 * @param padding    padding type
 */
public record Chat(
    @NotNull UUID id,
    @NotNull String name,
    @NotNull DiffieHellmanGroupId dhGroup,
    @NotNull Algorithm algorithm,
    @NotNull CipherModeType cipherMode,
    @NotNull PaddingType padding
) implements Comparable<Chat> {

    public Chat {
        requireNonNull(id, "chat ID");
        requireNonNull(name, "name");
        requireNonNull(dhGroup, "Diffie-Hellman group");
        requireNonNull(algorithm, "algorithm");
        requireNonNull(cipherMode, "cipher mode");
        requireNonNull(padding, "padding");
    }

    @Override
    public int compareTo(@NotNull Chat other) {
        return Comparator.comparing(Chat::name)
            .thenComparing(Chat::id)
            .compare(this, other);
    }

    public enum DiffieHellmanGroupId {

        FFDHE2048(DiffieHellmanGroup.FFDHE2048),
        FFDHE3072(DiffieHellmanGroup.FFDHE3072),
        FFDHE4096(DiffieHellmanGroup.FFDHE4096),
        FFDHE6144(DiffieHellmanGroup.FFDHE6144),
        FFDHE8192(DiffieHellmanGroup.FFDHE8192);

        private final DiffieHellmanGroup group;

        DiffieHellmanGroupId(DiffieHellmanGroup group) {
            this.group = group;
        }

        public DiffieHellmanGroup group() {
            return group;
        }
    }

    public enum Algorithm {

        /**
         * Configures RC5-32/20/16.
         */
        RC5(() -> new Rc5BlockCipher(new Rc5Parameters(Rc5Parameters.WordSize.WORD_SIZE_32, 20, 16))),
        /**
         * Configures RC6-32/20/16
         */
        RC6(() -> new Rc6BlockCipher(new Rc6Parameters(Rc6Parameters.WordSize.WORD_SIZE_32, 20, 16)));

        private final Supplier<BlockCipher> cipherCreator;

        Algorithm(Supplier<BlockCipher> cipherCreator) {
            this.cipherCreator = cipherCreator;
        }

        public BlockCipher createCipher() {
            return cipherCreator.get();
        }
    }

    //region Mapper
    public static final Mapper MAPPER = Mappers.getMapper(Mapper.class);

    @org.mapstruct.Mapper
    public interface Mapper {

        Chat toDomain(ChatDto chat);

        DiffieHellmanGroupId dhGroupToDomain(ChatDto.DiffieHellmanGroup dhGroup);
        ChatDto.DiffieHellmanGroup dhGroupToDto(DiffieHellmanGroupId dhGroup);

        Algorithm algorithmToDomain(ChatDto.Algorithm algorithm);
        ChatDto.Algorithm algorithmToDto(Algorithm algorithm);

        CipherModeType cipherModeToDomain(ChatDto.CipherMode cipherMode);
        ChatDto.CipherMode cipherModeToDto(CipherModeType cipherMode);

        PaddingType paddingToDomain(ChatDto.Padding padding);
        ChatDto.Padding paddingToDto(PaddingType padding);
    }
    //endregion
}

package dora.messenger.client.store.chat;

import dora.crypto.SymmetricCipher;
import dora.crypto.block.BlockCipher;
import dora.messenger.client.persistence.ChatSession;
import dora.messenger.protocol.chat.BlobDto;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import static java.util.Objects.requireNonNull;

final class EncryptionSupport {

    private EncryptionSupport() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static byte[] generateIv(@NotNull Chat chat, @NotNull SecureRandom random) {
        requireNonNull(chat, "chat");
        requireNonNull(random, "random");

        BlockCipher cipher = chat.algorithm().createCipher();

        byte[] iv = new byte[cipher.blockSize()];
        random.nextBytes(iv);

        return iv;
    }

    public static BlobDto encryptString(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        @NotNull String plaintext,
        byte @NotNull [] iv
    ) throws InterruptedException {
        requireNonNull(plaintext, "plaintext");
        return encryptBytes(chat, session, plaintext.getBytes(StandardCharsets.UTF_8), iv);
    }

    public static BlobDto encryptBytes(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        byte @NotNull [] plaintext,
        byte @NotNull [] iv
    ) throws InterruptedException {
        requireNonNull(chat, "chat");
        requireNonNull(session, "chat session");
        requireNonNull(plaintext, "plaintext bytes");
        requireNonNull(iv, "initialization vector");

        SymmetricCipher cipher = createCipher(chat, session, iv);
        byte[] ciphertext = cipher.encrypt(plaintext);

        Base64.Encoder encoder = Base64.getEncoder();
        return new BlobDto(encoder.encodeToString(iv), encoder.encodeToString(ciphertext));
    }

    public static String decryptString(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        @NotNull BlobDto blob
    ) throws InterruptedException {
        return new String(decryptBytes(chat, session, blob), StandardCharsets.UTF_8);
    }

    public static byte[] decryptBytes(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        @NotNull BlobDto blob
    ) throws InterruptedException {
        requireNonNull(chat, "chat");
        requireNonNull(session, "chat session");
        requireNonNull(blob, "blob to decrypt");

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] iv = decoder.decode(blob.iv());
        byte[] ciphertext = decoder.decode(blob.ciphertext());

        SymmetricCipher cipher = createCipher(chat, session, iv);
        return cipher.decrypt(ciphertext);
    }

    public static SymmetricCipher createCipher(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        byte @NotNull [] iv
    ) {
        requireNonNull(chat, "chat");
        requireNonNull(session, "chat session");
        requireNonNull(iv, "initialization vector");

        BlockCipher cipher = chat.algorithm().createCipher();

        int keySize = cipher.keySizes().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Cipher does not advertise key sizes"));

        byte[] key = Arrays.copyOf(session.getSharedSecret(), keySize);

        return SymmetricCipher.builder()
            .cipher(cipher)
            .mode(chat.cipherMode())
            .padding(chat.padding())
            .key(key)
            .iv(iv)
            .build();
    }
}

package dora.crypto;

import dora.crypto.block.mode.CipherMode;
import dora.crypto.block.mode.Parameters;
import dora.crypto.block.padding.Padding;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

public final class SymmetricCipherContext {

    private final CipherMode cipherMode;
    private final Padding padding;

    public SymmetricCipherContext(
        @NotNull CipherMode cipherMode,
        @NotNull Padding padding
    ) {
        this.cipherMode = requireNonNull(cipherMode, "cipher mode");
        this.padding = requireNonNull(padding, "padding");
    }

    public void init(byte @NotNull[] key, @NotNull Parameters parameters) {
        cipherMode.init(
            requireNonNull(key, "key"),
            requireNonNull(parameters, "parameters")
        );
    }

    public byte[] encrypt(byte @NotNull [] data) throws InterruptedException {
        byte[] padded = padding.pad(requireNonNull(data, "data"), cipherMode.blockSize());
        return cipherMode.encrypt(padded);
    }

    public byte[] decrypt(byte @NotNull[] data) throws InterruptedException {
        byte[] decrypted = cipherMode.decrypt(requireNonNull(data, "data"));
        return padding.unpad(decrypted, cipherMode.blockSize());
    }

    public @NotNull InputStream encryptingInputStream(@NotNull InputStream stream) {
        return new CipherInputStream(cipherMode, padding, stream, true);
    }

    public @NotNull InputStream decryptingInputStream(@NotNull InputStream stream) {
        return new CipherInputStream(cipherMode, padding, stream, false);
    }

    public @NotNull OutputStream encryptingOutputStream(@NotNull OutputStream stream) {
        return new CipherOutputStream(cipherMode, padding, stream, true);
    }

    public @NotNull OutputStream decryptingOutputStream(@NotNull OutputStream stream) {
        return new CipherOutputStream(cipherMode, padding, stream, false);
    }
}

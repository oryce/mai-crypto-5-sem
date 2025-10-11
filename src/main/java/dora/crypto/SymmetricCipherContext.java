package dora.crypto;

import dora.crypto.mode.CipherMode;
import dora.crypto.mode.Parameters;
import dora.crypto.padding.Padding;

import static java.util.Objects.requireNonNull;

public final class SymmetricCipherContext {

    private final CipherMode cipherMode;
    private final Padding padding;

    private byte[] key;

    public SymmetricCipherContext(CipherMode cipherMode, Padding padding) {
        this.cipherMode = requireNonNull(cipherMode, "cipher mode");
        this.padding = requireNonNull(padding, "padding");
    }

    public void init(byte[] key, Parameters parameters) {
        this.key = requireNonNull(key, "key");
        cipherMode.init(requireNonNull(parameters, "parameters"));
    }

    public byte[] encrypt(byte[] data) throws InterruptedException {
        if (key == null) {
            throw new IllegalStateException("Cipher is not initialized");
        }

        byte[] padded = padding.pad(requireNonNull(data, "data"), cipherMode.blockSize());
        return cipherMode.encrypt(padded, key);
    }

    public byte[] decrypt(byte[] data) throws InterruptedException {
        if (key == null) {
            throw new IllegalStateException("Cipher is not initialized");
        }

        byte[] decrypted = cipherMode.decrypt(requireNonNull(data, "data"), key);
        return padding.unpad(decrypted, cipherMode.blockSize());
    }
}

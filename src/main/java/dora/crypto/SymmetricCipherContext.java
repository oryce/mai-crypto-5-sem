package dora.crypto;

import dora.crypto.mode.CipherMode;
import dora.crypto.mode.Parameters;
import dora.crypto.padding.Padding;

import static java.util.Objects.requireNonNull;

public final class SymmetricCipherContext {

    private final CipherMode cipherMode;
    private final Padding padding;

    public SymmetricCipherContext(CipherMode cipherMode, Padding padding) {
        this.cipherMode = requireNonNull(cipherMode, "cipher mode");
        this.padding = requireNonNull(padding, "padding");
    }

    public void init(byte[] key, Parameters parameters) {
        cipherMode.init(
            requireNonNull(key, "key"),
            requireNonNull(parameters, "parameters")
        );
    }

    public byte[] encrypt(byte[] data) throws InterruptedException {
        byte[] padded = padding.pad(requireNonNull(data, "data"), cipherMode.blockSize());
        return cipherMode.encrypt(padded);
    }

    public byte[] decrypt(byte[] data) throws InterruptedException {
        byte[] decrypted = cipherMode.decrypt(requireNonNull(data, "data"));
        return padding.unpad(decrypted, cipherMode.blockSize());
    }
}

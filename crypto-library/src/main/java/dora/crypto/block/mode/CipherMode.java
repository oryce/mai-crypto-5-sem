package dora.crypto.block.mode;

import dora.crypto.block.BlockCipher;
import org.jetbrains.annotations.NotNull;

/**
 * Cipher mode wraps a {@link BlockCipher} and operates on padded data to
 * provide encryption and decryption.
 * <p>
 * Cipher modes must be initialized prior to usage. Consult the implementation's
 * documentation on the appropriate parameter types.
 */
public interface CipherMode {

    /**
     * Returns the underlying block cipher.
     */
    BlockCipher cipher();

    /**
     * Returns the cipher's block size. Useful for (un)padding data.
     */
    default int blockSize() {
        return cipher().blockSize();
    }

    /**
     * Initializes the cipher mode.
     */
    void init(byte @NotNull [] key, Parameters parameters);

    /**
     * Encrypts padded data with the provided key.
     */
    byte[] encrypt(byte @NotNull [] plaintext) throws InterruptedException;

    /**
     * Decrypts padded data with the provided key.
     */
    byte[] decrypt(byte @NotNull [] ciphertext) throws InterruptedException;
}

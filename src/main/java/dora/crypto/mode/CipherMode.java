package dora.crypto.mode;

import dora.crypto.block.BlockCipher;

/**
 * Cipher mode wraps a {@link BlockCipher block cipher} and operates on padded
 * data to provide encryption or decryption.
 * <p>
 * Cipher modes have to be initialized prior to usage. To initialize a
 * cipher mode, provide {@link Parameters}. Different implementations have
 * different parameter requirements.
 */
public interface CipherMode {

    /**
     * Returns the underlying symmetric cipher.
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
    void init(Parameters parameters);

    /**
     * Encrypts padded data with the provided key.
     */
    byte[] encrypt(byte[] data, byte[] key) throws InterruptedException;

    /**
     * Decrypts padded data with the provided key.
     */
    byte[] decrypt(byte[] data, byte[] key) throws InterruptedException;
}

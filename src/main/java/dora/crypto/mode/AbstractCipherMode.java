package dora.crypto.mode;

import dora.crypto.block.BlockCipher;

abstract class AbstractCipherMode implements CipherMode {

    protected final BlockCipher cipher;
    protected final int blockSize;

    public AbstractCipherMode(BlockCipher cipher) {
        this.cipher = cipher;
        this.blockSize = cipher.blockSize();
    }

    @Override
    public BlockCipher cipher() {
        return cipher;
    }

    @Override
    public byte[] encrypt(byte[] plaintext, byte[] key)
    throws InterruptedException {
        if (plaintext.length % blockSize != 0) {
            throw new IllegalArgumentException("Plaintext not multiple of block size");
        }

        cipher.init(key);
        return encryptBlocks(plaintext);
    }

    protected abstract byte[] encryptBlocks(byte[] plaintext)
    throws InterruptedException;

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] key)
    throws InterruptedException {
        if (ciphertext.length % blockSize != 0) {
            throw new IllegalArgumentException("Ciphertext not multiple of block size");
        }

        cipher.init(key);
        return decryptBlocks(ciphertext);
    }

    protected abstract byte[] decryptBlocks(byte[] ciphertext)
    throws InterruptedException;
}

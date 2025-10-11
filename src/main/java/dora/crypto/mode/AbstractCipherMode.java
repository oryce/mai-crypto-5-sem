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
}

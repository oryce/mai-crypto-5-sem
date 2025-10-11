package dora.crypto.mode;

import dora.crypto.block.BlockCipher;

public abstract class AbstractCipherMode implements CipherMode {

    protected final BlockCipher cipher;

    public AbstractCipherMode(BlockCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public BlockCipher cipher() {
        return cipher;
    }
}

package dora.crypto.mode;

import dora.crypto.block.BlockCipher;

public final class PcbcCipherMode extends AbstractCipherMode {

    public PcbcCipherMode(BlockCipher cipher) {
        super(cipher);
    }

    @Override
    public void init(Parameters parameters) {
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] key) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        return new byte[0];
    }
}

package dora.crypto.mode;

import dora.crypto.block.BlockCipher;

public final class RandomDeltaCipherMode extends AbstractCipherMode {

    public RandomDeltaCipherMode(BlockCipher cipher) {
        super(cipher);
    }

    @Override
    public void init(Parameters parameters) {
    }

    @Override
    public byte[] encrypt(byte[] plaintext, byte[] key) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] key) {
        return new byte[0];
    }
}

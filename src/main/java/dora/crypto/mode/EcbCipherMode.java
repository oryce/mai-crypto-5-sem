package dora.crypto.mode;

import dora.crypto.block.BlockCipher;

import java.util.concurrent.ForkJoinPool;

public final class EcbCipherMode extends AbstractCipherMode {

    private final ForkJoinPool pool;

    public EcbCipherMode(BlockCipher cipher, ForkJoinPool pool) {
        super(cipher);
        this.pool = pool;
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

package dora.crypto.block.mode;

import dora.crypto.block.BlockCipher;
import dora.crypto.block.mode.Parameters.NoParameters;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

public final class EcbCipherMode extends AbstractCipherMode {

    private final ForkJoinPool pool;

    public EcbCipherMode(BlockCipher cipher, ForkJoinPool pool) {
        super(cipher);
        this.pool = pool;
    }

    @Override
    protected void initMode(Parameters parameters) {
        if (!(parameters instanceof NoParameters)) {
            throw new IllegalArgumentException("expected NoParameters");
        }
    }

    @Override
    protected byte[] encryptBlocks(byte[] plaintext) throws InterruptedException {
        byte[] ciphertext = new byte[plaintext.length];

        ParallelBlockProcessor.processBlocks(
            plaintext, blockSize, pool, (idx, start, end) -> {
                byte[] plainBlock = Arrays.copyOfRange(plaintext, start, end);
                byte[] cipherBlock = cipher.encrypt(plainBlock);
                System.arraycopy(cipherBlock, 0, ciphertext, start, end - start);

                return null;
            }
        );

        return ciphertext;
    }

    @Override
    protected byte[] decryptBlocks(byte[] ciphertext) throws InterruptedException {
        byte[] plaintext = new byte[ciphertext.length];

        ParallelBlockProcessor.processBlocks(
            plaintext, blockSize, pool, (idx, start, end) -> {
                byte[] cipherBlock = Arrays.copyOfRange(ciphertext, start, end);
                byte[] plainBlock = cipher.decrypt(cipherBlock);
                System.arraycopy(plainBlock, 0, plaintext, start, end - start);

                return null;
            }
        );

        return plaintext;
    }
}

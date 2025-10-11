package dora.crypto.mode;

import dora.crypto.block.BlockCipher;
import dora.crypto.mode.Parameters.IvParameters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public final class CbcCipherMode extends AbstractCipherMode {

    private final ForkJoinPool pool;
    private byte[] iv;

    public CbcCipherMode(BlockCipher cipher, ForkJoinPool pool) {
        super(cipher);
        this.pool = pool;
    }

    @Override
    public void init(Parameters parameters) {
        if (!(parameters instanceof IvParameters(byte[] ivParam))) {
            throw new IllegalArgumentException("expected IvParameters");
        }

        iv = ivParam;
    }

    @Override
    public byte[] encrypt(byte[] plaintext, byte[] key) throws InterruptedException {
        byte[] prevBlock = iv;
        byte[] ciphertext = new byte[plaintext.length];

        for (int i = 0; i < plaintext.length; i += blockSize) {
            byte[] block = Arrays.copyOfRange(plaintext, i, i + blockSize);

            for (int j = 0; j < block.length; j++) {
                block[j] ^= prevBlock[j];
            }

            byte[] encrypted = cipher.encrypt(block);
            System.arraycopy(encrypted, 0, ciphertext, i, encrypted.length);
            prevBlock = encrypted;
        }

        return ciphertext;
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] key) throws InterruptedException {
        List<DecryptResult> results = ParallelBlockProcessor.processBlocks(
            ciphertext, blockSize, pool, (idx, start, end) -> {
                byte[] cipherBlock = Arrays.copyOfRange(ciphertext, start, end);
                byte[] plainBlock = cipher.decrypt(cipherBlock);
                return new DecryptResult(cipherBlock, plainBlock);
            }
        );

        byte[] prevBlock = iv;
        byte[] plaintext = new byte[ciphertext.length];

        for (int i = 0; i < results.size(); i++) {
            DecryptResult result = results.get(i);
            byte[] cipherBlock = result.cipherBlock();
            byte[] plainBlock = result.plainBlock();

            for (int j = 0; j < plainBlock.length; j++) {
                plaintext[i * blockSize + j] = (byte) (plainBlock[j] ^ prevBlock[j]);
            }

            prevBlock = cipherBlock;
        }

        return plaintext;
    }

    private record DecryptResult(byte[] cipherBlock, byte[] plainBlock) {
    }
}

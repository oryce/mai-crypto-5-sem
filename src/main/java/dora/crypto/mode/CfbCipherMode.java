package dora.crypto.mode;

import dora.crypto.block.BlockCipher;
import dora.crypto.mode.Parameters.IvParameters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public final class CfbCipherMode extends AbstractCipherMode {

    private final ForkJoinPool pool;
    private byte[] iv;

    public CfbCipherMode(BlockCipher cipher, ForkJoinPool pool) {
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
    public byte[] encrypt(byte[] data, byte[] key) throws InterruptedException {
        byte[] prevBlock = iv;
        byte[] ciphertext = new byte[data.length];

        for (int i = 0; i < data.length; i += blockSize) {
            byte[] block = Arrays.copyOfRange(data, i, i + blockSize);
            byte[] encrypted = cipher.encrypt(prevBlock);

            for (int j = 0; j < block.length; j++) {
                encrypted[j] ^= block[j];
            }

            System.arraycopy(encrypted, 0, ciphertext, i, encrypted.length);
            prevBlock = encrypted;
        }

        return ciphertext;
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) throws InterruptedException {
        List<DecryptResult> results = ParallelBlockProcessor.processBlocks(
            data, blockSize, pool, (idx, start, end) -> {
                byte[] feedbackBlock;
                if (idx == 0) {
                    feedbackBlock = iv;
                } else {
                    int prevOffset = (idx - 1) * blockSize;
                    feedbackBlock = Arrays.copyOfRange(
                        data, prevOffset, prevOffset + blockSize);
                }

                byte[] encryptedFeedback = cipher.encrypt(feedbackBlock);
                byte[] cipherBlock = Arrays.copyOfRange(data, start, end);

                return new DecryptResult(encryptedFeedback, cipherBlock);
            }
        );

        byte[] plaintext = new byte[data.length];

        for (int i = 0; i < results.size(); i++) {
            DecryptResult result = results.get(i);
            byte[] encryptedFeedback = result.encryptedFeedback();
            byte[] cipherBlock = result.cipherBlock();

            byte[] decrypted = new byte[cipherBlock.length];
            for (int j = 0; j < cipherBlock.length; j++) {
                decrypted[j] = (byte) (encryptedFeedback[j] ^ cipherBlock[j]);
            }

            System.arraycopy(decrypted, 0, plaintext, i * blockSize, decrypted.length);
        }

        return plaintext;
    }

    private record DecryptResult(byte[] encryptedFeedback, byte[] cipherBlock) {
    }
}

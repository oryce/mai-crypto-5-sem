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
    public byte[] encrypt(byte[] plaintext, byte[] key) throws InterruptedException {
        byte[] prevBlock = iv;
        byte[] ciphertext = new byte[plaintext.length];

        for (int i = 0; i < plaintext.length; i += blockSize) {
            byte[] cipherBlock = cipher.encrypt(prevBlock);

            for (int j = 0; j < blockSize; j++) {
                cipherBlock[j] ^= plaintext[i + j];
                ciphertext[i + j] = cipherBlock[j];
            }

            prevBlock = cipherBlock;
        }

        return ciphertext;
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] key) throws InterruptedException {
        List<EncryptResult> results = ParallelBlockProcessor.processBlocks(
            ciphertext, blockSize, pool, (idx, start, end) -> {
                byte[] feedbackBlock;

                if (idx == 0) {
                    feedbackBlock = iv;
                } else {
                    int prevOffset = (idx - 1) * blockSize;
                    feedbackBlock = Arrays.copyOfRange(
                        ciphertext, prevOffset, prevOffset + blockSize);
                }

                byte[] encryptedFeedback = cipher.encrypt(feedbackBlock);
                byte[] cipherBlock = Arrays.copyOfRange(ciphertext, start, end);

                return new EncryptResult(encryptedFeedback, cipherBlock);
            }
        );

        byte[] plaintext = new byte[ciphertext.length];

        for (int i = 0; i < results.size(); i++) {
            EncryptResult result = results.get(i);
            byte[] encryptedFeedback = result.encryptedFeedback();
            byte[] cipherBlock = result.cipherBlock();

            for (int j = 0; j < cipherBlock.length; j++) {
                plaintext[i * blockSize + j] = (byte) (encryptedFeedback[j] ^ cipherBlock[j]);
            }
        }

        return plaintext;
    }

    private record EncryptResult(byte[] encryptedFeedback, byte[] cipherBlock) {
    }
}

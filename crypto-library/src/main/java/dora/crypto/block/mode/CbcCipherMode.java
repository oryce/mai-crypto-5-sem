package dora.crypto.block.mode;

import dora.crypto.block.BlockCipher;
import dora.crypto.block.mode.Parameters.IvParameters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public final class CbcCipherMode extends AbstractCipherMode {

    private final ForkJoinPool pool;

    private byte[] prevBlock;

    public CbcCipherMode(BlockCipher cipher, ForkJoinPool pool) {
        super(cipher);
        this.pool = pool;
    }

    @Override
    protected void initMode(Parameters parameters) {
        if (!(parameters instanceof IvParameters(byte[] ivParam))) {
            throw new IllegalArgumentException("expected IvParameters");
        }

        if (ivParam.length != blockSize) {
            throw new IllegalArgumentException(
                "expected %d-byte IV".formatted(blockSize));
        }

        prevBlock = ivParam.clone();
    }

    @Override
    protected byte[] encryptBlocks(byte[] plaintext) {
        byte[] ciphertext = new byte[plaintext.length];

        for (int i = 0; i < plaintext.length; i += blockSize) {
            byte[] plainBlock = Arrays.copyOfRange(plaintext, i, i + blockSize);

            for (int j = 0; j < plainBlock.length; j++) {
                plainBlock[j] ^= prevBlock[j];
            }

            byte[] cipherBlock = cipher.encrypt(plainBlock);
            System.arraycopy(cipherBlock, 0, ciphertext, i, cipherBlock.length);
            prevBlock = cipherBlock;
        }

        return ciphertext;
    }

    @Override
    protected byte[] decryptBlocks(byte[] ciphertext) throws InterruptedException {
        List<DecryptResult> results = ParallelBlockProcessor.processBlocks(
            ciphertext, blockSize, pool, (idx, start, end) -> {
                byte[] cipherBlock = Arrays.copyOfRange(ciphertext, start, end);
                byte[] plainBlock = cipher.decrypt(cipherBlock);
                return new DecryptResult(cipherBlock, plainBlock);
            }
        );

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

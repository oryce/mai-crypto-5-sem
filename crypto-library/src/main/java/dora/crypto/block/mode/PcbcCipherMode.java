package dora.crypto.block.mode;

import dora.crypto.block.BlockCipher;
import dora.crypto.block.mode.Parameters.IvParameters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public final class PcbcCipherMode extends AbstractCipherMode {

    private final ForkJoinPool pool;

    private byte[] prevBlock;

    public PcbcCipherMode(BlockCipher cipher, ForkJoinPool pool) {
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
            byte[] cipherBlock = cipher.encrypt(xor(plainBlock, prevBlock));

            System.arraycopy(cipherBlock, 0, ciphertext, i, blockSize);

            prevBlock = xor(plainBlock, cipherBlock);
        }

        return ciphertext;
    }

    @Override
    protected byte[] decryptBlocks(byte[] ciphertext) throws InterruptedException {
        byte[] plaintext = new byte[ciphertext.length];

        List<DecryptResult> results = ParallelBlockProcessor.processBlocks(
            ciphertext, blockSize, pool, (idx, start, end) -> {
                byte[] cipherBlock = Arrays.copyOfRange(ciphertext, start, end);
                byte[] decryptedBlock = cipher.decrypt(cipherBlock);
                return new DecryptResult(cipherBlock, decryptedBlock);
            }
        );

        for (int i = 0; i < results.size(); i++) {
            DecryptResult result = results.get(i);
            byte[] cipherBlock = result.cipherBlock();
            byte[] decryptedBlock = result.decryptedBlock();

            byte[] plainBlock = xor(decryptedBlock, prevBlock);
            System.arraycopy(plainBlock, 0, plaintext, i * blockSize, blockSize);

            prevBlock = xor(cipherBlock, plainBlock);
        }

        return plaintext;
    }

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
    }

    private record DecryptResult(byte[] cipherBlock, byte[] decryptedBlock) {
    }
}

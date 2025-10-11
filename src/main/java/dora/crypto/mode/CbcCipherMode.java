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
    public byte[] encrypt(byte[] data, byte[] key) throws InterruptedException {
        byte[] prevBlock = iv;
        byte[] ciphertext = new byte[data.length];

        for (int i = 0; i < data.length; i += blockSize) {
            byte[] block = Arrays.copyOfRange(data, i, i + blockSize);

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
    public byte[] decrypt(byte[] data, byte[] key) throws InterruptedException {
        List<DecryptResult> results = ParallelBlockProcessor.processBlocks(
            data, blockSize, pool, (idx, start, end) -> {
                byte[] encrypted = Arrays.copyOfRange(data, start, end);
                byte[] decrypted = cipher.decrypt(encrypted);
                return new DecryptResult(encrypted, decrypted);
            }
        );

        // XOR blocks to form the decrypted plaintext.
        byte[] prevBlock = iv;
        byte[] plaintext = new byte[data.length];

        for (int i = 0; i < results.size(); i++) {
            DecryptResult result = results.get(i);
            byte[] encrypted = result.encrypted();
            byte[] decrypted = result.decrypted();

            for (int j = 0; j < decrypted.length; j++) {
                decrypted[j] ^= prevBlock[j];
            }

            System.arraycopy(decrypted, 0, plaintext, i * blockSize, decrypted.length);
            prevBlock = encrypted;
        }

        return plaintext;
    }

    private record DecryptResult(byte[] encrypted, byte[] decrypted) {
    }
}

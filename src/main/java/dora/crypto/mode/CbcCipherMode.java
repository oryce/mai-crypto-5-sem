package dora.crypto.mode;

import dora.crypto.block.BlockCipher;
import dora.crypto.mode.Parameters.IvParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.IntStream;

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

        for (int i = 0; i < data.length; i += cipher.blockSize()) {
            byte[] block = Arrays.copyOfRange(data, i, i + cipher.blockSize());

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
        // Batch blocks to reduce overhead.
        final int blocksPerTask = 64;
        int blocks = data.length / cipher.blockSize();
        int tasks = Math.ceilDiv(blocks, blocksPerTask);

        // Collect decrypted blocks from tasks.
        ForkJoinTask<List<DecryptResult>> resultTask = pool.submit(() ->
            IntStream.range(0, tasks)
                .parallel()
                .mapToObj((taskIdx) -> {
                    int startBlock = taskIdx * blocksPerTask;
                    int endBlock = Math.min(startBlock + blocksPerTask, blocks);

                    List<DecryptResult> taskResults = new ArrayList<>(endBlock - startBlock);

                    for (int blockIdx = startBlock; blockIdx < endBlock; blockIdx++) {
                        int offset = blockIdx * cipher.blockSize();

                        byte[] encrypted = Arrays.copyOfRange(
                            data, offset, offset + cipher.blockSize());
                        byte[] decrypted = cipher.decrypt(encrypted);

                        taskResults.add(new DecryptResult(encrypted, decrypted));
                    }

                    return taskResults;
                })
                .flatMap(Collection::stream)
                .toList()
        );

        List<DecryptResult> results;
        try {
            results = resultTask.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

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

            System.arraycopy(decrypted, 0, plaintext, i * cipher.blockSize(), decrypted.length);
            prevBlock = encrypted;
        }

        return plaintext;
    }

    private record DecryptResult(byte[] encrypted, byte[] decrypted) {
    }
}

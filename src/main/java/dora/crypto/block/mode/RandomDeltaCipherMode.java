package dora.crypto.block.mode;

import dora.crypto.block.BlockCipher;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public final class RandomDeltaCipherMode extends AbstractCipherMode {

    private final ForkJoinPool pool;

    private byte[] nonce;
    private int counter;
    private Random random;

    public RandomDeltaCipherMode(BlockCipher cipher, ForkJoinPool pool) {
        super(cipher);
        this.pool = pool;
    }

    @Override
    protected void initMode(Parameters parameters) {
        if (!(parameters instanceof RandomDeltaParameters(
            byte[] nonceParam,
            int counterParam,
            Long seedParam
        ))) {
            throw new IllegalArgumentException("expected RandomDeltaParameters");
        }

        if (nonceParam.length != nonceSize()) {
            throw new IllegalArgumentException(
                "expected %d-byte nonce".formatted(nonceSize()));
        }
        if (counterParam < 0) {
            throw new IllegalArgumentException("counter must be positive");
        }

        nonce = nonceParam.clone();
        counter = counterParam;
        random = new Random();

        if (seedParam != null) {
            random.setSeed(seedParam);
        }
    }

    public int nonceSize() {
        return blockSize / 2;
    }

    @Override
    protected byte[] encryptBlocks(byte[] plaintext) throws InterruptedException {
        return processBlocks(plaintext);
    }

    @Override
    protected byte[] decryptBlocks(byte[] ciphertext) throws InterruptedException {
        return processBlocks(ciphertext);
    }

    private byte[] processBlocks(byte[] data) throws InterruptedException {
        byte[] result = new byte[data.length];

        long[] increments = IntStream.range(0, data.length / blockSize)
            .mapToLong((i) -> random.nextLong(1, 100))
            .toArray();

        ParallelBlockProcessor.processBlocks(
            data, blockSize, pool, (idx, start, end) -> {
                long increment = idx == 0 ? 0 : Arrays.stream(increments).limit(idx).sum();
                byte[] counterBlock = createCounterBlock((int) (counter + increment));
                byte[] encryptedCounter = cipher.encrypt(counterBlock);

                for (int j = start; j < end; j++) {
                    result[j] = (byte) (data[j] ^ encryptedCounter[j - start]);
                }

                return null;
            }
        );

        counter += (int) Arrays.stream(increments).sum();
        return result;
    }

    private byte[] createCounterBlock(int counter) {
        byte[] block = new byte[blockSize];

        System.arraycopy(nonce, 0, block, 0, nonce.length);

        block[blockSize - 4] = (byte) (counter >>> 24);
        block[blockSize - 3] = (byte) (counter >>> 16);
        block[blockSize - 2] = (byte) (counter >>>  8);
        block[blockSize - 1] = (byte) (counter       );

        return block;
    }

    public record RandomDeltaParameters(
        byte[] nonce,
        int counter,
        @Nullable Long seed
    ) implements Parameters {
    }
}

package dora.crypto.block.mode;

import dora.crypto.block.BlockCipher;

import java.util.concurrent.ForkJoinPool;

public final class CtrCipherMode extends AbstractCipherMode {

    private final ForkJoinPool pool;

    private byte[] nonce;
    private int counter;

    public CtrCipherMode(BlockCipher cipher, ForkJoinPool pool) {
        super(cipher);
        this.pool = pool;
    }

    @Override
    protected void initMode(Parameters parameters) {
        if (!(parameters instanceof CtrParameters(
            byte[] nonceParam,
            int counterParam
        ))) {
            throw new IllegalArgumentException("expected CtrParameters");
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

        ParallelBlockProcessor.processBlocks(
            data, blockSize, pool, (idx, start, end) -> {
                byte[] counterBlock = createCounterBlock(counter + idx);
                byte[] encryptedCounter = cipher.encrypt(counterBlock);

                for (int j = start; j < end; j++) {
                    result[j] = (byte) (data[j] ^ encryptedCounter[j - start]);
                }

                return null;
            }
        );

        counter += result.length / blockSize;
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

    public record CtrParameters(byte[] nonce, int counter) implements Parameters {
    }
}

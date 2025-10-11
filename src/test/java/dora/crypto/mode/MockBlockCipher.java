package dora.crypto.mode;

import dora.crypto.block.BlockCipher;

public final class MockBlockCipher implements BlockCipher {

    private final int blockSize;
    private byte[] key;

    public MockBlockCipher(int blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    public int blockSize() {
        return blockSize;
    }

    @Override
    public void init(byte[] key) {
        this.key = key.clone();
    }

    @Override
    public byte[] encrypt(byte[] block) {
        byte[] result = new byte[block.length];

        for (int i = 0; i < block.length; i++) {
            result[i] = (byte) (block[i] ^ key[i % key.length]);
        }

        return result;
    }

    @Override
    public byte[] decrypt(byte[] block) {
        return encrypt(block);
    }
}

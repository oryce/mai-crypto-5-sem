package dora.crypto.block.mode;

import dora.crypto.block.BlockCipher;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

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
    public Set<Integer> keySizes() {
        return Set.of(blockSize);
    }

    @Override
    public void init(byte @NotNull [] key) {
        this.key = key.clone();
    }

    @Override
    public byte[] encrypt(byte @NotNull [] plaintext) {
        byte[] result = new byte[plaintext.length];

        for (int i = 0; i < plaintext.length; i++) {
            result[i] = (byte) (plaintext[i] ^ key[i % key.length]);
        }

        return result;
    }

    @Override
    public byte[] decrypt(byte @NotNull [] ciphertext) {
        return encrypt(ciphertext);
    }
}

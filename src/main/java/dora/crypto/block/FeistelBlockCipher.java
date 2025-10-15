package dora.crypto.block;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public class FeistelBlockCipher implements BlockCipher {

    private final KeySchedule keySchedule;
    private final RoundFunction roundFunction;
    private final int blockSize;

    private byte[][] roundKeys;

    public FeistelBlockCipher(@NotNull KeySchedule keySchedule,
                              @NotNull RoundFunction roundFunction,
                              int blockSize) {
        this.keySchedule = requireNonNull(keySchedule, "key schedule");
        this.roundFunction = requireNonNull(roundFunction, "round function");
        this.blockSize = blockSize;

        if (blockSize % 2 != 0) {
            throw new IllegalArgumentException("Block size must be a multiple of two");
        }
    }

    @Override
    public int blockSize() {
        return blockSize;
    }

    @Override
    public void init(byte[] key) {
        roundKeys = keySchedule.roundKeys(key);
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");
        if (plaintext.length != blockSize)
            throw new IllegalArgumentException("Invalid block size");

        // (1) Split the block into two equal parts.
        byte[] l = Arrays.copyOfRange(plaintext, 0, plaintext.length / 2);
        byte[] r = Arrays.copyOfRange(plaintext, plaintext.length / 2, plaintext.length);

        // (2) For each round compute:
        //   - L_i+1 = R_i
        //   - R_i+1 = L_i xor F(R_i, K_i)
        for (byte[] roundKey : roundKeys) {
            byte[] rNew = new byte[r.length];
            byte[] f = roundFunction.apply(r, roundKey);

            for (int k = 0; k < rNew.length; k++) {
                rNew[k] = (byte) (l[k] ^ f[k]);
            }

            l = r;
            r = rNew;
        }

        // (3) The ciphertext is (R_n+1, L_n+1).
        byte[] ciphertext = new byte[plaintext.length];
        System.arraycopy(r, 0, ciphertext, 0, r.length);
        System.arraycopy(l, 0, ciphertext, r.length, l.length);

        return ciphertext;
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");
        if (ciphertext.length != blockSize)
            throw new IllegalArgumentException("Invalid block size");

        // (1) Split the block into two equal parts.
        byte[] r = Arrays.copyOfRange(ciphertext, 0, ciphertext.length / 2);
        byte[] l = Arrays.copyOfRange(ciphertext, ciphertext.length / 2, ciphertext.length);

        // (2) For each round compute:
        //   - R_i = R_i+1
        //   - L_i = R_i+1 xor F(L_i+1, K_i)
        for (int i = roundKeys.length - 1; i >= 0; i--) {
            byte[] lNew = new byte[l.length];
            byte[] f = roundFunction.apply(l, roundKeys[i]);

            for (int k = 0; k < lNew.length; k++) {
                lNew[k] = (byte) (r[k] ^ f[k]);
            }

            r = l;
            l = lNew;
        }

        // (3) The plaintext is (L_0, R_0).
        byte[] plaintext = new byte[ciphertext.length];
        System.arraycopy(l, 0, plaintext, 0, l.length);
        System.arraycopy(r, 0, plaintext, l.length, r.length);

        return plaintext;
    }
}

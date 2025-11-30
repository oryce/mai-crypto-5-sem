package dora.crypto.block.des;

import dora.crypto.block.KeySchedule;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public final class DesKeySchedule implements KeySchedule {

    /**
     * Permuted Choice 1 (PC-1).
     */
    private static final int[] PC1 = new int[] {
        57, 49, 41, 33, 25, 17, 9,
        1, 58, 50, 42, 34, 26, 18,
        10, 2, 59, 51, 43, 35, 27,
        19, 11, 3, 60, 52, 44, 36,
        63, 55, 47, 39, 31, 23, 15,
        7, 62, 54, 46, 38, 30, 22,
        14, 6, 61, 53, 45, 37, 29,
        21, 13, 5, 28, 20, 12, 4
    };

    /**
     * Permuted Choice 2 (PC-2).
     */
    private static final int[] PC2 = new int[] {
        14, 17, 11, 24, 1, 5,
        3, 28, 15, 6, 21, 10,
        23, 19, 12, 4, 26, 8,
        16, 7, 27, 20, 13, 2,
        41, 52, 31, 37, 47, 55,
        30, 40, 51, 45, 33, 48,
        44, 49, 39, 56, 34, 53,
        46, 42, 50, 36, 29, 32
    };

    /**
     * Shifts in each round.
     */
    private static final int[] KEY_SHIFT = new int[] {
        1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
    };

    @Override
    public byte[][] roundKeys(byte @NotNull [] key) {
        requireNonNull(key, "key");

        if (key.length != 8)
            throw new IllegalArgumentException("Expected a 64-bit key");

        byte[] permutedKey = Permutations.permute(key, PC1, false, true);

        // ------------------------- permutedKey -------------------------
        // ------------ left ------------- ------------ right ------------
        // 00000000 00000000 00000000 0000 0000 00000000 00000000 00000000

        int left = (permutedKey[0] & 0xff) << 20
            | (permutedKey[1] & 0xff) << 12
            | (permutedKey[2] & 0xff) << 4
            | (permutedKey[3] & 0xf0) >> 4;

        int right = (permutedKey[3] & 0x0f) << 24
            | (permutedKey[4] & 0xff) << 16
            | (permutedKey[5] & 0xff) << 8
            | (permutedKey[6] & 0xff);

        byte[][] keys = new byte[16][6];

        for (int i = 0; i < keys.length; i++) {
            right = rotateLeft(right, KEY_SHIFT[i]);
            left = rotateLeft(left, KEY_SHIFT[i]);

            byte[] concat = new byte[] {
                (byte) (left >> 20),
                (byte) (left >> 12),
                (byte) (left >> 4),
                (byte) ((left << 4) | (right >> 24)),
                (byte) (right >> 16),
                (byte) (right >> 8),
                (byte) right,
            };

            keys[i] = Permutations.permute(concat, PC2, false, true);
        }

        return keys;
    }

    private int rotateLeft(int value, int distance) {
        return ((value << distance) | (value >> 28 - distance)) & 0x0fffffff;
    }
}

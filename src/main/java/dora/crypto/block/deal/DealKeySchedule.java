package dora.crypto.block.deal;

import dora.crypto.block.KeySchedule;
import dora.crypto.block.des.DesBlockCipher;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * DEAL support 128-bit, 192-bit and 256-bit keys. 128-bit and 192-bit
 * keys provide 6 rounds of encryption, 256-bit keys provide 8.
 */
public final class DealKeySchedule implements KeySchedule {

    private final DesBlockCipher des;

    public DealKeySchedule(byte @NotNull [] desKey) {
        des = new DesBlockCipher();
        des.init(requireNonNull(desKey, "DES key"));
    }

    @Override
    public byte[][] roundKeys(byte @NotNull [] key) {
        int parts, rounds;

        switch (key.length) {
            case 128 / 8:
                parts = 2;
                rounds = 6;
                break;
            case 192 / 8:
                parts = 3;
                rounds = 6;
                break;
            case 256 / 8:
                parts = 4;
                rounds = 8;
                break;
            default:
                throw new IllegalArgumentException("Expected a 128-bit, 192-bit or a 256-bit key");
        }

        byte[][] keyParts = new byte[parts][8];
        byte[][] roundKeys = new byte[rounds][8];

        for (int i = 0; i < parts; i++) {
            keyParts[i] = Arrays.copyOfRange(key, i * 8, (i + 1) * 8);
        }

        roundKeys[0] = des.encrypt(keyParts[0]);
        for (int i = 1; i < parts; i++) {
            roundKeys[i] = des.encrypt(xor(keyParts[i], roundKeys[i - 1]));
        }

        for (int k = parts; k < rounds; k++) {
            // Use wrapping powers of two for the constant block.
            byte[] constant = toByteArray(1L << (k - parts));

            for (int i = 0; i < 8; i++) {
                roundKeys[k][i] = (byte) (keyParts[k % parts][i]
                                              ^ roundKeys[k - 1][i]
                                              ^ constant[i]);
            }
        }

        return roundKeys;
    }

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
    }

    private static byte[] toByteArray(long value) {
        return ByteBuffer.allocate(Long.BYTES)
            .putLong(value)
            .array();
    }
}

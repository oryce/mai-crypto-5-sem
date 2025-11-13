package dora.crypto.block;

import org.jetbrains.annotations.NotNull;

public interface KeySchedule {

    byte[][] roundKeys(byte @NotNull [] key);
    static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
    }
}

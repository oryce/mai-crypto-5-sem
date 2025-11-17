package dora.crypto.block.rc5;

import dora.crypto.block.KeySchedule;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public final class RC5KeySchedule implements KeySchedule {
    private final RC5Parameters parameters;

    public RC5KeySchedule(@NotNull RC5Parameters params){
        this.parameters = params;
    }

    @Override
    public byte[][] roundKeys(byte @NotNull [] key) {
        var pq = new PQConstants(parameters.w().bitCount());
        int resultOneKeySize = parameters.w().byteCount();
        var c = Math.ceilDiv(parameters.b(), resultOneKeySize);
        if (c == 0){
            c = 1;
        }

        byte[][] l = new byte[c][resultOneKeySize];
        int keysCount = 2 * (parameters.r() + 1);
        byte[][] s = new byte[keysCount][resultOneKeySize];

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < resultOneKeySize; j++){
                if (i * resultOneKeySize + j < key.length){
                    l[i][j] = key[i * resultOneKeySize + j];
                } else {
                    l[i][j] = 0;
                }
            }
        }

        s[0] = pq.p().toByteArray();
        for (int i = 1; i < keysCount; i++){
            s[i] = addMod32(s[i-1], pq.q().toByteArray());
        }

        byte[] g = new byte[resultOneKeySize];
        byte[] h = new byte[resultOneKeySize];

        int n = Math.max(3 * c, (3 * 2 * parameters.r() + 1));

        int i = 0;
        int j = 0;

        for (int tmp = 0; tmp < n; tmp++){
            s[i] = shiftLeft(addMod32(addMod32(s[i], g), h), BigInteger.valueOf(3) );
            g = s[i];
            l[j] = shiftLeft(addMod32(addMod32(s[i], g), h), new BigInteger(addMod32(g, h)));
            h = l[j];
            i = (i + 1) % (2 * (parameters.r() + 1));
            j = (j + 1) % c;
        }

        return s;
    }

    public static byte[] addMod32(byte[] num1, byte[] num2) {
        byte[] result = new byte[4];

        int carry = 0;

        for (int i = 3; i >= 0; i--) {
            int sum = (num1[i] & 0xFF) + (num2[i] & 0xFF) + carry;
            result[i] = (byte) (sum & 0xFF);
            carry = (sum >> 8) & 0xFF;
        }

        return result;
    }

    public static byte[] shiftLeft(byte[] array, BigInteger pos) {
        pos = pos.mod(BigInteger.valueOf(array.length));

        int positions = pos.intValue();

        byte[] result = new byte[array.length];

        for (int i = 0; i < array.length; i++) {
            result[(i - positions + array.length) % array.length] = array[i];
        }

        return result;
    }
}

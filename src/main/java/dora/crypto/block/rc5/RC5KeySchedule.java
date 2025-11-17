package dora.crypto.block.rc5;

import dora.crypto.block.KeySchedule;
import org.jetbrains.annotations.NotNull;
import static dora.crypto.block.rc5.RC5BlockCipher.shiftLeft;
import static dora.crypto.block.rc5.RC5BlockCipher.addModW;
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
            s[i] = addModW(s[i-1], pq.q().toByteArray(), parameters.w().bitCount());
        }

        byte[] g = new byte[resultOneKeySize];
        byte[] h = new byte[resultOneKeySize];

        int n = Math.max(3 * c, (3 * 2 * parameters.r() + 1));

        int i = 0;
        int j = 0;
        int w = parameters.w().bitCount();
        for (int tmp = 0; tmp < n; tmp++){
            s[i] = shiftLeft(addModW(addModW(s[i], g, w), h, w), BigInteger.valueOf(3));
            g = s[i];
            l[j] = shiftLeft(addModW(addModW(l[j], g, w), h, w),new BigInteger(addModW(g, h, w)));
            h = l[j];
            i = (i + 1) % (2 * (parameters.r() + 1));
            j = (j + 1) % c;
        }

        return s;
    }
}

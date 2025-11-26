package dora.crypto.block.rc5;

import dora.crypto.block.KeySchedule;
import org.jetbrains.annotations.NotNull;
import java.math.BigInteger;
import java.util.Arrays;

import static dora.crypto.block.rc5.RC5BlockCipher.shiftLeft;
import static dora.crypto.block.rc5.RC5BlockCipher.addModW;
import static java.util.Objects.requireNonNull;

public final class RC5KeySchedule implements KeySchedule {

    private final RC5Parameters parameters;

    public RC5KeySchedule(@NotNull RC5Parameters parameters) {
        this.parameters = requireNonNull(parameters, "parameters");
    }

    @Override
    public byte[][] roundKeys(byte @NotNull [] key) {
        PQConstants pq = new PQConstants(parameters.w().bitCount());
        int resultOneKeySize = parameters.w().byteCount();
        int c = Math.max(1, Math.ceilDiv(parameters.b(), resultOneKeySize));

        byte[][] l = new byte[c][resultOneKeySize];
        int keysCount = 2 * (parameters.r() + 1);
        byte[][] s = new byte[keysCount][resultOneKeySize];

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < resultOneKeySize; j++) {
                if (i * resultOneKeySize + j < key.length) {
                    l[i][j] = key[i * resultOneKeySize + j];
                } else {
                    l[i][j] = 0;
                }
            }
        }

        s[0] = Arrays.copyOf(pq.p(), pq.q().length);
        for (int i = 1; i < keysCount; i++) {
            s[i] = addModW(s[i - 1], pq.q(), parameters.w().bitCount());
        }

        byte[] g = new byte[resultOneKeySize];
        byte[] h = new byte[resultOneKeySize];

        int n = Math.max(3 * c, (3 * 2 * parameters.r() + 1));

        int i = 0;
        int j = 0;
        int w = parameters.w().bitCount();
        for (int tmp = 0; tmp < n; tmp++) {
            s[i] = shiftLeft(addModW(addModW(s[i], g, w), h, w), new byte[] {3});
            g = Arrays.copyOf(s[i], resultOneKeySize);
            l[j] = shiftLeft(addModW(addModW(l[j], g, w), h, w), addModW(g, h, w));
            h = Arrays.copyOf(l[j], resultOneKeySize);
            i = (i + 1) % (2 * (parameters.r() + 1));
            j = (j + 1) % c;
        }

        return s;
    }
}

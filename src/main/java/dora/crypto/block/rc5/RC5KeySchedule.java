package dora.crypto.block.rc5;

import dora.crypto.block.KeySchedule;
import org.jetbrains.annotations.NotNull;
import java.math.BigInteger;
import java.util.Arrays;

import static dora.crypto.block.rc5.RC5BlockCipher.shiftLeft;
import static dora.crypto.block.rc5.RC5BlockCipher.addModW;

public final class RC5KeySchedule implements KeySchedule {
    private final RC5Parameters parameters;

    public RC5KeySchedule(@NotNull RC5Parameters params) {
        this.parameters = params;
    }

    @Override
    public byte[][] roundKeys(byte @NotNull [] key) {
        var pq = new PQConstants(parameters.w().bitCount());
        int resultOneKeySize = parameters.w().byteCount();
        int c = Math.max(1, Math.ceilDiv(parameters.b(), resultOneKeySize));

        byte[][] l = new byte[c][resultOneKeySize];
        int keysCount = 2 * (parameters.r() + 1);
        byte[][] s = new byte[keysCount][resultOneKeySize];

        // Заполняем L массив с ключа
        for (int i = 0; i < c; i++) {
            for (int j = 0; j < resultOneKeySize; j++) {
                if (i * resultOneKeySize + j < key.length) {
                    l[i][j] = key[i * resultOneKeySize + j];
                } else {
                    l[i][j] = 0;
                }
            }
        }

        // Инициализируем массив S
        s[0] = safeToByteArray(pq.p(), resultOneKeySize);
        for (int i = 1; i < keysCount; i++) {
            s[i] = addModW(s[i - 1], safeToByteArray(pq.q(), resultOneKeySize), parameters.w().bitCount());
        }

        byte[] g = new byte[resultOneKeySize];
        byte[] h = new byte[resultOneKeySize];

        int n = Math.max(3 * c, (3 * 2 * parameters.r() + 1));

        int i = 0;
        int j = 0;
        int w = parameters.w().bitCount();
        for (int tmp = 0; tmp < n; tmp++) {
            s[i] = shiftLeft(addModW(addModW(s[i], g, w), h, w), BigInteger.valueOf(3));
            g = Arrays.copyOf(s[i], resultOneKeySize);
            l[j] = shiftLeft(addModW(addModW(l[j], g, w), h, w), new BigInteger(addModW(g, h, w)).mod(BigInteger.valueOf(w)));
            h = Arrays.copyOf(l[j], resultOneKeySize);
            i = (i + 1) % (2 * (parameters.r() + 1));
            j = (j + 1) % c;
        }

        return s;
    }

    // Безопасное преобразование BigInteger в массив байт заданного размера
    private byte[] safeToByteArray(BigInteger value, int size) {
        byte[] bytes = value.toByteArray();
        if (bytes.length < size) {
            // Добавляем нули в начале, если размер байтов меньше необходимого
            byte[] padded = new byte[size];
            System.arraycopy(bytes, 0, padded, size - bytes.length, bytes.length);
            return padded;
        } else if (bytes.length > size) {
            // Обрезаем старший байт, если размер байтов больше необходимого
            byte[] truncated = new byte[size];
            System.arraycopy(bytes, bytes.length - size, truncated, 0, size);
            return truncated;
        } else {
            return bytes;
        }
    }
}

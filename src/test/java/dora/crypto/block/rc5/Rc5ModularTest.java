package dora.crypto.block.rc5;
import net.jqwik.api.*;
import java.math.BigInteger;

import static dora.crypto.block.rc5.RC5BlockCipher.addModW;
import static dora.crypto.block.rc5.RC5BlockCipher.subModW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

public class Rc5ModularTest {

    public static void main(String[] args) {
        int w = 16; // 16 бит
        long aa = 0x0110;   // Максимальное 16-битное число
        long bb = 0x1111;   // Добавляем 2

        // Преобразуем числа в массивы байт длиной 2
        byte[] a = longToBytes(aa, 2);
        byte[] b = longToBytes(bb, 2);

        // Преобразуем обратно в int для проверки
        long aInt = ((a[0] & 0xFF) << 8) | (a[1] & 0xFF);
        long bInt = ((b[0] & 0xFF) << 8) | (b[1] & 0xFF);

        // Сложение по модулю 2^16
        long expected = aInt;


        byte[] expectedBytes = new byte[] {
                (byte) ((expected >> 8) & 0xFF),
                (byte) (expected & 0xFF)
        };

        // Используем твою функцию addModW
        byte[] tmp = addModW(a, b, 16);
        byte[] result = subModW(tmp, b, 16);


        // Вывод результата
        System.out.println("Expected: " + String.format("0x%02X%02X", expectedBytes[0], expectedBytes[1]));
        System.out.println("Result  : " + String.format("0x%02X%02X", result[0], result[1]));
    }

    public static byte[] longToBytes(long num, int byteCount) {
        if (byteCount < 1 || byteCount > 8) {
            throw new IllegalArgumentException("byteCount должен быть от 1 до 8");
        }

        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            // Сдвигаем на нужное количество бит (big-endian)
            bytes[i] = (byte) (num >> (8 * (byteCount - 1 - i)));
        }
        return bytes;
    }

    public static long bytesToLong(byte[] bytes) {
        if (bytes.length < 1 || bytes.length > 8) {
            throw new IllegalArgumentException("Длина массива должна быть от 1 до 8");
        }

        long result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= ((long) (bytes[i] & 0xFF)) << (8 * (bytes.length - 1 - i));
        }
        return result;
    }

}

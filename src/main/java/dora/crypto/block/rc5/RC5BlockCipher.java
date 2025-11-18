package dora.crypto.block.rc5;

import dora.crypto.block.BlockCipher;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;

public class RC5BlockCipher implements BlockCipher {
    private final RC5Parameters parameters;
    private final RC5KeySchedule keySchedule;
    private byte[][] roundKeys;

    public RC5BlockCipher(@NotNull RC5Parameters params) {
        parameters = params;
        keySchedule = new RC5KeySchedule(parameters);
    }

    @Override
    public int blockSize() {
        return parameters.w().byteCount() * 2; // Два блока (a и b)
    }

    @Override
    public void init(byte @NotNull [] key) {
        roundKeys = keySchedule.roundKeys(key);
    }

    @Override
    public byte[] encrypt(byte @NotNull [] plaintext) {
        var a = Arrays.copyOfRange(plaintext, 0, plaintext.length / 2);
        var b = Arrays.copyOfRange(plaintext, plaintext.length / 2, plaintext.length);

        // Инициализация с раундовыми ключами
        a = addModW(a, roundKeys[0], parameters.w().bitCount());
        b = addModW(b, roundKeys[1], parameters.w().bitCount());

        // Основной цикл шифрования
        for (int i = 0; i < parameters.r(); i++) {
            a = addModW(shiftLeft(xor(a, b), b), roundKeys[2 * i], parameters.w().bitCount());
            b = addModW(shiftLeft(xor(b, a), a), roundKeys[2 * i + 1], parameters.w().bitCount());
        }

        byte[] res = new byte[blockSize()];

        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

    @Override
    public byte[] decrypt(byte @NotNull [] ciphertext) {
        var a = Arrays.copyOfRange(ciphertext, 0, ciphertext.length / 2);
        var b = Arrays.copyOfRange(ciphertext, ciphertext.length / 2, ciphertext.length);

        // Основной цикл дешифрования (обратный порядок раундов)
        for (int i = parameters.r() - 1; i >= 0; i--) {
            b = xor(shiftRight(subModW(b, roundKeys[2 * i + 1], parameters.w().bitCount()), a), a);
            a = xor(shiftRight(subModW(a, roundKeys[2 * i], parameters.w().bitCount()), b), b);
        }

        a = subModW(a, roundKeys[0], parameters.w().bitCount());
        b = subModW(b, roundKeys[1], parameters.w().bitCount());


        byte[] res = new byte[blockSize()];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

    // Сложение по модулю 2^w
    public static byte[] addModW(byte[] num1, byte[] num2, int w) {
        int byteSize = w / 8;
        byte[] result = new byte[byteSize];
        int carry = 0;

        for (int i = byteSize - 1; i >= 0; i--) {
            int sum = (num1[i] & 0xFF) + (num2[i] & 0xFF) + carry;
            result[i] = (byte) (sum & 0xFF);
            carry = (sum >> 8) & 0xFF;
        }

        return result;
    }

    // Вычитание по модулю 2^w
    public static byte[] subModW(byte[] num1, byte[] num2, int w) {
        int byteSize = w / 8;
        byte[] result = new byte[byteSize];
        int borrow = 0;

        for (int i = byteSize - 1; i >= 0; i--) {
            int diff = (num1[i] & 0xFF) - (num2[i] & 0xFF) - borrow;
            if (diff < 0) {
                diff += 0x100;
                borrow = 1;
            } else {
                borrow = 0;
            }

            result[i] = (byte) (diff & 0xFF);
        }

        return result;
    }

    // Сдвиг влево
    public static byte[] shiftLeft(byte[] array, byte[] pos) {
        pos = new byte[] {pos[pos.length - 1]};
        int positions = (int) (bytesToLong(pos) % (array.length * 8));

        if (positions == 0) {
            return array.clone();
        }

        byte[] result = new byte[array.length];
        int totalBits = array.length * 8;

        for (int i = 0; i < totalBits; i++) {
            int originalIndex = i;
            int shiftedIndex = (i + positions) % totalBits;

            int originalByteIndex = originalIndex / 8;
            int originalBitIndex = 7 - (originalIndex % 8); // Биты нумеруются справа налево

            int shiftedByteIndex = shiftedIndex / 8;
            int shiftedBitIndex = 7 - (shiftedIndex % 8);

            // Получаем бит из исходного массива
            boolean bitValue = ((array[originalByteIndex] >> originalBitIndex) & 1) == 1;

            // Устанавливаем бит в результирующем массиве
            if (bitValue) {
                result[shiftedByteIndex] |= (byte) (1 << shiftedBitIndex);
            }
        }

        return result;
    }

    public static byte[] shiftRight(byte[] array, byte[] pos) {
        pos = new byte[] {pos[pos.length - 1]};
        int positions = (int) (bytesToLong(pos) % (array.length * 8));

        if (positions == 0) {
            return array.clone();
        }

        byte[] result = new byte[array.length];
        int totalBits = array.length * 8;

        for (int i = 0; i < totalBits; i++) {
            int originalIndex = i;
            int shiftedIndex = (i - positions + totalBits) % totalBits;

            int originalByteIndex = originalIndex / 8;
            int originalBitIndex = 7 - (originalIndex % 8);

            int shiftedByteIndex = shiftedIndex / 8;
            int shiftedBitIndex = 7 - (shiftedIndex % 8);

            // Получаем бит из исходного массива
            boolean bitValue = ((array[originalByteIndex] >> originalBitIndex) & 1) == 1;

            // Устанавливаем бит в результирующем массиве
            if (bitValue) {
                result[shiftedByteIndex] |= (byte) (1 << shiftedBitIndex);
            }
        }

        return result;
    }

    // Операция XOR
    private byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
    }

    public static byte[] longToBytes(int num, int byteCount) {
        if (byteCount < 1 || byteCount > 8) {
            throw new IllegalArgumentException("byteCount должен быть от 1 до 8");
        }

        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            bytes[i] = (byte) (num >> (8 * (byteCount - 1 - i)));
        }
        return bytes;
    }

    public static int bytesToLong(byte[] bytes) {
        if (bytes.length < 1 || bytes.length > 8) {
            throw new IllegalArgumentException("Длина массива должна быть от 1 до 8");
        }

        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= ((int) (bytes[i] & 0xFF)) << (8 * (bytes.length - 1 - i));
        }
        return result;
    }
}

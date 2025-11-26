package dora.crypto.block.rc5;

import dora.crypto.block.BlockCipher;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class RC5BlockCipher implements BlockCipher {

    private final RC5Parameters parameters;
    private final RC5KeySchedule keySchedule;
    private byte[][] roundKeys;

    public RC5BlockCipher(@NotNull RC5Parameters parameters) {
        this.parameters = requireNonNull(parameters, "parameters");
        keySchedule = new RC5KeySchedule(this.parameters);
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
        requireNonNull(plaintext, "plaintext");

        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");

        if (plaintext.length != blockSize()){
            throw new IllegalArgumentException("Invalid block size");
        }

        byte[] a = Arrays.copyOfRange(plaintext, 0, plaintext.length / 2);
        byte[] b = Arrays.copyOfRange(plaintext, plaintext.length / 2, plaintext.length);

        a = addModW(a, roundKeys[0], parameters.w().bitCount());
        b = addModW(b, roundKeys[1], parameters.w().bitCount());

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
        requireNonNull(ciphertext, "ciphertext");

        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");

        byte[] a = Arrays.copyOfRange(ciphertext, 0, ciphertext.length / 2);
        byte[] b = Arrays.copyOfRange(ciphertext, ciphertext.length / 2, ciphertext.length);

        for (int i = parameters.r() - 1; i >= 0; i--) {
            b = xor(shiftRight(subModW(b, roundKeys[2 * i + 1], parameters.w().bitCount()), a), a);
            a = xor(shiftRight(subModW(a, roundKeys[2 * i], parameters.w().bitCount()), b), b);
        }

        a = subModW(a, roundKeys[0], parameters.w().bitCount());
        b = subModW(b, roundKeys[1], parameters.w().bitCount());

        byte[] result = new byte[blockSize()];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);

        return result;
    }

    /**
     * Adds two byte-array integers modulo 2^w.
     *
     * @param num1 the first value
     * @param num2 the second value
     * @param w the bit width for the modulus (mod 2^w)
     * @return (num1 + num2) mod 2^w as a byte array
     */
    public static byte[] addModW(byte[] num1, byte[] num2, int w) {
        int byteSize = w / Byte.SIZE;
        byte[] result = new byte[byteSize];
        int carry = 0;

        for (int i = byteSize - 1; i >= 0; i--) {
            int sum = (num1[i] & 0xFF) + (num2[i] & 0xFF) + carry;
            result[i] = (byte) (sum & 0xFF);
            carry = (sum >> 8) & 0xFF;
        }

        return result;
    }

    /**
     * Subtracts two byte-array integers modulo 2^w.
     *
     * @param num1 the first value
     * @param num2 the second value
     * @param w the bit width for the modulus (mod 2^w)
     * @return (num1 + num2) mod 2^w as a byte array
     */
    public static byte[] subModW(byte[] num1, byte[] num2, int w) {
        int byteSize = w / Byte.SIZE;
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

    /**
     * Shifts left a number, represented in byte array
     *
     * @param array the number to be shifted
     * @param pos amount of shifts represented in byte array
     * @return shifted number
     */
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
            int originalBitIndex = 7 - (originalIndex % 8);

            int shiftedByteIndex = shiftedIndex / 8;
            int shiftedBitIndex = 7 - (shiftedIndex % 8);

            boolean bitValue = ((array[originalByteIndex] >> originalBitIndex) & 1) == 1;

            if (bitValue) {
                result[shiftedByteIndex] |= (byte) (1 << shiftedBitIndex);
            }
        }

        return result;
    }

    /**
     * Shifts right a number, represented in byte array
     *
     * @param array the number to be shifted
     * @param pos amount of shifts represented in byte array
     * @return shifted number
     */
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

            boolean bitValue = ((array[originalByteIndex] >> originalBitIndex) & 1) == 1;

            if (bitValue) {
                result[shiftedByteIndex] |= (byte) (1 << shiftedBitIndex);
            }
        }

        return result;
    }

    private byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
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

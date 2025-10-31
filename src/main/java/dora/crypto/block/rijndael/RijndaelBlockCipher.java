package dora.crypto.block.rijndael;

import dora.crypto.block.BlockCipher;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class RijndaelBlockCipher implements BlockCipher {

    private final RijndaelParameters parameters;
    private final RijndaelKeySchedule keySchedule;

    public RijndaelBlockCipher(@NotNull RijndaelParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        this.keySchedule = new RijndaelKeySchedule(parameters);
    }

    private byte[][] roundKeys;

    @Override
    public int blockSize() {
        return parameters.blockSize().bytes();
    }

    @Override
    public void init(byte @NotNull [] key) {
        Objects.requireNonNull(key, "key");
        roundKeys = keySchedule.roundKeys(key);
    }

    @Override
    public byte[] encrypt(byte @NotNull [] plaintext) {
        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");

        Objects.requireNonNull(plaintext, "plaintext");
        if (plaintext.length != blockSize())
            throw new IllegalArgumentException("Invalid block size");

        byte[] state = plaintext.clone();
        state = addRoundKey(state, roundKeys[0]);

        for (int round = 1; round < parameters.rounds(); round++) {
            state = subBytes(state);
            state = shiftRows(state);
            state = mixColumns(state);
            state = addRoundKey(state, roundKeys[round]);
        }

        state = subBytes(state);
        state = shiftRows(state);
        state = addRoundKey(state, roundKeys[parameters.rounds()]);

        return state;
    }

    @Override
    public byte[] decrypt(byte @NotNull [] ciphertext) {
        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");

        Objects.requireNonNull(ciphertext, "ciphertext");
        if (ciphertext.length != blockSize())
            throw new IllegalArgumentException("Invalid block size");

        byte[] state = ciphertext.clone();
        state = addRoundKey(state, roundKeys[parameters.rounds()]);

        for (int round = parameters.rounds() - 1; round >= 1; round--) {
            state = invShiftRows(state);
            state = invSubBytes(state);
            state = addRoundKey(state, roundKeys[round]);
            state = invMixColumns(state);
        }

        state = invShiftRows(state);
        state = invSubBytes(state);
        state = addRoundKey(state, roundKeys[0]);

        return state;
    }

    private byte[] addRoundKey(byte[] state, byte[] roundKey) {
        byte[] result = new byte[state.length];

        for (int i = 0; i < state.length; i++) {
            result[i] = (byte) (state[i] ^ roundKey[i]);
        }

        return result;
    }

    //region SubBytes
    private byte[] subBytes(byte[] state) {
        byte[] result = new byte[state.length];

        for (int i = 0; i < state.length; i++) {
            result[i] = parameters.sBox().lookup(state[i]);
        }

        return result;
    }

    private byte[] invSubBytes(byte[] state) {
        byte[] result = new byte[state.length];

        for (int i = 0; i < state.length; i++) {
            result[i] = parameters.inverseSBox().lookup(state[i]);
        }

        return result;
    }
    //endregion

    //region ShiftRows
    private byte[] shiftRows(byte[] state) {
        byte[] result = new byte[state.length];
        int blockWords = parameters.blockSize().words();

        for (int col = 0; col < blockWords; col++) {
            for (int row = 0; row < 4; row++) {
                int shiftCol = (col - row + blockWords) % blockWords;
                result[shiftCol * 4 + row] = state[col * 4 + row];
            }
        }

        return result;
    }

    private byte[] invShiftRows(byte[] state) {
        byte[] result = new byte[state.length];
        int blockWords = parameters.blockSize().words();

        for (int col = 0; col < blockWords; col++) {
            for (int row = 0; row < 4; row++) {
                int shiftCol = (col + row) % blockWords;
                result[shiftCol * 4 + row] = state[col * 4 + row];
            }
        }

        return result;
    }
    //endregion

    //region MixColumns
    private byte[] mixColumns(byte[] state) {
        byte[] result = new byte[state.length];
        GaloisField field = new GaloisField();

        final byte[] transformation = new byte[] {
            2, 3, 1, 1,
            1, 2, 3, 1,
            1, 1, 2, 3,
            3, 1, 1, 2
        };

        for (int col = 0; col < parameters.blockSize().words(); col++) {
            for (int row = 0; row < 4; row++) {
                for (int k = 0; k < 4; k++) {
                    result[col * 4 + row] = field.add(
                        field.mulModUnchecked(
                            state[col * 4 + k],
                            transformation[row * 4 + k],
                            parameters.modulus()
                        ),
                        result[col * 4 + row]
                    );
                }
            }
        }

        return result;
    }

    private byte[] invMixColumns(byte[] state) {
        byte[] result = new byte[state.length];
        GaloisField field = new GaloisField();

        final byte[] transformation = new byte[] {
            0xe, 0xb, 0xd, 0x9,
            0x9, 0xe, 0xb, 0xd,
            0xd, 0x9, 0xe, 0xb,
            0xb, 0xd, 0x9, 0xe
        };

        for (int col = 0; col < parameters.blockSize().words(); col++) {
            for (int row = 0; row < 4; row++) {
                for (int k = 0; k < 4; k++) {
                    result[col * 4 + row] = field.add(
                        field.mulModUnchecked(
                            state[col * 4 + k],
                            transformation[row * 4 + k],
                            parameters.modulus()
                        ),
                        result[col * 4 + row]
                    );
                }
            }
        }

        return result;
    }
    //endregion
}

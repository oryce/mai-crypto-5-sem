package dora.crypto.block.rijndael;

import dora.crypto.block.KeySchedule;
import dora.crypto.block.rijndael.RijndaelParameters.BlockSize;
import dora.crypto.block.rijndael.RijndaelParameters.KeySize;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public final class RijndaelKeySchedule implements KeySchedule {

    private final RijndaelParameters parameters;

    public RijndaelKeySchedule(@NotNull RijndaelParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
    }

    @Override
    public byte[][] roundKeys(byte @NotNull [] key) {
        Objects.requireNonNull(key, "key");

        KeySize keySize = parameters.keySize();
        BlockSize blockSize = parameters.blockSize();

        if (key.length != keySize.bytes())
            throw new IllegalArgumentException("Invalid key size");

        /* https://en.wikipedia.org/wiki/AES_key_schedule#The_key_schedule */

        // Length of the key in 32-bit words.
        int n = keySize.words();
        // Length of the block in 32-bit words.
        int b = blockSize.words();
        // Number of round keys needed.
        int r = parameters.rounds() + 1;
        // 32-bit words of the expanded key.
        byte[][] w = new byte[b * r][4];

        // The key expansion logic is as follows:
        //
        // W_i = K_i, if i < N;
        //       W_i-N xor SubWord(RotWord(W_i-1)) xor rcon_i/N,
        //           if i >= N and i === 0 (mod N);
        //       W_i-N xor SubWord(W_i-1)
        //           if i >= N, N > 6, and i === 4 (mod N);
        //       W_i-N xor W_i-1, otherwise.

        for (int i = 0; i < w.length; i++) {
            if (i < n) {
                w[i] = Arrays.copyOfRange(key, 4 * i, 4 * i + 4);
            } else if (i % n == 0) {
                w[i] = xor(
                    w[i - n],
                    xor(
                        subWord(rotWord(w[i - 1])),
                        parameters.rcon()[i / n - 1]
                    )
                );
            } else if (n < 6 && i % n == 4) {
                w[i] = xor(w[i - n], subWord(w[i - 1]));
            } else {
                w[i] = xor(w[i - n], w[i - 1]);
            }
        }

        // Assemble the rounds keys stored in columns.
        byte[][] roundKeys = new byte[r][blockSize.bytes()];

        for (int round = 0; round < roundKeys.length; round++) {
            for (int column = 0; column < b; column++) {
                System.arraycopy(w[round * b + column], 0, roundKeys[round], column * 4, 4);
            }
        }

        return roundKeys;
    }

    private byte[] rotWord(byte[] word) {
        return new byte[] { word[1], word[2], word[3], word[0] };
    }

    private byte[] subWord(byte[] word) {
        return new byte[] {
            parameters.sBox().lookup(word[0]),
            parameters.sBox().lookup(word[1]),
            parameters.sBox().lookup(word[2]),
            parameters.sBox().lookup(word[3]),
        };
    }

    private byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[4];

        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
    }
}

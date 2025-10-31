package dora.crypto.block.rijndael;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public final class RijndaelParameters {

    private final KeySize keySize;
    private final BlockSize blockSize;
    private final RijndaelSBox sBox;
    private final RijndaelInverseSBox inverseSBox;

    public RijndaelParameters(
        @NotNull KeySize keySize,
        @NotNull BlockSize blockSize,
        short modulus
    ) {
        this.keySize = Objects.requireNonNull(keySize, "key size");
        this.blockSize = Objects.requireNonNull(blockSize, "block size");
        this.sBox = new RijndaelSBox(modulus);
        this.inverseSBox = new RijndaelInverseSBox(modulus);
    }

    public int numRounds() {
        return Math.max(keySize.numColumns(), blockSize.numColumns()) + 6;
    }

    public KeySize keySize() {
        return keySize;
    }

    public BlockSize blockSize() {
        return blockSize;
    }

    public RijndaelSBox sBox() {
        return sBox;
    }

    public RijndaelInverseSBox inverseSBox() {
        return inverseSBox;
    }

    public enum KeySize {
        KEY_128(128),
        KEY_192(192),
        KEY_256(256);

        private final int value;

        KeySize(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public int numColumns() {
            return value / 2;
        }

        public static KeySize of(int value) {
            return Arrays.stream(values())
                .filter((size) -> size.value() == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid key size"));
        }
    }

    public enum BlockSize {
        BLOCK_128(128),
        BLOCK_192(192),
        BLOCK_256(256);

        private final int value;

        BlockSize(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public int numColumns() {
            return value / 4;
        }

        public static BlockSize of(int value) {
            return Arrays.stream(values())
                .filter((size) -> size.value() == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid block size"));
        }
    }
}

package dora.crypto.block.rijndael;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public final class RijndaelParameters {

    private static final short AES_MODULUS = 0x11b;

    private final KeySize keySize;
    private final BlockSize blockSize;
    private final RijndaelSBox sBox;
    private final RijndaelInverseSBox inverseSBox;
    private final RijndaelRcon rcon;

    public RijndaelParameters(
        @NotNull KeySize keySize,
        @NotNull BlockSize blockSize,
        short modulus
    ) {
        this.keySize = Objects.requireNonNull(keySize, "key size");
        this.blockSize = Objects.requireNonNull(blockSize, "block size");
        this.sBox = new RijndaelSBox(modulus);
        this.inverseSBox = new RijndaelInverseSBox(modulus);
        this.rcon = new RijndaelRcon(modulus, keySize.words(), blockSize.words(), rounds());
    }

    //region Factory methods
    public static RijndaelParameters aes128() {
        return new RijndaelParameters(KeySize.KEY_128, BlockSize.BLOCK_128, AES_MODULUS);
    }

    public static RijndaelParameters aes192() {
        return new RijndaelParameters(KeySize.KEY_192, BlockSize.BLOCK_128, AES_MODULUS);
    }

    public static RijndaelParameters aes256() {
        return new RijndaelParameters(KeySize.KEY_256, BlockSize.BLOCK_128, AES_MODULUS);
    }
    //endregion

    //region Getters
    public int rounds() {
        return Math.max(keySize.words(), blockSize.words()) + 6;
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

    public byte[][] rcon() {
        return rcon.value();
    }
    //endregion

    public enum KeySize {
        KEY_128(16),
        KEY_192(24),
        KEY_256(32);

        private final int bytes;

        KeySize(int bytes) {
            this.bytes = bytes;
        }

        public int bytes() {
            return bytes;
        }

        public int words() {
            return bytes / 4;
        }

        public static KeySize ofBytes(int bytes) {
            return Arrays.stream(values())
                .filter((size) -> size.bytes() == bytes)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid key size"));
        }
    }

    public enum BlockSize {
        BLOCK_128(16),
        BLOCK_192(24),
        BLOCK_256(32);

        private final int bytes;

        BlockSize(int bytes) {
            this.bytes = bytes;
        }

        public int bytes() {
            return bytes;
        }

        public int words() {
            return bytes / 4;
        }

        public static BlockSize ofBytes(int bytes) {
            return Arrays.stream(values())
                .filter((size) -> size.bytes() == bytes)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid block size"));
        }
    }
}

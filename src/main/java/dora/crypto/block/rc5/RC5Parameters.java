package dora.crypto.block.rc5;

import java.util.Arrays;


public class RC5Parameters {
    public enum BlockHalfSize {
        KEY_16W(16),
        KEY_32W(32),
        KEY_64W(64);

        private final int bitCount;

        BlockHalfSize(int bits) {
            this.bitCount = bits;
        }

        int bitCount() {
            return bitCount;
        }

        int byteCount(){
            return bitCount / 8;
        }

        public static BlockHalfSize ofBytes(int bits) {
            return Arrays.stream(values())
                    .filter((size) -> size.bitCount() == bits)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid key size"));
        }
    }

    private final BlockHalfSize w;
    private final int r;
    private final int b;


    public RC5Parameters(int w, int r, int b) {

        if (w != 16 && w != 32 && w != 64) {
            throw new IllegalArgumentException("W must be 16, 32 or 64");
        }

        if (intNotInRange(r)) {
            throw new IllegalArgumentException("R must be in [0; 255]");
        }

        if (intNotInRange(b)) {
            throw new IllegalArgumentException("B must be in [0; 255]");
        }
        this.w = BlockHalfSize.ofBytes(w);
        this.r = r;
        this.b = b;
    }

    private boolean intNotInRange(int val) {
        return !(0 <= val && val <= 255);
    }

    public BlockHalfSize w() {
        return w;
    }

    public int r() {
        return r;
    }

    public int b() {
        return b;
    }
}

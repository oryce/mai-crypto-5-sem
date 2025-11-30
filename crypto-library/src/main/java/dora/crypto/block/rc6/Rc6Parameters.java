package dora.crypto.block.rc6;

/**
 * @param wordSize the length of a word in bits
 * @param rounds   the number of rounds to use when encrypting data
 * @param keySize  the length of the key in bytes
 */
public record Rc6Parameters(WordSize wordSize, int rounds, int keySize) {

    public Rc6Parameters {
        if (!(rounds >= 1 && rounds <= 255))
            throw new IllegalArgumentException("rounds must be between 1 and 255");
        if (!(keySize >= 0 && keySize <= 255))
            throw new IllegalArgumentException("key size must be between 0 and 255");
    }

    public static Rc6Parameters aesCandidate() {
        return new Rc6Parameters(WordSize.WORD_SIZE_32, 20, 16);
    }

    public enum WordSize {

        WORD_SIZE_16(16),
        WORD_SIZE_32(32),
        WORD_SIZE_64(64);

        private final int bits;

        WordSize(int bits) {
            this.bits = bits;
        }

        public int bits() {
            return bits;
        }

        public int bytes() {
            return Math.divideExact(bits, Byte.SIZE);
        }
    }
}

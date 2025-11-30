package dora.crypto.block.rc5;

/**
 * @param wordSize the length of a word in bits
 * @param rounds   the number of rounds to use when encrypting data
 * @param keySize  the length of the key in bytes
 */
public record Rc5Parameters(WordSize wordSize, int rounds, int keySize) {

    public Rc5Parameters {
        if (!(rounds >= 1 && rounds <= 255))
            throw new IllegalArgumentException("rounds must be between 1 and 255");
        if (!(keySize >= 0 && keySize <= 255))
            throw new IllegalArgumentException("key size must be between 0 and 255");
    }

    public enum WordSize {

        WORD_SIZE_16(16),
        WORD_SIZE_32(32),
        WORD_SIZE_64(64);

        private final int bits;

        WordSize(int bits) {
            this.bits = bits;
        }

        int bits() {
            return bits;
        }

        int bytes() {
            return bits / Byte.SIZE;
        }
    }
}

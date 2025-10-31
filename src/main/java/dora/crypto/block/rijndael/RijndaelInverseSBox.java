package dora.crypto.block.rijndael;

public final class RijndaelInverseSBox {

    private final byte[] sBox = new byte[256];

    /**
     * Initializes the inverse Rijndael S-Box.
     *
     * @param modulus irreducible modulus in GF(2^8).
     */
    RijndaelInverseSBox(short modulus) {
        GaloisField field = new GaloisField();
        init(field, modulus);
    }

    public byte lookup(byte b) {
        return sBox[Byte.toUnsignedInt(b)];
    }

    private void init(GaloisField field, short modulus) {
        for (int s = 0; s < 256; s++) {
            byte b = (byte) (rotateLeft((byte) s, 1)
                                 ^ rotateLeft((byte) s, 3)
                                 ^ rotateLeft((byte) s, 6)
                                 ^ 0x05);
            sBox[s] = b == 0 ? 0 : field.invUnchecked(b, modulus);
        }
    }

    private byte rotateLeft(byte b, int distance) {
        int i = Byte.toUnsignedInt(b);
        return (byte) ((i << distance) | (i >> Byte.SIZE - distance));
    }
}

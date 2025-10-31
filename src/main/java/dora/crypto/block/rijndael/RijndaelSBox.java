package dora.crypto.block.rijndael;

public final class RijndaelSBox {

    private final byte[] sBox = new byte[256];

    public RijndaelSBox(short modulus) {
        GaloisField field = new GaloisField();

        if (!field.irreducible(modulus))
            throw new IllegalArgumentException("Modulus may not be reducible");

        init(field, modulus);
    }

    public byte lookup(byte b) {
        return sBox[Byte.toUnsignedInt(b)];
    }

    private void init(GaloisField field, short modulus) {
        for (int a = 0; a < sBox.length; a++) {
            byte b = a == 0 ? 0 : field.inv((byte) a, modulus);
            sBox[a] = (byte) (b ^ rotateLeft(b, 1)
                                  ^ rotateLeft(b, 2)
                                  ^ rotateLeft(b, 3)
                                  ^ rotateLeft(b, 4)
                                  ^ 0x63);
        }
    }

    private byte rotateLeft(byte b, int distance) {
        int i = Byte.toUnsignedInt(b);
        return (byte) ((i << distance) | (i >> Byte.SIZE - distance));
    }
}

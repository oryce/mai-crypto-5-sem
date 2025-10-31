package dora.crypto.block.rijndael;

public final class RijndaelRcon {

    /**
     * The round constant <code>rcon_i</code> for round <code>i</code> of the key
     * expansion is the 32-bit word: <p><code>rcon_i = [rc_i 0 0 0]</code>.
     */
    private final byte[][] rcon;

    /**
     * Initializes Rijndael round constants.
     *
     * @param modulus    irreducible modulus in GF(2^8)
     * @param keyWords   length of the key in 32-bit words
     * @param blockWords length of the block in 32-bit words
     * @param rounds     amount of rounds
     */
    public RijndaelRcon(short modulus, int keyWords, int blockWords, int rounds) {
        GaloisField field = new GaloisField();

        if (!field.irreducible(modulus))
            throw new IllegalArgumentException("Modulus may not be reducible");

        rcon = new byte[Math.ceilDiv(blockWords * (rounds + 1), keyWords)][4];
        init(field, modulus);
    }

    public byte[][] value() {
        return rcon;
    }

    private void init(GaloisField field, short modulus) {
        rcon[0][0] = 0b1;

        for (int i = 1; i < rcon.length; i++) {
            rcon[i][0] = field.mulMod(rcon[i - 1][0], (byte) 0b10, modulus);
        }
    }
}

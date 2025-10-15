package dora.crypto.mode;

import dora.crypto.block.BlockCipher;
import dora.crypto.mode.Parameters.IvParameters;

import java.util.Arrays;

public final class PcbcCipherMode extends AbstractCipherMode {

    private byte[] prevBlock;

    public PcbcCipherMode(BlockCipher cipher) {
        super(cipher);
    }

    @Override
    protected void initMode(Parameters parameters) {
        if (!(parameters instanceof IvParameters(byte[] ivParam))) {
            throw new IllegalArgumentException("expected IvParameters");
        }

        if (ivParam.length != blockSize) {
            throw new IllegalArgumentException(
                "expected %d-byte IV".formatted(blockSize));
        }

        prevBlock = ivParam.clone();
    }

    @Override
    protected byte[] encryptBlocks(byte[] plaintext) {
        byte[] ciphertext = new byte[plaintext.length];

        for (int i = 0; i < plaintext.length; i += blockSize) {
            byte[] plainBlock = Arrays.copyOfRange(plaintext, i, i + blockSize);
            byte[] cipherBlock = cipher.encrypt(xor(plainBlock, prevBlock));

            System.arraycopy(cipherBlock, 0, ciphertext, i, blockSize);

            prevBlock = xor(plainBlock, cipherBlock);
        }

        return ciphertext;
    }

    @Override
    protected byte[] decryptBlocks(byte[] ciphertext) {
        byte[] plaintext = new byte[ciphertext.length];

        for (int i = 0; i < ciphertext.length; i += blockSize) {
            byte[] cipherBlock = Arrays.copyOfRange(ciphertext, i, i + blockSize);
            byte[] plainBlock = xor(cipher.decrypt(cipherBlock), prevBlock);

            System.arraycopy(plainBlock, 0, plaintext, i, blockSize);

            prevBlock = xor(cipherBlock, plainBlock);
        }

        return plaintext;
    }

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
    }
}

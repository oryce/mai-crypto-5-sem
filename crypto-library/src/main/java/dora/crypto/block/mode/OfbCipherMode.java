package dora.crypto.block.mode;

import dora.crypto.block.BlockCipher;
import dora.crypto.block.mode.Parameters.IvParameters;

public final class OfbCipherMode extends AbstractCipherMode {

    private byte[] prevBlock;

    public OfbCipherMode(BlockCipher cipher) {
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
        return processBlocks(plaintext);
    }

    @Override
    protected byte[] decryptBlocks(byte[] ciphertext) {
        return processBlocks(ciphertext);
    }

    private byte[] processBlocks(byte[] data) {
        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i += blockSize) {
            byte[] encrypted = cipher.encrypt(prevBlock);

            for (int j = 0; j < blockSize; j++) {
                result[i + j] = (byte) (encrypted[j] ^ data[i + j]);
            }

            prevBlock = encrypted;
        }

        return result;
    }
}

package dora.crypto.padding;

import java.util.Arrays;

public abstract class AbstractPadding implements Padding {

    protected static final byte[] NO_PADDING = new byte[0];

    @Override
    public byte[] pad(byte[] data, int blockSize) {
        int remaining = blockSize - data.length % blockSize;

        byte[] padding = padding(remaining, blockSize);
        if (padding.length == 0) return data.clone();

        byte[] padded = new byte[data.length + padding.length];
        System.arraycopy(data, 0, padded, 0, data.length);
        System.arraycopy(padding, 0, padded, data.length, padding.length);

        return padded;
    }

    protected abstract byte[] padding(int remaining, int blockSize);

    @Override
    public byte[] unpad(byte[] data, int blockSize) {
        int paddingSize = paddingSize(data, blockSize);

        if (paddingSize > blockSize) {
            throw new IllegalArgumentException("Data is not properly padded");
        }

        return Arrays.copyOfRange(data, 0, data.length - paddingSize);
    }

    protected abstract int paddingSize(byte[] data, int blockSize);
}

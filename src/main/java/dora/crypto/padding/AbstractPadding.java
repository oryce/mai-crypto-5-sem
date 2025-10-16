package dora.crypto.padding;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public abstract class AbstractPadding implements Padding {

    protected static final byte[] NO_PADDING = new byte[0];

    @Override
    public byte[] pad(byte @NotNull [] data, int blockSize) {
        requireNonNull(data, "data");

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
    public byte[] unpad(byte @NotNull [] data, int blockSize) {
        requireNonNull(data, "data");

        int paddingSize = paddingSize(data, blockSize);

        if (paddingSize > blockSize) {
            throw new IllegalArgumentException(
                "Data is not properly padded. Such issues can arise if the decryption failed.");
        }

        return Arrays.copyOfRange(data, 0, data.length - paddingSize);
    }

    protected abstract int paddingSize(byte[] data, int blockSize);
}

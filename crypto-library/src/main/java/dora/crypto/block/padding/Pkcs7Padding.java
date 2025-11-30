package dora.crypto.block.padding;

import java.util.Arrays;

public final class Pkcs7Padding extends AbstractPadding {

    @Override
    protected byte[] padding(int remaining, int blockSize) {
        if (remaining == 0) remaining = blockSize;
        byte[] padded = new byte[remaining];
        Arrays.fill(padded, (byte) remaining);
        return padded;
    }

    @Override
    protected int paddingSize(byte[] data, int blockSize) {
        return data[data.length - 1] & 0xff;
    }
}

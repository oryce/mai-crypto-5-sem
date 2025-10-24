package dora.crypto.block.padding;

import java.util.Arrays;

public final class AnsiX923Padding extends AbstractPadding {

    @Override
    protected byte[] padding(int remaining, int blockSize) {
        if (remaining == 0) return NO_PADDING;

        byte[] padding = new byte[remaining];

        Arrays.fill(padding, (byte) 0);
        padding[remaining - 1] = (byte) remaining;

        return padding;
    }

    @Override
    protected int paddingSize(byte[] data, int blockSize) {
        return data[data.length - 1] & 0xff;
    }
}

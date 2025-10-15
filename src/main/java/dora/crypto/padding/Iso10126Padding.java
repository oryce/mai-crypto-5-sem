package dora.crypto.padding;

import java.security.SecureRandom;

public class Iso10126Padding extends AbstractPadding {

    private final SecureRandom random;

    public Iso10126Padding(SecureRandom random) {
        this.random = random;
    }

    public Iso10126Padding() {
        this(new SecureRandom());
    }

    @Override
    protected byte[] padding(int remaining, int blockSize) {
        if (remaining == 0) return NO_PADDING;

        byte[] padding = new byte[remaining];

        for (int i = 0; i < remaining - 1; i++) {
            padding[i] = (byte) random.nextInt(256);
        }

        padding[remaining - 1] = (byte) remaining;
        return padding;
    }

    @Override
    protected int paddingSize(byte[] data, int blockSize) {
        return data[data.length - 1] & 0xff;
    }
}

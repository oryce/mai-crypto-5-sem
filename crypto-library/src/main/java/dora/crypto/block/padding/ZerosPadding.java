package dora.crypto.block.padding;

public final class ZerosPadding extends AbstractPadding {

    @Override
    protected byte[] padding(int remaining, int blockSize) {
        return new byte[remaining];
    }

    @Override
    protected int paddingSize(byte[] data, int blockSize) {
        int size = 0;
        int i = data.length - 1;
        int j = blockSize - 1;

        while (j >= 0 && data[i] == 0) {
            size++;
            i--;
            j--;
        }

        return size;
    }
}

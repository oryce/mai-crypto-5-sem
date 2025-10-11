package dora.crypto.padding;

public final class Pkcs7Padding implements Padding {

    @Override
    public byte[] pad(byte[] data, int blockSize) {
        return new byte[0];
    }

    @Override
    public byte[] unpad(byte[] data, int blockSize) {
        return new byte[0];
    }
}

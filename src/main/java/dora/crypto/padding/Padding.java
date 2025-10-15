package dora.crypto.padding;

public interface Padding {

    byte[] pad(byte[] data, int blockSize);

    byte[] unpad(byte[] data, int blockSize);
}

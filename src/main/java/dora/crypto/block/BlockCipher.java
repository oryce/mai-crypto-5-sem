package dora.crypto.block;

public interface BlockCipher {

    int blockSize();

    void init(byte[] key);

    byte[] encrypt(byte[] block);

    byte[] decrypt(byte[] block);
}

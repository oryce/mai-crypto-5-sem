package dora.crypto.block;

import org.jetbrains.annotations.NotNull;

public interface BlockCipher {

    int blockSize();

    void init(byte @NotNull [] key);

    byte[] encrypt(byte @NotNull [] plaintext);

    byte[] decrypt(byte @NotNull [] ciphertext);
}

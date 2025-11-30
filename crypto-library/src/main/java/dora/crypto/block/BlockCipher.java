package dora.crypto.block;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface BlockCipher {

    int blockSize();

    Set<Integer> keySizes();

    void init(byte @NotNull [] key);

    byte[] encrypt(byte @NotNull [] plaintext);

    byte[] decrypt(byte @NotNull [] ciphertext);
}

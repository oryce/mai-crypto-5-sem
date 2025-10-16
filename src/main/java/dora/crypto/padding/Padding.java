package dora.crypto.padding;

import org.jetbrains.annotations.NotNull;

public interface Padding {

    byte[] pad(byte @NotNull [] data, int blockSize);

    byte[] unpad(byte @NotNull [] data, int blockSize);
}

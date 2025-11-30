package dora.crypto.block;

import org.jetbrains.annotations.NotNull;

public interface RoundFunction {

    byte[] apply(byte @NotNull [] block, byte @NotNull [] key);
}

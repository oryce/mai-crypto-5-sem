package dora.crypto.block;

import org.jetbrains.annotations.NotNull;

public interface KeySchedule {

    byte[][] roundKeys(byte @NotNull [] key);
}

package dora.crypto.block;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface KeySchedule {

    byte[][] roundKeys(byte @NotNull [] key);

    Set<Integer> keySizes();
}

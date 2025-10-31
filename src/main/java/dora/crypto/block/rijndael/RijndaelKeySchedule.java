package dora.crypto.block.rijndael;

import dora.crypto.block.KeySchedule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class RijndaelKeySchedule implements KeySchedule {

    private final RijndaelParameters parameters;

    public RijndaelKeySchedule(@NotNull RijndaelParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
    }

    @Override
    public byte[][] roundKeys(byte @NotNull [] key) {
        Objects.requireNonNull(key, "key");

        return new byte[0][];
    }
}

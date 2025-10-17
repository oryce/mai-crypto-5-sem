package dora.crypto.block.deal;

import dora.crypto.block.FeistelBlockCipher;
import org.jetbrains.annotations.NotNull;

public final class DealBlockCipher extends FeistelBlockCipher {

    /**
     * Constructs a DEAL block cipher instance.
     *
     * @param desKey key to use for DES operations
     */
    public DealBlockCipher(byte @NotNull [] desKey) {
        super(
            new DealKeySchedule(desKey),
            new DealRoundFunction(desKey),
            16
        );
    }
}

package dora.crypto.block.deal;

import dora.crypto.block.RoundFunction;
import dora.crypto.block.des.DesBlockCipher;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * DEAL uses DES encryption as its round function.
 */
public final class DealRoundFunction implements RoundFunction {

    private final DesBlockCipher des;

    public DealRoundFunction(byte @NotNull [] desKey) {
        des = new DesBlockCipher();
        des.init(requireNonNull(desKey, "DES key"));
    }

    @Override
    public byte[] apply(byte @NotNull [] block, byte @NotNull [] key) {
        return des.encrypt(block);
    }
}

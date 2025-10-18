package dora.crypto.block.deal;

import dora.crypto.block.RoundFunction;
import dora.crypto.block.des.DesBlockCipher;
import org.jetbrains.annotations.NotNull;

/**
 * DEAL uses DES encryption as its round function.
 */
public final class DealRoundFunction implements RoundFunction {

    @Override
    public byte[] apply(byte @NotNull [] block, byte @NotNull [] key) {
        DesBlockCipher des = new DesBlockCipher();
        des.init(key);
        return des.encrypt(block);
    }
}

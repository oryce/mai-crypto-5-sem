package dora.crypto.block.rc5;

import dora.crypto.block.BlockCipher;
import org.jetbrains.annotations.NotNull;

public class RC5BlockCipher implements BlockCipher {
    private final RC5Parameters parameters;
    private final RC5KeySchedule keyShedule;
    private byte[][] roundKeys;

    public RC5BlockCipher(@NotNull RC5Parameters params){
        parameters = params;
        keyShedule = new RC5KeySchedule(parameters);
    }

    @Override
    public int blockSize() {
        return parameters.w() * 2;
    }

    @Override
    public void init(byte @NotNull [] key) {
        roundKeys = keyShedule.roundKeys(key);
    }

    @Override
    public byte[] encrypt(byte @NotNull [] plaintext) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte @NotNull [] ciphertext) {
        return new byte[0];
    }
}

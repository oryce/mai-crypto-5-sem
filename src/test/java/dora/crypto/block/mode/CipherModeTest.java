package dora.crypto.block.mode;

import net.jqwik.api.*;

public abstract class CipherModeTest {

    protected CipherMode cipherMode;

    CipherModeTest(CipherMode cipherMode) {
        this.cipherMode = cipherMode;
    }

    @Provide
    Arbitrary<byte[]> multipleOfBlockSize() {
        return Arbitraries.integers()
            .between(0, 128)
            .flatMap((blocks) -> {
                int size = blocks * cipherMode.blockSize();
                return Arbitraries.bytes().array(byte[].class).ofSize(size);
            });
    }
}

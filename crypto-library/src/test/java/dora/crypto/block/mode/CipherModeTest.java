package dora.crypto.block.mode;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class CipherModeTest {

    protected CipherMode cipherMode;

    CipherModeTest(CipherMode cipherMode) {
        this.cipherMode = cipherMode;
    }

    @Property(tries = 100)
    void notMultipleOfBlockSizeThrowsException(@ForAll byte[] data) {
        Assume.that(data.length % cipherMode.blockSize() != 0);

        assertThatThrownBy(() -> cipherMode.encrypt(data))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cipherMode.decrypt(data))
            .isInstanceOf(IllegalArgumentException.class);
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

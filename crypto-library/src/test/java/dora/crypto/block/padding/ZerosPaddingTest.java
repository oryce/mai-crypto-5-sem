package dora.crypto.block.padding;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

public class ZerosPaddingTest {

    private final Padding padding;

    ZerosPaddingTest() {
        padding = new ZerosPadding();
    }

    @Property(tries = 1000)
    void unpaddingPaddedDataEqualsOriginal(
        @ForAll @Size(min = 1) byte[] data,
        @ForAll @IntRange(min = 1, max = 255) int blockSize
    ) {
        Assume.that(data[data.length - 1] != 0);

        byte[] padded = padding.pad(data, blockSize);
        byte[] unpadded = padding.unpad(padded, blockSize);

        assertThat(unpadded).isEqualTo(data);
    }
}

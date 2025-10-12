package dora.crypto.padding;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

public class AnsiX923PaddingTest {

    private final Padding padding;

    AnsiX923PaddingTest() {
        padding = new AnsiX923Padding();
    }

    @Property(tries = 1000)
    void unpaddingPaddedDataEqualsOriginal(
        @ForAll @Size(min = 1) byte[] data,
        @ForAll @IntRange(min = 1, max = 255) int blockSize
    ) {
        byte[] padded = padding.pad(data, blockSize);
        byte[] unpadded = padding.unpad(padded, blockSize);

        assertThat(unpadded).isEqualTo(data);
    }
}

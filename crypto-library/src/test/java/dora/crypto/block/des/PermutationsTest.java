package dora.crypto.block.des;

import net.jqwik.api.Example;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PermutationsTest {

    @Example
    void oneByte_Reverse_ZeroIndexed() {
        byte[] input = { (byte) 0b11110010 };
        byte[] expected = { (byte) 0b01001111 };
        int[] pBox = { 0, 1, 2, 3, 4, 5, 6, 7 };

        byte[] output = Permutations.permute(input, pBox, true, false);

        assertThat(toBinaryString(output))
            .isEqualTo(toBinaryString(expected));
    }

    @Example
    void oneByte_OneIndexed() {
        byte[] input = { (byte) 0b10101101 };
        byte[] expected = { (byte) 0b01101110 };
        int[] pBox = { 4, 5, 1, 2, 3, 6, 8, 7 };

        byte[] output = Permutations.permute(input, pBox, false, true);

        assertThat(toBinaryString(output))
            .isEqualTo(toBinaryString(expected));
    }

    @Example
    void multipleBytes_Reverse_ZeroIndexed() {
        byte[] input = { (byte) 0b01011010, (byte) 0b11001010 };
        byte[] expected = { (byte) 0b01010011, (byte) 0b01011010 };
        int[] pBox = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

        byte[] output = Permutations.permute(input, pBox, true, false);

        assertThat(toBinaryString(output))
            .isEqualTo(toBinaryString(expected));
    }

    @Example
    void multipleBytes_OneIndexed() {
        byte[] input = { (byte) 0b01011010, (byte) 0b11001010 };
        byte[] expected = { (byte) 0b01010011, (byte) 0b01011010 };
        int[] pBox = new int[] { 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

        byte[] output = Permutations.permute(input, pBox, false, true);

        assertThat(toBinaryString(output))
            .isEqualTo(toBinaryString(expected));
    }

    @Example
    void oneByte_Expand() {
        byte[] input = { (byte) 0b00000001 };
        byte[] expected = { (byte) 0b11111111, (byte) 0b11111111 };
        int[] pBox = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

        byte[] output = Permutations.permute(input, pBox, true, true);

        assertThat(toBinaryString(output))
            .isEqualTo(toBinaryString(expected));
    }

    @Example
    void multipleBytes_Compress() {
        byte[] input = { (byte) 0b11111111, (byte) 0b11111111 };
        byte[] expected = { (byte) 0b10000000 };
        int[] pBox = new int[] { 1 };

        byte[] output = Permutations.permute(input, pBox, true, true);

        assertThat(toBinaryString(output))
            .isEqualTo(toBinaryString(expected));
    }

    private static String toBinaryString(byte[] bytes) {
        return IntStream.range(0, bytes.length)
            .mapToObj((i) -> Integer.toBinaryString((bytes[i] & 0xFF) + 0x100).substring(1))
            .collect(Collectors.joining());
    }
}

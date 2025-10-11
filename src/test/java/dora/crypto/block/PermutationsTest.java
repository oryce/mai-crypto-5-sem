package dora.crypto.block;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PermutationsTest {

    private static String toBinaryString(byte[] words) {
        return IntStream.range(0, words.length)
            .mapToObj((i) -> Integer.toBinaryString((words[i] & 0xFF) + 0x100).substring(1))
            .collect(Collectors.joining());
    }

    @Test
    public void testOneByte_Reverse_ZeroIndexed() {
        byte[] input = { (byte) 0b11110010 };
        byte[] expected = { (byte) 0b01001111 };
        int[] pBox = { 0, 1, 2, 3, 4, 5, 6, 7 };

        Permutations.permute(input, pBox, true, false);

        assertThat(toBinaryString(input)).isEqualTo(toBinaryString(expected));
    }

    @Test
    public void testOneByte_OneIndexed() {
        byte[] input = { (byte) 0b10101101 };
        byte[] expected = { (byte) 0b01101110 };
        int[] pBox = { 4, 5, 1, 2, 3, 6, 8, 7 };

        Permutations.permute(input, pBox, false, true);

        assertThat(toBinaryString(input)).isEqualTo(toBinaryString(expected));
    }

    @Test
    public void testMultipleBytes_Reverse_ZeroIndexed() {
        byte[] input = { (byte) 0b01011010, (byte) 0b11001010 };
        byte[] expected = { (byte) 0b01010011, (byte) 0b01011010 };
        int[] pBox = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

        Permutations.permute(input, pBox, true, false);

        assertThat(toBinaryString(input)).isEqualTo(toBinaryString(expected));
    }

    @Test
    public void testMultipleBytes_OneIndexed() {
        byte[] input = { (byte) 0b01011010, (byte) 0b11001010 };
        byte[] expected = { (byte) 0b01010011, (byte) 0b01011010 };
        int[] pBox = new int[] { 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

        Permutations.permute(input, pBox, false, true);

        assertThat(toBinaryString(input)).isEqualTo(toBinaryString(expected));
    }
}

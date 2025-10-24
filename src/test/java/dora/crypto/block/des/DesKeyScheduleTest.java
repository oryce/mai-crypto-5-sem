package dora.crypto.block.des;

import dora.crypto.block.KeySchedule;
import net.jqwik.api.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DesKeyScheduleTest {

    private final KeySchedule keySchedule;

    DesKeyScheduleTest() {
        keySchedule = new DesKeySchedule();
    }

    @Property(tries = 100)
    void invalidKeySizeThrowsException(@ForAll byte[] key) {
        Assume.that(key.length != 8);

        assertThatThrownBy(() -> keySchedule.roundKeys(key))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Example
    void keyScheduleWorks() {
        /* https://simewu.com/des/ */

        byte[] input = new byte[] {
            (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
            (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef
        };

        byte[][] expected = Arrays.stream(new long[] {
                parseKey("000010 110000 001001 100111 100110 110100 100110 100101"),
                parseKey("011010 011010 011001 011001 001001 010110 101000 100110"),
                parseKey("010001 011101 010010 001010 101101 000010 100011 010010"),
                parseKey("011100 101000 100111 010010 101001 011000 001001 010111"),
                parseKey("001111 001110 100000 000011 000101 111010 011011 000010"),
                parseKey("001000 110010 010100 011110 001111 001000 010101 000101"),
                parseKey("011011 000000 010010 010101 000010 101110 010011 000110"),
                parseKey("010101 111000 100000 111000 011011 001110 010110 000001"),
                parseKey("110000 001100 100111 101001 001001 101011 100000 111001"),
                parseKey("100100 011110 001100 000111 011000 110001 110101 110010"),
                parseKey("001000 010001 111110 000011 000011 011000 100100 111010"),
                parseKey("011100 010011 000011 100101 010001 010101 110001 010100"),
                parseKey("100100 011100 010011 010000 010010 011000 000011 111100"),
                parseKey("010101 000100 001110 110110 100000 011101 110010 001101"),
                parseKey("101101 101001 000100 000101 000010 100001 011010 110101"),
                parseKey("110010 100011 110100 000011 101110 000111 000000 110010")
            })
            .mapToObj(this::keyToArray)
            .toArray(byte[][]::new);

        byte[][] output = keySchedule.roundKeys(input);

        assertThat(output).isEqualTo(expected);
    }

    private long parseKey(String binaryKey) {
        binaryKey = binaryKey.replace(" ", "");
        return Long.parseLong(binaryKey, 2);
    }

    private byte[] keyToArray(long key) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(key);
        return Arrays.copyOfRange(buffer.array(), 2, 8);
    }
}

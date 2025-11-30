package dora.crypto.block.rijndael;

import dora.crypto.block.KeySchedule;
import net.jqwik.api.Example;

import java.util.Arrays;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

public class RijndaelKeyScheduleTest {

    @Example
    void aesKeyScheduleWorks() {
        KeySchedule keySchedule = new RijndaelKeySchedule(RijndaelParameters.aes128());

        byte[][] roundKeys = keySchedule.roundKeys(new byte[] {
            (byte) 0xaa, (byte) 0xbb, (byte) 0x62, (byte) 0x16,
            (byte) 0x19, (byte) 0x3e, (byte) 0x96, (byte) 0xbe,
            (byte) 0x0a, (byte) 0x4c, (byte) 0xad, (byte) 0xa5,
            (byte) 0x02, (byte) 0x87, (byte) 0x4b, (byte) 0x93
        });

        String[] formattedKeys = Arrays.stream(roundKeys)
            .map((roundKey) -> HexFormat.of().formatHex(roundKey))
            .toArray(String[]::new);

        String[] expectedKeys = new String[] {
            "aabb6216193e96be0a4cada502874b93",
            "bc08be61a53628dfaf7a857aadfdcee9",
            "ea83a0f44fb5882be0cf0d514d32c3b8",
            "cdadcc178218443c62d7496d2fe58ad5",
            "1cd3cf029ecb8b3efc1cc253d3f94886",
            "95818b640b4a005af756c20924af8a8f",
            "ccfff852c7b5f80830e33a01144cb08e",
            "a518e1a862ad19a0524e23a14602932f",
            "52c4f4f23069ed526227cef324255ddc",
            "768872c446e19f9624c6516500e30cb9",
            "517624a71797bb313351ea5433b2e6ed"
        };

        assertThat(formattedKeys).hasSameSizeAs(expectedKeys);

        for (int i = 0; i < formattedKeys.length; i++) {
            assertThat(formattedKeys[i])
                .describedAs("keys[%d] should equal expected[%d]", i, i)
                .isEqualTo(expectedKeys[i]);
        }
    }
}

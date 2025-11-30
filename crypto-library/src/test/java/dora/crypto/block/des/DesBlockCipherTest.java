package dora.crypto.block.des;

import dora.crypto.block.BlockCipher;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

public class DesBlockCipherTest {

    private final BlockCipher blockCipher;

    DesBlockCipherTest() {
        blockCipher = new DesBlockCipher();
    }

    @Property(tries = 1000)
    void decryptedCiphertextEqualsPlaintext(
        @ForAll @Size(value = 8) byte[] plaintext,
        @ForAll @Size(value = 8) byte[] key
    ) {
        blockCipher.init(key);

        byte[] encrypted = blockCipher.encrypt(plaintext);
        byte[] decrypted = blockCipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    /* https://simewu.com/des/ */

    @Example
    void encryptionWorks() {
        byte[] input = new byte[] {
            (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
            (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef
        };

        byte[] key = new byte[] {
            (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
            (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef
        };

        byte[] expected = new byte[] {
            (byte) 0x56, (byte) 0xcc, (byte) 0x09, (byte) 0xe7,
            (byte) 0xcf, (byte) 0xdc, (byte) 0x4c, (byte) 0xef
        };

        blockCipher.init(key);
        byte[] output = blockCipher.encrypt(input);

        assertThat(output).isEqualTo(expected);
    }

    @Example
    void decryptionWorks() {
        byte[] input = new byte[] {
            (byte) 0x56, (byte) 0xcc, (byte) 0x09, (byte) 0xe7,
            (byte) 0xcf, (byte) 0xdc, (byte) 0x4c, (byte) 0xef
        };

        byte[] key = new byte[] {
            (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
            (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef
        };

        byte[] expected = new byte[] {
            (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
            (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef
        };

        blockCipher.init(key);
        byte[] output = blockCipher.decrypt(input);

        assertThat(output).isEqualTo(expected);
    }
}

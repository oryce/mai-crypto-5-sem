package dora.crypto.block.rijndael;

import dora.crypto.block.rijndael.RijndaelParameters.BlockSize;
import dora.crypto.block.rijndael.RijndaelParameters.KeySize;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

public class RijndaelBlockCipherTest {

    @Property(tries = 1000)
    void decryptedCiphertextEqualsPlaintext(
        @ForAll @Size(value = 16) byte[] plaintext,
        @ForAll @Size(value = 16) byte[] key,
        @ForAll("irreducibleModulus") short modulus
    ) {
        RijndaelBlockCipher blockCipher = new RijndaelBlockCipher(
            new RijndaelParameters(KeySize.KEY_128, BlockSize.BLOCK_128, modulus)
        );

        blockCipher.init(key);

        byte[] encrypted = blockCipher.encrypt(plaintext);
        byte[] decrypted = blockCipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Provide("irreducibleModulus")
    Arbitrary<Short> irreducibleModulus() {
        GaloisField field = new GaloisField();
        return Arbitraries.of(field.irreducibles());
    }

    @Example
    void encryptionIsCorrect() {
        RijndaelBlockCipher blockCipher = new RijndaelBlockCipher(
            RijndaelParameters.aes128());

        byte[] plaintext = new byte[] {
            (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89,
            (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0xfe, (byte) 0xdc,
            (byte) 0xba, (byte) 0x98, (byte) 0x76, (byte) 0x54, (byte) 0x32,
            (byte) 0x10
        };

        byte[] key = new byte[] {
            (byte) 0x0f, (byte) 0x15, (byte) 0x71, (byte) 0xc9, (byte) 0x47,
            (byte) 0xd9, (byte) 0xe8, (byte) 0x59, (byte) 0x1c, (byte) 0xb7,
            (byte) 0xad, (byte) 0xd6, (byte) 0xaf, (byte) 0x7f, (byte) 0x67,
            (byte) 0x98
        };

        byte[] expected = new byte[] {
            (byte) 0x34, (byte) 0xd3, (byte) 0xf0, (byte) 0xee, (byte) 0xcb,
            (byte) 0x4d, (byte) 0xfa, (byte) 0x16, (byte) 0xcb, (byte) 0x8b,
            (byte) 0xf0, (byte) 0x7f, (byte) 0x29, (byte) 0xa0, (byte) 0xcb,
            (byte) 0x79
        };

        blockCipher.init(key);
        byte[] ciphertext = blockCipher.encrypt(plaintext);

        assertThat(ciphertext).isEqualTo(expected);
    }

    @Example
    void decryptionIsCorrect() {
        RijndaelBlockCipher blockCipher = new RijndaelBlockCipher(
            RijndaelParameters.aes128());

        byte[] ciphertext = new byte[] {
            (byte) 0x34, (byte) 0xd3, (byte) 0xf0, (byte) 0xee, (byte) 0xcb,
            (byte) 0x4d, (byte) 0xfa, (byte) 0x16, (byte) 0xcb, (byte) 0x8b,
            (byte) 0xf0, (byte) 0x7f, (byte) 0x29, (byte) 0xa0, (byte) 0xcb,
            (byte) 0x79
        };

        byte[] key = new byte[] {
            (byte) 0x0f, (byte) 0x15, (byte) 0x71, (byte) 0xc9, (byte) 0x47,
            (byte) 0xd9, (byte) 0xe8, (byte) 0x59, (byte) 0x1c, (byte) 0xb7,
            (byte) 0xad, (byte) 0xd6, (byte) 0xaf, (byte) 0x7f, (byte) 0x67,
            (byte) 0x98
        };

        byte[] expected = new byte[] {
            (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89,
            (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0xfe, (byte) 0xdc,
            (byte) 0xba, (byte) 0x98, (byte) 0x76, (byte) 0x54, (byte) 0x32,
            (byte) 0x10
        };

        blockCipher.init(key);
        byte[] plaintext = blockCipher.decrypt(ciphertext);

        assertThat(plaintext).isEqualTo(expected);
    }
}

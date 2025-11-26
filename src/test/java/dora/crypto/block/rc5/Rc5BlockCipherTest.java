package dora.crypto.block.rc5;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.HexFormat;

import static dora.crypto.block.rc5.Rc5Parameters.WordSize.WORD_SIZE_16;
import static dora.crypto.block.rc5.Rc5Parameters.WordSize.WORD_SIZE_32;
import static dora.crypto.block.rc5.Rc5Parameters.WordSize.WORD_SIZE_64;
import static org.assertj.core.api.Assertions.assertThat;

public class Rc5BlockCipherTest {

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext_16(
        @ForAll @Size(value = 4) byte[] plaintext,
        @ForAll @Size(max = 255) byte[] key,
        @ForAll @IntRange(min = 1, max = 255) int rounds
    ) {
        Rc5Parameters parameters = new Rc5Parameters(WORD_SIZE_16, rounds, key.length);
        Rc5BlockCipher cipher = new Rc5BlockCipher(parameters);
        cipher.init(key);

        byte[] encrypted = cipher.encrypt(plaintext);
        byte[] decrypted = cipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext_32(
        @ForAll @Size(value = 8) byte[] plaintext,
        @ForAll @Size(max = 255) byte[] key,
        @ForAll @IntRange(min = 1, max = 255) int rounds
    ) {
        Rc5Parameters parameters = new Rc5Parameters(WORD_SIZE_32, rounds, key.length);
        Rc5BlockCipher cipher = new Rc5BlockCipher(parameters);
        cipher.init(key);

        byte[] encrypted = cipher.encrypt(plaintext);
        byte[] decrypted = cipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext_64(
        @ForAll @Size(value = 16) byte[] plaintext,
        @ForAll @Size(max = 255) byte[] key,
        @ForAll @IntRange(min = 1, max = 255) int rounds
    ) {
        Rc5Parameters parameters = new Rc5Parameters(WORD_SIZE_64, rounds, key.length);
        Rc5BlockCipher cipher = new Rc5BlockCipher(parameters);
        cipher.init(key);

        byte[] encrypted = cipher.encrypt(plaintext);
        byte[] decrypted = cipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Example
    void testVector1() {
        byte[] plaintext = parseBytes("0000000000000000");
        byte[] key = parseBytes("00000000000000000000000000000000");
        byte[] ciphertext = parseBytes("21A5DBEE154B8F6D");

        Rc5Parameters parameters = new Rc5Parameters(WORD_SIZE_32, 12, key.length);
        Rc5BlockCipher cipher = new Rc5BlockCipher(parameters);
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(ciphertext);
        assertThat(cipher.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    @Example
    void testVector2() {
        byte[] plaintext = parseBytes("21A5DBEE154B8F6D");
        byte[] key = parseBytes("915F4619BE41B2516355A50110A9CE91");
        byte[] ciphertext = parseBytes("F7C013AC5B2B8952");

        Rc5Parameters parameters = new Rc5Parameters(WORD_SIZE_32, 12, key.length);
        Rc5BlockCipher cipher = new Rc5BlockCipher(parameters);
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(ciphertext);
        assertThat(cipher.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    @Example
    void testVector3() {
        byte[] plaintext = parseBytes("F7C013AC5B2B8952");
        byte[] key = parseBytes("783348E75AEB0F2FD7B169BB8DC16787");
        byte[] ciphertext = parseBytes("2F42B3B70369FC92");

        Rc5Parameters parameters = new Rc5Parameters(WORD_SIZE_32, 12, key.length);
        Rc5BlockCipher cipher = new Rc5BlockCipher(parameters);
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(ciphertext);
        assertThat(cipher.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    @Example
    void testVector4() {
        byte[] plaintext = parseBytes("2F42B3B70369FC92");
        byte[] key = parseBytes("DC49DB1375A5584F6485B413B5F12BAF");
        byte[] ciphertext = parseBytes("65C178B284D197CC");

        Rc5Parameters parameters = new Rc5Parameters(WORD_SIZE_32, 12, key.length);
        Rc5BlockCipher cipher = new Rc5BlockCipher(parameters);
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(ciphertext);
        assertThat(cipher.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    @Example
    void testVector5() {
        byte[] plaintext = parseBytes("65C178B284D197CC");
        byte[] key = parseBytes("5269F149D41BA0152497574D7F153125");
        byte[] ciphertext = parseBytes("EB44E415DA319824");

        Rc5Parameters parameters = new Rc5Parameters(WORD_SIZE_32, 12, key.length);
        Rc5BlockCipher cipher = new Rc5BlockCipher(parameters);
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(ciphertext);
        assertThat(cipher.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    private static byte[] parseBytes(String hex) {
        return HexFormat.of().parseHex(hex);
    }
}

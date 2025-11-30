package dora.crypto.block.rc6;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.HexFormat;

import static dora.crypto.block.rc6.Rc6Parameters.WordSize.WORD_SIZE_16;
import static dora.crypto.block.rc6.Rc6Parameters.WordSize.WORD_SIZE_32;
import static dora.crypto.block.rc6.Rc6Parameters.WordSize.WORD_SIZE_64;
import static org.assertj.core.api.Assertions.assertThat;

public class Rc6BlockCipherTest {

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext_wordSize16(
        @ForAll @Size(value = 8) byte[] plaintext,
        @ForAll @Size(max = 255) byte[] key,
        @ForAll @IntRange(min = 1, max = 255) int rounds
    ) {
        Rc6Parameters parameters = new Rc6Parameters(WORD_SIZE_16, rounds, key.length);
        Rc6BlockCipher cipher = new Rc6BlockCipher(parameters);
        cipher.init(key);

        byte[] encrypted = cipher.encrypt(plaintext);
        byte[] decrypted = cipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext_wordSize32(
        @ForAll @Size(value = 16) byte[] plaintext,
        @ForAll @Size(max = 255) byte[] key,
        @ForAll @IntRange(min = 1, max = 255) int rounds
    ) {
        Rc6Parameters parameters = new Rc6Parameters(WORD_SIZE_32, rounds, key.length);
        Rc6BlockCipher cipher = new Rc6BlockCipher(parameters);
        cipher.init(key);

        byte[] encrypted = cipher.encrypt(plaintext);
        byte[] decrypted = cipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext_wordSize64(
        @ForAll @Size(value = 32) byte[] plaintext,
        @ForAll @Size(max = 255) byte[] key,
        @ForAll @IntRange(min = 1, max = 255) int rounds
    ) {
        Rc6Parameters parameters = new Rc6Parameters(WORD_SIZE_64, rounds, key.length);
        Rc6BlockCipher cipher = new Rc6BlockCipher(parameters);
        cipher.init(key);

        byte[] encrypted = cipher.encrypt(plaintext);
        byte[] decrypted = cipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    /* https://web.archive.org/web/20181223080309/http://people.csail.mit.edu/rivest/rc6.pdf */

    @Example
    void testVector1() {
        byte[] plaintext = parseHex("00000000000000000000000000000000");
        byte[] key = parseHex("00000000000000000000000000000000");
        byte[] expected = parseHex("8fc3a53656b1f778c129df4e9848a41e");

        Rc6BlockCipher cipher = new Rc6BlockCipher(Rc6Parameters.aesCandidate());
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(expected);
        assertThat(cipher.decrypt(expected)).isEqualTo(plaintext);
    }

    @Example
    void testVector2() {
        byte[] plaintext = parseHex("02132435465768798a9bacbdcedfe0f1");
        byte[] key = parseHex("0123456789abcdef0112233445566778");
        byte[] expected = parseHex("524e192f4715c6231f51f6367ea43f18");

        Rc6BlockCipher cipher = new Rc6BlockCipher(Rc6Parameters.aesCandidate());
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(expected);
        assertThat(cipher.decrypt(expected)).isEqualTo(plaintext);
    }

    @Example
    void testVector3() {
        byte[] plaintext = parseHex("00000000000000000000000000000000");
        byte[] key = parseHex("000000000000000000000000000000000000000000000000");
        byte[] expected = parseHex("6cd61bcb190b30384e8a3f168690ae82");

        Rc6Parameters parameters = new Rc6Parameters(WORD_SIZE_32, 20, 24);
        Rc6BlockCipher cipher = new Rc6BlockCipher(parameters);
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(expected);
        assertThat(cipher.decrypt(expected)).isEqualTo(plaintext);
    }

    @Example
    void testVector4() {
        byte[] plaintext = parseHex("02132435465768798a9bacbdcedfe0f1");
        byte[] key = parseHex("0123456789abcdef0112233445566778899aabbccddeeff0");
        byte[] expected = parseHex("688329d019e505041e52e92af95291d4");

        Rc6Parameters parameters = new Rc6Parameters(WORD_SIZE_32, 20, 24);
        Rc6BlockCipher cipher = new Rc6BlockCipher(parameters);
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(expected);
        assertThat(cipher.decrypt(expected)).isEqualTo(plaintext);
    }

    @Example
    void testVector5() {
        byte[] plaintext = parseHex("00000000000000000000000000000000");
        byte[] key = parseHex("0000000000000000000000000000000000000000000000000000000000000000");
        byte[] expected = parseHex("8f5fbd0510d15fa893fa3fda6e857ec2");

        Rc6Parameters parameters = new Rc6Parameters(WORD_SIZE_32, 20, 32);
        Rc6BlockCipher cipher = new Rc6BlockCipher(parameters);
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(expected);
        assertThat(cipher.decrypt(expected)).isEqualTo(plaintext);
    }

    @Example
    void testVector6() {
        byte[] plaintext = parseHex("02132435465768798a9bacbdcedfe0f1");
        byte[] key = parseHex("0123456789abcdef0112233445566778899aabbccddeeff01032547698badcfe");
        byte[] expected = parseHex("c8241816f0d7e48920ad16a1674e5d48");

        Rc6Parameters parameters = new Rc6Parameters(WORD_SIZE_32, 20, 32);
        Rc6BlockCipher cipher = new Rc6BlockCipher(parameters);
        cipher.init(key);

        assertThat(cipher.encrypt(plaintext)).isEqualTo(expected);
        assertThat(cipher.decrypt(expected)).isEqualTo(plaintext);
    }

    private static byte[] parseHex(String hex) {
        return HexFormat.of().parseHex(hex);
    }
}

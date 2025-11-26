package dora.crypto.block.rc5;

import dora.crypto.block.BlockCipher;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

public class Rc5BlockCipherTest {

    private BlockCipher blockCipher;

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext64(
        @ForAll @Size(value = 16) byte[] plaintext,
        @ForAll @Size(min = 1, max = 255) byte[] key,
        @ForAll @IntRange(min = 0, max = 255) int r
    ) {
        blockCipher = new RC5BlockCipher(new RC5Parameters(64, r, key.length));
        blockCipher.init(key);

        byte[] encrypted = blockCipher.encrypt(plaintext);
        byte[] decrypted = blockCipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext32(
       @ForAll @Size(value = 8) byte[] plaintext,
       @ForAll @Size(min = 1, max = 255) byte[] key,
       @ForAll @IntRange(min = 0, max = 255) int r
    ) {
        blockCipher = new RC5BlockCipher(new RC5Parameters(32, r, key.length));
        blockCipher.init(key);

        byte[] encrypted = blockCipher.encrypt(plaintext);
        byte[] decrypted = blockCipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext16(
        @ForAll @Size(value = 4) byte[] plaintext,
        @ForAll @Size(min = 1, max = 255) byte[] key,
        @ForAll @IntRange(min = 0, max = 255) int r
    ) {
        blockCipher = new RC5BlockCipher(new RC5Parameters(16, r, key.length));
        blockCipher.init(key);

        byte[] encrypted = blockCipher.encrypt(plaintext);
        byte[] decrypted = blockCipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }
}

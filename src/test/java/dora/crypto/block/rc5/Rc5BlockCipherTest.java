package dora.crypto.block.rc5;

import dora.crypto.block.BlockCipher;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

public class Rc5BlockCipherTest {

    private final BlockCipher blockCipher;

    Rc5BlockCipherTest () {
        blockCipher = new RC5BlockCipher(new RC5Parameters(64, 10, 17));
    }

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext(
            @ForAll @Size(value = 16) byte[] plaintext,
            @ForAll @Size(value = 16) byte[] key
    ) {
        blockCipher.init(key);

        byte[] encrypted = blockCipher.encrypt(plaintext);
        byte[] decrypted = blockCipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }
}

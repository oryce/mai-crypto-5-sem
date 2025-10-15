package dora.crypto.mode;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;

import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;

public class EcbCipherModeTest extends CipherModeTest {

    EcbCipherModeTest() {
        super(new EcbCipherMode(
            new MockBlockCipher(16),
            ForkJoinPool.commonPool()
        ));
    }

    @Property(tries = 1000)
    void decryptedCiphertextEqualsPlaintext(
        @ForAll("multipleOfBlockSize") byte[] plaintext,
        @ForAll @Size(min = 1) byte[] key
    ) throws InterruptedException {
        cipherMode.init(Parameters.NO_PARAMETERS);
        byte[] encrypted = cipherMode.encrypt(plaintext, key);

        cipherMode.init(Parameters.NO_PARAMETERS);
        byte[] decrypted = cipherMode.decrypt(encrypted, key);

        assertThat(decrypted).isEqualTo(plaintext);
    }
}

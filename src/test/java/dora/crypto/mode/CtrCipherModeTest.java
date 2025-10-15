package dora.crypto.mode;

import dora.crypto.mode.CtrCipherMode.CtrParameters;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.constraints.Size;

import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;

public class CtrCipherModeTest extends CipherModeTest {

    CtrCipherModeTest() {
        super(new CtrCipherMode(
            new MockBlockCipher(16),
            ForkJoinPool.commonPool()
        ));
    }

    @Property(tries = 1000)
    void decryptedCiphertextEqualsPlaintext(
        @ForAll("multipleOfBlockSize") byte[] plaintext,
        @ForAll @Size(min = 1) byte[] key,
        @ForAll @Size(value = 8) byte[] nonce,
        @ForAll @Positive int counter
    ) throws InterruptedException {
        cipherMode.init(key, new CtrParameters(nonce, counter));
        byte[] encrypted = cipherMode.encrypt(plaintext);

        cipherMode.init(key, new CtrParameters(nonce, counter));
        byte[] decrypted = cipherMode.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }
}

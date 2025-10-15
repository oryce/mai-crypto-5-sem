package dora.crypto.mode;

import dora.crypto.mode.RandomDeltaCipherMode.RandomDeltaParameters;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.constraints.Size;

import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomDeltaCipherModeTest extends CipherModeTest {

    RandomDeltaCipherModeTest() {
        super(new RandomDeltaCipherMode(
            new MockBlockCipher(16),
            ForkJoinPool.commonPool()
        ));
    }

    @Property(tries = 1000)
    void decryptedCiphertextEqualsPlaintext(
        @ForAll("multipleOfBlockSize") byte[] plaintext,
        @ForAll @Size(min = 1) byte[] key,
        @ForAll @Size(value = 8) byte[] nonce,
        @ForAll @Positive int counter,
        @ForAll long seed
    ) throws InterruptedException {
        cipherMode.init(new RandomDeltaParameters(nonce, counter, seed));
        byte[] encrypted = cipherMode.encrypt(plaintext, key);

        cipherMode.init(new RandomDeltaParameters(nonce, counter, seed));
        byte[] decrypted = cipherMode.decrypt(encrypted, key);

        assertThat(decrypted).isEqualTo(plaintext);
    }
}

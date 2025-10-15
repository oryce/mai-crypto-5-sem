package dora.crypto.mode;

import dora.crypto.mode.Parameters.IvParameters;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

public class PcbcCipherModeTest extends CipherModeTest {

    PcbcCipherModeTest() {
        super(new PcbcCipherMode(new MockBlockCipher(16)));
    }

    @Property(tries = 1000)
    void decryptedCiphertextEqualsPlaintext(
        @ForAll("multipleOfBlockSize") byte[] plaintext,
        @ForAll @Size(min = 1) byte[] key,
        @ForAll @Size(value = 16) byte[] iv
    ) throws InterruptedException {
        cipherMode.init(new IvParameters(iv));
        byte[] encrypted = cipherMode.encrypt(plaintext, key);

        cipherMode.init(new IvParameters(iv));
        byte[] decrypted = cipherMode.decrypt(encrypted, key);

        assertThat(decrypted).isEqualTo(plaintext);
    }
}

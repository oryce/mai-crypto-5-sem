package dora.crypto.block.deal;

import dora.crypto.block.BlockCipher;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

public class DealBlockCipherTest {

    @Property(tries = 1000)
    void decryptedCiphertextEqualsPlaintext(
        @ForAll @Size(value = 16) byte[] plaintext,
        @ForAll("dealSizedKeys") byte[] dealKey,
        @ForAll @Size(value = 8) byte[] desKey
    ) {
        BlockCipher cipher = new DealBlockCipher(desKey);
        cipher.init(dealKey);

        byte[] encrypted = cipher.encrypt(plaintext);
        byte[] decrypted = cipher.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Provide
    Arbitrary<byte[]> dealSizedKeys() {
        return Arbitraries.integers().between(2, 4).flatMap((i) -> {
            int keySize = i * 8; // 16 (128 bits), 24 (192 bits), 32 (256 bits)
            return Arbitraries.bytes().array(byte[].class).ofSize(keySize);
        });
    }
}

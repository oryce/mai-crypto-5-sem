package dora.crypto.block.rc5;

import dora.crypto.block.BlockCipher;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

public class Rc5BlockCipherTest {



    Rc5BlockCipherTest () {

    }

    @Property(tries = 100)
    void decryptedCiphertextEqualsPlaintext(
            @ForAll("testConfigurations") TestConfig config
    ) {
        RC5Parameters params = new RC5Parameters(config.w, config.r, config.b);
        RC5BlockCipher blockCipher = new RC5BlockCipher(params);
        blockCipher.init(config.key);
        byte[] encrypted = blockCipher.encrypt(config.plaintext);
        byte[] decrypted = blockCipher.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(config.plaintext);
    }

    @Provide
    Arbitrary<TestConfig> testConfigurations() {
        Arbitrary<Integer> wordSizes = Arbitraries.of(16, 32, 64);
        Arbitrary<Integer> keySizes = Arbitraries.integers()
                .between(1, 64));

        Arbitrary<Integer> rounds = Arbitraries.integers()
                .between(1, 255);

        return Combinators.combine(wordSizes, keySizes, rounds).flatMap((w, b, r) -> {
            int plaintextLength = w / 4;

            Arbitrary<byte[]> plaintext = Arbitraries.bytes()
                    .array(byte[].class)
                    .ofSize(plaintextLength);

            Arbitrary<byte[]> key = Arbitraries.bytes()
                    .array(byte[].class)
                    .ofSize(b);

            return Combinators.combine(plaintext, key)
                    .as((p, k) -> new TestConfig(p, k, w, b, r));
        });
    }

    static class TestConfig {
        final byte[] plaintext;
        final byte[] key;
        final int w;
        final int b;
        final int r;

        TestConfig(byte[] plaintext, byte[] key, int w, int b, int r) {
            this.plaintext = plaintext;
            this.key = key;
            this.w = w;
            this.b = b;
            this.r = r;
        }
    }
}

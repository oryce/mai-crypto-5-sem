package dora.crypto.rsa;

import dora.crypto.rsa.Rsa.PrimalityTestType;
import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Positive;

import java.math.BigInteger;
import java.util.Map;

import static dora.crypto.rsa.Rsa.PrimalityTestType.FERMAT;
import static dora.crypto.rsa.Rsa.PrimalityTestType.MILLER_RABIN;
import static dora.crypto.rsa.Rsa.PrimalityTestType.SOLOVAY_STRASSEN;
import static org.assertj.core.api.Assertions.assertThat;

public class RsaTest {

    private static final double CERTAINTY = 0.99;
    private static final int PRIME_SIZE = 512;

    private final Map<PrimalityTestType, Rsa> rsa;

    RsaTest() {
        rsa = Map.ofEntries(
            Map.entry(FERMAT, new Rsa(FERMAT, CERTAINTY, PRIME_SIZE)),
            Map.entry(MILLER_RABIN, new Rsa(MILLER_RABIN, CERTAINTY, PRIME_SIZE)),
            Map.entry(SOLOVAY_STRASSEN, new Rsa(SOLOVAY_STRASSEN, CERTAINTY, PRIME_SIZE))
        );
    }

    @Property
    public void decryptedCiphertextEqualsPlaintext(
        @ForAll PrimalityTestType primalityTest,
        @ForAll @Positive BigInteger plaintext
    ) {
        Rsa cipher = rsa.get(primalityTest);

        // Ensure that the plaintext does not exceed modulus.
        Assume.that(cipher.canEncrypt(plaintext));

        assertThat(cipher.decrypt(cipher.encrypt(plaintext)))
            .isEqualTo(plaintext);
    }
}

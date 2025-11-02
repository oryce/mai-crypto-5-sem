package dora.crypto.rsa;

import dora.crypto.rsa.ProbabilisticTests.RsaService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

import java.math.BigInteger;

import static dora.crypto.rsa.ProbabilisticTests.RsaService.PrimalityTest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RSAHelpFuncTest {

    @Example
    void constructorThrowsException() {
        assertThatThrownBy(() -> new RsaService(MILLER_RABIN, 0.999, 256))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PrimeBitLength < 512 is not secure for RSA");
    }

    @Example
    void constructor_initializes_with_valid_keys_for_miller_rabin(@ForAll @Size(min = 512, max = 4048) int primeBitLength) {
        RsaService service = new RsaService(MILLER_RABIN, 0.999, Math.max(512, primeBitLength));
        BigInteger n = service.getN();
        BigInteger e = service.getE();
        BigInteger d = service.getD();

        assertThat(n).isNotNull().isPositive();
        assertThat(e).isEqualTo(BigInteger.valueOf(65537));
        assertThat(d).isPositive().isLessThan(n);
    }
}

class RsaServiceTableMillerTests {

    private final RsaService service = new RsaService(MILLER_RABIN, 0.999, 512); // Используем фиксированный сервис для тестов (маленький для скорости)

    @Property
    void RSAEncryptDecrypt(@ForAll("EncryptDecryptCase") BigInteger a) {
        BigInteger enc = service.encrypt(a);

        assertThat(service.decrypt(enc)).isEqualTo(a);
    }

    @Provide
    Arbitrary<BigInteger> EncryptDecryptCase() {
        return Arbitraries.oneOf(
            Arbitraries.bigIntegers().between(new BigInteger("1".repeat(100)), new BigInteger("1".repeat(154)))
        );
    }
}

class RsaServiceTableFermatTests {

    private final RsaService service = new RsaService(FERMAT, 0.999, 512); // Используем фиксированный сервис для тестов (маленький для скорости)

    @Property
    void RSAEncryptDecrypt(@ForAll("EncryptDecryptCase") BigInteger a) {
        BigInteger enc = service.encrypt(a);

        assertThat(service.decrypt(enc)).isEqualTo(a);
    }

    @Provide
    Arbitrary<BigInteger> EncryptDecryptCase() {
        return Arbitraries.oneOf(
                Arbitraries.bigIntegers().between(new BigInteger("1".repeat(100)), new BigInteger("1".repeat(154)))
        );
    }
}

class RsaServiceTableSolovayTests {

    private final RsaService service = new RsaService(SOLOVAY_STRASSEN, 0.999, 512); // Используем фиксированный сервис для тестов (маленький для скорости)

    @Property
    void RSAEncryptDecrypt(@ForAll("EncryptDecryptCase") BigInteger a) {
        BigInteger enc = service.encrypt(a);

        assertThat(service.decrypt(enc)).isEqualTo(a);
    }

    @Provide
    Arbitrary<BigInteger> EncryptDecryptCase() {
        return Arbitraries.oneOf(
                Arbitraries.bigIntegers().between(new BigInteger("1".repeat(100)), new BigInteger("1".repeat(154)))
        );
    }
}
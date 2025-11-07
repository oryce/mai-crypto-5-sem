package dora.crypto.rsa;

import dora.crypto.rsa.primality.FermatPrimalityTest;
import dora.crypto.rsa.primality.MillerRabinPrimalityTest;
import dora.crypto.rsa.primality.PrimalityTest;
import dora.crypto.rsa.primality.SolovayStrassenPrimalityTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.function.Supplier;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

public final class Rsa {

    private final RsaMath math = new RsaMath();

    /** Modulus. */
    private final BigInteger n;
    /** Public exponent. */
    private final BigInteger e;
    /** Private exponent. */
    private final BigInteger d;

    public Rsa(@NotNull PrimalityTestType primalityTest, double certainty, int primeSize) {
        var generator = new KeyPairGenerator(primalityTest, certainty, primeSize);
        var keyPair = generator.generate();

        n = keyPair.n();
        e = keyPair.e();
        d = keyPair.d();
    }

    public boolean canEncrypt(@NotNull BigInteger plaintext) {
        Objects.requireNonNull(plaintext, "plaintext");
        return plaintext.signum() > 0 && plaintext.compareTo(n) < 0;
    }

    public BigInteger encrypt(@NotNull BigInteger plaintext) {
        Objects.requireNonNull(plaintext, "plaintext");

        if (!canEncrypt(plaintext))
            throw new IllegalArgumentException("plaintext must be positive and may not exceed modulus");

        return math.modPow(plaintext, e, n);
    }

    public BigInteger decrypt(@NotNull BigInteger ciphertext) {
        Objects.requireNonNull(ciphertext, "ciphertext");
        return math.modPow(ciphertext, d, n);
    }

    public enum PrimalityTestType {

        FERMAT(FermatPrimalityTest::new),
        MILLER_RABIN(MillerRabinPrimalityTest::new),
        SOLOVAY_STRASSEN(SolovayStrassenPrimalityTest::new);

        private final Supplier<PrimalityTest> creator;

        PrimalityTestType(Supplier<PrimalityTest> creator) {
            this.creator = creator;
        }

        public PrimalityTest create() {
            return creator.get();
        }
    }

    public static final class KeyPairGenerator {

        private static final BigInteger DEFAULT_EXPONENT = BigInteger.valueOf(65537);

        private final PrimalityTest primalityTest;
        private final double certainty;
        private final int primeSize;

        private final SecureRandom random = new SecureRandom();
        private final RsaMath math = new RsaMath();

        public KeyPairGenerator(@NotNull PrimalityTestType primalityTest, double certainty, int primeSize) {
            if (!(certainty >= 0.5 && certainty < 1.0))
                throw new IllegalArgumentException("certainty must be in [0.5; 1)");
            if (primeSize < 512)
                throw new IllegalArgumentException("insecure prime size");

            this.primalityTest = Objects.requireNonNull(primalityTest.create(), "primality test");
            this.certainty = certainty;
            this.primeSize = primeSize;
        }

        public KeyPair generate() {
            BigInteger e = DEFAULT_EXPONENT;

            // NB: from JCE's RSAKeyPairGenerator.
            int keySize = primeSize * 2;
            int pqDiffSize = Math.ceilDiv(keySize, 2) - 100;

            while (true) {
                BigInteger p = nextProbablePrime();
                BigInteger q;

                // Generate `q` until it's reasonably distant from `p` to prevent Fermat's attack.
                do {
                    q = nextProbablePrime();
                } while (p.subtract(q).abs().compareTo(TWO.pow(pqDiffSize)) <= 0);

                BigInteger n = p.multiply(q);

                KeyPair keyPair = createKeyPair(n, e, p, q);
                if (keyPair != null) return keyPair;
            }
        }

        protected @Nullable KeyPair createKeyPair(BigInteger n, BigInteger e, BigInteger p, BigInteger q) {
            BigInteger phi = p.subtract(ONE).multiply(q.subtract(ONE));
            if (!math.gcd(phi, e).equals(ONE))
                /* `phi` and `e` are not coprime, try again */
                return null;

            BigInteger d = math.modInverse(e, phi);
            if (d.compareTo(TWO.pow(p.bitLength())) <= 0)
                /* prevent Wiener's attack */
                return null;

            return new KeyPair(n, d, e);
        }

        private BigInteger nextProbablePrime() {
            BigInteger p;

            do {
                p = new BigInteger(primeSize, random)
                    .setBit(primeSize - 1) /* enforce key length */
                    .setBit(0) /* odd number (even numbers are obviously not prime) */;
            } while (!primalityTest.isProbablyPrime(p, certainty));

            return p;
        }

        public record KeyPair(BigInteger n, BigInteger e, BigInteger d) {
        }
    }
}

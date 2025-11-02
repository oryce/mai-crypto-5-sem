package dora.crypto.rsa.ProbabilisticTests;

import dora.crypto.rsa.NumberTheory;
import dora.crypto.rsa.ProbabilisticTests.Tests.*;

import java.math.BigInteger;
import java.security.SecureRandom;

import static java.math.BigInteger.TWO;

public class RsaService {

    private BigInteger N; // Module
    private BigInteger e; // Public exponent
    private BigInteger d; // Private exponent

    private final KeyGenerationService keyGenService;

    public enum PrimalityTest {
        FERMAT(new FermatPrimality()),
        MILLER_RABIN(new MillerRabinPrimality()),
        SOLOVAY_STRASSEN(new SolovayStrassenPrimality());

        private final ProbabilisticPrimality strategy;

        PrimalityTest(ProbabilisticPrimality strategy) {
            this.strategy = strategy;
        }

        public ProbabilisticPrimality getStrategy() {
            return strategy;
        }
    }

    public static class KeyGenerationService {

        private final ProbabilisticPrimality primalityTest;
        private final double minProbability;
        protected final int primeBitLength;
        protected final SecureRandom random;

        // Standard public exponent
        private static final BigInteger PUBLIC_EXPONENT = new BigInteger("65537");

        /**
         * Constructor of the key generation service.
         *
         * @param test           The simplicity test used.
         * @param minProbability Minimum probability of simplicity [0.5, 1).
         * @param primeBitLength is the bit length for p and q.
         */
        public KeyGenerationService(PrimalityTest test, double minProbability, int primeBitLength) {
            this.primalityTest = test.getStrategy();
            this.minProbability = minProbability;
            this.primeBitLength = primeBitLength;
            this.random = new SecureRandom();
        }

        public KeyPair generateKeys() {
            BigInteger p, q, N, phi, d;

            while (true) {
                p = generateProbablePrime();

                do {
                    q = generateProbablePrime();

                    // Farm attack is effective if |p-q| is small.
                    // We require that p and q differ by a significant amount.
                } while (p.equals(q) || p.subtract(q).abs().bitLength() < (primeBitLength / 2 - 100));

                N = p.multiply(q);

                // phi = (p-1) * (q-1)
                phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

                if (!phi.gcd(PUBLIC_EXPONENT).equals(BigInteger.ONE)) {
                    continue; // e is not suitable, we generate p and q again
                }

                d = NumberTheory.modInverse(PUBLIC_EXPONENT, phi);

                // Wiener's attack is effective if d is "too small",
                // specifically d < (1/3)*N^(1/4)
                // FIPS 186-4 requires d > 2^(nlen/2), where nlen = N.bitLength()
                // nlen~2*(bitLength/2), where bitLength is the bit length of p/q
                BigInteger minD = TWO.pow(N.bitLength() / 2);
                if (d.compareTo(minD) <= 0) {
                    continue;
                }

                return new KeyPair(N, PUBLIC_EXPONENT, d);
            }
        }

        protected BigInteger generateProbablePrime() {
            BigInteger p;
            do {
                p = new BigInteger(primeBitLength, random);

                if (p.bitLength() < primeBitLength) {
                    p = p.setBit(primeBitLength - 1);
                }

                if (!p.testBit(0)) {
                    p = p.setBit(0);
                }
            } while (!primalityTest.isProbablyPrime(p, minProbability));
            return p;
        }

        public record KeyPair(BigInteger N, BigInteger e, BigInteger d) {
        }
    }

    /**
     * Constructor of the main RSA service.
     *
     * @param test The simplicity test used.
     * @param minProbability Minimum probability of simplicity for p and q [0.5, 1).
     * @param primeBitLength is the bit length for generating p and q.
     */
    public RsaService(PrimalityTest test, double minProbability, int primeBitLength) {
        if (primeBitLength < 512) {
            throw new IllegalArgumentException("PrimeBitLength < 512 is not secure for RSA");
        }
        this.keyGenService = new KeyGenerationService(test, minProbability, primeBitLength);
        generateNewKeys();
    }

    public void generateNewKeys() {
        KeyGenerationService.KeyPair pair = this.keyGenService.generateKeys();
        this.N = pair.N();
        this.e = pair.e();
        this.d = pair.d();
    }


     // C = M^e mod N
    public BigInteger encrypt(BigInteger message) {
        if (message.compareTo(N) >= 0) {
            throw new IllegalArgumentException("Message must be less than N");
        }

        return NumberTheory.modPow(message, e, N);
    }


     // M = C^d mod N
    public BigInteger decrypt(BigInteger ciphertext) {
        return NumberTheory.modPow(ciphertext, d, N);
    }

    public BigInteger getN() {
        return N;
    }

    public BigInteger getE() {
        return e;
    }

    public BigInteger getD() {
        return d;
    }
}
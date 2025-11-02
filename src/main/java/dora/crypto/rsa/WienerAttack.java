package dora.crypto.rsa;

import dora.crypto.rsa.ProbabilisticTests.RsaService;

import java.math.BigInteger;

public class WienerAttack {
    private static final BigInteger ONE = BigInteger.ONE;

    public static class VulnerableKeyGenerationService extends RsaService.KeyGenerationService {

        // Standard public exponent
        private static final BigInteger PUBLIC_EXPONENT = new BigInteger("65537");

        public VulnerableKeyGenerationService(RsaService.PrimalityTest test, double minProbability, int primeBitLength) {
            super(test, minProbability, primeBitLength);
        }

        @Override
        public RsaService.KeyGenerationService.KeyPair generateKeys() {
            BigInteger p, q, N, phi, d, e;

            int vulnerableDBitLength = this.primeBitLength / 8;

            if (vulnerableDBitLength < 16) {
                vulnerableDBitLength = 16;
            }

            while (true) {
                p = generateProbablePrime();

                do {
                    q = generateProbablePrime();

                } while (p.equals(q) || p.subtract(q).abs().bitLength() < (primeBitLength / 2 - 100));

                N = p.multiply(q);
                phi = p.subtract(ONE).multiply(q.subtract(ONE));

                d = new BigInteger(vulnerableDBitLength, this.random);

                if (d.compareTo(BigInteger.TWO) <= 0 || !d.gcd(phi).equals(ONE)) {
                    continue;
                }

                BigInteger nSqrt = isqrt(N);
                assert nSqrt != null;
                BigInteger nFourthRoot = isqrt(nSqrt);
                assert nFourthRoot != null;
                BigInteger wienerLimit = nFourthRoot.divide(BigInteger.valueOf(3));

                if (d.compareTo(wienerLimit) >= 0) {
                    continue;
                }

                e = d.modInverse(phi);

                if (e.compareTo(PUBLIC_EXPONENT) <= 0) {
                    continue;
                }

                if (e.compareTo(N) >= 0) {
                    continue;
                }

                System.out.println("Keys:");
                System.out.println("    N: " + N);
                System.out.println("    d: " + d + " (Secret)");
                System.out.println("    e: " + e + " (Public)");
                System.out.println("    d < (N^(1/4)/3) (bit): " + wienerLimit.bitLength());

                return new KeyPair(N, e, d);
            }
        }

        /**
         * Newton-Raphson
         */
        private static BigInteger isqrt(BigInteger n) {

            if (n.signum() < 0) return null;
            if (n.equals(BigInteger.ZERO) || n.equals(ONE)) return n;

            BigInteger x0 = ONE.shiftLeft((n.bitLength() + 1) / 2);
            BigInteger x1 = n.divide(x0).add(x0).divide(BigInteger.TWO);

            while (x1.compareTo(x0) < 0) {
                x0 = x1;
                x1 = n.divide(x0).add(x0).divide(BigInteger.TWO);
            }
            return x0;
        }
    }
}

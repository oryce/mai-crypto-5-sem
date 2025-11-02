package dora.crypto.rsa.ProbabilisticTests;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public abstract class AbstractProbabilisticPrimality implements ProbabilisticPrimality {
    private static final Random RANDOM = new SecureRandom();
    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger THREE = BigInteger.valueOf(3);

    @Override
    public boolean isProbablyPrime(BigInteger n, double minProb) {
        if (minProb < 0.5 || minProb >= 1) {
            throw new IllegalArgumentException("minProb must be in [0.5, 1)");
        }

        if (n == null || n.signum() <= 0 || n.equals(ONE)) {
            return false;
        }
        if (n.equals(TWO) || n.equals(THREE)) {
            return true;
        }
        if (!n.testBit(0)) {
            return false;
        }

        // 1/2^k <= errorProb
        double errorProb = 1.0 - minProb;
        int k = (int) Math.ceil(Math.log(1.0 / errorProb) / Math.log(2.0));

        for (int i = 0; i < k; i++) {
            BigInteger a;
            do {
                a = new BigInteger(n.bitLength() - 1, RANDOM);
            } while (a.compareTo(TWO) < 0 || a.compareTo(n.subtract(TWO)) >= 0);
            if (!iteration(a, n)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Customized behavior of a single test iteration.
     * Must return true if witness a passes the test (n is probably a prime for this a),
     * false if a is a witness to the composite.
     *
     * @param a witness
     * @param n is the number being tested
     * @return true if it passes
     */
    protected abstract boolean iteration(BigInteger a, BigInteger n);
}
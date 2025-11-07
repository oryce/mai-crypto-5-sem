package dora.crypto.rsa.primality;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

abstract class AbstractPrimalityTest implements PrimalityTest {

    private static final BigInteger THREE = BigInteger.valueOf(3);

    private final Random random = new Random();

    @Override
    public boolean isProbablyPrime(@NotNull BigInteger n, double certainty) {
        Objects.requireNonNull(n, "number being tested");

        if (!(certainty >= 0.5 && certainty < 1))
            throw new IllegalArgumentException("minProb must be in [0.5; 1)");

        if (n.signum() <= 0 /* n <= 0 */ || n.equals(ONE)) return false;
        if (n.equals(TWO) || n.equals(THREE)) return true;
        if (!n.testBit(0) /* n % 2 == 0 */) return false;

        // Solve 1/(2^k) <= errorProb for k (`iterations`).
        double errorProb = 1.0 - certainty;
        int iterations = (int) Math.ceil(Math.log(1 / errorProb) / Math.log(2));

        for (int i = 0; i < iterations; i++) {
            // Pick a number in [2; n-2] and test it.
            BigInteger a = randomBigInteger(n.subtract(THREE)).add(TWO);
            if (!test(n, a)) return false;
        }

        return true;
    }

    private BigInteger randomBigInteger(BigInteger upperBound) {
        BigInteger result;

        do {
            result = new BigInteger(upperBound.bitLength(), random);
        } while (result.compareTo(upperBound) >= 0);

        return result;
    }

    /**
     * Checks whether <code>n</code> is probably prime according to witness <code>a</code>.
     *
     * @param n number being tested
     * @param a witness
     * @return <code>true</code>, if <code>n</code> is probably prime
     */
    protected abstract boolean test(BigInteger n, BigInteger a);
}
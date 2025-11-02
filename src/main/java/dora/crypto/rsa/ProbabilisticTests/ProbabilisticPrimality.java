package dora.crypto.rsa.ProbabilisticTests;

import java.math.BigInteger;

public interface ProbabilisticPrimality {
    /**
     * Checks whether n is probably prime with probability >= minProb.
     *
     * @param n test value (BigInteger > 1)
     * @param minProb minimum probability of prime in [0.5, 1)
     * @return true if n is likely prime, otherwise false
     */
    boolean isProbablyPrime(BigInteger n, double minProb);
}

package dora.crypto.rsa.primality;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public interface PrimalityTest {

    /**
     * Checks whether a {@link BigInteger} is probably prime with probability >= <code>certainty</code>.
     *
     * @param certainty probability in <code>[0.5; 1)</code>
     */
    boolean isProbablyPrime(@NotNull BigInteger n, double certainty);
}

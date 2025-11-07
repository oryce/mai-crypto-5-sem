package dora.crypto.rsa.primality;

import dora.crypto.rsa.RsaMath;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

public final class SolovayStrassenPrimalityTest extends AbstractPrimalityTest {

    private final RsaMath math = new RsaMath();

    @Override
    protected boolean test(BigInteger n, BigInteger a) {
        if (!math.gcd(a, n).equals(ONE)) return false;

        BigInteger left = math.modPow(a, n.subtract(ONE).divide(TWO), n);
        BigInteger right = math.jacobiSymbol(a, n).mod(n);

        return left.equals(right);
    }
}
package dora.crypto.rsa.primality;

import dora.crypto.rsa.RsaMath;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;

public final class FermatPrimalityTest extends AbstractPrimalityTest {

    private final RsaMath math = new RsaMath();

    @Override
    protected boolean test(BigInteger n, BigInteger a) {
        if (!math.gcd(n, a).equals(ONE)) return false;
        return math.modPow(a, n.subtract(ONE), n).equals(ONE);
    }
}
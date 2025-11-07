package dora.crypto.rsa.primality;

import dora.crypto.rsa.RsaMath;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

public final class MillerRabinPrimalityTest extends AbstractPrimalityTest {

    private final RsaMath math = new RsaMath();

    @Override
    protected boolean test(BigInteger n, BigInteger a) {
        if (!math.gcd(n, a).equals(ONE)) return false;

        /* n-1 = 2^s * d */
        int s = 0;
        BigInteger d = n.subtract(ONE);

        while (!d.testBit(0) /* n % 2 == 0 */) {
            s++;
            d = d.shiftRight(1) /* n /= 2 */;
        }

        assert s > 0 : "invariant";
        assert d.signum() > 0 : "invariant";

        BigInteger x = math.modPow(a, d, n);
        BigInteger y = null;

        for (int i = 0; i < s; i++) {
            y = math.modPow(x, TWO, n);

            if (y.equals(ONE) && !x.equals(ONE) && !x.equals(n.subtract(ONE))) {
                return false;
            }

            x = y;
        }

        return y.equals(ONE);
    }
}
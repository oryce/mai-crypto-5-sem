package dora.crypto.rsa;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;

public final class RsaMath {

    private static final BigInteger NEGATIVE_ONE = BigInteger.valueOf(-1);
    private static final BigInteger THREE = BigInteger.valueOf(3);
    private static final BigInteger FOUR = BigInteger.valueOf(4);
    private static final BigInteger FIVE = BigInteger.valueOf(5);
    private static final BigInteger SIX = BigInteger.valueOf(6);

    /**
     * Computes the greatest common divisor of two {@link BigInteger}s.
     */
    public BigInteger gcd(BigInteger a, BigInteger b) {
        a = a.abs();
        b = b.abs();

        while (!b.equals(ZERO)) {
            BigInteger temp = b;
            b = a.mod(b);
            a = temp;
        }

        return a;
    }

    /**
     * Computes the Extended Euclidean Algorithm of two {@link BigInteger}s.
     */
    public BigInteger[] eGcd(BigInteger a, BigInteger b) {
        BigInteger r0 = a.abs(), r = b.abs();
        BigInteger s0 = ONE, s = ZERO;
        BigInteger t0 = ZERO, t = ONE;

        while (!r.equals(ZERO)) {
            BigInteger quot = r0.divide(r);
            BigInteger temp;

            // (r0, r) = (r, r0 - quot * r)
            temp = r0;
            r0 = r;
            r = temp.subtract(quot.multiply(r));

            // (s0, s) = (s, s0 - quot * s)
            temp = s0;
            s0 = s;
            s = temp.subtract(quot.multiply(s));

            // (t0, t) = (t, t0 - quot * t)
            temp = t0;
            t0 = t;
            t = temp.subtract(quot.multiply(t));
        }

        if (a.signum() < 0) s0 = s0.negate();
        if (b.signum() < 0) t0 = t0.negate();

        return new BigInteger[] { r0, s0, t0 };
    }

    /**
     * Raises a {@link BigInteger} to the power of <code>exp</code> modulo <code>mod</code>.
     * <p>
     * Modulus must be positive.
     */
    public BigInteger modPow(BigInteger x, BigInteger exp, BigInteger mod) {
        if (mod.signum() < 0)
            throw new ArithmeticException("modulus must be positive");
        if (exp.signum() < 0)
            return modPow(modInverse(x, mod), exp.negate(), mod);

        x = x.mod(mod);

        // Negative exponentiation equals 1, except for 0^0 (mod 1).
        if (exp.equals(ZERO)) {
            return x.equals(ZERO) && mod.equals(ONE) ? ZERO : ONE;
        }

        BigInteger result = ONE;

        while (exp.signum() > 0) {
            if (exp.testBit(0)) {
                result = result.multiply(x).mod(mod);
            }

            x = x.multiply(x).mod(mod);
            exp = exp.shiftRight(1);
        }

        return result;
    }

    /**
     * Computes the multiplicative inverse of a {@link BigInteger} modulo <code>mod</code>.
     * <p>
     * Modulus must be positive.
     */
    public BigInteger modInverse(BigInteger x, BigInteger mod) {
        if (mod.signum() <= 0)
            throw new ArithmeticException("modulus must be positive");

        BigInteger[] eGcd = eGcd(x.mod(mod), mod);

        if (!eGcd[0].equals(ONE))
            throw new ArithmeticException("multiplicative inverse does not exist");

        return eGcd[1].mod(mod);
    }

    /**
     * Computes the Jacobi symbol for integer <code>a</code> and positive odd integer <code>n</code>.
     */
    public BigInteger jacobiSymbol(BigInteger a, BigInteger n) {
        /* https://en.wikipedia.org/wiki/Jacobi_symbol#Implementation_in_C++ */

        if (n.signum() <= 0 || !n.testBit(0))
            throw new ArithmeticException("n must be a positive odd integer");

        // Step 1.
        a = a.mod(n);

        // Step 3.
        BigInteger t = ZERO;

        while (!a.equals(ZERO)) {
            BigInteger[] divMod;

            // Step 2.
            while ((divMod = a.divideAndRemainder(FOUR))[1].equals(ZERO) /* a % 4 == 0 */) {
                a = divMod[0] /* a /= 4 */;
            }

            divMod = a.divideAndRemainder(TWO);
            if (divMod[1].equals(ZERO) /* a % 2 == 0 */) {
                t = t.xor(n);
                a = divMod[0] /* a /= 2 */;
            }

            // Step 4.
            t = t.xor(a.and(n).and(TWO));
            BigInteger r = n.remainder(a);
            n = a;
            a = r;
        }

        if (!n.equals(ONE))
            return ZERO;
// @formatter:off
        else if (!(t.xor(t.shiftRight(1)).and(TWO)).equals(ZERO))
                /* ((t ^ (t >> 1)) & 2) != 0 */
// @formatter:on
            return NEGATIVE_ONE;
        else
            return ONE;
    }

    /**
     * Computes the Legendre symbol for <code>a</code> and <code>p</code>, where <code>p</code>
     * is an odd prime number.
     */
    public BigInteger legendreSymbol(BigInteger a, BigInteger p) {
        if (p.equals(TWO) || !isPrime(p))
            throw new ArithmeticException("p must be an odd prime number");

        a = a.mod(p);

        if (a.equals(ZERO))
            return ZERO;

        BigInteger r = modPow(a, p.subtract(ONE).shiftRight(1), p);
        return r.equals(ONE) ? ONE : NEGATIVE_ONE;
    }

    /**
     * Returns whether a {@link BigInteger} is a prime number.
     */
    private static boolean isPrime(BigInteger n) {
        if (n.compareTo(TWO) < 0) return false;
        if (n.equals(TWO) || n.equals(THREE)) return true;

        if (n.remainder(TWO).equals(ZERO) ||
            n.remainder(THREE).equals(ZERO))
            return false;

        for (BigInteger k = FIVE; k.multiply(k).compareTo(n) <= 0 /* k * k <= n */
            ; k = k.add(SIX)
        ) {
            if (n.remainder(k).equals(ZERO) /* n % k == 0 */ ||
                n.remainder(k.add(TWO)).equals(ZERO) /* n % (k + 2) == 0 */)
                return false;
        }

        return true;
    }
}

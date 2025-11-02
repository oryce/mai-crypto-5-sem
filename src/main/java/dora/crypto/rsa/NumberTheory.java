package dora.crypto.rsa;

import java.math.BigInteger;

public class NumberTheory {

    public static BigInteger gcd(BigInteger a, BigInteger b) {
        a = a.abs();
        b = b.abs();
        while (b.compareTo(BigInteger.ZERO) != 0) {
            BigInteger temp = b;
            b = a.mod(b);
            a = temp;
        }
        return a;
    }

    public static BigInteger[] extendedGcd(BigInteger a, BigInteger b) {
        a = a.abs();
        b = b.abs();
        if (b.equals(BigInteger.ZERO)) {
            return new BigInteger[]{a, BigInteger.ONE, BigInteger.ZERO};
        }
        BigInteger[] previous = extendedGcd(b, a.mod(b));
        BigInteger x = previous[2];
        BigInteger y = previous[1].subtract(a.divide(b).multiply(previous[2]));
        return new BigInteger[]{previous[0], x, y};
    }

    public static BigInteger modPow(BigInteger a, BigInteger exponent, BigInteger modulus) {
        if (modulus.equals(BigInteger.ZERO)) {
            throw new IllegalArgumentException("Modulus cannot be zero");
        }
        a = a.mod(modulus).abs();
        BigInteger result = BigInteger.ONE;
        while (exponent.compareTo(BigInteger.ZERO) > 0) {
            if (exponent.mod(BigInteger.valueOf(2L)).equals(BigInteger.ONE)) {
                result = result.multiply(a).mod(modulus);
            }
            a = a.multiply(a).mod(modulus);
            exponent = exponent.divide(BigInteger.valueOf(2L));
        }
        return result;
    }

    public static BigInteger modInverse(BigInteger a, BigInteger m) {
        if (m.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Modulus must not be zero");
        }

        BigInteger t = BigInteger.ZERO, newT = BigInteger.ONE;
        BigInteger r = m, newR = a.abs().mod(m);

        while (!newR.equals(BigInteger.ZERO)) {
            BigInteger quotient = r.divide(newR);
            BigInteger temp = newT;
            newT = t.subtract(quotient.multiply(newT));
            t = temp;

            temp = newR;
            newR = r.subtract(quotient.multiply(temp));
            r = temp;
        }

        if (!r.equals(BigInteger.ONE)) {
            throw new ArithmeticException("Inverse does not exist: gcd(a, m) != 1");
        }

        if (t.signum() < 0) {
            t = t.add(m);
        }
        return t;
    }

    public static BigInteger jacobiSymbol(BigInteger a, BigInteger n) {
        if (n.compareTo(BigInteger.ZERO) <= 0 || n.mod(BigInteger.TWO).equals(BigInteger.ZERO) || n.equals(BigInteger.ONE)) {
            throw new IllegalArgumentException("n must be positive odd integer > 1");
        }

        if (!a.gcd(n).equals(BigInteger.ONE)) {
            return BigInteger.ZERO;
        }

        BigInteger r = BigInteger.ONE;

        if (a.compareTo(BigInteger.ZERO) < 0) {
            a = a.negate();
            if (n.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                r = r.negate();
            }
        }

        while (!a.equals(BigInteger.ZERO)) {
            int t = 0;
            while (a.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                t++;
                a = a.divide(BigInteger.TWO);
            }

            if (t % 2 != 0) {
                BigInteger bMod8 = n.mod(BigInteger.valueOf(8));
                if (bMod8.equals(BigInteger.valueOf(3)) || bMod8.equals(BigInteger.valueOf(5))) {
                    r = r.negate();
                }
            }

            if (a.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3)) &&
                    n.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                r = r.negate();
            }

            BigInteger temp = a;
            a = n.mod(temp);
            n = temp;
        }

        return r;
    }

    public static BigInteger legendreSymbol(BigInteger a, BigInteger p) {
        if (p.compareTo(BigInteger.ZERO) <= 0 || !isPrime(p)) {
            throw new IllegalArgumentException("p must be a positive prime");
        }

        if (p.equals(BigInteger.TWO)) {
            a = a.mod(p);
            if (a.equals(BigInteger.ZERO)) {
                return BigInteger.ZERO;
            }
            BigInteger aMod8 = a.mod(BigInteger.valueOf(8));
            if (aMod8.equals(BigInteger.ONE) || aMod8.equals(BigInteger.valueOf(7))) {
                return BigInteger.ONE;
            } else {
                return BigInteger.valueOf(-1);
            }
        }

        return jacobiSymbol(a, p);
    }

    public static boolean isPrime(BigInteger n) {
        if (n.compareTo(BigInteger.valueOf(2L)) < 0) return false;
        if (n.equals(BigInteger.valueOf(2L)) || n.equals(BigInteger.valueOf(3L))) return true;
        if (n.mod(BigInteger.valueOf(2L)).equals(BigInteger.ZERO) || n.mod(BigInteger.valueOf(3L)).equals(BigInteger.ZERO)) return false;
        for (BigInteger i = BigInteger.valueOf(5L); i.multiply(i).compareTo(n) <= 0; i = i.add(BigInteger.valueOf(6L))) {
            if (n.mod(i).equals(BigInteger.ZERO) || n.mod(i.add(BigInteger.valueOf(2L))).equals(BigInteger.ZERO)) return false;
        }
        return true;
    }
}

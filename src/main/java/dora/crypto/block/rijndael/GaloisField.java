package dora.crypto.block.rijndael;

import java.util.*;
import java.util.stream.LongStream;

public final class GaloisField {

    /**
     * Returns the degree of a polynomial in GF(2^8).
     */
    public int degree(byte f) {
        return degree(Byte.toUnsignedLong(f));
    }

    /**
     * Adds two polynomials in GF(2^8).
     */
    public byte add(byte a, byte b) {
        return (byte) add(
            Byte.toUnsignedLong(a),
            Byte.toUnsignedLong(b)
        );
    }

    /**
     * Multiplies two polynomials in GF(2^8) modulo <code>mod</code>.
     */
    public byte mulMod(byte a, byte b, short mod) {
        if (!irreducible(mod))
            throw new IllegalArgumentException("Modulus may not be reducible");

        return (byte) mulMod(
            Byte.toUnsignedLong(a),
            Byte.toUnsignedLong(b),
            Short.toUnsignedLong(mod)
        );
    }

    /**
     * Returns the multiplicative inverse of a polynomial in GF(2^8) modulo <code>mod</code>.
     */
    public byte inv(byte f, short mod) {
        if (!irreducible(mod))
            throw new IllegalArgumentException("Modulus may not be reducible");

        return (byte) inv(
            Byte.toUnsignedLong(f),
            Short.toUnsignedLong(mod)
        );
    }

    /**
     * Returns whether a degree-8 polynomial is irreducible in GF(2^8).
     */
    public boolean irreducible(short f) {
        return irreducible(Short.toUnsignedLong(f));
    }

    /**
     * Returns a collection of irreducible degree-8 polynomials in GF(2^8).
     */
    public Collection<Short> irreducibles() {
        return irreducibles(8).stream()
            .map(Long::shortValue)
            .toList();
    }

    /**
     * Factorizes a polynomial in GF(2^N) into irreducible factors.
     */
    public Collection<Long> factorize(long f) {
        if (f == 0) throw new IllegalArgumentException("Cannot factor zero");
        if (f == 1) return Collections.emptyList();

        List<Long> factors = new ArrayList<>();
        int maxDegree = degree(f) / 2;

        for (int d = 1; d <= maxDegree; d++) {
            for (long p : irreducibles(d)) {
                DivMod divMod;
                while ((divMod = divMod(f, p)).remainder() == 0) {
                    factors.add(p);
                    f = divMod.quotient();
                }
            }
        }

        if (f > 1) {
            factors.add(f);
        }

        return factors;
    }

    //region Implementation
    /**
     * Returns the degree of a polynomial in GF(2^N).
     */
    private int degree(long f) {
        return (Long.SIZE - 1) - Long.numberOfLeadingZeros(f);
    }

    /**
     * Adds two polynomials in GF(2^N).
     */
    private long add(long a, long b) {
        return a ^ b;
    }

    /**
     * Multiplies two polynomials in GF(2^N).
     */
    private long mul(long a, long b) {
        long p = 0;

        for (int i = 0; i < Long.SIZE; i++) {
            if ((b & 1) == 1) p ^= a;
            b = b >>> 1;
            a = a << 1;
        }

        return p;
    }

    /**
     * Multiplies two polynomials in GF(2^N) modulo <code>mod</code>.
     */
    private long mulMod(long a, long b, long mod) {
        return divMod(mul(a, b), mod).remainder();
    }

    /**
     * Divides two polynomials in GF(2^N).
     */
    private DivMod divMod(long a, long b) {
        long q = 0, r = a;

        while (degree(r) >= degree(b)) {
            int lead = degree(r) - degree(b);
            q ^= 1L << lead;
            r ^= b << lead;
        }

        return new DivMod(q, r);
    }

    private record DivMod(long quotient, long remainder) {}

    /**
     * Computes the extended GCD of two polynomials in GF(2^N).
     */
    private EGcd eGcd(long a, long b) {
        long r0 = a, r = b;
        long s0 = 1, s = 0;
        long t0 = 0, t = 1;

        while (r != 0) {
            long quotient = divMod(r0, r).quotient();
            long temp;

            // (r0, r) = (r, r0 - quot * r)
            temp = r0;
            r0 = r;
            r = temp ^ mul(quotient, r);

            // (s0, s) = (s, s0 - quot * s)
            temp = s0;
            s0 = s;
            s = temp ^ mul(quotient, s);

            // (t0, t) = (t0, t - quot * t)
            temp = t0;
            t0 = t;
            t = temp ^ mul(quotient, t);
        }

        return new EGcd(r0, s0, t0);
    }

    private record EGcd(long gcd, long a, long b) {}

    /**
     * Returns the multiplicative inverse of a polynomial in GF(2^N) modulo <code>mod</code>.
     */
    private long inv(long f, long mod) {
        EGcd result = eGcd(f, mod);
        if (result.gcd() != 1)
            throw new IllegalArgumentException("Inverse element does not exist");
        return result.a();
    }

    /**
     * Returns whether a polynomial is irreducible in GF(2^N) using Rabin's test of irreducibility.
     */
    private boolean irreducible(long f) {
        int n = degree(f);
        if (n <= 0) return false;
        if (n == 1) return true;

        /* x is a degree-1 polynomial */
        final int x = 0b10;

        // For each distinct prime factor of n, check that the following holds:
        //   h := x^(q^(n/p)) - x; g := gcd(f, h); g != 0
        int k = n;

        for (int p = 2; p * p <= k; p++) {
            if (k % p != 0) continue;

            long h = pow2Mod(x, n / p, f) ^ x;
            long g = eGcd(f, h).gcd();
            if (g != 1) return false;

            while (k % p == 0) k /= p;
        }

        if (k > 1) {
            long h = pow2Mod(x, n / k, f) ^ x;
            long g = eGcd(f, h).gcd();
            if (g != 1) return false;
        }

        // At last, check that x^(q^n) == x.
        return pow2Mod(x, n, f) == x;
    }

    /**
     * Computes f^(2^exp) modulo <code>mod</code> in GF(2^N).
     */
    private long pow2Mod(long f, int exp, long mod) {
        long result = f;

        for (int i = 0; i < exp; i++) {
            result = mulMod(result, result, mod);
        }

        return result;
    }

    /**
     * Returns a collection of irreducible polynomials of some degree in GF(2^N).
     */
    private Collection<Long> irreducibles(int degree) {
        return LongStream.range(1L << degree, 1L << (degree + 1))
            .filter(this::irreducible)
            .boxed()
            .toList();
    }
    //endregion
}
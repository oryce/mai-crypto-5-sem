package dora.crypto.rsa.ProbabilisticTests.Tests;

import dora.crypto.rsa.NumberTheory;
import dora.crypto.rsa.ProbabilisticTests.AbstractProbabilisticPrimality;

import java.math.BigInteger;

public class MillerRabinPrimality extends AbstractProbabilisticPrimality {
    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger TWO = BigInteger.valueOf(2);

    @Override
    protected boolean iteration(BigInteger a, BigInteger n) {
        BigInteger d = NumberTheory.gcd(a, n);
        if (!d.equals(ONE)) {
            return false;
        }

        // n-1 = 2^s * d
        BigInteger nm1 = n.subtract(ONE);
        int s = 0;
        BigInteger oddD = nm1;
        while (!oddD.testBit(0)) {
            oddD = oddD.shiftRight(1);
            s++;
        }

        // a^d mod n
        BigInteger x = NumberTheory.modPow(a, oddD, n);
        if (x.equals(ONE) || x.equals(n.subtract(ONE))) {
            return true;
        }

        for (int r = 1; r < s; r++) {
            x = NumberTheory.modPow(x, TWO, n); // x = x^2 mod n
            if (x.equals(n.subtract(ONE))) {
                return true;
            }
        }
        return false;
    }
}
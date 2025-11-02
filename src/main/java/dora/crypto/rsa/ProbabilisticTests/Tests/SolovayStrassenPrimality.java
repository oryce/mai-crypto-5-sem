package dora.crypto.rsa.ProbabilisticTests.Tests;

import dora.crypto.rsa.NumberTheory;
import dora.crypto.rsa.ProbabilisticTests.AbstractProbabilisticPrimality;

import java.math.BigInteger;

public class SolovayStrassenPrimality extends AbstractProbabilisticPrimality {
    private static final BigInteger ONE = BigInteger.ONE;

    @Override
    protected boolean iteration(BigInteger a, BigInteger n) {

        BigInteger d = NumberTheory.gcd(a, n);
        if (!d.equals(ONE)) {
            return false;
        }

        BigInteger exp = n.subtract(ONE).divide(BigInteger.valueOf(2));
        BigInteger left = NumberTheory.modPow(a, exp, n);

        BigInteger right = NumberTheory.jacobiSymbol(a, n);

        if (right.equals(BigInteger.valueOf(-1))) {
            right = n.subtract(ONE);
        }
        return left.equals(right);
    }
}
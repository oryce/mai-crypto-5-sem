package dora.crypto.rsa.ProbabilisticTests.Tests;

import dora.crypto.rsa.NumberTheory;
import dora.crypto.rsa.ProbabilisticTests.AbstractProbabilisticPrimality;

import java.math.BigInteger;

public class FermatPrimality extends AbstractProbabilisticPrimality {
    private static final BigInteger ONE = BigInteger.ONE;

    @Override
    protected boolean iteration(BigInteger a, BigInteger n) {
        BigInteger exp = n.subtract(ONE);
        BigInteger res = NumberTheory.modPow(a, exp, n);
        return res.equals(ONE);
    }
}
package dora.crypto.rsa;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.Positive;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RsaMathTest {

    private final RsaMath rsaMath;

    RsaMathTest() {
        rsaMath = new RsaMath();
    }

    @Property
    public void gcdIsCorrect(@ForAll BigInteger a, @ForAll BigInteger b) {
        assertThat(rsaMath.gcd(a, b)).isEqualTo(a.gcd(b));
    }

    @Property
    public void eGcdIsCorrect(@ForAll BigInteger a, @ForAll BigInteger b) {
        assertThat(rsaMath.eGcd(a, b)[0]).isEqualTo(a.gcd(b));
    }

    @Property
    public void eGcdSolutionsAreCorrect(@ForAll BigInteger a, @ForAll BigInteger b) {
        BigInteger[] eGcd = rsaMath.eGcd(a, b);
        BigInteger gcd = eGcd[0], x = eGcd[1], y = eGcd[2];

        assertThat(a.multiply(x).add(b.multiply(y))).isEqualTo(gcd);
    }

    @Property
    public void modPowIsCorrect(
        @ForAll BigInteger x,
        @ForAll BigInteger exp,
        @ForAll @Positive BigInteger mod
    ) {
        if (exp.signum() < 0) {
            // `x` must be invertible modulo `mod`.
            Assume.that(x.gcd(mod).equals(BigInteger.ONE));
        }

        assertThat(rsaMath.modPow(x, exp, mod))
            .isEqualTo(x.modPow(exp, mod));
    }

    @Property
    public void modInverseIsCorrect(@ForAll BigInteger x, @ForAll @Positive BigInteger mod) {
        boolean invertible = x.gcd(mod).equals(BigInteger.ONE);

        if (invertible) {
            assertThat(rsaMath.modInverse(x, mod))
                .isEqualTo(x.modInverse(mod));
        } else {
            assertThatThrownBy(() -> rsaMath.modInverse(x, mod))
                .isInstanceOf(ArithmeticException.class);
        }
    }

    //region Jacobi symbol tests
    @Property
    public void jacobiPassesTestCases(@ForAll("jacobiTestCases") JacobiTestCase testCase) {
        assertThat(rsaMath.jacobiSymbol(
            BigInteger.valueOf(testCase.a()),
            BigInteger.valueOf(testCase.n())
        ))
            .describedAs("jacobi(%d, %d) = %d", testCase.a(), testCase.n(), testCase.expected())
            .isEqualTo(testCase.expected());
    }

    public record JacobiTestCase(int a, int n, int expected) {}

    /* https://en.wikipedia.org/wiki/Jacobi_symbol#Table_of_values */

    private static final int[] JACOBI_A = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30
    };

    private static final int[] JACOBI_N = {
        1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29,
        31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59
    };

    private static final int[][] JACOBI_VALUES = new int[][] {
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        { 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0 },
        { 1, -1, -1, 1, 0, 1, -1, -1, 1, 0, 1, -1, -1, 1, 0, 1, -1, -1, 1, 0, 1, -1, -1, 1, 0, 1, -1, -1, 1, 0 },
        { 1, 1, -1, 1, -1, -1, 0, 1, 1, -1, 1, -1, -1, 0, 1, 1, -1, 1, -1, -1, 0, 1, 1, -1, 1, -1, -1, 0, 1, 1 },
        { 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0 },
        { 1, -1, 1, 1, 1, -1, -1, -1, 1, -1, 0, 1, -1, 1, 1, 1, -1, -1, -1, 1, -1, 0, 1, -1, 1, 1, 1, -1, -1, -1 },
        { 1, -1, 1, 1, -1, -1, -1, -1, 1, 1, -1, 1, 0, 1, -1, 1, 1, -1, -1, -1, -1, 1, 1, -1, 1, 0, 1, -1, 1, 1 },
        { 1, 1, 0, 1, 0, 0, -1, 1, 0, 0, -1, 0, -1, -1, 0, 1, 1, 0, 1, 0, 0, -1, 1, 0, 0, -1, 0, -1, -1, 0 },
        { 1, 1, -1, 1, -1, -1, -1, 1, 1, -1, -1, -1, 1, -1, 1, 1, 0, 1, 1, -1, 1, -1, -1, -1, 1, 1, -1, -1, -1, 1 },
        { 1, -1, -1, 1, 1, 1, 1, -1, 1, -1, 1, -1, -1, -1, -1, 1, 1, -1, 0, 1, -1, -1, 1, 1, 1, 1, -1, 1, -1, 1 },
        { 1, -1, 0, 1, 1, 0, 0, -1, 0, -1, -1, 0, -1, 0, 0, 1, 1, 0, -1, 1, 0, 1, -1, 0, 1, 1, 0, 0, -1, 0 },
        { 1, 1, 1, 1, -1, 1, -1, 1, 1, -1, -1, 1, 1, -1, -1, 1, -1, 1, -1, -1, -1, -1, 0, 1, 1, 1, 1, -1, 1, -1 },
        { 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0 },
        { 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0 },
        { 1, -1, -1, 1, 1, 1, 1, -1, 1, -1, -1, -1, 1, -1, -1, 1, -1, -1, -1, 1, -1, 1, 1, 1, 1, -1, -1, 1, 0, 1 },
        { 1, 1, -1, 1, 1, -1, 1, 1, 1, 1, -1, -1, -1, 1, -1, 1, -1, 1, 1, 1, -1, -1, -1, -1, 1, -1, -1, 1, -1, -1 },
        { 1, 1, 0, 1, -1, 0, -1, 1, 0, -1, 0, 0, -1, -1, 0, 1, 1, 0, -1, -1, 0, 0, -1, 0, 1, -1, 0, -1, 1, 0 },
        { 1, -1, 1, 1, 0, -1, 0, -1, 1, 0, 1, 1, 1, 0, 0, 1, 1, -1, -1, 0, 0, -1, -1, -1, 0, -1, 1, 0, 1, 0 },
        { 1, -1, 1, 1, -1, -1, 1, -1, 1, 1, 1, 1, -1, -1, -1, 1, -1, -1, -1, -1, 1, -1, -1, -1, 1, 1, 1, 1, -1, 1 },
        { 1, 1, 0, 1, 1, 0, -1, 1, 0, 1, 1, 0, 0, -1, 0, 1, -1, 0, -1, 1, 0, 1, -1, 0, 1, 0, 0, -1, -1, 0 },
        { 1, 1, -1, 1, 1, -1, -1, 1, 1, 1, -1, -1, -1, -1, -1, 1, -1, 1, -1, 1, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1 },
        { 1, -1, -1, 1, -1, 1, -1, -1, 1, 1, 1, -1, 1, 1, 1, 1, 1, -1, -1, -1, 1, -1, 1, 1, 1, -1, -1, -1, -1, -1 },
        { 1, -1, 0, 1, 0, 0, -1, -1, 0, 0, 1, 0, -1, 1, 0, 1, -1, 0, 1, 0, 0, -1, -1, 0, 0, 1, 0, -1, 1, 0 },
        { 1, 1, 1, 1, -1, 1, 1, 1, 1, -1, -1, 1, -1, 1, -1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1, -1, 1, 1, -1, -1 },
        { 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1 },
        { 1, -1, 0, 1, 1, 0, -1, -1, 0, -1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, -1, 1, 0, 1, -1, 0, -1, 1, 0 },
        { 1, -1, -1, 1, -1, 1, 1, -1, 1, 1, 1, -1, 1, -1, 1, 1, 1, -1, -1, -1, -1, -1, -1, 1, 1, -1, -1, 1, 1, -1 },
        { 1, 1, -1, 1, 0, -1, 1, 1, 1, 0, 0, -1, 1, 1, 0, 1, 1, 1, -1, 0, -1, 0, -1, -1, 0, 1, -1, 1, -1, 0 },
        { 1, 1, 0, 1, -1, 0, 1, 1, 0, -1, -1, 0, -1, 1, 0, 1, -1, 0, 0, -1, 0, -1, -1, 0, 1, -1, 0, 1, 1, 0 },
        { 1, -1, 1, 1, 1, -1, 1, -1, 1, -1, -1, 1, -1, -1, 1, 1, 1, -1, 1, 1, 1, 1, -1, -1, 1, 1, 1, 1, 1, -1 }
    };

    @Provide("jacobiTestCases")
    public Arbitrary<JacobiTestCase> jacobiTestCases() {
        List<JacobiTestCase> cases = new ArrayList<>();

        for (int i = 0; i < JACOBI_N.length; i++) {
            for (int j = 0; j < JACOBI_A.length; j++) {
                cases.add(new JacobiTestCase(
                    JACOBI_A[j],
                    JACOBI_N[i],
                    JACOBI_VALUES[i][j]
                ));
            }
        }

        return Arbitraries.of(cases);
    }
    //endregion

    //region Legendre symbol tests
    @Property
    public void legendrePassesTestCases(@ForAll("legendreTestCases") LegendreTestCase testCase) {
        assertThat(rsaMath.legendreSymbol(
            BigInteger.valueOf(testCase.a()),
            BigInteger.valueOf(testCase.p())
        ))
            .describedAs("legendre(%d, %d) = %d", testCase.a(), testCase.p(), testCase.expected())
            .isEqualTo(testCase.expected());
    }

    public record LegendreTestCase(int a, int p, int expected) {
    }

    /* https://en.wikipedia.org/wiki/Legendre_symbol#Table_of_values */

    private static final int[] LEGENDRE_A = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30
    };

    private static final int[] LEGENDRE_P = {
        3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53,
        59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127
    };

    private static final int[][] LEGENDRE_VALUES = new int[][] {
        { 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0 },
        { 1, -1, -1, 1, 0, 1, -1, -1, 1, 0, 1, -1, -1, 1, 0, 1, -1, -1, 1, 0, 1, -1, -1, 1, 0, 1, -1, -1, 1, 0 },
        { 1, 1, -1, 1, -1, -1, 0, 1, 1, -1, 1, -1, -1, 0, 1, 1, -1, 1, -1, -1, 0, 1, 1, -1, 1, -1, -1, 0, 1, 1 },
        { 1, -1, 1, 1, 1, -1, -1, -1, 1, -1, 0, 1, -1, 1, 1, 1, -1, -1, -1, 1, -1, 0, 1, -1, 1, 1, 1, -1, -1, -1 },
        { 1, -1, 1, 1, -1, -1, -1, -1, 1, 1, -1, 1, 0, 1, -1, 1, 1, -1, -1, -1, -1, 1, 1, -1, 1, 0, 1, -1, 1, 1 },
        { 1, 1, -1, 1, -1, -1, -1, 1, 1, -1, -1, -1, 1, -1, 1, 1, 0, 1, 1, -1, 1, -1, -1, -1, 1, 1, -1, -1, -1, 1 },
        { 1, -1, -1, 1, 1, 1, 1, -1, 1, -1, 1, -1, -1, -1, -1, 1, 1, -1, 0, 1, -1, -1, 1, 1, 1, 1, -1, 1, -1, 1 },
        { 1, 1, 1, 1, -1, 1, -1, 1, 1, -1, -1, 1, 1, -1, -1, 1, -1, 1, -1, -1, -1, -1, 0, 1, 1, 1, 1, -1, 1, -1 },
        { 1, -1, -1, 1, 1, 1, 1, -1, 1, -1, -1, -1, 1, -1, -1, 1, -1, -1, -1, 1, -1, 1, 1, 1, 1, -1, -1, 1, 0, 1 },
        { 1, 1, -1, 1, 1, -1, 1, 1, 1, 1, -1, -1, -1, 1, -1, 1, -1, 1, 1, 1, -1, -1, -1, -1, 1, -1, -1, 1, -1, -1 },
        { 1, -1, 1, 1, -1, -1, 1, -1, 1, 1, 1, 1, -1, -1, -1, 1, -1, -1, -1, -1, 1, -1, -1, -1, 1, 1, 1, 1, -1, 1 },
        { 1, 1, -1, 1, 1, -1, -1, 1, 1, 1, -1, -1, -1, -1, -1, 1, -1, 1, -1, 1, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1 },
        { 1, -1, -1, 1, -1, 1, -1, -1, 1, 1, 1, -1, 1, 1, 1, 1, 1, -1, -1, -1, 1, -1, 1, 1, 1, -1, -1, -1, -1, -1 },
        { 1, 1, 1, 1, -1, 1, 1, 1, 1, -1, -1, 1, -1, 1, -1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1, -1, 1, 1, -1, -1 },
        { 1, -1, -1, 1, -1, 1, 1, -1, 1, 1, 1, -1, 1, -1, 1, 1, 1, -1, -1, -1, -1, -1, -1, 1, 1, -1, -1, 1, 1, -1 },
        { 1, -1, 1, 1, 1, -1, 1, -1, 1, -1, -1, 1, -1, -1, 1, 1, 1, -1, 1, 1, 1, 1, -1, -1, 1, 1, 1, 1, 1, -1 },
        { 1, -1, 1, 1, 1, -1, -1, -1, 1, -1, -1, 1, 1, 1, 1, 1, -1, -1, 1, 1, -1, 1, -1, -1, 1, -1, 1, -1, -1, -1 },
        { 1, -1, -1, 1, -1, 1, -1, -1, 1, 1, -1, -1, -1, 1, 1, 1, 1, -1, 1, -1, 1, 1, 1, 1, 1, 1, -1, -1, 1, -1 },
        { 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, -1, 1, -1, -1, 1, 1, -1, 1, 1, 1, -1, -1, -1, 1, 1, -1, 1, -1, 1, 1 },
        { 1, 1, 1, 1, -1, 1, -1, 1, 1, -1, -1, 1, -1, -1, -1, 1, -1, 1, 1, -1, -1, -1, 1, 1, 1, -1, 1, -1, -1, -1 },
        { 1, 1, -1, 1, 1, -1, -1, 1, 1, 1, 1, -1, 1, -1, -1, 1, -1, 1, 1, 1, 1, 1, 1, -1, 1, 1, -1, -1, -1, -1 },
        { 1, -1, 1, 1, -1, -1, 1, -1, 1, 1, 1, 1, -1, -1, -1, 1, 1, -1, -1, -1, 1, -1, 1, -1, 1, 1, 1, 1, 1, 1 },
        { 1, 1, -1, 1, 1, -1, -1, 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, 1, -1, 1, 1, 1, -1, -1, 1, -1, -1, -1, -1, -1 },
        { 1, 1, 1, 1, -1, 1, -1, 1, 1, -1, 1, 1, -1, -1, -1, 1, -1, 1, -1, -1, -1, 1, -1, 1, 1, -1, 1, -1, -1, -1 },
        { 1, -1, -1, 1, 1, 1, -1, -1, 1, -1, -1, -1, 1, 1, -1, 1, 1, -1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1 },
        { 1, 1, -1, 1, -1, -1, 1, 1, 1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, 1, -1, 1, 1, -1, 1, 1, 1 },
        { 1, -1, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, -1, 1, -1, -1, 1, -1, -1, -1, 1, -1, 1, -1, 1, -1, 1, 1 },
        { 1, -1, 1, 1, 1, -1, 1, -1, 1, -1, -1, 1, -1, -1, 1, 1, -1, -1, -1, 1, 1, 1, -1, -1, 1, 1, 1, 1, 1, -1 },
        { 1, 1, -1, 1, -1, -1, 1, 1, 1, -1, 1, -1, 1, 1, 1, 1, -1, 1, -1, -1, -1, 1, -1, -1, 1, 1, -1, 1, -1, 1 },
        { 1, 1, -1, 1, -1, -1, -1, 1, 1, -1, 1, -1, 1, -1, 1, 1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, -1, -1, 1 },
    };

    @Provide("legendreTestCases")
    public Arbitrary<LegendreTestCase> legendreTestCases() {
        List<LegendreTestCase> cases = new ArrayList<>();

        for (int i = 0; i < LEGENDRE_P.length; i++) {
            for (int j = 0; j < LEGENDRE_A.length; j++) {
                cases.add(new LegendreTestCase(
                    LEGENDRE_A[j],
                    LEGENDRE_P[i],
                    LEGENDRE_VALUES[i][j]
                ));
            }
        }

        return Arbitraries.of(cases);
    }
    //endregion
}

package dora.crypto.rsa;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Positive;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;

import static dora.crypto.rsa.NumberTheory.modPow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


public class NumberTheoryTest {

    // gcd tests
    @Property
    void gcdProperties(@ForAll("gcdTestCases") GcdTestCase testCase) {
        BigInteger a = testCase.a;
        BigInteger b = testCase.b;
        BigInteger expected = testCase.expected;

        BigInteger actual = NumberTheory.gcd(a, b);

        assertThat(actual).isEqualTo(expected);

        assertThat(actual).isGreaterThanOrEqualTo(BigInteger.ZERO);

        if (!actual.equals(BigInteger.ZERO)) {
            assertThat(a.remainder(actual)).isEqualTo(BigInteger.ZERO);
            assertThat(b.remainder(actual)).isEqualTo(BigInteger.ZERO);
        }

        assertThat(NumberTheory.gcd(b, a)).isEqualTo(actual);

        if (a.equals(BigInteger.ZERO) && !b.equals(BigInteger.ZERO)) {
            assertThat(actual).isEqualTo(b.abs());
        }
        if (b.equals(BigInteger.ZERO) && !a.equals(BigInteger.ZERO)) {
            assertThat(actual).isEqualTo(a.abs());
        }
    }

    @Provide
    Arbitrary<GcdTestCase> gcdTestCases() {
        return Arbitraries.oneOf(
                Arbitraries.integers()
                        .between(1, 1000)
                        .map(BigInteger::valueOf)
                        .flatMap(a -> Arbitraries.integers()
                                .between(1, 1000)
                                .map(BigInteger::valueOf)
                                .map(b -> new GcdTestCase(a, b, a.gcd(b)))
                        ),
                Arbitraries.integers()
                        .between(1, 10000)
                        .map(BigInteger::valueOf)
                        .map(a -> new GcdTestCase(a, BigInteger.ZERO, a)),
                Arbitraries.of(new GcdTestCase(
                        BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO
                )),
                Arbitraries.integers()
                        .between(-1000, -1)
                        .map(BigInteger::valueOf)
                        .flatMap(a -> Arbitraries.integers()
                                .between(1, 1000)
                                .map(BigInteger::valueOf)
                                .map(b -> new GcdTestCase(a, b, a.abs().gcd(b)))
                        )
        );
    }

    @Property
    void gcdGeneralProperties(
            @ForAll @Positive int a,
            @ForAll @Positive int b
    ) {
        BigInteger bigA = BigInteger.valueOf(a);
        BigInteger bigB = BigInteger.valueOf(b);

        BigInteger gcd = NumberTheory.gcd(bigA, bigB);

        assertThat(gcd).isGreaterThanOrEqualTo(BigInteger.ZERO);
        assertThat(bigA.remainder(gcd)).isEqualTo(BigInteger.ZERO);
        assertThat(bigB.remainder(gcd)).isEqualTo(BigInteger.ZERO);

        assertThat(NumberTheory.gcd(bigB, bigA)).isEqualTo(gcd);

        if (!bigB.equals(BigInteger.ZERO)) {
            BigInteger mod = bigA.remainder(bigB);
            assertThat(NumberTheory.gcd(bigB, mod)).isEqualTo(gcd);
        }
    }

    private record GcdTestCase(BigInteger a, BigInteger b, BigInteger expected) {
        @Override
        public @NotNull String toString() {
            return String.format("GCD(%s, %s) = %s", a, b, expected);
        }
    }


    // extendedGcd tests
    @Property
    void extendedGcdNonNegativeProperties(
            @ForAll("nonNegativeSmall") BigInteger a,
            @ForAll("nonNegativeSmall") BigInteger b
    ) {
        BigInteger[] res = NumberTheory.extendedGcd(a, b);
        assertThat(res).hasSize(3);

        BigInteger d = res[0];
        BigInteger x = res[1];
        BigInteger y = res[2];

        assertThat(d).isEqualTo(a.gcd(b));
        assertThat(a.multiply(x).add(b.multiply(y))).isEqualTo(d);
        assertThat(d).isGreaterThanOrEqualTo(BigInteger.ZERO);

        if (!d.equals(BigInteger.ZERO)) {
            assertThat(a.remainder(d)).isEqualTo(BigInteger.ZERO);
            assertThat(b.remainder(d)).isEqualTo(BigInteger.ZERO);
        }


        if (!b.equals(BigInteger.ZERO)) {
            BigInteger r = a.remainder(b);
            BigInteger[] prev = NumberTheory.extendedGcd(b, r);
            BigInteger q = a.divide(b);
            assertThat(d).isEqualTo(prev[0]);
            assertThat(x).isEqualTo(prev[2]);
            assertThat(y).isEqualTo(prev[1].subtract(q.multiply(prev[2])));
        }

        if (b.equals(BigInteger.ZERO)) {
            assertThat(d).isEqualTo(a);
            assertThat(x).isEqualTo(BigInteger.ONE);
            assertThat(y).isEqualTo(BigInteger.ZERO);
        }
    }

    @Property
    void extendedGcdZeroAProperty(@ForAll("positiveSmall") BigInteger b) {
        BigInteger a = BigInteger.ZERO;
        BigInteger[] res = NumberTheory.extendedGcd(a, b);
        assertThat(res).hasSize(3);

        BigInteger d = res[0];
        BigInteger x = res[1];
        BigInteger y = res[2];

        assertThat(d).isEqualTo(b);
        assertThat(a.multiply(x).add(b.multiply(y))).isEqualTo(d);
        assertThat(x).isEqualTo(BigInteger.ZERO);
        assertThat(y).isEqualTo(BigInteger.ONE);
    }

    @Property
    void extendedGcdHandlesNegatives(
            @ForAll("signedSmall") BigInteger a,
            @ForAll("signedSmall") BigInteger b
    ) {
        BigInteger[] res = NumberTheory.extendedGcd(a, b);
        BigInteger[] absRes = NumberTheory.extendedGcd(a.abs(), b.abs());
        assertThat(res).containsExactlyElementsOf(Arrays.asList(absRes));

        BigInteger d = res[0];
        BigInteger x = res[1];
        BigInteger y = res[2];
        assertThat(a.abs().multiply(x).add(b.abs().multiply(y))).isEqualTo(d);
    }

    @Provide
    Arbitrary<BigInteger> nonNegativeSmall() {
        return Arbitraries.integers()
                .between(0, 10000)
                .map(BigInteger::valueOf);
    }

    @Provide
    Arbitrary<BigInteger> positiveSmall() {
        return Arbitraries.integers()
                .between(1, 10000)
                .map(BigInteger::valueOf);
    }

    @Provide
    Arbitrary<BigInteger> signedSmall() {
        return Arbitraries.integers()
                .between(-10000, 10000)
                .map(BigInteger::valueOf);
    }

    @Property
    void extendedGcdSymmetry(@ForAll("nonNegativeSmall") BigInteger a, @ForAll("nonNegativeSmall") BigInteger b) {
        BigInteger[] resAb = NumberTheory.extendedGcd(a, b);
        BigInteger[] resBa = NumberTheory.extendedGcd(b, a);
        assertThat(resAb[0]).isEqualTo(resBa[0]);

        assertThat(b.multiply(resBa[1]).add(a.multiply(resBa[2]))).isEqualTo(resAb[0]);
    }


    // ModPow tests
    @Property
    void modPowCorrectness(@ForAll("smallModPowCases") ModPowTestCase testCase) {
        BigInteger expected = slowModPow(testCase.a, testCase.exponent, testCase.modulus);
        BigInteger actual = modPow(testCase.a, testCase.exponent, testCase.modulus);

        assertThat(actual).isEqualTo(expected);
    }

    @Provide
    Arbitrary<ModPowTestCase> smallModPowCases() {
        return positiveModuli()
                .flatMap(modulus -> smallNonNegativeExponents()
                        .flatMap(exponent -> Arbitraries.integers()
                                .between(-10000, 10000)
                                .map(BigInteger::valueOf)
                                .map(a -> new ModPowTestCase(a, exponent, modulus))
                        )
                );
    }

    @Property
    void modPowGeneralProperties(
            @ForAll("positiveModuli") BigInteger modulus,
            @ForAll("nonNegativeExponents") BigInteger exponent,
            @ForAll @Positive int baseInt
    ) {
        BigInteger a = BigInteger.valueOf(baseInt);
        BigInteger result = modPow(a, exponent, modulus);

        assertThat(result).isGreaterThanOrEqualTo(BigInteger.ZERO);

        if (exponent.equals(BigInteger.ZERO)) {
            assertThat(result).isEqualTo(BigInteger.ONE);
        }

        if (a.equals(BigInteger.ZERO) && exponent.compareTo(BigInteger.ZERO) > 0) {
            assertThat(result).isEqualTo(BigInteger.ZERO);
        }

        if (modulus.equals(BigInteger.ONE)) {
            BigInteger expected = exponent.compareTo(BigInteger.ZERO) > 0 ? BigInteger.ZERO : BigInteger.ONE;
            assertThat(result).isEqualTo(expected);
        }

        if (!(modulus.equals(BigInteger.ONE) && exponent.equals(BigInteger.ZERO))) {
            assertThat(result).isLessThan(modulus);
        }

        BigInteger aShifted = a.add(modulus);
        BigInteger resultShifted = modPow(aShifted, exponent, modulus);
        assertThat(resultShifted).isEqualTo(result);

        BigInteger r = a.mod(modulus);
        BigInteger negativeCongruent = r.equals(BigInteger.ZERO) ? modulus.negate() : r.subtract(modulus);
        BigInteger resultNeg = modPow(negativeCongruent, exponent, modulus);
        assertThat(resultNeg).isEqualTo(result);
    }

    @Property
    void modPowHandlesLargeExponentsProperties(
            @ForAll("positiveModuli") BigInteger modulus,
            @ForAll("largeNonNegativeExponents") BigInteger exponent,
            @ForAll @Positive int baseInt
    ) {
        BigInteger a = BigInteger.valueOf(baseInt);
        BigInteger result = modPow(a, exponent, modulus);

        assertThat(result).isGreaterThanOrEqualTo(BigInteger.ZERO);
        assertThat(result).isLessThan(modulus);

        if (exponent.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            BigInteger halfExp = exponent.divide(BigInteger.valueOf(2));
            BigInteger halfPow = modPow(a, halfExp, modulus);
            BigInteger square = halfPow.multiply(halfPow).mod(modulus);
            assertThat(result).isEqualTo(square);
        }

        if (exponent.mod(BigInteger.valueOf(2)).equals(BigInteger.ONE)) {
            BigInteger evenExp = exponent.subtract(BigInteger.ONE);
            BigInteger evenPow = modPow(a, evenExp, modulus);
            BigInteger expected = evenPow.multiply(a).mod(modulus);
            assertThat(result).isEqualTo(expected);
        }
    }

    @Property
    void modPowNegativeExponentReturnsOne(@ForAll("positiveModuli") BigInteger modulus) {
        BigInteger negativeExp = BigInteger.valueOf(-5);
        BigInteger a = BigInteger.ONE;
        BigInteger result = modPow(a, negativeExp, modulus);

        assertThat(result).isEqualTo(BigInteger.ONE);
    }

    @Provide
    Arbitrary<BigInteger> positiveModuli() {
        return Arbitraries.integers()
                .between(1, 10000)
                .map(BigInteger::valueOf);
    }

    @Provide
    Arbitrary<BigInteger> smallNonNegativeExponents() {
        return Arbitraries.integers()
                .between(0, 100)
                .map(BigInteger::valueOf);
    }

    @Provide
    Arbitrary<BigInteger> nonNegativeExponents() {
        return Arbitraries.integers()
                .between(0, 1000)
                .map(BigInteger::valueOf);
    }

    @Provide
    Arbitrary<BigInteger> largeNonNegativeExponents() {
        return Arbitraries.bigIntegers()
                .between(BigInteger.valueOf(1001), BigInteger.valueOf(1000000));
    }

    private BigInteger slowModPow(BigInteger base, BigInteger exponent, BigInteger modulus) {
        if (modulus.equals(BigInteger.ZERO)) {
            throw new IllegalArgumentException("Modulus cannot be zero");
        }
        if (exponent.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Exponent cannot be negative");
        }
        BigInteger result = BigInteger.ONE;
        base = base.mod(modulus).abs();
        for (BigInteger i = BigInteger.ZERO; i.compareTo(exponent) < 0; i = i.add(BigInteger.ONE)) {
            result = result.multiply(base).mod(modulus);
        }
        return result;
    }

    private record ModPowTestCase(BigInteger a, BigInteger exponent, BigInteger modulus) {
        @Override
        public @NotNull String toString() {
            return String.format("modPow(%s, %s, %s)", a, exponent, modulus);
        }
    }


    // Legandr
    @Property
    void legendreSymbolCorrectness(@ForAll("smallLegendreCases") LegendreTestCase testCase) {
        BigInteger a = testCase.a;
        BigInteger p = testCase.p;
        BigInteger expected = slowLegendre(a, p);

        BigInteger actual = NumberTheory.legendreSymbol(a, p);

        assertThat(actual).isEqualTo(expected);
    }

    @Provide
    Arbitrary<LegendreTestCase> smallLegendreCases() {
        return smallPrimes()
                .flatMap(p -> Arbitraries.integers()
                        .between(-1000, 1000)
                        .map(BigInteger::valueOf)
                        .map(a -> new LegendreTestCase(a, p))
                );
    }

    @Property
    void legendreSymbolProperties(@ForAll("smallLegendreCases") LegendreTestCase testCase) {
        BigInteger a = testCase.a;
        BigInteger p = testCase.p;

        BigInteger ls = NumberTheory.legendreSymbol(a, p);

        assertThat(ls).isIn(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(-1L));

        BigInteger aMod = a.mod(p);
        assertThat(NumberTheory.legendreSymbol(aMod, p)).isEqualTo(ls);

        if (aMod.equals(BigInteger.ZERO)) {
            assertThat(ls).isEqualTo(BigInteger.ZERO);
        }

        if (aMod.equals(BigInteger.ONE)) {
            assertThat(ls).isEqualTo(BigInteger.ONE);
        }

        BigInteger b = BigInteger.valueOf(3);
        BigInteger lsAb = NumberTheory.legendreSymbol(a.multiply(b), p);
        BigInteger lsB = NumberTheory.legendreSymbol(b, p);
        assertThat(lsAb).isEqualTo(ls.multiply(lsB));

        BigInteger minusOne = BigInteger.valueOf(-1L);
        BigInteger legMinusOne = NumberTheory.legendreSymbol(minusOne, p);
        BigInteger exp = p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2L));
        BigInteger expectedMinusOne = modPow(minusOne, exp, p).equals(BigInteger.ONE) ? BigInteger.ONE : BigInteger.valueOf(-1L);
        assertThat(legMinusOne).isEqualTo(expectedMinusOne);
    }

    @Property
    void legendreSymbolThrowsForInvalidP(
            @ForAll("invalidPs") BigInteger p,
            @ForAll @Positive int baseInt
    ) {
        BigInteger a = BigInteger.valueOf(baseInt);
        assertThatThrownBy(() -> NumberTheory.legendreSymbol(a, p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("p must be a positive prime");
    }

    @Provide
    Arbitrary<BigInteger> invalidPs() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(-10, 0).map(BigInteger::valueOf),
                Arbitraries.integers().between(1, 100).filter(n -> !NumberTheory.isPrime(BigInteger.valueOf(n))).map(BigInteger::valueOf)
        );
    }

    @Provide
    Arbitrary<BigInteger> smallPrimes() {
        return Arbitraries.integers()
                .between(2, 200)
                .filter(n -> NumberTheory.isPrime(BigInteger.valueOf(n)))
                .map(BigInteger::valueOf);
    }

    private BigInteger slowLegendre(BigInteger a, BigInteger p) {
        a = a.mod(p);
        if (a.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }
        if (a.equals(BigInteger.ONE)) {
            return BigInteger.ONE;
        }

        boolean isResidue = false;
        for (BigInteger x = BigInteger.ZERO; x.compareTo(p) < 0; x = x.add(BigInteger.ONE)) {
            BigInteger sq = x.multiply(x).mod(p);
            if (sq.equals(a)) {
                isResidue = true;
                break;
            }
        }
        return isResidue ? BigInteger.ONE : BigInteger.valueOf(-1L);
    }

    private record LegendreTestCase(BigInteger a, BigInteger p) {
        @Override
        public @NotNull String toString() {
            return String.format("legendre(%s, %s)", a, p);
        }
    }

    // jacoby Simbol
    @Property
    void jacobySymbolCorrectness(@ForAll("JacobyCases") JacobyTestCase testCase) {
        BigInteger a = testCase.a;
        BigInteger p = testCase.p;

        BigInteger actual = NumberTheory.jacobiSymbol(a, p);

        assertThat(actual).isEqualTo(testCase.expected);
    }

    @Provide
    Arbitrary<JacobyTestCase> JacobyCases() {
        return Arbitraries.of(
                new JacobyTestCase(BigInteger.valueOf(7), BigInteger.valueOf(15), BigInteger.valueOf(-1)),
                new JacobyTestCase(BigInteger.ONE, BigInteger.valueOf(21), BigInteger.ONE),
                new JacobyTestCase(BigInteger.ONE, BigInteger.valueOf(17), BigInteger.ONE),
                new JacobyTestCase(BigInteger.valueOf(3), BigInteger.valueOf(9), BigInteger.ZERO),
                new JacobyTestCase(BigInteger.valueOf(5), BigInteger.valueOf(25), BigInteger.ZERO),
                new JacobyTestCase(BigInteger.valueOf(219), BigInteger.valueOf(383), BigInteger.ONE),
                new JacobyTestCase(BigInteger.valueOf(201), BigInteger.valueOf(379), BigInteger.valueOf(-1)),
                new JacobyTestCase(BigInteger.valueOf(983), BigInteger.valueOf(1103), BigInteger.ONE),
                new JacobyTestCase(BigInteger.valueOf(-983), BigInteger.valueOf(1103), BigInteger.valueOf(-1))

        );
    }

    private record JacobyTestCase(BigInteger a, BigInteger p, BigInteger expected) {
        @Override
        public @NotNull String toString() {
            return String.format("Jacoby (%s, %s, %s)", a, p, expected);
        }
    }
}

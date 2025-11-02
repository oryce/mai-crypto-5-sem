package dora.crypto.rsa;

import net.jqwik.api.*;

import java.math.BigInteger;

import dora.crypto.rsa.ProbabilisticTests.Tests.*;
import org.jetbrains.annotations.NotNull;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


public class ProbabilisticPrimalityTest {

    private boolean deterministicIsPrime(int num) {
        if (num <= 1) return false;
        if (num == 2 || num == 3) return true;
        if (num % 2 == 0 || num % 3 == 0) return false;
        for (int i = 5; i * i <= num; i += 6) {
            if (num % i == 0 || num % (i + 2) == 0) return false;
        }
        return true;
    }

    private final FermatPrimality fermat = new FermatPrimality();
    private final SolovayStrassenPrimality solovayStrassen = new SolovayStrassenPrimality();
    private final MillerRabinPrimality millerRabin = new MillerRabinPrimality();

    @Provide
    Arbitrary<BigInteger> smallPrimes() {
        return Arbitraries.integers()
                .between(2, 100000)
                .filter(this::deterministicIsPrime)
                .map(BigInteger::valueOf);
    }

    @Provide
    Arbitrary<BigInteger> smallComposites() {
        return Arbitraries.integers()
                .between(4, 100000)
                .filter(n -> !deterministicIsPrime(n))
                .map(BigInteger::valueOf);
    }

    @Provide
    Arbitrary<BigInteger> evenNumbersGreaterThanTwo() {
        return Arbitraries.integers()
                .between(4, 100000)
                .filter(n -> n % 2 == 0)
                .map(BigInteger::valueOf);
    }

    @Provide
    Arbitrary<BigInteger> invalidNegativesOrZeroOrOne() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(-100, 0).map(BigInteger::valueOf),
                Arbitraries.just(BigInteger.ONE)
        );
    }

    @Provide
    Arbitrary<SpecialCase> specialPrimeCases() {
        return Arbitraries.of(
                new SpecialCase(BigInteger.valueOf(2), 0.99, true),
                new SpecialCase(BigInteger.valueOf(3), 0.99, true),
                new SpecialCase(BigInteger.valueOf(5), 0.99, true),
                new SpecialCase(BigInteger.valueOf(17), 0.99, true)
        );
    }

    @Provide
    Arbitrary<SpecialCase> specialCompositeCases() {
        return Arbitraries.of(
                new SpecialCase(BigInteger.valueOf(4), 0.99, false),
                new SpecialCase(BigInteger.valueOf(9), 0.99, false),
                new SpecialCase(BigInteger.valueOf(15), 0.99, false),
                new SpecialCase(BigInteger.valueOf(25), 0.99, false)
        );
    }

    @Property
    void fermatTestPrimes(@ForAll("smallPrimes") BigInteger n) {
        assertThat(fermat.isProbablyPrime(n, 0.99)).isTrue();
    }

    @Property
    void solovayStrassenTestPrimes(@ForAll("smallPrimes") BigInteger n) {
        assertThat(solovayStrassen.isProbablyPrime(n, 0.99)).isTrue();
    }

    @Property
    void millerRabinTestPrimes(@ForAll("smallPrimes") BigInteger n) {
        assertThat(millerRabin.isProbablyPrime(n, 0.99)).isTrue();
    }

    @Property
    void fermatTestComposites(@ForAll("smallComposites") BigInteger n) {
        assertThat(fermat.isProbablyPrime(n, 0.99)).isFalse();
    }

    @Property
    void solovayStrassenTestComposites(@ForAll("smallComposites") BigInteger n) {
        assertThat(solovayStrassen.isProbablyPrime(n, 0.99)).isFalse();
    }

    @Property
    void millerRabinTestComposites(@ForAll("smallComposites") BigInteger n) {
        assertThat(millerRabin.isProbablyPrime(n, 0.99)).isFalse();
    }

    @Property
    void testEvenNumbersGreaterThanTwo(@ForAll("evenNumbersGreaterThanTwo") BigInteger n) {
        assertThat(fermat.isProbablyPrime(n, 0.99)).isFalse();
        assertThat(solovayStrassen.isProbablyPrime(n, 0.99)).isFalse();
        assertThat(millerRabin.isProbablyPrime(n, 0.99)).isFalse();
    }

    @Property
    void testSpecialPrimeCases(@ForAll("specialPrimeCases") SpecialCase sc) {
        assertThat(fermat.isProbablyPrime(sc.n, sc.confidence)).isEqualTo(sc.expected);
        assertThat(solovayStrassen.isProbablyPrime(sc.n, sc.confidence)).isEqualTo(sc.expected);
        assertThat(millerRabin.isProbablyPrime(sc.n, sc.confidence)).isEqualTo(sc.expected);
    }

    @Property
    void testSpecialCompositeCases(@ForAll("specialCompositeCases") SpecialCase sc) {
        assertThat(fermat.isProbablyPrime(sc.n, sc.confidence)).isEqualTo(sc.expected);
        assertThat(solovayStrassen.isProbablyPrime(sc.n, sc.confidence)).isEqualTo(sc.expected);
        assertThat(millerRabin.isProbablyPrime(sc.n, sc.confidence)).isEqualTo(sc.expected);
    }

    // Exception tests for invalid confidence
    @Property
    void testInvalidConfidence(@ForAll("smallPrimes") BigInteger n) {
        assertThatThrownBy(() -> fermat.isProbablyPrime(n, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minProb must be in [0.5, 1)");
        assertThatThrownBy(() -> solovayStrassen.isProbablyPrime(n, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minProb must be in [0.5, 1)");
        assertThatThrownBy(() -> millerRabin.isProbablyPrime(n, 0.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minProb must be in [0.5, 1)");
        assertThatThrownBy(() -> solovayStrassen.isProbablyPrime(n, -2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minProb must be in [0.5, 1)");
        assertThatThrownBy(() -> millerRabin.isProbablyPrime(n, -0.4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minProb must be in [0.5, 1)");
    }

    // Exception tests for invalid n
    @Property
    void testInvalidN(@ForAll("invalidNegativesOrZeroOrOne") BigInteger n) {
        assertThat(fermat.isProbablyPrime(n, 0.99)).isFalse();
        assertThat(solovayStrassen.isProbablyPrime(n, 0.99)).isFalse();
        assertThat(millerRabin.isProbablyPrime(n, 0.99)).isFalse();
    }

    @Property
    void consistencyWithDeterministicForPrimes(@ForAll("smallPrimes") BigInteger n) {
        assertThat(fermat.isProbablyPrime(n, 0.99)).isEqualTo(NumberTheory.isPrime(n));
        assertThat(solovayStrassen.isProbablyPrime(n, 0.99)).isEqualTo(NumberTheory.isPrime(n));
        assertThat(millerRabin.isProbablyPrime(n, 0.99)).isEqualTo(NumberTheory.isPrime(n));
    }

    private record SpecialCase(BigInteger n, double confidence, boolean expected) {
        @Override
        public @NotNull String toString() {
            return String.format("isPrime(%s, %.3f) = %s", n, confidence, expected);
        }
    }
}
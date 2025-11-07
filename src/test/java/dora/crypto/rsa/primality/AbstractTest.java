package dora.crypto.rsa.primality;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.Negative;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractTest {

    private static final int SIEVE_SIZE = 10_000_000;

    protected final PrimalityTest primalityTest;

    protected AbstractTest(PrimalityTest primalityTest) {
        this.primalityTest = primalityTest;
    }

    @Example
    void zeroIsNotPrime() {
        assertThat(primalityTest.isProbablyPrime(BigInteger.ZERO, 0.99)).isFalse();
    }

    @Example
    void oneIsNotPrime() {
        assertThat(primalityTest.isProbablyPrime(BigInteger.ONE, 0.99)).isFalse();
    }

    @Property
    void negativesAreNotPrime(@ForAll @Negative BigInteger n) {
        assertThat(primalityTest.isProbablyPrime(n, 0.99)).isFalse();
    }

    @Property
    void primesTestPositive(@ForAll("primes") BigInteger n) {
        assertThat(primalityTest.isProbablyPrime(n, 0.99)).isTrue();
    }

    @Property
    void compositesTestNegative(@ForAll("composites") BigInteger n) {
        assertThat(primalityTest.isProbablyPrime(n, 0.99)).isFalse();
    }

    @Provide("primes")
    Arbitrary<BigInteger> primes() {
        BitSet sieve = primeSieve(SIEVE_SIZE);

        List<BigInteger> primes = IntStream.range(2, sieve.size())
            .filter(sieve::get)
            .mapToObj(BigInteger::valueOf)
            .toList();

        return Arbitraries.of(primes);
    }

    @Provide("composites")
    Arbitrary<BigInteger> composites() {
        BitSet sieve = primeSieve(SIEVE_SIZE);

        List<BigInteger> composites = IntStream.range(1, sieve.size())
            .filter((n) -> !sieve.get(n))
            .mapToObj(BigInteger::valueOf)
            .toList();

        return Arbitraries.of(composites);
    }

    private BitSet primeSieve(int n) {
        BitSet primes = new BitSet(n + 1);

        for (int i = 0; i <= n; i++) {
            primes.set(i);
        }

        for (int p = 2; p * p <= n; p++) {
            if (primes.get(p)) {
                for (int i = p * p; i <= n; i += p) {
                    primes.clear(i);
                }
            }
        }

        return primes;
    }
}

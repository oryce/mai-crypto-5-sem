package dora.crypto.block.rijndael;

import net.jqwik.api.*;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GaloisFieldTest {

    private final GaloisField field;

    GaloisFieldTest() {
        field = new GaloisField();
    }

    //region Degree tests
    @Example
    void degreeOfZero() {
        assertThat(field.degree((byte) 0b0)).isEqualTo(-1);
    }

    @Example
    void degreeOfOne() {
        assertThat(field.degree((byte) 0b1)).isEqualTo(0);
    }

    @Example
    void degreeOfPolynomials() {
        assertThat(field.degree((byte) 0b10)).isEqualTo(1);
        assertThat(field.degree((byte) 0b100)).isEqualTo(2);
        assertThat(field.degree((byte) 0b1000)).isEqualTo(3);
        assertThat(field.degree((byte) 0b10000000)).isEqualTo(7);
    }

    @Example
    void degreeWithLowerBits() {
        assertThat(field.degree((byte) 0b11)).isEqualTo(1);
        assertThat(field.degree((byte) 0b101)).isEqualTo(2);
        assertThat(field.degree((byte) 0b11111111)).isEqualTo(7);
    }
    //endregion

    //region Addition tests
    @Example
    void additionWorks() {
        assertThat(field.add((byte) 0b101, (byte) 0b011))
            .isEqualTo((byte) 0b110);
        assertThat(field.add((byte) 0b11110000, (byte) 0b00001111))
            .isEqualTo((byte) 0b11111111);
    }

    @Property
    void additionIsCommutative(@ForAll byte a, @ForAll byte b) {
        assertThat(field.add(a, b)).isEqualTo(field.add(b, a));
    }

    @Property
    void additionIsAssociative(
        @ForAll byte a,
        @ForAll byte b,
        @ForAll byte c
    ) {
        byte left = field.add(field.add(a, b), c);
        byte right = field.add(a, field.add(b, c));
        assertThat(left).isEqualTo(right);
    }

    @Property
    void addingZeroIsIdentity(@ForAll byte a) {
        assertThat(field.add(a, (byte) 0)).isEqualTo(a);
    }

    @Property
    void addingSelfIsZero(@ForAll byte a) {
        assertThat(field.add(a, a)).isEqualTo((byte) 0);
    }
    //endregion

    //region Multiplication tests
    @Example
    void multiplicationWithIrreducibles() {
        short mod = (short) 0b100011011;
        
        // (x^7 + x^6) * (x^6 + x^5) mod (x^8 + x^4 + x^3 + x + 1)
        byte a = (byte) 0b11000000;
        byte b = (byte) 0b01100000;
        byte result = field.mulMod(a, b, mod);
        
        assertThat(field.degree(result)).isLessThan(8);
    }

    @Property
    void multiplicationIsCommutative(@ForAll byte a, @ForAll byte b) {
        short mod = (short) 0b100011011;
        assertThat(field.mulMod(a, b, mod)).isEqualTo(field.mulMod(b, a, mod));
    }

    @Property
    void multiplyingByZeroEqualsZero(@ForAll byte f) {
        short mod = (short) 0b100011011;
        assertThat(field.mulMod(f, (byte) 0, mod)).isEqualTo((byte) 0);
    }

    @Property
    void multiplyingByOneIsIdentity(@ForAll byte f) {
        short mod = (short) 0b100011011;
        assertThat(field.mulMod(f, (byte) 1, mod)).isEqualTo(f);
    }

    @Example
    void multiplicationThrowsOnReducibleModulus() {
        short reducibleMod = (short) 0b100000000;
        assertThatThrownBy(() -> field.mulMod((byte) 0b101, (byte) 0b11, reducibleMod))
            .isInstanceOf(IllegalArgumentException.class);
    }
    //endregion

    //region Inverse tests
    @Example
    void inverseOfOneIsOne() {
        short mod = (short) 0b100011011;
        assertThat(field.inv((byte) 1, mod)).isEqualTo((byte) 1);
    }

    @Property
    void multiplyingByInverseGivesOne(@ForAll("nonZeroBytes") byte f) {
        short mod = (short) 0b100011011;

        byte inverse = field.inv(f, mod);
        byte result = field.mulMod(f, inverse, mod);

        assertThat(result).isEqualTo((byte) 1);
    }

    @Example
    void inverseOfZeroThrows() {
        short mod = (short) 0b100011011;
        assertThatThrownBy(() -> field.inv((byte) 0, mod))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Example
    void inverseThrowsOnReducibleModulus() {
        short reducibleMod = (short) 0b100000000;
        assertThatThrownBy(() -> field.inv((byte) 5, reducibleMod))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Provide
    Arbitrary<Byte> nonZeroBytes() {
        return Arbitraries.bytes().filter((b) -> b != 0);
    }
    //endregion

    //region Irreducibility tests
    @Example
    void someIrreduciblePolynomials() {
        assertThat(field.irreducible((short) 0b100011101)).isTrue();
        assertThat(field.irreducible((short) 0b100101011)).isTrue();
        assertThat(field.irreducible((short) 0b100111001)).isTrue();
    }

    @Example
    void someReduciblePolynomials() {
        assertThat(field.irreducible((short) 0b100000000)).isFalse();
        assertThat(field.irreducible((short) 0b100000001)).isFalse();
        assertThat(field.irreducible((short) 0b100001100)).isFalse();
    }

    @Example
    void zeroIsNotIrreducible() {
        assertThat(field.irreducible((short) 0)).isFalse();
    }

    @Example
    void linearPolynomialsAreIrreducible() {
        assertThat(field.irreducible((short) 0b10)).isTrue();
        assertThat(field.irreducible((short) 0b11)).isTrue();
    }

    @Example
    void thirtyIrreducibleModules() {
        assertThat(field.irreducibles()).hasSize(30);
    }

    @Example
    void allIrreduciblesPassIrreducibleTest() {
        Collection<Short> irreducibles = field.irreducibles();

        for (short poly : irreducibles) {
            assertThat(field.irreducible(poly))
                .as("0b%s should be irreducible", Integer.toBinaryString(poly))
                .isTrue();
        }
    }

    @Example
    void irreduciblesContainsAesPolynomial() {
        short aesPolynomial = (short) 0b100011011;
        assertThat(field.irreducibles()).contains(aesPolynomial);
    }
    //endregion

    //region Factorization tests
    @Example
    void factorizeThrowsOnZero() {
        assertThatThrownBy(() -> field.factorize(0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Example
    void factorizeOneReturnsEmpty() {
        assertThat(field.factorize(1)).isEmpty();
    }

    @Example
    void factorizeIrreducibleReturnsItself() {
        long irreducible = 0b100011011;
        assertThat(field.factorize(irreducible)).containsExactly(irreducible);
    }

    @Example
    void factorizeComposite() {
        long composite = 0b101;
        Collection<Long> factors = field.factorize(composite);
        assertThat(factors).containsExactly(0b11L, 0b11L);
    }

    @Example
    void factorizeLargerComposite() {
        long composite = 0b100000001;
        Collection<Long> factors = field.factorize(composite);
        assertThat(factors).hasSize(8).allMatch((f) -> f == 0b11);
    }
    //endregion
}

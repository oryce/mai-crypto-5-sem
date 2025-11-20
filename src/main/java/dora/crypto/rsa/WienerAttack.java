package dora.crypto.rsa;

import dora.crypto.rsa.Rsa.KeyPairGenerator;
import dora.crypto.rsa.Rsa.PrimalityTestType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;

public final class WienerAttack {

    private static final BigInteger FOUR = BigInteger.valueOf(4);

    public Optional<AttackResult> attack(@NotNull BigInteger n, @NotNull BigInteger e) {
        Objects.requireNonNull(n, "modulus");
        Objects.requireNonNull(e, "public exponent");

        List<BigInteger> continuedFraction = continuedFraction(e, n);
        List<Convergent> convergents = convergents(continuedFraction);

        for (Convergent convergent : convergents) {
            BigInteger k = convergent.p();
            BigInteger d = convergent.q();

            if (k.equals(ZERO))
                continue;

            /* f_n = (e * d_n - 1) / k_n */
            BigInteger f = e.multiply(d).subtract(ONE).divide(k);
            /* Solve x^2 - ((N - f_n) + 1)*x + N = 0; b = (N - f) + 1 */
            BigInteger b = n.subtract(f).add(ONE);
            /* disc = b^2 - 4 * 1 * n */
            BigInteger disc = b.multiply(b).subtract(FOUR.multiply(n));

            if (disc.signum() < 0)
                /* equation cannot be solved */
                continue;

            BigInteger[] sqrtDisc = disc.sqrtAndRemainder();

            if (!sqrtDisc[1].equals(ZERO))
                /* `disc` is not a square */
                continue;

            BigInteger[] pDivRem = b.add(sqrtDisc[0]).divideAndRemainder(TWO);
            BigInteger[] qDivRem = b.subtract(sqrtDisc[0]).divideAndRemainder(TWO);

            boolean divisionSucceeded = pDivRem[1].equals(ZERO) && qDivRem[1].equals(ZERO);
            boolean attackSucceeded = pDivRem[0].multiply(qDivRem[0]).equals(n);

            if (divisionSucceeded && attackSucceeded) {
                return Optional.of(new AttackResult(d, f, convergents));
            }
        }

        return Optional.empty();
    }

    public record AttackResult(BigInteger d, BigInteger f, List<Convergent> convergents) {
    }

    private List<BigInteger> continuedFraction(BigInteger p, BigInteger q) {
        List<BigInteger> result = new ArrayList<>();

        while (q.signum() > 0) {
            BigInteger[] divRem = p.divideAndRemainder(q);

            result.add(divRem[0]);

            p = q;
            q = divRem[1];
        }

        return result;
    }

    private List<Convergent> convergents(List<BigInteger> continuedFraction) {
        if (continuedFraction.isEmpty())
            return Collections.emptyList();

        List<Convergent> result = new ArrayList<>(continuedFraction.size() + 2);
        result.add(new Convergent(ZERO, ONE));
        result.add(new Convergent(ONE, ZERO));

        for (BigInteger a : continuedFraction) {
            Convergent cm1 = result.get(result.size() - 1);
            Convergent cm2 = result.get(result.size() - 2);

            Convergent c = new Convergent(
                cm1.p().multiply(a).add(cm2.p()),
                cm1.q().multiply(a).add(cm2.q())
            );

            result.add(c);
        }

        return result;
    }

    public record Convergent(BigInteger p, BigInteger q) {
    }

    public static class VulnerableKeyPairGenerator extends KeyPairGenerator {

        private static final BigInteger THREE = BigInteger.valueOf(3);

        public VulnerableKeyPairGenerator(@NotNull PrimalityTestType primalityTest, double certainty, int primeSize) {
            super(primalityTest, certainty, primeSize);
        }

        @Override
        protected @Nullable KeyPair createKeyPair(BigInteger n, BigInteger e, BigInteger p, BigInteger q) {
            BigInteger phi = p.subtract(ONE).multiply(q.subtract(ONE));

            int privateKeySize = primeSize() / 2;
            BigInteger d = new BigInteger(privateKeySize, random);

            if (!phi.gcd(d).equals(ONE))
                /* `phi` and `d` are not coprime, try again */
                return null;

            if (overWienerLimit(d, n))
                /* `d` is too large for the attack to work, try again */
                return null;

            e = math.modInverse(d, phi);

            return new KeyPair(n, e, d);
        }

        private boolean overWienerLimit(BigInteger d, BigInteger n) {
            BigInteger nSecondRoot = n.sqrt();
            BigInteger nFourthRoot = nSecondRoot.sqrt();
            return d.compareTo(nFourthRoot.divide(THREE)) >= 0;
        }
    }
}

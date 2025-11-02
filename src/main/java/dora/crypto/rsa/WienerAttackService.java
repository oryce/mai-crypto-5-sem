package dora.crypto.rsa;

import dora.crypto.rsa.ProbabilisticTests.ProbabilisticPrimality;
import dora.crypto.rsa.ProbabilisticTests.RsaService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WienerAttackService {
    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger TWO = BigInteger.TWO;
    private static final BigInteger FOUR = BigInteger.valueOf(4);

    private final ProbabilisticPrimality primalityTest;  // Для проверки p, q (опционально)
    private final double minProbability;

    public WienerAttackService(RsaService.PrimalityTest test, double minProbability) {
        this.primalityTest = test.getStrategy();
        this.minProbability = minProbability;
    }

    public record AttackResult(BigInteger d, BigInteger phi, List<Convergent> convergents) {}

    public record Convergent(BigInteger num, BigInteger den) {}

    public Optional<AttackResult> attack(BigInteger n, BigInteger e) {
        if (n.compareTo(ZERO) <= 0 || e.compareTo(ONE) <= 0 || e.compareTo(n) >= 0) {
            throw new IllegalArgumentException("Invalid public key (N, e)");
        }

        List<BigInteger> cf = getContinuedFractionExpansion(e, n);
        List<Convergent> convergents = getConvergents(cf);

        BigInteger trace;
        BigInteger disc;
        BigInteger sqrtDisc;
        BigInteger p, q;

        for (Convergent conv : convergents) {
            BigInteger kappa = conv.num();
            BigInteger dCand = conv.den();

            if (kappa.equals(ZERO)) continue;

            BigInteger edMinus1 = e.multiply(dCand).subtract(ONE);
            if (!edMinus1.mod(kappa).equals(ZERO)) continue;

            BigInteger phi = edMinus1.divide(kappa);


            trace = n.subtract(phi).add(ONE);
            disc = trace.multiply(trace).subtract(FOUR.multiply(n));
            if (disc.signum() < 0) continue;

            sqrtDisc = isqrt(disc);
            if (sqrtDisc == null || !sqrtDisc.multiply(sqrtDisc).equals(disc)) continue;

            if (trace.add(sqrtDisc).mod(TWO).equals(ZERO) && trace.subtract(sqrtDisc).mod(TWO).equals(ZERO)) {
                p = trace.add(sqrtDisc).divide(TWO);
                q = trace.subtract(sqrtDisc).divide(TWO);
                if (p.multiply(q).equals(n)) {
                    // Опционально: проверить простоту p, q
                    if (primalityTest.isProbablyPrime(p, minProbability) && primalityTest.isProbablyPrime(q, minProbability)) {
                        return Optional.of(new AttackResult(dCand, phi, new ArrayList<>(convergents)));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Calculates the continued fraction coefficients for num / den.
     */
    private List<BigInteger> getContinuedFractionExpansion(BigInteger num, BigInteger den) {
        List<BigInteger> cf = new ArrayList<>();
        while (den.signum() > 0) {
            BigInteger a = num.divide(den);
            cf.add(a);
            BigInteger remainder = num.mod(den);
            num = den;
            den = remainder;
        }
        return cf;
    }

    /**
     * Builds convergents from cf coefficients.
     */
    private List<Convergent> getConvergents(List<BigInteger> cf) {
        List<Convergent> convergents = new ArrayList<>();
        if (cf.isEmpty()) return convergents;

        BigInteger hPrevPrev = ZERO;  // h_{-2}
        BigInteger hPrev = ONE;       // h_{-1}
        BigInteger kPrevPrev = ONE;   // k_{-2}
        BigInteger kPrev = ZERO;      // k_{-1}

        for (BigInteger a : cf) {
            BigInteger h = a.multiply(hPrev).add(hPrevPrev);
            BigInteger k = a.multiply(kPrev).add(kPrevPrev);
            convergents.add(new Convergent(h, k));
            hPrevPrev = hPrev;
            hPrev = h;
            kPrevPrev = kPrev;
            kPrev = k;
        }
        return convergents;
    }

    private static BigInteger isqrt(BigInteger n) {
        if (n.signum() < 0) return null;
        if (n.equals(ZERO) || n.equals(ONE)) return n;

        BigInteger x0 = ONE.shiftLeft((n.bitLength() + 1) / 2);
        BigInteger x1 = n.divide(x0).add(x0).divide(TWO);

        while (x1.compareTo(x0) < 0) {
            x0 = x1;
            x1 = n.divide(x0).add(x0).divide(TWO);
        }
        return x0;
    }
}

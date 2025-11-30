package dora.crypto.dh;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.security.SecureRandom;

import static java.math.BigInteger.TWO;
import static java.util.Objects.requireNonNull;

public final class DiffieHellman {

    private final BigInteger p;
    private final BigInteger g;
    private final SecureRandom random;

    private BigInteger a;
    private BigInteger s;

    public DiffieHellman(
        @NotNull BigInteger p,
        @NotNull BigInteger g,
        @NotNull SecureRandom random
    ) {
        this.p = requireNonNull(p, "modulus");
        this.g = requireNonNull(g, "base");
        this.random = requireNonNull(random, "random");
    }

    public DiffieHellman(@NotNull BigInteger p, @NotNull BigInteger g) {
        this(p, g, new SecureRandom());
    }

    public static DiffieHellman of(@NotNull DiffieHellmanGroup group) {
        requireNonNull(group, "group");
        return new DiffieHellman(group.modulus(), group.base());
    }

    /**
     * Initiates the key exchange. Returns the public key.
     *
     * @return public key
     */
    public BigInteger initiate() {
        // Generate `a` in [2; p-2].
        do {
            a = new BigInteger(p.bitLength(), random);
        } while (a.compareTo(TWO) < 0 || a.compareTo(p.subtract(TWO)) > 0);

        // Compute `A = g^a mod p`
        return g.modPow(a, p);
    }

    /**
     * Resumes the key exchange with the given private key.
     *
     * @param a private key
     */
    public void resume(@NotNull BigInteger a) {
        this.a = requireNonNull(a, "private key");
    }

    /**
     * Returns the private key.
     *
     * @return private key
     */
    public BigInteger privateKey() {
        if (a == null)
            throw new IllegalStateException("Key exchange is not initialized");
        return a;
    }

    /**
     * Completes the key exchange.
     *
     * @param B peer public key
     */
    public void complete(@NotNull BigInteger B) {
        if (a == null)
            throw new IllegalStateException("Key exchange is not initialized");

        requireNonNull(B, "peer public key");
        s = B.modPow(a, p);
    }

    /**
     * Returns the result of the key exchange.
     *
     * @return shared secret
     */
    public BigInteger sharedSecret() {
        if (s == null)
            throw new IllegalStateException("Key exchange is not completed");
        return s;
    }
}

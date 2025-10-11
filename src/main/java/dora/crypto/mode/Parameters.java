package dora.crypto.mode;

/**
 * A marker interface for cipher mode parameters.
 * <p>
 * Implementations of {@link CipherMode} are expected to up-cast to required
 * parameter types, such as {@link IvParameters}.
 */
public interface Parameters {

    Parameters NO_PARAMETERS = new NoParameters();

    final class NoParameters implements Parameters {
    }

    /**
     * Initialization Vector parameters.
     */
    record IvParameters(byte[] iv) implements Parameters {
    }
}

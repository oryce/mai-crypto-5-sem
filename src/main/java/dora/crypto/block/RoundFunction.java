package dora.crypto.block;

public interface RoundFunction {

    byte[] apply(byte[] block, byte[] key);
}

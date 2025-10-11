package dora.crypto.block;

public interface KeySchedule {

    byte[][] roundKeys(byte[] key);
}

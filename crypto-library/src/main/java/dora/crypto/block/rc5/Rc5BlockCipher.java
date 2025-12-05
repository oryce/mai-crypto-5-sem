package dora.crypto.block.rc5;

import dora.crypto.block.BlockCipher;
import dora.crypto.block.Word;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public final class Rc5BlockCipher implements BlockCipher {

    private final Rc5Parameters parameters;
    private final Rc5KeySchedule keySchedule;

    private Word[] roundKeys;

    public Rc5BlockCipher(@NotNull Rc5Parameters parameters) {
        this.parameters = requireNonNull(parameters, "parameters");
        this.keySchedule = new Rc5KeySchedule(parameters);
    }

    @Override
    public int blockSize() {
        return parameters.wordSize().bytes() * 2; // encryption is done in 2-word blocks
    }

    @Override
    public void init(byte @NotNull [] key) {
        roundKeys = Arrays.stream(keySchedule.roundKeys(key))
            .map(Word::fromByteArray)
            .toArray(Word[]::new);
    }

    @Override
    public byte[] encrypt(byte @NotNull [] plaintext) {
        requireNonNull(plaintext, "plaintext");

        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");
        if (plaintext.length != blockSize())
            throw new IllegalArgumentException("Invalid block size");

        Word a = Word.fromByteArray(Arrays.copyOfRange(plaintext, 0, plaintext.length / 2));
        Word b = Word.fromByteArray(Arrays.copyOfRange(plaintext, plaintext.length / 2, plaintext.length));

        a = a.add(roundKeys[0]);
        b = b.add(roundKeys[1]);

        for (int i = 1; i <= parameters.rounds(); i++) {
            a = a.xor(b).rotateLeft(b).add(roundKeys[2 * i]);
            b = b.xor(a).rotateLeft(a).add(roundKeys[2 * i + 1]);
        }

        return concatWords(a, b);
    }

    @Override
    public byte[] decrypt(byte @NotNull [] ciphertext) {
        requireNonNull(ciphertext, "ciphertext");

        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");
        if (ciphertext.length != blockSize())
            throw new IllegalArgumentException("Invalid block size");

        Word a = Word.fromByteArray(Arrays.copyOfRange(ciphertext, 0, ciphertext.length / 2));
        Word b = Word.fromByteArray(Arrays.copyOfRange(ciphertext, ciphertext.length / 2, ciphertext.length));

        for (int i = parameters.rounds(); i > 0; i--) {
            b = b.sub(roundKeys[2 * i + 1]).rotateRight(a).xor(a);
            a = a.sub(roundKeys[2 * i]).rotateRight(b).xor(b);
        }

        b = b.sub(roundKeys[1]);
        a = a.sub(roundKeys[0]);

        return concatWords(a, b);
    }

    private static byte[] concatWords(Word a, Word b) {
        byte[] aBytes = a.toByteArray();
        byte[] bBytes = b.toByteArray();

        byte[] result = new byte[aBytes.length + bBytes.length];
        System.arraycopy(aBytes, 0, result, 0, aBytes.length);
        System.arraycopy(bBytes, 0, result, aBytes.length, bBytes.length);

        return result;
    }
}

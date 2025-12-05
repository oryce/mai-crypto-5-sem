package dora.crypto.block.rc6;

import dora.crypto.block.BlockCipher;
import dora.crypto.block.Word;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public final class Rc6BlockCipher implements BlockCipher {

    private final Rc6Parameters parameters;
    private final Rc6KeySchedule keySchedule;

    private final Word one;
    private final Word two;
    private final Word base;

    private Word[] roundKeys;

    public Rc6BlockCipher(@NotNull Rc6Parameters parameters) {
        this.parameters = requireNonNull(parameters, "parameters");
        this.keySchedule = new Rc6KeySchedule(parameters);

        int wordBits = parameters.wordSize().bits();

        this.one = Word.of(1, wordBits);
        this.two = Word.of(2, wordBits);
        this.base = Word.of((long) (Math.log(wordBits) / Math.log(2)), wordBits);
    }

    @Override
    public int blockSize() {
        return parameters.wordSize().bytes() * 4;
    }

    @Override
    public void init(byte @NotNull [] key) {
        roundKeys = Arrays.stream(keySchedule.roundKeys(key))
            .map(Word::fromByteArray)
            .toArray(Word[]::new);
    }

    /* https://en.wikipedia.org/wiki/RC6#Encryption/decryption */

    @Override
    public byte[] encrypt(byte @NotNull [] plaintext) {
        requireNonNull(plaintext, "plaintext");

        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");
        if (plaintext.length != blockSize())
            throw new IllegalArgumentException("Invalid block size");

        Word[] words = splitBlock(plaintext);
        int rounds = parameters.rounds();

        // B = B + S[0]
        words[1] = words[1].add(roundKeys[0]);
        // D = D + S[1]
        words[3] = words[3].add(roundKeys[1]);

        for (int i = 1; i <= rounds; i++) {
            // t = (B * (2B + 1)) <<< lg w
            Word t = words[1].mul(two.mul(words[1]).add(one)).rotateLeft(base);
            // u = (D * (2D + 1)) <<< lg w
            Word u = words[3].mul(two.mul(words[3]).add(one)).rotateLeft(base);

            // A = ((A ^ t) <<< u) + S[2i]
            words[0] = words[0].xor(t).rotateLeft(u).add(roundKeys[2 * i]);
            // C = ((C ^ u) <<< t) + S[2i + 1]
            words[2] = words[2].xor(u).rotateLeft(t).add(roundKeys[2 * i + 1]);

            // (A, B, C, D) = (B, C, D, A)
            rotateLeft(words);
        }

        // A = A + S[2r + 2]
        words[0] = words[0].add(roundKeys[2 * rounds + 2]);
        // C = C + S[2r + 3]
        words[2] = words[2].add(roundKeys[2 * rounds + 3]);

        return joinWords(words);
    }

    @Override
    public byte[] decrypt(byte @NotNull [] ciphertext) {
        requireNonNull(ciphertext, "ciphertext");

        if (roundKeys == null)
            throw new IllegalStateException("Cipher is not initialized");
        if (ciphertext.length != blockSize())
            throw new IllegalArgumentException("Invalid block size");

        Word[] words = splitBlock(ciphertext);
        int rounds = parameters.rounds();

        // C = C - S[2r + 3]
        words[2] = words[2].sub(roundKeys[2 * rounds + 3]);
        // A = A - S[2r + 2]
        words[0] = words[0].sub(roundKeys[2 * rounds + 2]);

        for (int i = rounds; i >= 1; i--) {
            // (A, B, C, D) = (D, A, B, C)
            rotateRight(words);

            // u = (D * (2D + 1)) <<< lg w
            Word u = words[3].mul(two.mul(words[3]).add(one)).rotateLeft(base);
            // t = (B * (2B + 1)) <<< lg w
            Word t = words[1].mul(two.mul(words[1]).add(one)).rotateLeft(base);

            // C = ((C - S[2i + 1]) >>> t) ^ u
            words[2] = words[2].sub(roundKeys[2 * i + 1]).rotateRight(t).xor(u);
            // A = ((A - S[2i]) >>> u) ^ t
            words[0] = words[0].sub(roundKeys[2 * i]).rotateRight(u).xor(t);
        }

        // D = D - S[1]
        words[3] = words[3].sub(roundKeys[1]);
        // B = B - S[0]
        words[1] = words[1].sub(roundKeys[0]);

        return joinWords(words);
    }

    private Word[] splitBlock(byte[] block) {
        Word[] words = new Word[4];
        int wordSize = parameters.wordSize().bytes();

        // Assume a block is four words.
        assert block.length == words.length * wordSize : "invariant";

        for (int i = 0; i < words.length; i++) {
            byte[] wordBytes = Arrays.copyOfRange(block, i * wordSize, (i + 1) * wordSize);
            words[i] = Word.fromByteArray(wordBytes);
        }

        return words;
    }

    private byte[] joinWords(Word[] words) {
        // Assume a block has been split into four words.
        assert words.length == 4 : "invariant";

        int wordSize = parameters.wordSize().bytes();
        byte[] block = new byte[wordSize * 4];

        for (int i = 0; i < words.length; i++) {
            byte[] wordBytes = words[i].toByteArray();
            System.arraycopy(wordBytes, 0, block, i * wordSize, wordSize);
        }

        return block;
    }

    private void rotateLeft(Word[] words) {
        Word temp = words[0];

        for (int i = 1; i < words.length; i++) {
            words[i - 1] = words[i];
        }

        words[words.length - 1] = temp;
    }

    private void rotateRight(Word[] words) {
        Word temp = words[words.length - 1];

        for (int i = words.length - 1; i > 0; i--) {
            words[i] = words[i - 1];
        }

        words[0] = temp;
    }
}

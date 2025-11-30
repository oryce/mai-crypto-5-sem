package dora.crypto.block.rc5;

import dora.crypto.block.KeySchedule;
import dora.crypto.block.Word;
import dora.crypto.block.rc5.Rc5Parameters.WordSize;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class Rc5KeySchedule implements KeySchedule {

    private static final Map<WordSize, Word> P = Map.ofEntries(
        Map.entry(WordSize.WORD_SIZE_16, Word.of(0xB7E1L, 16)),
        Map.entry(WordSize.WORD_SIZE_32, Word.of(0xB7E15163L, 32)),
        Map.entry(WordSize.WORD_SIZE_64, Word.of(0xB7E151628AED2A6BL, 64))
    );

    private static final Map<WordSize, Word> Q = Map.ofEntries(
        Map.entry(WordSize.WORD_SIZE_16, Word.of(0x9E37L, 16)),
        Map.entry(WordSize.WORD_SIZE_32, Word.of(0x9E3779B9L, 32)),
        Map.entry(WordSize.WORD_SIZE_64, Word.of(0x9E3779B97F4A7C15L, 64))
    );

    private final Rc5Parameters parameters;

    public Rc5KeySchedule(@NotNull Rc5Parameters parameters) {
        this.parameters = requireNonNull(parameters, "parameters");
    }

    @Override
    public byte[][] roundKeys(byte @NotNull [] key) {
        requireNonNull(key, "key");

        if (key.length != parameters.keySize())
            throw new IllegalArgumentException("Invalid key size");

        /* https://en.wikipedia.org/wiki/RC5#Key_expansion */

        Word p = P.get(parameters.wordSize());
        Word q = Q.get(parameters.wordSize());

        int wordBits = parameters.wordSize().bits();
        int wordBytes = parameters.wordSize().bytes();
        int keyWords = Math.max(1, Math.ceilDiv(key.length, wordBytes));
        int roundKeys = 2 * (parameters.rounds() + 1);

        Word[] L = new Word[keyWords];

        for (int i = 0; i < keyWords; i++) {
            byte[] keyWordBytes = Arrays.copyOfRange(key, i * wordBytes, (i + 1) * wordBytes);
            L[i] = Word.fromByteArray(keyWordBytes);
        }

        Word[] S = new Word[roundKeys];
        S[0] = p;

        for (int i = 1; i < roundKeys; i++) {
            S[i] = S[i - 1].add(q);
        }

        Word three = Word.of(3, wordBits);
        Word A = Word.zero(wordBits);
        Word B = Word.zero(wordBits);

        int i = 0;
        int j = 0;

        for (int k = 0; k < 3 * Math.max(roundKeys, keyWords); k++) {
            A = S[i] = S[i].add(A).add(B).rotateLeft(three);
            B = L[j] = L[j].add(A).add(B).rotateLeft(A.add(B));
            i = (i + 1) % roundKeys;
            j = (j + 1) % keyWords;
        }

        return Arrays.stream(S).map(Word::toByteArray).toArray(byte[][]::new);
    }

    @Override
    public Set<Integer> keySizes() {
        return Set.of(parameters.keySize());
    }
}

package dora.crypto;

import dora.crypto.block.BlockCipher;
import dora.crypto.block.mode.*;
import dora.crypto.block.mode.CtrCipherMode.CtrParameters;
import dora.crypto.block.mode.Parameters.IvParameters;
import dora.crypto.block.mode.RandomDeltaCipherMode.RandomDeltaParameters;
import dora.crypto.block.padding.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

public final class SymmetricCipher {

    private final SymmetricCipherContext context;
    private final byte[] key;
    private final Parameters parameters;

    public SymmetricCipher(
        @NotNull BlockCipher cipher,
        @NotNull CipherModeType cipherMode,
        @NotNull PaddingType padding,
        byte @NotNull [] key,
        byte @Nullable [] iv,
        @Nullable List<Object> args,
        @Nullable ForkJoinPool pool
    ) {
        this.context = new SymmetricCipherContext(
            cipherMode.createMode(
                requireNonNull(cipher, "cipher"),
                requireNonNullElseGet(pool, ForkJoinPool::commonPool)
            ),
            padding.createPadding()
        );
        this.key = requireNonNull(key, "key");
        this.parameters = cipherMode.createParameters(
            requireNonNullElse(iv, new byte[0]),
            requireNonNullElseGet(args, Collections::emptyList)
        );
    }

    public byte[] encrypt(byte @NotNull [] data) throws InterruptedException {
        context.init(key, parameters);
        return context.encrypt(data);
    }

    public void encryptFile(@NotNull Path input, @NotNull Path output)
    throws IOException, InterruptedException {
        requireNonNull(input, "input file");
        requireNonNull(output, "output file");

        context.init(key, parameters);

        try (var fis = Files.newInputStream(input);
             var fos = Files.newOutputStream(output)) {
            context.encryptStream(fis, fos);
        }
    }

    public byte[] decrypt(byte @NotNull [] data) throws InterruptedException {
        context.init(key, parameters);
        return context.decrypt(data);
    }

    public void decryptFile(@NotNull Path input, @NotNull Path output)
    throws IOException, InterruptedException {
        requireNonNull(input, "input file");
        requireNonNull(output, "output file");

        context.init(key, parameters);

        try (var fis = Files.newInputStream(input);
             var fos = Files.newOutputStream(output)) {
            context.decryptStream(fis, fos);
        }
    }

    public enum CipherModeType {

        CBC(CbcCipherMode::new, ParameterCreator.iv()),
        CFB(CfbCipherMode::new, ParameterCreator.iv()),
        CTR(CtrCipherMode::new, ParameterCreator.ctr()),
        ECB(EcbCipherMode::new, ParameterCreator.none()),
        OFB((cipher, pool) -> new OfbCipherMode(cipher), ParameterCreator.iv()),
        PCBC((cipher, pool) -> new PcbcCipherMode(cipher), ParameterCreator.iv()),
        RANDOM_DELTA(RandomDeltaCipherMode::new, ParameterCreator.randomDelta());

        private final InstanceCreator instanceCreator;
        private final ParameterCreator parametersCreator;

        CipherModeType(
            InstanceCreator instanceCreator,
            ParameterCreator parametersCreator
        ) {
            this.instanceCreator = instanceCreator;
            this.parametersCreator = parametersCreator;
        }

        public CipherMode createMode(BlockCipher cipher, ForkJoinPool pool) {
            return instanceCreator.create(cipher, pool);
        }

        public Parameters createParameters(byte[] iv, List<?> args) {
            return parametersCreator.create(iv, args);
        }

        @FunctionalInterface
        private interface InstanceCreator {

            CipherMode create(BlockCipher cipher, ForkJoinPool pool);
        }

        @FunctionalInterface
        private interface ParameterCreator {

            Parameters create(byte[] iv, List<?> args);

            static ParameterCreator none() {
                return (iv, args) -> Parameters.NO_PARAMETERS;
            }

            static ParameterCreator iv() {
                return (iv, args) -> new IvParameters(iv);
            }

            static ParameterCreator ctr() {
                return (iv, args) -> new CtrParameters(
                    /* nonce   */ iv,
                    /* counter */ requireNonNullElse(argumentAt(args, 0), 0)
                );
            }

            static ParameterCreator randomDelta() {
                return (iv, args) -> new RandomDeltaParameters(
                    /* nonce   */ iv,
                    /* counter */ requireNonNullElse(argumentAt(args, 0), 0),
                    /* seed    */ argumentAt(args, 1)
                );
            }

            @SuppressWarnings("unchecked")
            private static <T> @Nullable T argumentAt(List<?> args, int idx) {
                if (args.size() < idx) return null;
                return (T) args.get(idx);
            }
        }
    }

    public enum PaddingType {

        ANSI_X923(AnsiX923Padding::new),
        ISO_10126(Iso10126Padding::new),
        PKCS7(Pkcs7Padding::new),
        ZEROS(ZerosPadding::new);

        private final Supplier<Padding> creator;

        PaddingType(Supplier<Padding> creator) {
            this.creator = creator;
        }

        public Padding createPadding() {
            return creator.get();
        }
    }

    //region Builder
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private BlockCipher blockCipher;
        private CipherModeType cipherMode;
        private PaddingType padding;
        private byte[] key;
        private byte[] iv;
        private List<Object> args = new ArrayList<>();
        private ForkJoinPool pool;

        public Builder cipher(BlockCipher blockCipher) {
            this.blockCipher = blockCipher;
            return this;
        }

        public Builder mode(CipherModeType cipherMode) {
            this.cipherMode = cipherMode;
            return this;
        }

        public Builder padding(PaddingType padding) {
            this.padding = padding;
            return this;
        }

        public Builder key(byte[] key) {
            this.key = key;
            return this;
        }

        public Builder iv(byte[] iv) {
            this.iv = iv;
            return this;
        }

        public Builder arguments(List<Object> args) {
            this.args = args;
            return this;
        }

        public Builder arguments(Object... args) {
            this.args = Arrays.asList(args);
            return this;
        }

        public <T> Builder argument(T arg) {
            this.args.add(arg);
            return this;
        }

        public Builder pool(ForkJoinPool pool) {
            this.pool = pool;
            return this;
        }

        public SymmetricCipher build() {
            return new SymmetricCipher(
                blockCipher,
                cipherMode,
                padding,
                key,
                iv,
                args,
                pool
            );
        }
    }
    //endregion
}

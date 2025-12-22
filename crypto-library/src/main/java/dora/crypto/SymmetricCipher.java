package dora.crypto;

import dora.crypto.block.BlockCipher;
import dora.crypto.block.mode.CbcCipherMode;
import dora.crypto.block.mode.CfbCipherMode;
import dora.crypto.block.mode.CipherMode;
import dora.crypto.block.mode.CtrCipherMode;
import dora.crypto.block.mode.CtrCipherMode.CtrParameters;
import dora.crypto.block.mode.EcbCipherMode;
import dora.crypto.block.mode.OfbCipherMode;
import dora.crypto.block.mode.Parameters;
import dora.crypto.block.mode.Parameters.IvParameters;
import dora.crypto.block.mode.PcbcCipherMode;
import dora.crypto.block.mode.RandomDeltaCipherMode;
import dora.crypto.block.mode.RandomDeltaCipherMode.RandomDeltaParameters;
import dora.crypto.block.padding.AnsiX923Padding;
import dora.crypto.block.padding.Iso10126Padding;
import dora.crypto.block.padding.Padding;
import dora.crypto.block.padding.Pkcs7Padding;
import dora.crypto.block.padding.ZerosPadding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    public @NotNull InputStream encryptingInputStream(@NotNull InputStream stream) {
        context.init(key, parameters);
        return context.encryptingInputStream(stream);
    }

    public @NotNull OutputStream encryptingOutputStream(@NotNull OutputStream stream) {
        context.init(key, parameters);
        return context.encryptingOutputStream(stream);
    }

    public void encryptFile(@NotNull Path input, @NotNull Path output) throws IOException {
        requireNonNull(input, "input file");
        requireNonNull(output, "output file");

        try (var fis = Files.newInputStream(input);
             var fos = encryptingOutputStream(Files.newOutputStream(output))) {
            fis.transferTo(fos);
        }
    }

    public byte[] decrypt(byte @NotNull [] data) throws InterruptedException {
        context.init(key, parameters);
        return context.decrypt(data);
    }

    public @NotNull InputStream decryptingInputStream(@NotNull InputStream stream) {
        context.init(key, parameters);
        return context.decryptingInputStream(stream);
    }

    public @NotNull OutputStream decryptingOutputStream(@NotNull OutputStream stream) {
        context.init(key, parameters);
        return context.decryptingOutputStream(stream);
    }

    public void decryptFile(@NotNull Path input, @NotNull Path output) throws IOException {
        requireNonNull(input, "input file");
        requireNonNull(output, "output file");

        try (var fis = decryptingInputStream(Files.newInputStream(input));
             var fos = Files.newOutputStream(output)) {
            fis.transferTo(fos);
        }
    }

    public enum CipherModeType {

        CBC(CbcCipherMode::new, ParameterCreator.iv()),
        CFB(CfbCipherMode::new, ParameterCreator.iv()),
        CTR(CtrCipherMode::new, ParameterCreator.ctr()),
        ECB(EcbCipherMode::new, ParameterCreator.none()),
        OFB((cipher, pool) -> new OfbCipherMode(cipher), ParameterCreator.iv()),
        PCBC(PcbcCipherMode::new, ParameterCreator.iv()),
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
                if (idx >= args.size()) return null;
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

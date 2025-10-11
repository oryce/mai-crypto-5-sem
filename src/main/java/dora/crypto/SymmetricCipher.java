package dora.crypto;

import dora.crypto.block.BlockCipher;
import dora.crypto.mode.CbcCipherMode;
import dora.crypto.mode.CfbCipherMode;
import dora.crypto.mode.CipherMode;
import dora.crypto.mode.CtrCipherMode;
import dora.crypto.mode.EcbCipherMode;
import dora.crypto.mode.OfbCipherMode;
import dora.crypto.mode.Parameters;
import dora.crypto.mode.Parameters.IvParameters;
import dora.crypto.mode.PcbcCipherMode;
import dora.crypto.mode.RandomDeltaCipherMode;
import dora.crypto.padding.AnsiX923Padding;
import dora.crypto.padding.Iso10126Padding;
import dora.crypto.padding.Padding;
import dora.crypto.padding.Pkcs7Padding;
import dora.crypto.padding.ZerosPadding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

public final class SymmetricCipher {

    private final SymmetricCipherContext context;
    private final Parameters parameters;
    private final byte[] key;

    public SymmetricCipher(
        @NotNull BlockCipher cipher,
        @NotNull CipherModeType cipherMode,
        @NotNull PaddingType padding,
        byte @NotNull [] key,
        byte @Nullable [] iv,
        @Nullable List<?> args,
        @Nullable ForkJoinPool pool
    ) {
        this.context = new SymmetricCipherContext(
            cipherMode.createMode(cipher, requireNonNullElseGet(pool, ForkJoinPool::commonPool)),
            padding.createPadding()
        );
        this.parameters = cipherMode.createParameters(
            requireNonNullElse(iv, new byte[0]),
            requireNonNullElseGet(args, Collections::emptyList)
        );
        this.key = requireNonNull(key, "key");
    }

    public byte[] encrypt(byte[] data) throws InterruptedException {
        context.init(key, parameters);
        return context.encrypt(data);
    }

    public void encryptFile(Path input, Path output)
    throws InterruptedException, IOException {
        context.init(key, parameters);

        try (var fis = Files.newInputStream(input);
             var fos = Files.newOutputStream(output)) {
            var buffer = new byte[65536];
            var bytesRead = 0;

            while ((bytesRead = fis.read(buffer)) > 0) {
                fos.write(context.encrypt(buffer), 0, bytesRead);
            }
        }
    }

    public byte[] decrypt(byte[] data) throws InterruptedException {
        context.init(key, parameters);
        return context.decrypt(data);
    }

    public void decryptFile(Path input, Path output)
    throws InterruptedException, IOException {
        context.init(key, parameters);

        try (var fis = Files.newInputStream(input);
             var fos = Files.newOutputStream(output)) {
            var buffer = new byte[65536];
            var bytesRead = 0;

            while ((bytesRead = fis.read(buffer)) > 0) {
                fos.write(context.decrypt(buffer), 0, bytesRead);
            }
        }
    }

    public enum CipherModeType {

        CBC(CbcCipherMode::new, ParameterCreator.iv()),
        CFB(CfbCipherMode::new, ParameterCreator.iv()),
        CTR(CtrCipherMode::new, ParameterCreator.iv()),
        ECB(EcbCipherMode::new, ParameterCreator.none()),
        OFB((cipher, pool) -> new OfbCipherMode(cipher), ParameterCreator.iv()),
        PCBC((cipher, pool) -> new PcbcCipherMode(cipher), ParameterCreator.iv()),
        RANDOM_DELTA((cipher, pool) -> new RandomDeltaCipherMode(cipher), ParameterCreator.iv());

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
        }
    }

    public enum PaddingType {

        ANSI_X293(AnsiX923Padding::new),
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
}

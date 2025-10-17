package dora.crypto;

import dora.crypto.SymmetricCipher.CipherModeType;
import dora.crypto.SymmetricCipher.PaddingType;
import dora.crypto.block.deal.DealBlockCipher;
import dora.crypto.block.des.DesBlockCipher;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.constraints.Size;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class SymmetricCipherTest {

    @Example
    void decryptFile_DES_CBC_AnsiX923Padding(
        @ForAll @Size(value = 8) byte[] key,
        @ForAll @Size(value = 8) byte[] iv
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DesBlockCipher())
                .mode(CipherModeType.CBC)
                .padding(PaddingType.ANSI_X923)
                .key(key)
                .iv(iv)
                .build()
        );
    }

    @Example
    void decryptFile_DES_CFB_Pkcs7Padding(
        @ForAll @Size(value = 8) byte[] key,
        @ForAll @Size(value = 8) byte[] iv
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DesBlockCipher())
                .mode(CipherModeType.CFB)
                .padding(PaddingType.PKCS7)
                .key(key)
                .iv(iv)
                .build()
        );
    }

    @Example
    void decryptFile_DES_CTR_AnsiX923Padding(
        @ForAll @Size(value = 8) byte[] key,
        @ForAll @Size(value = 4) byte[] nonce,
        @ForAll @Positive int counter
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DesBlockCipher())
                .mode(CipherModeType.CTR)
                .padding(PaddingType.ANSI_X923)
                .key(key)
                .iv(nonce)
                .argument(counter)
                .build()
        );
    }

    @Example
    void decryptFile_DES_ECB_Iso10126Padding(
        @ForAll @Size(value = 8) byte[] key
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DesBlockCipher())
                .mode(CipherModeType.ECB)
                .padding(PaddingType.ISO_10126)
                .key(key)
                .build()
        );
    }

    @Example
    void decryptFile_DES_OFB_Pkcs7Padding(
        @ForAll @Size(value = 8) byte[] key,
        @ForAll @Size(value = 8) byte[] iv
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DesBlockCipher())
                .mode(CipherModeType.OFB)
                .padding(PaddingType.PKCS7)
                .key(key)
                .iv(iv)
                .build()
        );
    }

    @Example
    void decryptFile_DES_PCBC_AnsiX923Padding(
        @ForAll @Size(value = 8) byte[] key,
        @ForAll @Size(value = 8) byte[] iv
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DesBlockCipher())
                .mode(CipherModeType.PCBC)
                .padding(PaddingType.ANSI_X923)
                .key(key)
                .iv(iv)
                .build()
        );
    }

    @Example
    void decryptFile_DES_RandomDelta_Iso10126Padding(
        @ForAll @Size(value = 8) byte[] key,
        @ForAll @Size(value = 4) byte[] nonce,
        @ForAll @Positive int counter,
        @ForAll long seed
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DesBlockCipher())
                .mode(CipherModeType.RANDOM_DELTA)
                .padding(PaddingType.ISO_10126)
                .key(key)
                .iv(nonce)
                .arguments(counter, seed)
                .build()
        );
    }

    @Example
    void decryptFile_DEAL_CBC_AnsiX923Padding(
        @ForAll @Size(value = 16) byte[] key,
        @ForAll @Size(value = 16) byte[] iv,
        @ForAll @Size(value = 8) byte[] desKey
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DealBlockCipher(desKey))
                .mode(CipherModeType.CBC)
                .padding(PaddingType.ANSI_X923)
                .key(key)
                .iv(iv)
                .build()
        );
    }

    @Example
    void decryptFile_DEAL_CFB_Iso10126Padding(
        @ForAll @Size(value = 16) byte[] key,
        @ForAll @Size(value = 16) byte[] iv,
        @ForAll @Size(value = 8) byte[] desKey
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DealBlockCipher(desKey))
                .mode(CipherModeType.CFB)
                .padding(PaddingType.ISO_10126)
                .key(key)
                .iv(iv)
                .build()
        );
    }

    @Example
    void decryptFile_DEAL_CTR_Pkcs7Padding(
        @ForAll @Size(value = 16) byte[] key,
        @ForAll @Size(value = 8) byte[] nonce,
        @ForAll @Positive int counter,
        @ForAll @Size(value = 8) byte[] desKey
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DealBlockCipher(desKey))
                .mode(CipherModeType.CTR)
                .padding(PaddingType.ISO_10126)
                .key(key)
                .iv(nonce)
                .argument(counter)
                .build()
        );
    }

    @Example
    void decryptFile_DEAL_ECB_AnsiX923Padding(
        @ForAll @Size(value = 16) byte[] key,
        @ForAll @Size(value = 8) byte[] desKey
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DealBlockCipher(desKey))
                .mode(CipherModeType.ECB)
                .padding(PaddingType.ANSI_X923)
                .key(key)
                .build()
        );
    }

    @Example
    void decryptFile_DEAL_OFB_Iso10126Padding(
        @ForAll @Size(value = 16) byte[] key,
        @ForAll @Size(value = 16) byte[] iv,
        @ForAll @Size(value = 8) byte[] desKey
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DealBlockCipher(desKey))
                .mode(CipherModeType.OFB)
                .padding(PaddingType.ISO_10126)
                .key(key)
                .iv(iv)
                .build()
        );
    }

    @Example
    void decryptFile_DEAL_PCBC_Pkcs7Padding(
        @ForAll @Size(value = 16) byte[] key,
        @ForAll @Size(value = 16) byte[] iv,
        @ForAll @Size(value = 8) byte[] desKey
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DealBlockCipher(desKey))
                .mode(CipherModeType.PCBC)
                .padding(PaddingType.PKCS7)
                .key(key)
                .iv(iv)
                .build()
        );
    }

    @Example
    void decryptFile_DEAL_RandomDelta_AnsiX923Padding(
        @ForAll @Size(value = 16) byte[] key,
        @ForAll @Size(value = 8) byte[] nonce,
        @ForAll @Positive int counter,
        @ForAll long seed,
        @ForAll @Size(value = 8) byte[] desKey
    ) throws IOException, InterruptedException {
        decryptFileTests(
            SymmetricCipher.builder()
                .cipher(new DealBlockCipher(desKey))
                .mode(CipherModeType.RANDOM_DELTA)
                .padding(PaddingType.ANSI_X923)
                .key(key)
                .iv(nonce)
                .arguments(counter, seed)
                .build()
        );
    }

    private void decryptFileTests(SymmetricCipher cipher)
    throws IOException, InterruptedException {
        decryptFileTest(cipher, "/allocator_red_black_tree_tests.cpp");
        decryptFileTest(cipher, "/code_pen.jpg");
        decryptFileTest(cipher, "/wireshark.jpg");
    }

    private void decryptFileTest(SymmetricCipher cipher, String resourcePath)
    throws IOException, InterruptedException {
        Path inputFile = null;
        Path encryptedFile = null;
        Path decryptedFile = null;

        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) throw new IOException("Resource not found");

            inputFile = Files.createTempFile("input", null);
            encryptedFile = Files.createTempFile("encrypted", null);
            decryptedFile = Files.createTempFile("decrypted", null);

            byte[] inputBytes = stream.readAllBytes();
            Files.write(inputFile, inputBytes);

            cipher.encryptFile(inputFile, encryptedFile);
            cipher.decryptFile(encryptedFile, decryptedFile);

            byte[] outputBytes = Files.readAllBytes(decryptedFile);
            assertThat(inputBytes).isEqualTo(outputBytes);
        } finally {
            if (inputFile != null) Files.deleteIfExists(inputFile);
            if (encryptedFile != null) Files.deleteIfExists(encryptedFile);
            if (decryptedFile != null) Files.deleteIfExists(decryptedFile);
        }
    }
}

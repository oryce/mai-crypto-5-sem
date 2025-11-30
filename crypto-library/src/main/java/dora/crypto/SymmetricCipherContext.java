package dora.crypto;

import dora.crypto.block.mode.CipherMode;
import dora.crypto.block.mode.Parameters;
import dora.crypto.block.padding.Padding;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public final class SymmetricCipherContext {

    private final CipherMode cipherMode;
    private final Padding padding;

    public SymmetricCipherContext(
        @NotNull CipherMode cipherMode,
        @NotNull Padding padding
    ) {
        this.cipherMode = requireNonNull(cipherMode, "cipher mode");
        this.padding = requireNonNull(padding, "padding");
    }

    public void init(byte @NotNull[] key, @NotNull Parameters parameters) {
        cipherMode.init(
            requireNonNull(key, "key"),
            requireNonNull(parameters, "parameters")
        );
    }

    public byte[] encrypt(byte @NotNull [] data) throws InterruptedException {
        byte[] padded = padding.pad(requireNonNull(data, "data"), cipherMode.blockSize());
        return cipherMode.encrypt(padded);
    }

    public byte[] decrypt(byte @NotNull[] data) throws InterruptedException {
        byte[] decrypted = cipherMode.decrypt(requireNonNull(data, "data"));
        return padding.unpad(decrypted, cipherMode.blockSize());
    }

    public void encryptStream(
        @NotNull InputStream inputStream,
        @NotNull OutputStream outputStream
    ) throws IOException, InterruptedException {
        processStream(inputStream, outputStream, true);
    }

    public void decryptStream(
        @NotNull InputStream inputStream,
        @NotNull OutputStream outputStream
    ) throws IOException, InterruptedException {
        processStream(inputStream, outputStream, false);
    }

    private void processStream(
        @NotNull InputStream inputStream,
        @NotNull OutputStream outputStream,
        boolean encrypt
    ) throws IOException, InterruptedException {
        requireNonNull(inputStream, "input stream");
        requireNonNull(outputStream, "output stream");

        int blockSize = cipherMode.blockSize();

        // Read large amounts of data to take advantage of parallel processing.
        byte[] buffer = new byte[524288];
        byte[] remainder = new byte[blockSize];
        int remainderLen = 0;

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            if (bytesRead == 0) continue;

            // Combine the leftovers of the previous chunk with the new data.
            byte[] combined = new byte[remainderLen + bytesRead];
            System.arraycopy(remainder, 0, combined, 0, remainderLen);
            System.arraycopy(buffer, 0, combined, remainderLen, bytesRead);

            // For encryption, process all complete blocks. For decryption,
            // hold back one block to handle padding at EOF.
            int blocks = combined.length / blockSize;
            int processBlocks = encrypt ? blocks : Math.max(0, blocks - 1);
            int processLen = processBlocks * blockSize;

            if (processLen > 0) {
                byte[] chunk = Arrays.copyOf(combined, processLen);
                byte[] processed = encrypt
                    ? cipherMode.encrypt(chunk)
                    : cipherMode.decrypt(chunk);
                outputStream.write(processed);
            }

            // Save the rest for later.
            remainderLen = combined.length - processLen;
            System.arraycopy(combined, processLen, remainder, 0, remainderLen);
        }

        // Process remaining data.
        byte[] result = Arrays.copyOf(remainder, remainderLen);

        if (encrypt) {
            result = padding.pad(result, blockSize);
            result = cipherMode.encrypt(result);
        } else {
            result = cipherMode.decrypt(result);
            result = padding.unpad(result, blockSize);
        }

        outputStream.write(result);
    }
}

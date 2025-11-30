package dora.crypto;

import dora.crypto.block.mode.CipherMode;
import dora.crypto.block.padding.Padding;
import org.jetbrains.annotations.NotNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class CipherOutputStream extends FilterOutputStream {

    private final CipherMode cipherMode;
    private final Padding padding;
    private final int blockSize;

    /** Whether data should be encrypted or decrypted. */
    private final boolean encrypt;

    /** Remaining blocks to be processed. */
    private final byte[] remainder;
    private int remainderLen;

    /** Whether the stream is closed. */
    private boolean closed;

    CipherOutputStream(
        @NotNull CipherMode cipherMode,
        @NotNull Padding padding,
        @NotNull OutputStream stream,
        boolean encrypt
    ) {
        super(stream);

        this.cipherMode = requireNonNull(cipherMode, "cipher mode");
        this.padding = requireNonNull(padding, "padding");
        this.encrypt = encrypt;

        blockSize = cipherMode.blockSize();
        remainder = new byte[blockSize * 2];
    }

    @Override
    public void write(int b) throws IOException {
        byte[] oneByte = { (byte) b };
        write(oneByte, 0, 1);
    }

    @Override
    public void write(byte @NotNull [] buffer, int offset, int length) throws IOException {
        requireNonNull(buffer, "buffer");

        Objects.checkFromIndexSize(offset, length, buffer.length);
        if (length == 0) return;
        if (closed) throw new IOException("Stream closed");

        // Combine remainder with the read data.
        byte[] combined = new byte[remainderLen + length];
        System.arraycopy(remainder, 0, combined, 0, remainderLen);
        System.arraycopy(buffer, offset, combined, remainderLen, length);

        // For encryption, process all complete blocks.
        // For decryption, hold back one block to handle padding at EOF.
        int blocks = combined.length / blockSize;
        int processBlocks = encrypt ? blocks : Math.max(0, blocks - 1);
        int processLen = processBlocks * blockSize;

        if (processLen > 0) {
            byte[] chunk = Arrays.copyOf(combined, processLen);
            byte[] processed;

            try {
                processed = encrypt ? cipherMode.encrypt(chunk) : cipherMode.decrypt(chunk);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Cipher operation interrupted", e);
            }

            out.write(processed);
        }

        // Process the rest later.
        remainderLen = combined.length - processLen;
        System.arraycopy(combined, processLen, remainder, 0, remainderLen);
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }

        closed = true;

        // Process the remainder
        byte[] result = Arrays.copyOf(remainder, remainderLen);
        try {
            if (encrypt) {
                result = padding.pad(result, blockSize);
                result = cipherMode.encrypt(result);
            } else {
                result = cipherMode.decrypt(result);
                result = padding.unpad(result, blockSize);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Cipher operation interrupted", e);
        }

        out.write(result);
        out.flush();
        out.close();
    }
}

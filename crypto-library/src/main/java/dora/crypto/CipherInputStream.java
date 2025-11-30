package dora.crypto;

import dora.crypto.block.mode.CipherMode;
import dora.crypto.block.padding.Padding;
import org.jetbrains.annotations.NotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class CipherInputStream extends FilterInputStream {

    private final CipherMode cipherMode;
    private final Padding padding;
    private final int blockSize;

    /** Whether data should be encrypted or decrypted. */
    private final boolean encrypt;

    /** Input buffer. Data is read from this buffer. */
    private final byte[] inputBuffer = new byte[524288];

    /** Output buffer. Data is written to this buffer. */
    private byte[] outputBuffer = new byte[0];
    /** How much of the output buffer has been consumed. */
    private int outputOffset;

    /** Remaining blocks to be processed. */
    private final byte[] remainder;
    private int remainderLen;

    /** Whether there's no more data to be read. */
    private boolean eof;
    /** Whether the stream is closed. */
    private boolean closed;

    CipherInputStream(
        @NotNull CipherMode cipherMode,
        @NotNull Padding padding,
        @NotNull InputStream stream,
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
    public int read() throws IOException {
        byte[] oneByte = new byte[1];

        int read = read(oneByte, 0, 1);
        if (read == -1) return -1;

        return oneByte[0] & 0xff;
    }

    @Override
    public int read(byte @NotNull [] buffer, int offset, int length) throws IOException {
        requireNonNull(buffer, "buffer");

        Objects.checkFromIndexSize(offset, length, buffer.length);
        if (length == 0) return 0;
        if (closed) throw new IOException("Stream closed");

        int totalRead = 0;

        while (length > 0) {
            if (outputOffset >= outputBuffer.length) {
                if (!fillBuffer()) {
                    // No more data to be read.
                    return totalRead == 0 ? -1 : totalRead;
                }
            }

            // Copy `length` bytes (or what's been read) into the buffer.
            int toCopy = Math.min(length, outputBuffer.length - outputOffset);
            System.arraycopy(outputBuffer, outputOffset, buffer, offset, toCopy);

            outputOffset += toCopy;
            offset += toCopy;
            length -= toCopy;
            totalRead += toCopy;
        }

        return totalRead;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            in.close();
        }
    }

    private boolean fillBuffer() throws IOException {
        outputBuffer = new byte[0];
        outputOffset = 0;

        if (eof) {
            return false;
        }

        while (true) {
            int bytesRead = in.read(inputBuffer);

            if (bytesRead == -1) {
                // No data to be read. Process the remainder.
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

                outputBuffer = result;
                eof = true;

                return outputBuffer.length > 0;
            }

            if (bytesRead == 0) {
                continue;
            }

            // Combine remainder with the read data.
            byte[] combined = new byte[remainderLen + bytesRead];
            System.arraycopy(remainder, 0, combined, 0, remainderLen);
            System.arraycopy(inputBuffer, 0, combined, remainderLen, bytesRead);

            // For encryption, process all complete blocks.
            // For decryption, hold back one block to handle padding at EOF.
            int blocks = combined.length / blockSize;
            int processBlocks = encrypt ? blocks : Math.max(0, blocks - 1);
            int processLen = processBlocks * blockSize;

            if (processLen > 0) {
                byte[] chunk = Arrays.copyOf(combined, processLen);

                try {
                    outputBuffer = encrypt ? cipherMode.encrypt(chunk) : cipherMode.decrypt(chunk);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Cipher operation interrupted", e);
                }

                remainderLen = combined.length - processLen;
                System.arraycopy(combined, processLen, remainder, 0, remainderLen);

                return true;
            }

            remainderLen = combined.length;
            System.arraycopy(combined, 0, remainder, 0, remainderLen);
        }
    }
}

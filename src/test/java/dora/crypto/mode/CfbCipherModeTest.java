package dora.crypto.mode;

import dora.crypto.block.BlockCipher;
import dora.crypto.mode.Parameters.IvParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CfbCipherModeTest {

    private BlockCipher cipher;
    private CipherMode cipherMode;
    private byte[] key;
    private byte[] iv;

    @BeforeEach
    public void setUp() {
        cipher = new MockBlockCipher(8);
        cipherMode = new CfbCipherMode(cipher, ForkJoinPool.commonPool());
        key = new byte[] { 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11 };
        iv = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };

        cipher.init(key);
        cipherMode.init(new IvParameters(iv));
    }

    @Test
    public void testInit_withInvalidParameters_throwsException() {
        CipherMode mode = new CfbCipherMode(cipher, ForkJoinPool.commonPool());

        assertThatThrownBy(() -> mode.init(Parameters.NO_PARAMETERS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("expected IvParameters");
    }

    @Test
    public void testEncrypt_emptyArray()
    throws InterruptedException {
        byte[] plaintext = new byte[0];
        byte[] ciphertext = cipherMode.encrypt(plaintext, key);
        assertThat(ciphertext).isEmpty();
    }

    @Test
    public void testEncrypt_singleBlock()
    throws InterruptedException {
        byte[] plaintext = new byte[] { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48 };

        byte[] ciphertext = cipherMode.encrypt(plaintext, key);
        assertThat(ciphertext).hasSize(8).isNotEqualTo(plaintext);

        byte[] encryptedIv = cipher.encrypt(iv);
        byte[] expectedCiphertext = xor(encryptedIv, plaintext);
        assertThat(ciphertext).isEqualTo(expectedCiphertext);
    }

    @Test
    public void testEncrypt_multipleBlocks()
    throws InterruptedException {
        byte[] plaintext = new byte[] {
            0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48,
            0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50
        };

        byte[] ciphertext = cipherMode.encrypt(plaintext, key);
        assertThat(ciphertext).hasSize(16).isNotEqualTo(plaintext);

        byte[] block1 = Arrays.copyOfRange(plaintext, 0, 8);
        byte[] encryptedIv = cipher.encrypt(iv);
        byte[] expectedC1 = xor(encryptedIv, block1);
        assertThat(Arrays.copyOfRange(ciphertext, 0, 8)).isEqualTo(expectedC1);

        byte[] block2 = Arrays.copyOfRange(plaintext, 8, 16);
        byte[] encryptedC1 = cipher.encrypt(expectedC1);
        byte[] expectedC2 = xor(encryptedC1, block2);
        assertThat(Arrays.copyOfRange(ciphertext, 8, 16)).isEqualTo(expectedC2);
    }

    @Test
    public void testEncrypt_withDifferentIv_producesDifferentCiphertext()
    throws InterruptedException {
        byte[] plaintext = new byte[] { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48 };
        byte[] iv1 = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        byte[] iv2 = new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18 };

        CipherMode mode1 = new CfbCipherMode(cipher, ForkJoinPool.commonPool());
        mode1.init(new IvParameters(iv1));
        cipher.init(key);
        byte[] ciphertext1 = mode1.encrypt(plaintext, key);

        CipherMode mode2 = new CfbCipherMode(cipher, ForkJoinPool.commonPool());
        mode2.init(new IvParameters(iv2));
        cipher.init(key);
        byte[] ciphertext2 = mode2.encrypt(plaintext, key);

        assertThat(ciphertext1).isNotEqualTo(ciphertext2);
    }

    @Test
    public void testDecrypt_emptyArray()
    throws InterruptedException {
        byte[] ciphertext = new byte[0];
        byte[] plaintext = cipherMode.decrypt(ciphertext, key);
        assertThat(plaintext).isEmpty();
    }

    @Test
    public void testDecrypt_singleBlock()
    throws InterruptedException {
        byte[] plaintext = new byte[] { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48 };

        byte[] ciphertext = cipherMode.encrypt(plaintext, key);
        byte[] decrypted = cipherMode.decrypt(ciphertext, key);
        assertThat(decrypted).isEqualTo(plaintext);
        
        byte[] encryptedIv = cipher.encrypt(iv);
        byte[] expectedDecrypted = xor(encryptedIv, ciphertext);
        assertThat(decrypted).isEqualTo(expectedDecrypted);
    }

    @Test
    public void testDecrypt_multipleBlocks()
    throws InterruptedException {
        byte[] plaintext = new byte[] {
            0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48,
            0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50
        };

        byte[] ciphertext = cipherMode.encrypt(plaintext, key);
        byte[] decrypted = cipherMode.decrypt(ciphertext, key);
        assertThat(decrypted).isEqualTo(plaintext);
        
        byte[] c1 = Arrays.copyOfRange(ciphertext, 0, 8);
        byte[] encryptedIv = cipher.encrypt(iv);
        byte[] expectedP1 = xor(encryptedIv, c1);
        assertThat(Arrays.copyOfRange(decrypted, 0, 8)).isEqualTo(expectedP1);
        
        byte[] c2 = Arrays.copyOfRange(ciphertext, 8, 16);
        byte[] encryptedC1 = cipher.encrypt(c1);
        byte[] expectedP2 = xor(encryptedC1, c2);
        assertThat(Arrays.copyOfRange(decrypted, 8, 16)).isEqualTo(expectedP2);
    }

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
    }
}

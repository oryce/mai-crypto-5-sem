package dora.crypto;

public final class Permutations {

    private Permutations() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Permutes bits across the input array in-place according to the permutation box. The input
     * array is treated as a contiguous sequence of bits.
     *
     * @param input        input array
     * @param pBox         resulting bit order
     * @param reverseOrder whether the bits are indexed right-to-left
     * @param oneIndexed   whether the bits are one-indexed
     */
    public static void permute(
        byte[] input,
        int[] pBox,
        boolean reverseOrder,
        boolean oneIndexed
    ) {
        int inputBits = input.length * Byte.SIZE;

        if (inputBits != pBox.length) {
            throw new IllegalArgumentException("P-box size does not match input size");
        }

        byte[] output = new byte[input.length];

        for (int dstIdx = 0; dstIdx < pBox.length; dstIdx++) {
            // `srcIdx` is the bit index from left to right.
            int srcIdx = pBox[dstIdx];
            if (oneIndexed) srcIdx--;
            if (reverseOrder) srcIdx = inputBits - srcIdx - 1;

            // Convert from MSB-first bit positions (in bytes) to LSB-first.
            int srcByte = srcIdx / Byte.SIZE, srcBit = Byte.SIZE - srcIdx % Byte.SIZE - 1;
            int dstByte = dstIdx / Byte.SIZE, dstBit = Byte.SIZE - dstIdx % Byte.SIZE - 1;
            boolean bitValue = (input[srcByte] & (1 << srcBit)) != 0;

            if (bitValue) {
                output[dstByte] |= (byte) (1 << dstBit);
            }
        }

        System.arraycopy(output, 0, input, 0, input.length);
    }
}

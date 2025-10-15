package dora.crypto;

public final class Permutations {

    private Permutations() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Permutes bits across the input array according to the permutation box. The input
     * array is treated as a contiguous sequence of bits.
     * <p>
     * The permutation box may contain less or more bits than in the input array, in which
     * case it's called a "compression" or "expansion" box. If the bits count match, the box
     * is "straight".
     *
     * @param input        input array
     * @param pBox         resulting bit order
     * @param reverseOrder whether the bits are indexed right-to-left
     * @param oneIndexed   whether the bits are one-indexed
     */
    public static byte[] permute(
        byte[] input,
        int[] pBox,
        boolean reverseOrder,
        boolean oneIndexed
    ) {
        int inputBits = input.length * Byte.SIZE;
        int outputBytes = Math.ceilDiv(pBox.length, 8); // round up

        byte[] output = new byte[outputBytes];

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

        return output;
    }
}

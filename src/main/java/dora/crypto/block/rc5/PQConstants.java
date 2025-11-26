package dora.crypto.block.rc5;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

public class PQConstants {

    private static final Map<Integer, byte[][]> constantMap = Map.of(
            16, new byte[][] {new byte[]{(byte) 0xB7, (byte) 0xE1},
                    new byte[] {(byte) 0x9E, 0x37}},

            32, new byte[][] {new byte[]{(byte) 0xB7, (byte) 0xE1, (byte) 0x51, (byte) 0x63},
                    new byte[] {(byte) 0x9E, (byte) 0x37, (byte) 0x79, (byte) 0xB9}},

            64, new byte[][] {new byte[]{
                    (byte) 0xB7, (byte) 0xE1, (byte) 0x51, (byte) 0x62,
                    (byte) 0x8A, (byte) 0xED, (byte) 0x2A, (byte) 0x6B
                    },
                    new byte[] {
                            (byte) 0x9E, (byte) 0x37, (byte) 0x79, (byte) 0xB9,
                            (byte) 0x7F, (byte) 0x4A, (byte) 0x7C, (byte) 0x15
            }}
    );

    private final byte[] p;
    private final byte[] q;

    public byte[] p() {return this.p;}
    public byte[] q() {return this.q;}

    public PQConstants(int w){
        var val = constantMap.get(w);

        if (val == null){
            throw new IllegalArgumentException("Incorrect w");
        }

        p = Arrays.copyOf(val[0], val[0].length);
        q = Arrays.copyOf(val[1], val[1].length);
    }
}

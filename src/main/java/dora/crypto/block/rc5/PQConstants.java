package dora.crypto.block.rc5;

import java.math.BigInteger;
import java.util.Map;

public class PQConstants {

    private static final Map<Integer, BigInteger[]> constantMap = Map.of(
            16, new BigInteger[] {new BigInteger("B7E1", 16), new BigInteger("9E37", 16)},
            32, new BigInteger[]  {new BigInteger("B7E15163", 16), new BigInteger("9E3779B9", 16)},
            64, new BigInteger[] {new BigInteger("B7E151628AED2A6B", 16), new BigInteger("9E3779B97F4A7C15", 16)}
    );

    private final BigInteger p;
    private final BigInteger q;

    public BigInteger p() {return this.p;}
    public BigInteger q() {return this.q;}

    public PQConstants(int w){
        var val = constantMap.get(w);

        if (val == null){
            throw new IllegalArgumentException("Incorrect w");
        }

        p = val [0];
        q = val[1];
    }
}

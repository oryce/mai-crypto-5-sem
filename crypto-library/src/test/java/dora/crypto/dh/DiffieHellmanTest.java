package dora.crypto.dh;

import net.jqwik.api.Example;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class DiffieHellmanTest {

    @Example
    void itWorks() {
        DiffieHellman aliceKex = DiffieHellman.of(DiffieHellmanGroup.FFDHE2048);
        DiffieHellman bobKex = DiffieHellman.of(DiffieHellmanGroup.FFDHE2048);

        BigInteger alicePublicKey = aliceKex.initiate();
        BigInteger bobPublicKey = bobKex.initiate();

        aliceKex.complete(bobPublicKey);
        bobKex.complete(alicePublicKey);

        assertThat(aliceKex.sharedSecret()).isEqualTo(bobKex.sharedSecret());
    }
}

package dora.crypto.rsa;

import dora.crypto.rsa.Rsa.KeyPairGenerator;
import dora.crypto.rsa.Rsa.KeyPairGenerator.KeyPair;
import dora.crypto.rsa.WienerAttack.AttackResult;
import dora.crypto.rsa.WienerAttack.VulnerableKeyPairGenerator;
import net.jqwik.api.Example;

import java.util.Optional;

import static dora.crypto.rsa.Rsa.PrimalityTestType.MILLER_RABIN;
import static org.assertj.core.api.Assertions.assertThat;

public class WienerAttackTest {

    private static final double CERTAINTY = 0.95;
    private static final int PRIME_SIZE = 512;

    private final KeyPairGenerator vulnerableGenerator;
    private final WienerAttack wienerAttack;

    WienerAttackTest() {
        vulnerableGenerator = new VulnerableKeyPairGenerator(MILLER_RABIN, CERTAINTY, PRIME_SIZE);
        wienerAttack = new WienerAttack();
    }

    @Example
    void wienerAttackFindsPrivateExponent() {
        KeyPair keyPair = vulnerableGenerator.generate();
        Optional<AttackResult> resultOptional = wienerAttack.attack(keyPair.n(), keyPair.e());

        assertThat(resultOptional).hasValueSatisfying((result) ->
            assertThat(result.d())
                .describedAs("calculated private exponent equals original value")
                .isEqualTo(keyPair.d()));
    }
}

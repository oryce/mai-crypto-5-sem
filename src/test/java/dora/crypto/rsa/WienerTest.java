package dora.crypto.rsa;

import dora.crypto.rsa.ProbabilisticTests.RsaService;
import net.jqwik.api.Example;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.Optional;

public class WienerTest {
    @Example
     void testWienerAttack() {
        int primeBitLength = 512;
        double minProbability = 0.999;
        RsaService.PrimalityTest test = RsaService.PrimalityTest.MILLER_RABIN;

        WienerAttack.VulnerableKeyGenerationService vulnerableGen = new WienerAttack.VulnerableKeyGenerationService(test, minProbability, primeBitLength);
        RsaService.KeyGenerationService.KeyPair weakPair = vulnerableGen.generateKeys();
        BigInteger N = weakPair.N();
        BigInteger e = weakPair.e();
        BigInteger realD = weakPair.d();

        assertThat(realD).isPositive().isLessThan(N);
        assertThat(realD.bitLength()).as("d must be relatively small for vulnerability").isLessThanOrEqualTo(N.bitLength() / 4 + 10);  // Примерная проверка уязвимости

        System.out.println("Generated weak key: N bit length = " + N.bitLength() + ", d bit length = " + realD.bitLength());

        WienerAttackService attackService = new WienerAttackService(test, minProbability);
        Optional<WienerAttackService.AttackResult> resultOpt = attackService.attack(N, e);

        System.out.println(resultOpt);

        assertThat(resultOpt).as("The attack must be successful for the vulnerable key").isPresent();

        WienerAttackService.AttackResult result = resultOpt.get();
        BigInteger recoveredD = result.d();
        BigInteger recoveredPhi = result.phi();
        var convergents = result.convergents();

        assertThat(recoveredD).as("The restored d must match the real one").isEqualTo(realD);
        assertThat(convergents).as("There must be a collection of convergents (at least 1)").hasSizeGreaterThanOrEqualTo(1);
        assertThat(recoveredPhi).as("φ(N) must be positive and less than N").isPositive().isLessThan(N);

        BigInteger message = new BigInteger("7".repeat(148));
        BigInteger ciphertext = NumberTheory.modPow(message, e, N);
        BigInteger decrypted = NumberTheory.modPow(ciphertext, recoveredD, N);
        assertThat(decrypted).as("Decryption with recovered d should work").isEqualTo(message);

        System.out.println("Recovered φ(N): " + recoveredPhi);
        System.out.println("Convergent count: " + convergents.size());
    }
}

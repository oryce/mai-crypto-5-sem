package dora.crypto.block.deal;

import dora.crypto.block.KeySchedule;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DealKeyScheduleTest {

    @Property(tries = 100)
    void invalidKeySizeThrowsException(
        @ForAll byte[] dealKey,
        @ForAll byte[] desKey
    ) {
        Assume.that(dealKey.length != 16
            && dealKey.length != 24
            && dealKey.length != 32);
        Assume.that(desKey.length != 8);

        assertThatThrownBy(() -> {
            KeySchedule keySchedule = new DealKeySchedule(desKey);
            keySchedule.roundKeys(dealKey);
        })
            .isInstanceOf(IllegalArgumentException.class);
    }
}

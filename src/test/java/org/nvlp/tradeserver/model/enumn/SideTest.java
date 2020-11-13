package org.nvlp.tradeserver.model.enumn;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class SideTest {

    @Test
    void turn() {
        assertThat(Side.BUY.turn()).isEqualTo(Side.SELL);
        assertThat(Side.SELL.turn()).isEqualTo(Side.BUY);
    }
}
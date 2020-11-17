package org.nvlp.tradeserver.utils;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class JsonUtilsTest {

     @Test
     public void testToString() {
        String s = JsonUtils.toString(new Object());
        assertThat(s).isNull();
    }
}
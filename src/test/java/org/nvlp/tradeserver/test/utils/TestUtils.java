package org.nvlp.tradeserver.test.utils;

import java.util.Random;

public class TestUtils {
    private static Random random = new Random();
    public static int randomUid() {
        return random.nextInt();
    }
}

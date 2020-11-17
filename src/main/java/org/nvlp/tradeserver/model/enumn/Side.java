package org.nvlp.tradeserver.model.enumn;

import org.nvlp.tradeserver.utils.EnumUtils;

import java.util.Map;

public enum Side {
    BUY,
    SELL,
    ;
    private static final Map<String, Side> mapByName = EnumUtils.createMapping(Side.class, e->e.name());

    public static Side of(String side) {
        return mapByName.get(side);
    }

}

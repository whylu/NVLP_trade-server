package org.nvlp.tradeserver.model.enumn;

public enum OrderType {
    LIMIT
    ;

    public static OrderType of(String type) {
        if(LIMIT.name().equals(type)) {
            return LIMIT;
        }
        return null;
    }
}

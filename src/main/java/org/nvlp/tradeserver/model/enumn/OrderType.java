package org.nvlp.tradeserver.model.enumn;

public enum OrderType {
    LIMIT,
    ;

    public static OrderType of(String type) {
        OrderType[] values = OrderType.values();

        if(LIMIT.name().equals(type)) {
            return LIMIT;
        }
        return null;
    }
}

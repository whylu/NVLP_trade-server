package org.nvlp.tradeserver.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.nvlp.tradeserver.utils.JsonUtils;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {
    NONE(0, "Zero for no error"),

    // order

    INVALID_MARKER(-1, "Invalid market"),
    INVALID_ORDER_PRICE_MIN(-2, "Order price less than minimum"),
    INVALID_ORDER_PRICE_INCREMENT(-3, "Order price increment invalid"),
    INVALID_ORDER_SIZE_MIN(-4, "Order size less than minimum"),
    INVALID_ORDER_SIZE_INCREMENT(-5, "Order size increment invalid"),

    INSUFFICIENT_BALANCE(-6, "Insufficient balance"),
    MISSING_USER_ID(-7, "Missing userId"),
    MISSING_ORDER_TYPE(-8, "Missing order type"),
    MISSING_ORDER_SIDE(-9, "Missing order side"),
    INVALID_ORDER_PRICE_ZERO(-10, "Order price less than zero"),
    MISSING_ORDER_SIZE(-11, "Missing order size"),
    INVALID_ORDER_SIZE_ZERO(-12, "Order size less than zero"),
    INVALID_ORDER_TYPE(-13, "Invalid order type"),
    ;

    private final int code;
    private final String msg;
    private final String response;


    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.response = JsonUtils.toString(this);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return response;
    }
}

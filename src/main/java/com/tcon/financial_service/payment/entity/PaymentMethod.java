package com.tcon.financial_service.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    CARD("CARD"),
    CREDIT_CARD("CREDIT_CARD"),
    DEBIT_CARD("DEBIT_CARD"),
    UPI("UPI"),
    NET_BANKING("NET_BANKING"),
    WALLET("WALLET"),
    EMI("EMI");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentMethod fromValue(String value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.value.equalsIgnoreCase(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Invalid PaymentMethod: " + value +
                ". Valid values are: CARD, CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET, EMI");
    }

    @Override
    public String toString() {
        return value;
    }
}

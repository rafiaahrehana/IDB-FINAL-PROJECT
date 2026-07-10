package com.businessos.modules.finance.payment;

public enum PaymentMethod {
    BANK_TRANSFER("Bank Transfer"),
    CASH("Cash"),
    CHEQUE("Cheque"),
    CREDIT_CARD("Credit Card"),
    DIGITAL_WALLET("Digital Wallet"),
    MOBILE_PAYMENT("Mobile Payment"),
    OTHER("Other");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

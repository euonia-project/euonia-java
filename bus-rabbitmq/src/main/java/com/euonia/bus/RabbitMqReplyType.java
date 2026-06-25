package com.euonia.bus;

enum RabbitMqReplyType {
    EXCEPTION("EXCEPTION"),
    EMPTY("EMPTY"),
    MESSAGE("MESSAGE");

    private final String value;

    RabbitMqReplyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

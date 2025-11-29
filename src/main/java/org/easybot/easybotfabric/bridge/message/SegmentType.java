package org.easybot.easybotfabric.bridge.message;

public enum SegmentType {
    UNKNOWN(0),
    TEXT(2),
    IMAGE(3),
    AT(4),
    FILE(5),
    REPLY(6);

    private final int value;

    SegmentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SegmentType fromValue(int value) {
        for (SegmentType type : SegmentType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
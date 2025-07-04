package org.lby123165.easybot.bridge.message;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the type of a rich text message segment.
 */
public enum SegmentType {
    TEXT(0),
    AT(1),
    EMOJI(2),
    IMAGE(3),
    FILE(4);

    private final int value;
    private static final Map<Integer, SegmentType> map = new HashMap<>();

    SegmentType(int value) {
        this.value = value;
    }

    static {
        for (SegmentType type : SegmentType.values()) {
            map.put(type.value, type);
        }
    }

    public static SegmentType fromValue(int value) {
        return map.get(value);
    }

    public int getValue() {
        return value;
    }
}
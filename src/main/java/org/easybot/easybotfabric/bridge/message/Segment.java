package org.easybot.easybotfabric.bridge.message;

public abstract class Segment {
    public int type;

    public static Class<? extends Segment> getSegmentClass(SegmentType type) {
        if (type == null) return null;
        return switch (type) {
            case TEXT -> TextSegment.class;
            case AT -> AtSegment.class;
            case IMAGE -> ImageSegment.class;
            // 回复和文件尚未使用，因此我们可以暂时忽略它们。
            default -> null;
        };
    }
}
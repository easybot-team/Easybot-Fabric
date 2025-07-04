package org.lby123165.easybot.bridge.message;

import com.google.gson.annotations.SerializedName;

public abstract class Segment {
    @SerializedName("type")
    public SegmentType type;

    /**
     * A factory method to get the correct Segment class based on its type.
     * This is the missing method the compiler was looking for.
     * @param type The type of the segment.
     * @return The corresponding Segment class, or null if not found.
     */
    public static Class<? extends Segment> getSegmentClass(SegmentType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case TEXT:
                return TextSegment.class;
            case AT:
                return AtSegment.class;
            case IMAGE:
                return ImageSegment.class;
            // You can add EMOJI and FILE here later if needed.
            default:
                return null;
        }
    }
}
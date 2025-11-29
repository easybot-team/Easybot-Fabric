package org.easybot.easybotfabric.bridge.message;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AtSegment extends Segment {
    @SerializedName("at_player_names")
    public List<String> atPlayerNames;

    @SerializedName("at_user_name")
    public String atUserName;

    @SerializedName("at_user_id")
    public String atUserId;
}
package org.lby123165.easybot.bridge.model;

public class PlayerInfo {
    private String name;
    private String uuid;
    private String displayName;
    private int ping;
    private String world;
    private double x;
    private double y;
    private double z;

    public PlayerInfo(String name, String uuid, String displayName, int ping,
                     String world, double x, double y, double z) {
        this.name = name;
        this.uuid = uuid;
        this.displayName = displayName;
        this.ping = ping;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPing() {
        return ping;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}

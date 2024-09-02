package me.flyingtaco725.theplayershuffler;

import org.bukkit.Location;
import java.util.UUID;

public class MCPlayer {
    private final UUID id;
    private final Location location;
    private final double yaw;
    private final double pitch;

    public MCPlayer(UUID id, Location location, double yaw, double pitch) {
        this.id = id;
        this.location = location;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }
}

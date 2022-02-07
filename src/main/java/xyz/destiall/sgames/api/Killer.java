package xyz.destiall.sgames.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class Killer {
    private final String name;
    private final Entity entity;
    private final Block block;

    public Killer(String name) {
        this(name, null, null);
    }

    public Killer(String name, Entity entity) {
        this(name, entity, null);
    }

    public Killer(String name, Block block) {
        this(name, null, block);
    }

    public Killer(String name, Entity entity, Block block) {
        this.name = name;
        this.entity = entity;
        this.block = block;
    }

    public String getName() {
        return name;
    }

    public Block getBlock() {
        return block;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isEntity() {
        return getEntity() != null;
    }

    public boolean isBlock() {
        return getBlock() != null;
    }

    public boolean isQuit() {
        return getBlock() == null && getEntity() == null;
    }
}

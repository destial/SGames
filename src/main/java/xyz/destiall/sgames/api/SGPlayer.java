package xyz.destiall.sgames.api;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointer;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.player.Competitor;
import xyz.destiall.sgames.player.Spectator;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class SGPlayer implements Audience {
    protected final UUID uuid;
    protected final Match match;
    protected Location lastSeen;
    protected Player player;
    protected Audience audience;

    public SGPlayer(Player player, Match match) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.match = match;
        audience = SGames.INSTANCE.getPluginAudience().player(player);
    }

    public UUID getId() {
        return uuid;
    }

    public Optional<Player> getBukkit() {
        return player == null || !player.isOnline() ? Optional.empty() : Optional.of(player);
    }

    public void teleport(Location location) {
        if (player != null) player.teleport(location);
    }

    public PlayerInventory getInventory() {
        return player != null ? player.getInventory() : null;
    }

    public Match getMatch() {
        return match;
    }

    public void setBukkit(Player player) {
        if (player == null) {
            lastSeen = this.player.getLocation();
        }
        this.player = player;
        audience = player != null ? SGames.INSTANCE.getPluginAudience().player(player) : audience;
    }

    public void clearEffects() {
        if (player == null) return;
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        for (PotionEffect effect : effects) {
            player.removePotionEffect(effect.getType());
        }
    }

    public Location getLocation() {
        return player != null ? player.getLocation() : lastSeen;
    }

    public World getWorld() {
        return player != null ? player.getWorld() : match.getMap().getWorld();
    }

    public Stats getStats() {
        return SGames.INSTANCE.getDatastore().getStats(uuid);
    }

    public void sendMessage(String message) {
        if (audience != null) audience.sendMessage(Component.text(message));
    }

    public void clearInventory() {
        if (player != null) getInventory().clear();
    }

    public void openInventory(Inventory inventory) {
        if (player != null) player.openInventory(inventory);
    }

    public boolean isSpectating() {
        return this instanceof Spectator;
    }

    public boolean isPlaying() {
        return this instanceof Competitor;
    }

    public void setHealth(double health) {
        if (player != null) player.setHealth(health);
    }

    public double getHealth() {
        return player != null ? player.getHealth() : 20;
    }

    public void setHunger(int hunger) {
        if (player != null) player.setFoodLevel(hunger);
    }

    public int getHunger() {
        return player != null ? player.getFoodLevel() : 20;
    }

    public Vector getVelocity() {
        return player != null ? player.getVelocity() : new Vector(0, 0, 0);
    }

    public void setLevel(int level) {
        if (player != null) {
            player.setLevel(level);
            player.setExp(0);
        }
    }

    public void kick(String message) {
        if (player != null) {
            player.kickPlayer(message);
        }
    }

    public void kick(Component component) {
        kick(LegacyComponentSerializer.legacySection().serialize(component));
    }

    public void reset() {
        setHealth(20);
        setHunger(20);
        setLevel(0);
        clearInventory();
        clearEffects();
    }

    public void hide(SGPlayer other) {
        if (player != null && other.getBukkit().isPresent()) {
            player.hidePlayer(SGames.INSTANCE, other.player);
        }
    }

    public void unhide(SGPlayer other) {
        if (player != null && other.getBukkit().isPresent()) {
            player.showPlayer(SGames.INSTANCE, other.player);
        }
    }

    public String getName() {
        return player != null ? player.getName() : "Unknown";
    }

    public void showTitle(String title) {
        showTitle(title, null);
    }

    public void showTitle(Component title) {
        showTitle(title, null);
    }

    public void showTitle(String title, String subtitle) {
        showTitle(Component.text(title), subtitle != null ? Component.text(title) : Component.empty());
    }

    public void showTitle(Component title, Component subtitle) {
        Title.Times times = Title.Times.of(Duration.of(0, ChronoUnit.SECONDS), Duration.of(1500, ChronoUnit.MILLIS), Duration.of(1, ChronoUnit.SECONDS));
        Title component = Title.title(title, subtitle == null ? Component.empty() : subtitle, times);
        showTitle(component);
    }

    public void setGameMode(GameMode gm) {
        if (player != null) player.setGameMode(gm);
    }

    @Override
    public Audience filterAudience(Predicate<? super Audience> filter) {
        return audience.filterAudience(filter);
    }

    @Override
    public void forEachAudience(Consumer<? super Audience> action) {
        audience.forEachAudience(action);
    }

    @Override
    public void sendMessage(ComponentLike message) {
        audience.sendMessage(message);
    }

    @Override
    public void sendMessage(Identified source, ComponentLike message) {
        audience.sendMessage(source, message);
    }

    @Override
    public void sendMessage(Identity source, ComponentLike message) {
        audience.sendMessage(source, message);
    }

    @Override
    public void sendMessage(Component message) {
        audience.sendMessage(message);
    }

    @Override
    public void sendMessage(Identified source, Component message) {
        audience.sendMessage(source, message);
    }

    @Override
    public void sendMessage(Identity source, Component message) {
        audience.sendMessage(source, message);
    }

    @Override
    public void sendMessage(ComponentLike message, MessageType type) {
        audience.sendMessage(message, type);
    }

    @Override
    public void sendMessage(Identified source, ComponentLike message, MessageType type) {
        audience.sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(Identity source, ComponentLike message, MessageType type) {
        audience.sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(Component message, MessageType type) {
        audience.sendMessage(message, type);
    }

    @Override
    public void sendMessage(Identified source, Component message, MessageType type) {
        audience.sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(Identity source, Component message, MessageType type) {
        audience.sendMessage(source, message, type);
    }

    @Override
    public void sendActionBar(ComponentLike message) {
        audience.sendActionBar(message);
    }

    @Override
    public void sendActionBar(Component message) {
        audience.sendActionBar(message);
    }

    @Override
    public void sendPlayerListHeader(ComponentLike header) {
        audience.sendPlayerListHeader(header);
    }

    @Override
    public void sendPlayerListHeader(Component header) {
        audience.sendPlayerListHeader(header);
    }

    @Override
    public void sendPlayerListFooter(ComponentLike footer) {
        audience.sendPlayerListFooter(footer);
    }

    @Override
    public void sendPlayerListFooter(Component footer) {
        audience.sendPlayerListFooter(footer);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(ComponentLike header, ComponentLike footer) {
        audience.sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(Component header, Component footer) {
        audience.sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void showTitle(Title title) {
        audience.showTitle(title);
    }

    @Override
    public <T> void sendTitlePart(TitlePart<T> part, T value) {
        audience.sendTitlePart(part, value);
    }

    @Override
    public void clearTitle() {
        audience.clearTitle();
    }

    @Override
    public void resetTitle() {
        audience.resetTitle();
    }

    @Override
    public void showBossBar(BossBar bar) {
        audience.showBossBar(bar);
    }

    @Override
    public void hideBossBar(BossBar bar) {
        audience.hideBossBar(bar);
    }

    @Override
    public void playSound(Sound sound) {
        audience.playSound(sound);
    }

    @Override
    public void playSound(Sound sound, double x, double y, double z) {
        audience.playSound(sound, x, y, z);
    }

    @Override
    public void stopSound(Sound sound) {
        audience.stopSound(sound);
    }

    @Override
    public void playSound(Sound sound, Sound.Emitter emitter) {
        audience.playSound(sound, emitter);
    }

    @Override
    public void stopSound(SoundStop stop) {
        audience.stopSound(stop);
    }

    @Override
    public void openBook(Book.Builder book) {
        audience.openBook(book);
    }

    @Override
    public void openBook(Book book) {
        audience.openBook(book);
    }

    @Override
    public <T> Optional<T> get(Pointer<T> pointer) {
        return audience.get(pointer);
    }

    @Override
    public <T> T getOrDefault(Pointer<T> pointer, T defaultValue) {
        return audience.getOrDefault(pointer, defaultValue);
    }

    @Override
    public <T> T getOrDefaultFrom(Pointer<T> pointer, Supplier<? extends T> defaultValue) {
        return audience.getOrDefaultFrom(pointer, defaultValue);
    }

    @Override
    public Pointers pointers() {
        return audience.pointers();
    }
}

package xyz.destiall.sgames.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public class SoundUtils {
    private SoundUtils() {}

    public static Sound of(org.bukkit.Sound bukkitSound, float pitch, float volume) {
        String key = bukkitSound.getKey().getKey();
        return Sound.sound(Key.key(key), Sound.Source.BLOCK ,pitch, volume);
    }
}

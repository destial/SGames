package xyz.destiall.sgames.utils;

import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;

public class TextColorUtils {
    private TextColorUtils() {}

    public static TextColor RED = of(ChatColor.RED);
    public static TextColor DARK_RED = of(ChatColor.DARK_RED);
    public static TextColor DARK_PURPLE = of(ChatColor.DARK_PURPLE);
    public static TextColor LIGHT_PURPLE = of(ChatColor.LIGHT_PURPLE);
    public static TextColor BLUE = of(ChatColor.BLUE);
    public static TextColor DARK_BLUE = of(ChatColor.DARK_BLUE);
    public static TextColor DARK_AQUA = of(ChatColor.DARK_AQUA);
    public static TextColor AQUA = of(ChatColor.AQUA);
    public static TextColor GREEN = of(ChatColor.GREEN);
    public static TextColor DARK_GREEN = of(ChatColor.DARK_GREEN);
    public static TextColor GOLD = of(ChatColor.GOLD);
    public static TextColor YELLOW = of(ChatColor.YELLOW);
    public static TextColor WHITE = of(ChatColor.WHITE);
    public static TextColor GRAY = of(ChatColor.GRAY);
    public static TextColor DARK_GRAY = of(ChatColor.DARK_GRAY);
    public static TextColor BLACK = of(ChatColor.BLACK);


    public static TextColor of(ChatColor color) {
        return TextColor.color(color.getColor().getRGB());
    }
}

package xyz.destiall.sgames.api;

public interface Module {
    default void load() {}
    default void unload() {}
    default boolean isLoaded() {
        return true;
    }
}

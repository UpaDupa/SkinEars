package com.example.skinears;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Only your own ears are configurable. Auto color is restored by reset. */
public final class EarsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("skinears.json");

    public String type = "CAT";
    public boolean customColor = false;
    public int color = 0xB68A64;

    public static EarsConfig load() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                EarsConfig loaded = GSON.fromJson(Files.readString(CONFIG_FILE, StandardCharsets.UTF_8), EarsConfig.class);
                if (loaded != null) {
                    loaded.type = "DOG".equals(loaded.type) ? "DOG" : "CAT";
                    loaded.color &= 0xFFFFFF;
                    return loaded;
                }
            }
        } catch (Exception ignored) {
            // Corrupt config is replaced with defaults on the next save.
        }
        return new EarsConfig();
    }

    public void reset() {
        type = "CAT";
        customColor = false;
        color = 0xB68A64;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.writeString(CONFIG_FILE, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // The mod remains usable even if the config directory is read-only.
        }
    }
}

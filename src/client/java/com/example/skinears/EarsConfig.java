package com.example.skinears;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EarsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("skinears.json");

    public String type = "CAT";

    // Retained from the original mod for existing users' saved settings.
    public boolean customColor = false;
    public int color = 0xB68A64;

    // Independent choice of skin texture source for four distinct ear parts.
    public int outerLeftRegion = 0;
    public int outerRightRegion = 0;
    public int innerLeftRegion = 0;
    public int innerRightRegion = 0;

    // PINK = traditional pink inner ear; SKIN = skin-region texture;
    // CUSTOM = editable, separate RGB inner-ear color.
    public String innerMode = "PINK";
    public int innerColor = 0xF4A6B8;

    public static EarsConfig load() {
        try {
            if (Files.exists(FILE)) {
                EarsConfig loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), EarsConfig.class);
                if (loaded != null) {
                    loaded.type = "DOG".equals(loaded.type) ? "DOG" : "CAT";
                    loaded.color &= 0xFFFFFF;
                    // Older configs do not include these new fields.
                    if (!"SKIN".equals(loaded.innerMode) && !"CUSTOM".equals(loaded.innerMode))
                        loaded.innerMode = "PINK";
                    loaded.innerColor = loaded.innerColor == 0 ? 0xF4A6B8 : loaded.innerColor & 0xFFFFFF;
                    loaded.outerLeftRegion = normalize(loaded.outerLeftRegion);
                    loaded.outerRightRegion = normalize(loaded.outerRightRegion);
                    loaded.innerLeftRegion = normalize(loaded.innerLeftRegion);
                    loaded.innerRightRegion = normalize(loaded.innerRightRegion);
                    return loaded;
                }
            }
        } catch (Exception ignored) { }
        return new EarsConfig();
    }

    private static int normalize(int index) {
        return Math.floorMod(index, SkinRegion.values().length);
    }

    public void reset() {
        type = "CAT";
        customColor = false;
        color = 0xB68A64;
        innerMode = "PINK";
        innerColor = 0xF4A6B8;
        outerLeftRegion = 0;
        outerRightRegion = 0;
        innerLeftRegion = 0;
        innerRightRegion = 0;
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (Exception ignored) { }
    }
}

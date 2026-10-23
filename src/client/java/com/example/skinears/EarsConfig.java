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
    // Each ear can copy an exact pixel from the current skin.
    public boolean outerLeftPicked=false,outerRightPicked=false;
    public boolean innerLeftPicked=false,innerRightPicked=false;
    public int outerLeftX=8,outerLeftY=0,outerRightX=8,outerRightY=0;
    public int innerLeftX=8,innerLeftY=0,innerRightX=8,innerRightY=0;

    // Persist selected RGB colors so the picker works immediately, even offline.
    public int outerLeftPickedColor=0xFFFFFF,outerRightPickedColor=0xFFFFFF;
    public int innerLeftPickedColor=0xF4A6B8,innerRightPickedColor=0xF4A6B8;

    public static EarsConfig load() {
        try {
            if (Files.exists(FILE)) {
                EarsConfig loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), EarsConfig.class);
                if (loaded != null) {
                    loaded.type = "DOG".equals(loaded.type) ? "DOG" : "CAT";
                    loaded.color &= 0xFFFFFF;
                    // Older configs do not include these new fields.
                    if (!"SKIN".equals(loaded.innerMode) && !"CUSTOM".equals(loaded.innerMode) && !"PICKED".equals(loaded.innerMode))
                        loaded.innerMode = "PINK";
                    loaded.innerColor = loaded.innerColor == 0 ? 0xF4A6B8 : loaded.innerColor & 0xFFFFFF;
                    loaded.outerLeftRegion = normalize(loaded.outerLeftRegion);
                    loaded.outerRightRegion = normalize(loaded.outerRightRegion);
                    loaded.innerLeftRegion = normalize(loaded.innerLeftRegion);
                    loaded.innerRightRegion = normalize(loaded.innerRightRegion);
                    loaded.outerLeftX=clamp(loaded.outerLeftX);loaded.outerLeftY=clamp(loaded.outerLeftY);
                    loaded.outerRightX=clamp(loaded.outerRightX);loaded.outerRightY=clamp(loaded.outerRightY);
                    loaded.innerLeftX=clamp(loaded.innerLeftX);loaded.innerLeftY=clamp(loaded.innerLeftY);
                    loaded.innerRightX=clamp(loaded.innerRightX);loaded.innerRightY=clamp(loaded.innerRightY);
                    loaded.outerLeftPickedColor &= 0xFFFFFF;
                    loaded.outerRightPickedColor &= 0xFFFFFF;
                    loaded.innerLeftPickedColor &= 0xFFFFFF;
                    loaded.innerRightPickedColor &= 0xFFFFFF;
                    return loaded;
                }
            }
        } catch (Exception ignored) { }
        return new EarsConfig();
    }

    private static int normalize(int index) {
        return Math.floorMod(index, SkinRegion.values().length);
    }

    private static int clamp(int n){return Math.max(0,Math.min(63,n));}
    public void selectPixel(int part,int x,int y,int rgb){
        x=clamp(x);y=clamp(y);rgb &= 0xFFFFFF;
        switch(part){
            case 0 -> {outerLeftX=x;outerLeftY=y;outerLeftPicked=true;outerLeftPickedColor=rgb;}
            case 1 -> {outerRightX=x;outerRightY=y;outerRightPicked=true;outerRightPickedColor=rgb;}
            case 2 -> {innerLeftX=x;innerLeftY=y;innerLeftPicked=true;
                       innerLeftPickedColor=rgb;innerMode="PICKED";}
            case 3 -> {innerRightX=x;innerRightY=y;innerRightPicked=true;
                       innerRightPickedColor=rgb;innerMode="PICKED";}
            default -> throw new IllegalArgumentException("Invalid ear part");
        }
        save();
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
        outerLeftPicked=false;outerRightPicked=false;
        innerLeftPicked=false;innerRightPicked=false;
        outerLeftPickedColor=0xFFFFFF;outerRightPickedColor=0xFFFFFF;
        innerLeftPickedColor=0xF4A6B8;innerRightPickedColor=0xF4A6B8;
        outerLeftX=8;outerLeftY=0;outerRightX=8;outerRightY=0;
        innerLeftX=8;innerLeftY=0;innerRightX=8;innerRightY=0;
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (Exception ignored) { }
    }
}

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
 public boolean customColor = false;
 public int color = 0xB68A64;
 public static EarsConfig load() {
  try {
   if (Files.exists(FILE)) {
    EarsConfig c = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), EarsConfig.class);
    if (c != null) { c.type = "DOG".equals(c.type) ? "DOG" : "CAT"; c.color &= 0xFFFFFF; return c; }
   }
  } catch (Exception ignored) {}
  return new EarsConfig();
 }
 public void reset() { type="CAT"; customColor=false; color=0xB68A64; }
 public void save() {
  try { Files.createDirectories(FILE.getParent()); Files.writeString(FILE, GSON.toJson(this), StandardCharsets.UTF_8); }
  catch (Exception ignored) {}
 }
}

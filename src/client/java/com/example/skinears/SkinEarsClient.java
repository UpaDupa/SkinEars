package com.example.skinears;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
public final class SkinEarsClient implements ClientModInitializer {
 public static final String MOD_ID = "skinears";
 public static EarsConfig config;
 public static final SkinColorSampler COLORS = new SkinColorSampler();
 @Override public void onInitializeClient() {
  config=EarsConfig.load();
  KeyMapping.Category category=KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID,"settings"));
  KeyMapping key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skinears.open",InputConstants.Type.KEYBOARD,InputConstants.KEY_K,category));
  ClientTickEvents.END_CLIENT_TICK.register(client -> {
   while(key.consumeClick()) if(client.player!=null) client.gui.setScreen(new EarsScreen(client.gui.screen()));
  });
  LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType,renderer,helper,context) -> {
   if(renderer instanceof AvatarRenderer<?> avatar) helper.register(new EarsLayer(avatar));
  });
 }
}

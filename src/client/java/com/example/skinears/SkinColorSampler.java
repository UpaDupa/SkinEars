package com.example.skinears;

import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Safely downloads and caches the Minecraft skin image so the eyedropper can read pixels. */
public final class SkinColorSampler {
    private final Map<String, CompletableFuture<BufferedImage>> cache = new ConcurrentHashMap<>();
    private volatile int latestOwnColor = 0xB68A64;

    public int latestOwnColor(){return latestOwnColor;}
    public void setLatestOwnColor(int color){latestOwnColor=color & 0xFFFFFF;}

    private CompletableFuture<BufferedImage> imageFuture(PlayerSkin skin) {
        ClientAsset.Texture texture=skin.body();
        return cache.computeIfAbsent(texture.texturePath().toString(),
            key -> CompletableFuture.supplyAsync(() -> readSkin(texture)));
    }

    /** Returns -1 while loading, for transparent pixels, or on image failure. */
    public int pixel(PlayerSkin skin,int x,int y) {
        if (skin==null || x<0 || y<0 || x>=64 || y>=64) return -1;
        BufferedImage img=imageFuture(skin).getNow(null);
        if (img==null || x>=img.getWidth() || y>=img.getHeight()) return -1;
        int argb=img.getRGB(x,y);
        return ((argb>>>24)&255)<16 ? -1 : argb & 0xFFFFFF;
    }

    public boolean loaded(PlayerSkin skin) {
        if(skin==null)return false;
        return imageFuture(skin).getNow(null)!=null;
    }

    public boolean loading(PlayerSkin skin) {
        return skin!=null && !imageFuture(skin).isDone();
    }

    public void reload(PlayerSkin skin) {
        if(skin==null)return;
        cache.remove(skin.body().texturePath().toString());
        imageFuture(skin);
    }

    private static BufferedImage readSkin(ClientAsset.Texture texture) {
        try {
            if(texture instanceof ClientAsset.DownloadedTexture downloaded) {
                URI uri=URI.create(downloaded.url());
                // Mojang commonly supplies http://textures.minecraft.net/... skin URLs.
                // Accept the trusted Minecraft domain, but download securely via HTTPS.
                if(!"textures.minecraft.net".equalsIgnoreCase(uri.getHost()) ||
                   !("http".equalsIgnoreCase(uri.getScheme()) ||
                     "https".equalsIgnoreCase(uri.getScheme())) ||
                   uri.getRawPath()==null || !uri.getRawPath().matches("/texture/[a-zA-Z0-9]+"))
                    return null;
                URI secure=URI.create("https://textures.minecraft.net"+uri.getRawPath());
                HttpURLConnection conn=(HttpURLConnection)secure.toURL().openConnection();
                conn.setConnectTimeout(5500);
                conn.setReadTimeout(5500);
                conn.setInstanceFollowRedirects(false);
                try(InputStream stream=conn.getInputStream()) {
                    BufferedImage image=ImageIO.read(stream);
                    return image!=null && image.getWidth()==64 && image.getHeight()>=32 ? image:null;
                } finally {conn.disconnect();}
            }
            if(texture instanceof ClientAsset.ResourceTexture resourceTexture) {
                var optional=Minecraft.getInstance().getResourceManager().getResource(resourceTexture.texturePath());
                if(optional.isPresent())try(InputStream stream=optional.get().open()){
                    return ImageIO.read(stream);
                }
            }
        }catch(Exception ignored) {}
        return null;
    }

    /** Kept for compatibility with older configuration screens. */
    public int colorFor(PlayerSkin skin) {
        int sampled=pixel(skin,12,4);
        return sampled>=0?sampled:0xB68A64;
    }
}

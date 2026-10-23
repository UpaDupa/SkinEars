package com.example.skinears;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
public final class SkinColorSampler {
 private static final int FALLBACK=0xB68A64;
 private final Map<String,CompletableFuture<Integer>> cache=new ConcurrentHashMap<>();
 private volatile int latestOwnColor=FALLBACK;
 public int latestOwnColor(){return latestOwnColor;}
 public void setLatestOwnColor(int color){latestOwnColor=color&0xFFFFFF;}
 public int colorFor(PlayerSkin skin){
  ClientAsset.Texture texture=skin.body();
  String key=texture.texturePath().toString();
  CompletableFuture<Integer> future=cache.computeIfAbsent(key,unused->CompletableFuture.supplyAsync(()->loadColor(texture)));
  return future.getNow(FALLBACK);
 }
 private static int loadColor(ClientAsset.Texture texture){
  try {
   if(texture instanceof ClientAsset.DownloadedTexture downloaded){
    URI url=URI.create(downloaded.url());
    if(!"https".equalsIgnoreCase(url.getScheme())||!"textures.minecraft.net".equalsIgnoreCase(url.getHost()))return FALLBACK;
    HttpURLConnection connection=(HttpURLConnection)url.toURL().openConnection();
    connection.setConnectTimeout(2500);connection.setReadTimeout(2500);connection.setInstanceFollowRedirects(false);
    try(InputStream stream=connection.getInputStream()){return dominantTopColor(ImageIO.read(stream));}
    finally{connection.disconnect();}
   }
   if(texture instanceof ClientAsset.ResourceTexture resourceTexture){
    var resource=Minecraft.getInstance().getResourceManager().getResource(resourceTexture.texturePath());
    if(resource.isPresent())try(InputStream stream=resource.get().open()){return dominantTopColor(ImageIO.read(stream));}
   }
  }catch(Exception ignored){}
  return FALLBACK;
 }
 public static int dominantTopColor(BufferedImage skin){
  if(skin==null||skin.getWidth()<48||skin.getHeight()<8)return FALLBACK;
  int[] counts=new int[4096],red=new int[4096],green=new int[4096],blue=new int[4096];
  for(int y=0;y<8;y++)for(int x=0;x<8;x++){
   int base=skin.getRGB(8+x,y),overlay=skin.getRGB(40+x,y),alpha=(overlay>>>24)&255;
   if(alpha>0){
    int r=(((overlay>>16)&255)*alpha+((base>>16)&255)*(255-alpha))/255;
    int g=(((overlay>>8)&255)*alpha+((base>>8)&255)*(255-alpha))/255;
    int b=((overlay&255)*alpha+(base&255)*(255-alpha))/255;
    base=(r<<16)|(g<<8)|b;
   }
   int r=(base>>>16)&255,g=(base>>>8)&255,b=base&255;
   int idx=(r>>4)*256+(g>>4)*16+(b>>4);
   counts[idx]++;red[idx]+=r;green[idx]+=g;blue[idx]+=b;
  }
  int best=0;
  for(int i=1;i<counts.length;i++)if(counts[i]>counts[best])best=i;
  int n=Math.max(counts[best],1);
  return (red[best]/n<<16)|(green[best]/n<<8)|(blue[best]/n);
 }
}

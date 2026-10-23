package com.example.skinears;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Only head-top skin texture and manually picked skin pixels remain.
 * Inner ears also offer their original default pink.
 */
public final class EarsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE =
        FabricLoader.getInstance().getConfigDir().resolve("skinears.json");

    public String type="CAT";
    public String outerMode="HEAD";  // HEAD or PICKED
    public String innerMode="PINK";  // PINK, HEAD or PICKED

    public boolean outerLeftPicked=false,outerRightPicked=false;
    public boolean innerLeftPicked=false,innerRightPicked=false;
    public int outerLeftX=8,outerLeftY=0,outerRightX=8,outerRightY=0;
    public int innerLeftX=8,innerLeftY=0,innerRightX=8,innerRightY=0;
    public int outerLeftPickedColor=0xFFFFFF,outerRightPickedColor=0xFFFFFF;
    public int innerLeftPickedColor=0xF4A6B8,innerRightPickedColor=0xF4A6B8;

    public static EarsConfig load(){
        try{
            if(Files.exists(FILE)){
                EarsConfig c=GSON.fromJson(Files.readString(FILE,StandardCharsets.UTF_8),EarsConfig.class);
                if(c!=null){
                    c.type="DOG".equals(c.type)?"DOG":"CAT";
                    // Migrate previous RGB modes to head texture / default pink.
                    if(!"HEAD".equals(c.outerMode) && !"PICKED".equals(c.outerMode))
                        c.outerMode=(c.outerLeftPicked||c.outerRightPicked)?"PICKED":"HEAD";
                    if("SKIN".equals(c.innerMode))c.innerMode="HEAD";
                    if(!"PINK".equals(c.innerMode) && !"HEAD".equals(c.innerMode) &&
                       !"PICKED".equals(c.innerMode))
                        c.innerMode=(c.innerLeftPicked||c.innerRightPicked)?"PICKED":"PINK";
                    if("PICKED".equals(c.outerMode) &&
                       !c.outerLeftPicked && !c.outerRightPicked)c.outerMode="HEAD";
                    if("PICKED".equals(c.innerMode) &&
                       !c.innerLeftPicked && !c.innerRightPicked)c.innerMode="PINK";
                    c.outerLeftX=clamp(c.outerLeftX);c.outerLeftY=clamp(c.outerLeftY);
                    c.outerRightX=clamp(c.outerRightX);c.outerRightY=clamp(c.outerRightY);
                    c.innerLeftX=clamp(c.innerLeftX);c.innerLeftY=clamp(c.innerLeftY);
                    c.innerRightX=clamp(c.innerRightX);c.innerRightY=clamp(c.innerRightY);
                    c.outerLeftPickedColor&=0xFFFFFF;c.outerRightPickedColor&=0xFFFFFF;
                    c.innerLeftPickedColor&=0xFFFFFF;c.innerRightPickedColor&=0xFFFFFF;
                    return c;
                }
            }
        }catch(Exception ignored){}
        return new EarsConfig();
    }

    private static int clamp(int n){return Math.max(0,Math.min(63,n));}

    public void selectPixel(int part,int x,int y,int color){
        x=clamp(x);y=clamp(y);color&=0xFFFFFF;
        switch(part){
            case 0 -> {outerLeftX=x;outerLeftY=y;outerLeftPicked=true;
                       outerLeftPickedColor=color;outerMode="PICKED";}
            case 1 -> {outerRightX=x;outerRightY=y;outerRightPicked=true;
                       outerRightPickedColor=color;outerMode="PICKED";}
            case 2 -> {innerLeftX=x;innerLeftY=y;innerLeftPicked=true;
                       innerLeftPickedColor=color;innerMode="PICKED";}
            case 3 -> {innerRightX=x;innerRightY=y;innerRightPicked=true;
                       innerRightPickedColor=color;innerMode="PICKED";}
            default -> throw new IllegalArgumentException("Unknown ear section");
        }
        save();
    }

    public void reset(){
        type="CAT";outerMode="HEAD";innerMode="PINK";
        outerLeftPicked=false;outerRightPicked=false;
        innerLeftPicked=false;innerRightPicked=false;
        outerLeftX=8;outerLeftY=0;outerRightX=8;outerRightY=0;
        innerLeftX=8;innerLeftY=0;innerRightX=8;innerRightY=0;
        outerLeftPickedColor=0xFFFFFF;outerRightPickedColor=0xFFFFFF;
        innerLeftPickedColor=0xF4A6B8;innerRightPickedColor=0xF4A6B8;
    }

    public void save(){
        try{
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE,GSON.toJson(this),StandardCharsets.UTF_8);
        }catch(Exception ignored){}
    }
}

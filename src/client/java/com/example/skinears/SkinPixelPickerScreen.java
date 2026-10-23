package com.example.skinears;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * Clickable 64 x 64 Minecraft skin atlas. Select an exact, opaque pixel from
 * head, chest, arms or legs; the mod samples that coordinate on the current skin.
 */
public final class SkinPixelPickerScreen extends Screen {
    private final EarsScreen back;
    private final int part;
    private final EarsConfig settings=SkinEarsClient.config;
    private String status="Click a pixel to pick its color";
    private int pointerX=-1,pointerY=-1;

    public SkinPixelPickerScreen(EarsScreen back,int part) {
        super(Component.literal("Pick color from your skin"));
        this.back=back;
        this.part=part;
    }

    private String partName() {
        return switch(part) {
            case 0 -> "Outside left ear";
            case 1 -> "Outside right ear";
            case 2 -> "Inside left ear";
            default -> "Inside right ear";
        };
    }

    private int gridSize() {
        // Never taller than the available screen; use integer pixel scaling.
        return 64*Math.max(1,Math.min(4,(height-155)/64));
    }
    private int gridX(){return (width-gridSize())/2;}
    private int gridY(){return 31;}

    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Reload skin colors"),b->{
            if(minecraft.player!=null) {
                SkinEarsClient.COLORS.reload(minecraft.player.getSkin());
                status="Reloading skin pixel data...";
            }
        }).bounds(width/2-100,height-51,200,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back to Skin Ears"),
            b->onClose()).bounds(width/2-100,height-26,200,20).build());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics,
                                             int mouseX,int mouseY,float partialTick) {
        super.extractRenderState(graphics,mouseX,mouseY,partialTick);
        graphics.centeredText(font,title,width/2,9,0xFFFFFFFF);
        graphics.centeredText(font,Component.literal(partName()),width/2,20,0xFFDDDDDD);
        int x=gridX(),y=gridY(),s=gridSize(),scale=s/64;
        graphics.fill(x-2,y-2,x+s+2,y+s+2,0xFF444444);
        graphics.fill(x,y,x+s,y+s,0xFF222222);

        if(minecraft.player!=null) {
            PlayerSkin skin=minecraft.player.getSkin();
            graphics.blit(skin.body().texturePath(),x,y,x+s,y+s,0f,1f,0f,1f);
            if(mouseX>=x && mouseX<x+s && mouseY>=y && mouseY<y+s) {
                pointerX=(mouseX-x)/scale;pointerY=(mouseY-y)/scale;
                int cx=x+pointerX*scale,cy=y+pointerY*scale;
                graphics.fill(cx-1,cy-1,cx+scale+1,cy,0xFFFFFFFF);
                graphics.fill(cx-1,cy+scale,cx+scale+1,cy+scale+1,0xFFFFFFFF);
                graphics.fill(cx-1,cy,cx,cy+scale,0xFFFFFFFF);
                graphics.fill(cx+scale,cy,cx+scale+1,cy+scale,0xFFFFFFFF);
                int preview=SkinEarsClient.COLORS.pixel(skin,pointerX,pointerY);
                if(preview>=0) {
                    graphics.fill(x+s+8,y+8,x+s+27,y+27,0xFF000000|preview);
                }
            }
            if(SkinEarsClient.COLORS.loaded(skin)){
                if(status.startsWith("Loading") || status.startsWith("Reloading"))
                    status="Left-click any nontransparent skin pixel";
            } else if(SkinEarsClient.COLORS.loading(skin)){
                status="Loading skin pixels...";
            } else {
                status="Skin download failed - choose Reload skin colors";
            }
        }

        graphics.centeredText(font,
            Component.literal("Pixel colors from your full skin: head, body, arms and legs"),
            width/2,y+s+10,0xFFDDDDDD);
        graphics.centeredText(font,
            Component.literal(pointerX<0?status:
                "Pixel ("+pointerX+", "+pointerY+")  -  "+status),
            width/2,y+s+24,0xFFAAAAAA);
        graphics.centeredText(font,Component.literal("Left-click a colored pixel. Transparent pixels are ignored."),
            width/2,y+s+37,0xFFAAAAAA);
    }

    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick) {
        int x=gridX(),y=gridY(),size=gridSize();
        if(event.button()==0 &&
           event.x()>=x && event.x()<x+size &&
           event.y()>=y && event.y()<y+size && minecraft.player!=null) {
            int scale=size/64;
            int sx=(int)(event.x()-x)/scale;
            int sy=(int)(event.y()-y)/scale;
            PlayerSkin skin=minecraft.player.getSkin();
            int color=SkinEarsClient.COLORS.pixel(skin,sx,sy);
            if(color<0) {
                status=SkinEarsClient.COLORS.loaded(skin)?
                    "Transparent pixel - try another spot":"Skin not loaded yet";
            } else {
                settings.selectPixel(part,sx,sy,color);
                minecraft.gui.setScreen(back);
            }
            return true;
        }
        return super.mouseClicked(event,doubleClick);
    }

    @Override public void onClose(){minecraft.gui.setScreen(back);}
}

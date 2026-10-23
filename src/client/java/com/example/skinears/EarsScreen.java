package com.example.skinears;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Two easy options: use your head-top skin texture or click a skin pixel.
 * No RGB slider UI.
 */
public final class EarsScreen extends Screen {
    private final Screen previous;
    private final EarsConfig settings=SkinEarsClient.config;
    private int page=0;

    public EarsScreen(Screen previous){
        super(Component.literal("Skin Ears Settings"));
        this.previous=previous;
    }

    @Override protected void init(){
        int x=width/2-115;
        int top=Math.max(32,(height-225)/2);

        addRenderableWidget(Button.builder(
            Component.literal(page==0?"[Ears]":"Ears"),
            b->{page=0;rebuildWidgets();}).bounds(x,top,111,20).build());
        addRenderableWidget(Button.builder(
            Component.literal(page==1?"[Skin picker]":"Skin picker"),
            b->{page=1;rebuildWidgets();}).bounds(x+119,top,111,20).build());

        if(page==0){
            addRenderableWidget(Button.builder(
                Component.literal("CAT".equals(settings.type)?"[Cat ears]":"Cat ears"),
                b->{settings.type="CAT";saveAndRefresh();})
                .bounds(x,top+35,111,20).build());
            addRenderableWidget(Button.builder(
                Component.literal("DOG".equals(settings.type)?"[Husky ears]":"Husky ears"),
                b->{settings.type="DOG";saveAndRefresh();})
                .bounds(x+119,top+35,111,20).build());

            addRenderableWidget(Button.builder(Component.literal(
                "HEAD".equals(settings.outerMode)?"[Outer: Head-top texture]":"Outer: Use head-top texture"),
                b->{settings.outerMode="HEAD";saveAndRefresh();})
                .bounds(x,top+77,230,20).build());
            addRenderableWidget(Button.builder(Component.literal(
                "HEAD".equals(settings.innerMode)?"[Inner: Head-top texture]":"Inner: Use head-top texture"),
                b->{settings.innerMode="HEAD";saveAndRefresh();})
                .bounds(x,top+108,230,20).build());
            addRenderableWidget(Button.builder(Component.literal(
                "PINK".equals(settings.innerMode)?"[Inner: Default pink]":"Inner: Default pink"),
                b->{settings.innerMode="PINK";saveAndRefresh();})
                .bounds(x,top+139,230,20).build());
            addRenderableWidget(Button.builder(Component.literal("Reset settings"),
                b->{settings.reset();saveAndRefresh();})
                .bounds(x,top+171,230,20).build());
        } else {
            pickButton(x,top+36,"Outside left",0);
            pickButton(x,top+68,"Outside right",1);
            pickButton(x,top+100,"Inside left",2);
            pickButton(x,top+132,"Inside right",3);

            addRenderableWidget(Button.builder(Component.literal("Clear picks / use head texture"),
                b->{settings.outerMode="HEAD";settings.innerMode="PINK";
                    settings.outerLeftPicked=false;settings.outerRightPicked=false;
                    settings.innerLeftPicked=false;settings.innerRightPicked=false;
                    saveAndRefresh();})
                .bounds(x,top+169,230,20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose())
            .bounds(x,top+202,230,20).build());
    }

    private void pickButton(int x,int y,String label,int part){
        boolean picked=switch(part){
            case 0->settings.outerLeftPicked;
            case 1->settings.outerRightPicked;
            case 2->settings.innerLeftPicked;
            default->settings.innerRightPicked;
        };
        int rgb=switch(part){
            case 0->settings.outerLeftPickedColor;
            case 1->settings.outerRightPickedColor;
            case 2->settings.innerLeftPickedColor;
            default->settings.innerRightPickedColor;
        };
        String mode=part<2?settings.outerMode:settings.innerMode;
        String current="PICKED".equals(mode) && picked?
            String.format("#%06X",rgb&0xFFFFFF) :
            "HEAD".equals(mode)?"Head top":"PINK".equals(mode)?"Pink":"Pick color";
        addRenderableWidget(Button.builder(Component.literal(label+": "+current+"  >"),
            b->minecraft.gui.setScreen(new SkinPixelPickerScreen(this,part)))
            .bounds(x,y,230,20).build());
    }

    private void saveAndRefresh(){settings.save();rebuildWidgets();}

    @Override public void extractRenderState(GuiGraphicsExtractor graphics,
                                              int mouseX,int mouseY,float partialTick){
        super.extractRenderState(graphics,mouseX,mouseY,partialTick);
        int top=Math.max(32,(height-225)/2);
        graphics.centeredText(font,title,width/2,top-21,0xFFFFFFFF);
        String hint=page==0?
            "Outer: "+settings.outerMode+"   Inner: "+settings.innerMode :
            "Choose an ear part, then left-click a pixel on your skin";
        graphics.centeredText(font,Component.literal(hint),width/2,
            top+194,0xFFCCCCCC);
    }

    @Override public void onClose(){settings.save();minecraft.gui.setScreen(previous);}
    @Override public void removed(){settings.save();}
}

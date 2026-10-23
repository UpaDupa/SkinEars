package com.example.skinears;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Simple ear selector with RGB sliders and an exact skin-pixel picker. */
public final class EarsScreen extends Screen {
    private final Screen previous;
    private final EarsConfig settings=SkinEarsClient.config;
    // 0 = type, 1 = RGB, 2 = pick from skin
    private int page=0;
    private boolean editInner=false;

    public EarsScreen(Screen previous) {
        super(Component.literal("Skin Ears Settings"));
        this.previous=previous;
    }

    @Override protected void init() {
        int x=width/2-115;
        int top=Math.max(34,(height-225)/2);
        addRenderableWidget(Button.builder(Component.literal(page==0?"[Ears]":"Ears"),
            b->{page=0;rebuildWidgets();}).bounds(x,top,72,20).build());
        addRenderableWidget(Button.builder(Component.literal(page==1?"[RGB]":"RGB"),
            b->{page=1;rebuildWidgets();}).bounds(x+79,top,72,20).build());
        addRenderableWidget(Button.builder(Component.literal(page==2?"[Pick from skin]":"Skin picker"),
            b->{page=2;rebuildWidgets();}).bounds(x+158,top,72,20).build());

        if(page==0){
            addRenderableWidget(Button.builder(Component.literal(settings.type.equals("CAT")?"[Cat ears]":"Cat ears"),
                b->{settings.type="CAT";saveAndRefresh();}).bounds(x,top+37,111,20).build());
            addRenderableWidget(Button.builder(Component.literal(settings.type.equals("DOG")?"[Husky ears]":"Husky ears"),
                b->{settings.type="DOG";saveAndRefresh();}).bounds(x+119,top+37,111,20).build());
            addRenderableWidget(Button.builder(Component.literal("Reset all settings"),
                b->{settings.reset();editInner=false;saveAndRefresh();}).bounds(x,top+142,230,20).build());
        } else if(page==1){
            addRenderableWidget(Button.builder(Component.literal(editInner?"Edit outer RGB":"[Edit outer RGB]"),
                b->{editInner=false;settings.customColor=true;
                    settings.outerLeftPicked=false;settings.outerRightPicked=false;
                    saveAndRefresh();}).bounds(x,top+34,111,20).build());
            addRenderableWidget(Button.builder(Component.literal(editInner?"[Edit inner RGB]":"Edit inner RGB"),
                b->{editInner=true;settings.innerMode="CUSTOM";
                    settings.innerLeftPicked=false;settings.innerRightPicked=false;
                    saveAndRefresh();}).bounds(x+119,top+34,111,20).build());
            for(int channel=0;channel<3;channel++){
                addRenderableWidget(new ChannelSlider(x,top+69+channel*27,230,20,channel));
            }
            addRenderableWidget(Button.builder(Component.literal("Reset RGB and picks"),
                b->{settings.color=0xFFFFFF;settings.innerColor=0xF4A6B8;
                    settings.customColor=true;settings.innerMode="CUSTOM";
                    settings.outerLeftPicked=false;settings.outerRightPicked=false;
                    settings.innerLeftPicked=false;settings.innerRightPicked=false;
                    saveAndRefresh();}).bounds(x,top+164,230,20).build());
        } else {
            pickButton(x,top+37,"Outside left",0);
            pickButton(x,top+67,"Outside right",1);
            pickButton(x,top+97,"Inside left",2);
            pickButton(x,top+127,"Inside right",3);
            addRenderableWidget(Button.builder(Component.literal("Clear picked colors"),
                b->{settings.outerLeftPicked=false;settings.outerRightPicked=false;
                    settings.innerLeftPicked=false;settings.innerRightPicked=false;
                    settings.innerMode="CUSTOM";settings.customColor=true;
                    saveAndRefresh();}).bounds(x,top+166,230,20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose())
            .bounds(x,top+205,230,20).build());
    }

    private void pickButton(int x,int y,String label,int part){
        boolean picked=switch(part){
            case 0->settings.outerLeftPicked;
            case 1->settings.outerRightPicked;
            case 2->settings.innerLeftPicked;
            default->settings.innerRightPicked;
        };
        int color=switch(part){
            case 0->settings.outerLeftPickedColor;
            case 1->settings.outerRightPickedColor;
            case 2->settings.innerLeftPickedColor;
            default->settings.innerRightPickedColor;
        };
        String detail=picked?String.format("#%06X",color&0xFFFFFF):"RGB";
        addRenderableWidget(Button.builder(Component.literal(label+": "+detail),
            b->minecraft.gui.setScreen(new SkinPixelPickerScreen(this,part)))
            .bounds(x,y,230,20).build());
    }

    private void saveAndRefresh(){settings.save();rebuildWidgets();}

    @Override public void extractRenderState(GuiGraphicsExtractor graphics,
            int mouseX,int mouseY,float partialTick){
        super.extractRenderState(graphics,mouseX,mouseY,partialTick);
        int top=Math.max(34,(height-225)/2);
        graphics.centeredText(font,title,width/2,top-23,0xFFFFFFFF);
        String hint=switch(page){
            case 0->"Both ear styles tilt outwards";
            case 1->"RGB sliders set BOTH outer or inner ear colors";
            default->"Pick a skin pixel for each ear part";
        };
        graphics.centeredText(font,Component.literal(hint),width/2,top+193,0xFFCCCCCC);
        if(page==1){
            int c=editInner?settings.innerColor:settings.color;
            graphics.fill(width/2-19,top+153,width/2+19,top+159,0xFF000000|(c&0xFFFFFF));
        }
    }

    @Override public void onClose(){settings.save();minecraft.gui.setScreen(previous);}
    @Override public void removed(){settings.save();}

    private final class ChannelSlider extends AbstractSliderButton {
        private final int channel;
        private ChannelSlider(int x,int y,int w,int h,int channel){
            super(x,y,w,h,Component.empty(),
                (((editInner?settings.innerColor:settings.color)>>(16-8*channel))&255)/255.0);
            this.channel=channel;
            updateMessage();
        }
        @Override protected void updateMessage(){
            String name=switch(channel){case 0->"Red";case 1->"Green";default->"Blue";};
            setMessage(Component.literal(name+": "+Math.round(value*255)));
        }
        @Override protected void applyValue(){
            int shift=16-channel*8;
            int amount=(int)Math.round(value*255);
            if(editInner){
                settings.innerColor=(settings.innerColor&~(255<<shift))|(amount<<shift);
                settings.innerMode="CUSTOM";
                settings.innerLeftPicked=false;settings.innerRightPicked=false;
            }else{
                settings.color=(settings.color&~(255<<shift))|(amount<<shift);
                settings.customColor=true;
                settings.outerLeftPicked=false;settings.outerRightPicked=false;
            }
            settings.save();
        }
    }
}

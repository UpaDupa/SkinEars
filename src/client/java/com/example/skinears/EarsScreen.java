package com.example.skinears;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EarsScreen extends Screen {
    private final Screen previous;
    private final EarsConfig settings=SkinEarsClient.config;
    private int page=0; // 0: general, 1: skin regions, 2: manual color sliders

    public EarsScreen(Screen previous) {
        super(Component.literal("Skin Ears"));
        this.previous=previous;
    }

    @Override protected void init() {
        int left=width/2-115;
        int top=Math.max(34,height/2-122);
        addRenderableWidget(Button.builder(Component.literal("Main"),b->{page=0;rebuildWidgets();})
                .bounds(left,top,72,20).build());
        addRenderableWidget(Button.builder(Component.literal("Skin areas"),b->{page=1;rebuildWidgets();})
                .bounds(left+79,top,72,20).build());
        addRenderableWidget(Button.builder(Component.literal("Colors"),b->{page=2;rebuildWidgets();})
                .bounds(left+158,top,72,20).build());

        if (page==0) {
            addRenderableWidget(Button.builder(Component.literal(settings.type.equals("CAT")?"[Cat ears]":"Cat ears"),
                    b->{settings.type="CAT";saveAndRefresh();}).bounds(left,top+34,111,20).build());
            addRenderableWidget(Button.builder(Component.literal(settings.type.equals("DOG")?"[Husky ears]":"Husky ears"),
                    b->{settings.type="DOG";saveAndRefresh();}).bounds(left+119,top+34,111,20).build());
            addRenderableWidget(Button.builder(Component.literal(settings.customColor?"Outer color: Custom":"Outer color: Skin"),
                    b->{settings.customColor=!settings.customColor;saveAndRefresh();})
                    .bounds(left,top+67,230,20).build());
            addRenderableWidget(Button.builder(Component.literal("Inner ears: "+settings.innerMode),
                    b->{settings.innerMode=switch(settings.innerMode){
                        case "PINK"->"SKIN";case "SKIN"->"CUSTOM";default->"PINK";};
                        saveAndRefresh();}).bounds(left,top+100,230,20).build());
            addRenderableWidget(Button.builder(Component.literal("Reset all settings"),
                    b->{settings.reset();saveAndRefresh();})
                    .bounds(left,top+148,230,20).build());
        } else if(page==1) {
            addRegionButton(left,top+34,"Outer left",0);
            addRegionButton(left,top+64,"Outer right",1);
            addRegionButton(left,top+94,"Inner left",2);
            addRegionButton(left,top+124,"Inner right",3);
            addRenderableWidget(Button.builder(Component.literal("Reset skin areas"),
                    b->{settings.outerLeftRegion=0;settings.outerRightRegion=0;
                        settings.innerLeftRegion=0;settings.innerRightRegion=0;saveAndRefresh();})
                    .bounds(left,top+160,230,20).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal(settings.customColor?"Outer: Custom":"Outer: Skin"),
                    b->{settings.customColor=!settings.customColor;saveAndRefresh();})
                    .bounds(left,top+32,111,20).build());
            addRenderableWidget(Button.builder(Component.literal("Inner: "+settings.innerMode),
                    b->{settings.innerMode=switch(settings.innerMode){
                        case "PINK"->"SKIN";case "SKIN"->"CUSTOM";default->"PINK";};
                        saveAndRefresh();})
                    .bounds(left+119,top+32,111,20).build());
            addRenderableWidget(Button.builder(Component.literal("Edit outer RGB"),
                    b->{settings.customColor=true;editInner=false;saveAndRefresh();})
                    .bounds(left,top+59,111,20).build());
            addRenderableWidget(Button.builder(Component.literal("Edit inner RGB"),
                    b->{settings.innerMode="CUSTOM";editInner=true;saveAndRefresh();})
                    .bounds(left+119,top+59,111,20).build());
            for(int channel=0;channel<3;channel++)
                addRenderableWidget(new ChannelSlider(left,top+89+channel*24,230,20,channel));
            addRenderableWidget(Button.builder(Component.literal("Reset colors"),
                    b->{settings.customColor=false;settings.color=0xB68A64;settings.innerMode="PINK";
                        settings.innerColor=0xF4A6B8;editInner=false;saveAndRefresh();})
                    .bounds(left,top+172,230,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose())
                .bounds(left,top+205,230,20).build());
    }

    private boolean editInner=false;
    private void saveAndRefresh(){settings.save();rebuildWidgets();}
    private void addRegionButton(int x,int y,String label,int part){
        int index=switch(part){case 0->settings.outerLeftRegion;case 1->settings.outerRightRegion;
            case 2->settings.innerLeftRegion;default->settings.innerRightRegion;};
        String region=SkinRegion.fromIndex(index).label;
        addRenderableWidget(Button.builder(Component.literal(label+": "+region+"  >"),b->{
            int next=SkinRegion.next(index);
            switch(part){case 0->settings.outerLeftRegion=next;case 1->settings.outerRightRegion=next;
                case 2->settings.innerLeftRegion=next;default->settings.innerRightRegion=next;}
            saveAndRefresh();
        }).bounds(x,y,230,20).build());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float dt) {
        super.extractRenderState(graphics,mouseX,mouseY,dt);
        int top=Math.max(34,height/2-122);
        graphics.centeredText(font,title,width/2,top-19,0xFFFFFFFF);
        String hint=page==1?"Click a part to cycle the skin region":
            page==2?"RGB sliders edit the selected outer/inner color":
                "K to open • Saved automatically";
        graphics.centeredText(font,Component.literal(hint),width/2,top+194,0xFFCCCCCC);
    }

    @Override public void onClose(){settings.save();minecraft.gui.setScreen(previous);}
    @Override public void removed(){settings.save();}

    private final class ChannelSlider extends AbstractSliderButton {
        private final int channel;
        private ChannelSlider(int x,int y,int width,int height,int channel) {
            super(x,y,width,height,Component.empty(),
                    ((currentColor()>>(16-8*channel))&255)/255.0);
            this.channel=channel;updateMessage();
        }
        private int currentColor(){return editInner?settings.innerColor:settings.color;}
        @Override protected void updateMessage(){
            String label=switch(channel){case 0->"Red";case 1->"Green";default->"Blue";};
            setMessage(Component.literal((editInner?"Inner ":"Outer ")+label+": "+Math.round(value*255)));
        }
        @Override protected void applyValue(){
            int shift=16-8*channel;
            int val=(int)Math.round(value*255);
            if(editInner) {
                settings.innerMode="CUSTOM";
                settings.innerColor=(settings.innerColor&~(255<<shift))|(val<<shift);
            }else{
                settings.customColor=true;
                settings.color=(settings.color&~(255<<shift))|(val<<shift);
            }
            settings.save();
        }
    }
}

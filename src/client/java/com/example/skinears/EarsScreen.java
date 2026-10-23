package com.example.skinears;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class EarsScreen extends Screen {
 private final Screen previous;
 private final EarsConfig settings=SkinEarsClient.config;
 public EarsScreen(Screen previous){super(Component.literal("Skin Ears"));this.previous=previous;}
 @Override protected void init() {
  int left=width/2-110, top=Math.max(35,height/2-96);
  addRenderableWidget(Button.builder(Component.literal(settings.type.equals("CAT")?"[Cat ears]":"Cat ears"),b->{settings.type="CAT";settings.save();rebuildWidgets();}).bounds(left,top,106,20).build());
  addRenderableWidget(Button.builder(Component.literal(settings.type.equals("DOG")?"[Dog ears]":"Dog ears"),b->{settings.type="DOG";settings.save();rebuildWidgets();}).bounds(left+114,top,106,20).build());
  addRenderableWidget(Button.builder(Component.literal(settings.customColor?"Color: Custom":"Color: Match skin"),b->{
   if(!settings.customColor) settings.color=SkinEarsClient.COLORS.latestOwnColor();
   settings.customColor=!settings.customColor;settings.save();rebuildWidgets();
  }).bounds(left,top+30,220,20).build());
  for(int channel=0;channel<3;channel++){
   ChannelSlider slider=new ChannelSlider(left,top+61+channel*24,220,20,channel);
   slider.active=settings.customColor;addRenderableWidget(slider);
  }
  addRenderableWidget(Button.builder(Component.literal("Reset to default"),b->{settings.reset();settings.save();rebuildWidgets();}).bounds(left,top+145,106,20).build());
  addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(left+114,top+145,106,20).build());
 }
 @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick) {
  super.extractRenderState(graphics,mouseX,mouseY,partialTick);
  int top=Math.max(35,height/2-96);
  graphics.centeredText(font,title,width/2,top-22,0xFFFFFFFF);
  int color=settings.customColor?settings.color:SkinEarsClient.COLORS.latestOwnColor();
  graphics.fill(width/2-15,top+139,width/2+15,top+143,0xFF000000|color);
  graphics.centeredText(font,settings.customColor?String.format("Custom #%06X",color&0xFFFFFF):String.format("Auto #%06X",color&0xFFFFFF),width/2,top+176,0xFFDDDDDD);
  graphics.centeredText(font,"Matches skin top by default",width/2,top+190,0xFFAAAAAA);
 }
 @Override public void onClose(){settings.save();minecraft.gui.setScreen(previous);}
 @Override public void removed(){settings.save();}
 private final class ChannelSlider extends AbstractSliderButton {
  private final int channel;
  private ChannelSlider(int x,int y,int width,int height,int channel){
   super(x,y,width,height,Component.empty(),((settings.color>>(16-8*channel))&255)/255.0);
   this.channel=channel;updateMessage();
  }
  @Override protected void updateMessage(){
   String name=switch(channel){case 0->"Red";case 1->"Green";default->"Blue";};
   setMessage(Component.literal(name+": "+Math.round(value*255)));
  }
  @Override protected void applyValue(){
   int shift=16-8*channel, amount=(int)Math.round(value*255.0);
   settings.color=(settings.color&~(255<<shift))|(amount<<shift);settings.customColor=true;
  }
 }
}

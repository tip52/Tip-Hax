/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.world;

import com.example.addon.AddonTemplate;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import com.example.addon.mixin.AbstractSignEditScreenAccessor;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.StringSetting;

import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

public class AutoSign2b2t extends Module {


public enum MethodType {
    PACKET_METHOD,
    FINISH_EDITING_METHOD;  

    @Override
    public String toString() {
        switch(this) {
            case PACKET_METHOD: return "packet method";
            case FINISH_EDITING_METHOD: return "legit method";
            default: return super.toString();
        }
    }
}


    public AutoSign2b2t() {
        super(AddonTemplate.CATEGORY, "2b2t-auto-sign", "Writes signs with multiple methods, with a delay to work on 2b2t (minimum 375ms)");
    }
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

private String[] getTextFromSettings() {
    return new String[] {
        lineOne.get(),
        lineTwo.get(),
        lineThree.get(),
        lineFour.get()
    };
}


    private final Setting<String> lineOne = sgGeneral.add(new StringSetting.Builder()
            .name("line-one")
            .description("What to put on the first line of the sign.")
            .defaultValue("tip5")
            .build()
    );

        private final Setting<String> lineTwo = sgGeneral.add(new StringSetting.Builder()
            .name("line-two")
            .description("What to put on the second line of the sign.")
            .defaultValue("was")
            .build()
    );

    private final Setting<String> lineThree = sgGeneral.add(new StringSetting.Builder()
            .name("line-three")
            .description("What to put on the third line of the sign.")
            .defaultValue("here")
            .build()
    );

        private final Setting<String> lineFour = sgGeneral.add(new StringSetting.Builder()
            .name("line-four")
            .description("What to put on the fourth line of the sign.")
            .defaultValue("")
            .build()
    );

    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("delay")
        .description("The delay(ms) between writing the text and placing the sign.")
        .min(5.0)
        .sliderMax(1000.0)
        .defaultValue(375.0)
        .build()
    );

    private final Setting<MethodType> methodSetting = sgGeneral.add(new EnumSetting.Builder<MethodType>()
    .name("close method")
    .description("Select the method to use.")
    .defaultValue(MethodType.PACKET_METHOD)
    .build()
);


    @EventHandler
private void onOpenScreen(OpenScreenEvent event) {
    if (!(event.screen instanceof AbstractSignEditScreen)) return;

    String[] text = getTextFromSettings();
    if (text == null) return;

    SignBlockEntity sign = ((AbstractSignEditScreenAccessor) event.screen).getSign();

    MethodType selected1 = methodSetting.get();

    switch (selected1) {
        
    case FINISH_EDITING_METHOD -> ((AbstractSignEditScreenAccessor) event.screen).setMessages(getTextFromSettings()); // sets the text (very pro)

    }

    new Thread(() -> {
        try {
            Thread.sleep(delay.get().longValue());  // delay in ms
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mc.execute(() -> {
        MethodType selected = methodSetting.get();

        switch (selected) {
            //case CLOSE_METHOD -> ((AbstractSignEditScreenAccessor) event.screen).close();
            case FINISH_EDITING_METHOD -> ((AbstractSignEditScreenAccessor) event.screen).finishEditing();
            //case SET_SCREEN_METHOD -> mc.setScreen(null);
            case PACKET_METHOD ->  mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), true, text[0], text[1], text[2], text[3]));
        }
    });
    }).start();

    
    switch (selected1) {
        case PACKET_METHOD -> event.cancel();
    }

}
}
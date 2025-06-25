package com.example.addon.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSignEditScreen.class)
public interface AbstractSignEditScreenAccessor {
    @Accessor("blockEntity")
    SignBlockEntity getSign();

    @Accessor("text")
    SignText getText();

    @Accessor("messages")
    String[] getMessages();

    @Accessor("messages")
    void setMessages(String[] messages);

    @Invoker("finishEditing")
    void finishEditing();

    @Invoker("close")
    void close();
}

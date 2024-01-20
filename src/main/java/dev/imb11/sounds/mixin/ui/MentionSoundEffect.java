package dev.imb11.sounds.mixin.ui;

import dev.imb11.sounds.config.UISoundConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class MentionSoundEffect {
    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private float cooldownPeriod = 0f;

    @Inject(method = "render", at = @At("HEAD"))
    public void $cooldown_period(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        if(cooldownPeriod > 0) {
            cooldownPeriod -= this.client.getTickDelta() / 2f;
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"), cancellable = false)
    public void $mention_recieve_sound_effect(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        if(cooldownPeriod > 0 && UISoundConfig.get().enableChatSoundCooldown) {
            return;
        }

        cooldownPeriod = UISoundConfig.get().chatSoundCooldown * 20f;

        String messageString = message.getString();
        String username = client.getSession().getUsername();

        boolean isMention = messageString.toLowerCase().contains(username.toLowerCase());

        if(UISoundConfig.get().useAtForChatMentions) {
            isMention =  messageString.toLowerCase().contains("@" + username.toLowerCase());
        }

        if(UISoundConfig.get().ignoreSystemChats) {
            /*? >=1.20.2 {*/
            if(indicator == MessageIndicator.system() || indicator == MessageIndicator.chatError()) {
            /*?} else {*//*
            if(indicator == MessageIndicator.system()) {
            *//*?}*/
                return;
            }
        }

        if (isMention)
            UISoundConfig.get().mentionSoundEffect.playSound();
        else UISoundConfig.get().messageSoundEffect.playSound();
    }
}

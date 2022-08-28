package com.themysterys.mcciutils.mixin;

import com.themysterys.mcciutils.McciUtils;
import com.themysterys.mcciutils.util.config.ConfigInstance;
import com.themysterys.mcciutils.util.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.util.List;
import java.util.regex.Pattern;

import static com.themysterys.mcciutils.util.sounds.Sounds.FRIEND_SOUND;
import static com.themysterys.mcciutils.util.sounds.Sounds.MENTION_SOUND;

@Mixin(ChatHud.class)
public class ChatMixin {

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;)V")
    private void addMessage(Text message, CallbackInfo ci) {
        /*if (!McciUtils.isOnMCCI()) {
            return;
        }*/
        McciUtils.LOGGER.info("Message: " + message.getString());

        // Friend notifications
        if (ModConfig.INSTANCE.friendNotificationOptions != ConfigInstance.FriendNotificationOptions.OFF) {
            if (message.getString().matches("^(\\w{1,16}) has come online!")) {
                Pattern pattern = Pattern.compile("^(\\w{1,16}) has come online!");
                String name = pattern.matcher(message.getString()).replaceFirst("$1");
                if (name != null) {
                    if (ModConfig.INSTANCE.shouldShowFriendPopup()) {
                        ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
                        SystemToast.add(toastManager, SystemToast.Type.PERIODIC_NOTIFICATION, Text.of("Friend Joined"), Text.of(name));
                    }
                    if (MinecraftClient.getInstance().player != null && ModConfig.INSTANCE.shouldPlayFriendSound()) {
                        MinecraftClient.getInstance().player.playSound(FRIEND_SOUND, SoundCategory.MASTER, 1F,1F);
                    }
                }
            } else if (message.getString().matches("^(\\w{1,16}) has gone offline.")) {
                Pattern pattern = Pattern.compile("^(\\w{1,16}) has gone offline.");
                String name = pattern.matcher(message.getString()).replaceFirst("$1");
                if (name != null) {
                    if (ModConfig.INSTANCE.shouldShowFriendPopup()) {
                        ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
                        SystemToast.add(toastManager, SystemToast.Type.PERIODIC_NOTIFICATION, Text.of("Friend Left"), Text.of(name));
                    }
                    if (MinecraftClient.getInstance().player != null && ModConfig.INSTANCE.shouldPlayFriendSound()) {
                        MinecraftClient.getInstance().player.playSound(FRIEND_SOUND, SoundCategory.MASTER, 1F,1F);
                    }
                }
            }
        }

        // Chat mentions
        if (ModConfig.INSTANCE.enableChatMentions) {
            if (MinecraftClient.getInstance().player != null) {
                String username = MinecraftClient.getInstance().getSession().getUsername();

                Pattern pattern = Pattern.compile("^.. \\w{1,16}: ?.*" + username + ".*");
                // if message contains username but not at the beginning of the message
                // play sound
                if (pattern.matcher(message.getString()).find()) {

                    MinecraftClient.getInstance().player.playSound(MENTION_SOUND, SoundCategory.MASTER, 1F,1F);
                }
                // TODO: work out a way to color the username without causing a crash
                //message = Text.of(pattern.matcher(message.getString()).replaceAll(ModConfig.INSTANCE.chatMentionColor + "$1§r"));

            }
        }

        // Other notifiations
    }

    @ModifyVariable(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;)V", argsOnly = true)
    private Text modifyMessage(Text value) {
        System.out.println("Got message: " + value.getString());
        List<Text> fullMessage = value.getSiblings();
        String username = MinecraftClient.getInstance().getSession().getUsername();
        int i = 0;
        for (Text text : fullMessage) {
            System.out.println(i + ": " + text.getString());
            if (text.getString().contains(username)){
                System.out.println("Found username");
            }
            i++;
        }
        return value;
    }
}

package com.example.donutlarp.mixin;

import com.example.donutlarp.NameSwap;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    // Deliberately only touches the unsigned/system-message overload. Player-signed chat is left intact.
    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), argsOnly = true)
    private Text donutlarp$replaceNameInUnsignedMessages(Text message) {
        return NameSwap.replaceUnsignedText(message);
    }
}

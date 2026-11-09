package com.example.donutlarp.mixin;

import com.example.donutlarp.DonutLarpClient;
import com.example.donutlarp.NameSwap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void donutlarp$replaceLocalTabName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (DonutLarpClient.isActiveForCurrentServer() && DonutLarpClient.CONFIG.nameSwap
                && client.player != null && entry.getProfile().getId().equals(client.player.getUuid())) {
            cir.setReturnValue(NameSwap.getDisplayText());
        }
    }
}

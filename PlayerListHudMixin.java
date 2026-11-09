package com.example.donutlarp.mixin;

import com.example.donutlarp.DonutConfig;
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
public class PlayerListHudMixin {
    // Swaps your own entry in the tab list.
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true, require = 0)
    private void donutlarp$swapName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (!DonutConfig.active()) return;
        String me = MinecraftClient.getInstance().getSession().getUsername();
        if (!entry.getProfile().name().equals(me)) return;
        cir.setReturnValue(NameSwap.swap(cir.getReturnValue()));
    }
}

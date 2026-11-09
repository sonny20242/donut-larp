package com.example.donutlarp.mixin;

import com.example.donutlarp.DonutLarpClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void donutlarp$hideVanillaSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (DonutLarpClient.isActiveForCurrentServer() && DonutLarpClient.CONFIG.fakeBoard) {
            ci.cancel();
        }
    }
}

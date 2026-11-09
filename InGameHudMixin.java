package com.example.donutlarp.mixin;

import com.example.donutlarp.DonutConfig;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    // Hides the real sidebar while the fake board is showing.
    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true, require = 0)
    private void donutlarp$hideSidebar(CallbackInfo ci) {
        if (DonutConfig.active() && DonutConfig.get().fakeBoard) ci.cancel();
    }
}

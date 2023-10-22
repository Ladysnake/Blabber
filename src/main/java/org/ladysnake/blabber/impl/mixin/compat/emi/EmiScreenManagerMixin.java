package org.ladysnake.blabber.impl.mixin.compat.emi;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MinecraftClient;
import org.ladysnake.blabber.impl.client.BlabberDialogueScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiScreenManager.class)
public class EmiScreenManagerMixin {
    @Shadow private static MinecraftClient client;

    @Inject(method = "isDisabled", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void blabber$disableEmiInDialogues(CallbackInfoReturnable<Boolean> cir) {
        if (client.currentScreen instanceof BlabberDialogueScreen) {
            cir.setReturnValue(true);
        }
    }
}

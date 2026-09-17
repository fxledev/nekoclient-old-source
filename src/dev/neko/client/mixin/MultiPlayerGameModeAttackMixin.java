package dev.neko.client.mixin;

import dev.neko.client.NekoClient;
import dev.neko.client.module.ModuleManager;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_636.class)
public class MultiPlayerGameModeAttackMixin {
   @Inject(method = "attackEntity", at = @At("HEAD"))
   private void nekoclient$onAttack(class_1657 player, class_1297 target, CallbackInfo ci) {
      if (player == class_310.method_1551().field_1724 && target != player) {
         ModuleManager modules = NekoClient.modules();
         if (modules != null && modules.hitParticles != null && modules.hitParticles.isEnabled()) {
            modules.hitParticles.onHit(target);
         }
      }
   }
}

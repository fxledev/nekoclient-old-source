package dev.neko.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.neko.client.module.impl.FreecamModule;
import net.minecraft.class_329;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_329.class)
public class InGameHudFreecamMixin {
   @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"))
   private boolean nekoclient$showFreecamCrosshair(boolean firstPerson) {
      FreecamModule freecam = FreecamModule.get();
      return firstPerson || freecam != null && freecam.isActive();
   }
}

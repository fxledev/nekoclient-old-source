package dev.neko.client.mixin;

import dev.neko.client.module.impl.FreeLookModule;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1297.class)
public class EntityFreeLookMixin {
   @Inject(method = "changeLookDirection(DD)V", at = @At("HEAD"), cancellable = true)
   private void nekoclient$freeLookTurn(double d, double e, CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      if (this == mc.field_1724) {
         FreeLookModule freeLook = FreeLookModule.get();
         if (freeLook != null && freeLook.cameraMode()) {
            freeLook.addCameraLook(d, e);
            ci.cancel();
         }
      }
   }
}

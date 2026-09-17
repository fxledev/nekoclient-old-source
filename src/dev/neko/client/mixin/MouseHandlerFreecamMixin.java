package dev.neko.client.mixin;

import dev.neko.client.module.impl.FreecamModule;
import net.minecraft.class_310;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_312.class)
public abstract class MouseHandlerFreecamMixin {
   @Shadow
   private double field_1789;
   @Shadow
   private double field_1787;

   @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
   private void nekoclient$freecamMouse(double d, CallbackInfo ci) {
      FreecamModule freecam = FreecamModule.get();
      if (freecam != null && freecam.isActive()) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1755 == null) {
            double sensitivity = (Double)mc.field_1690.method_42495().method_41753() * 0.6 + 0.2;
            double factor = sensitivity * sensitivity * sensitivity * 8.0;
            double dx = this.field_1789 * factor * freecam.getLookSensitivity();
            double dy = this.field_1787 * factor * freecam.getLookSensitivity();
            float newYaw = freecam.getCurrentYaw() + (float)dx * 0.15F;
            float newPitch = freecam.getCurrentPitch() + (float)dy * 0.15F;
            freecam.setRotation(newYaw, newPitch);
            this.field_1789 = 0.0;
            this.field_1787 = 0.0;
            ci.cancel();
         }
      }
   }
}

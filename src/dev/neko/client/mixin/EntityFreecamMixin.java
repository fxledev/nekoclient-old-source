package dev.neko.client.mixin;

import dev.neko.client.module.impl.FreecamModule;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1297.class)
public class EntityFreecamMixin {
   @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
   private void nekoclient$freecamSeeOwnBody(class_1657 viewer, CallbackInfoReturnable<Boolean> cir) {
      class_1297 self = (class_1297)this;
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && self == mc.field_1724 && viewer == mc.field_1724) {
         FreecamModule freecam = FreecamModule.get();
         if (freecam != null && freecam.isActive() && freecam.isShowPlayerModel()) {
            cir.setReturnValue(false);
         }
      }
   }
}

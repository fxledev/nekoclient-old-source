package dev.neko.client.mixin;

import dev.neko.client.module.impl.FreecamModule;
import net.minecraft.class_310;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_743.class)
public class KeyboardInputFreecamMixin {
   @Inject(method = "tick", at = @At("TAIL"))
   private void nekoclient$freecamReapplyCachedBodyInput(CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1724.field_3913 == (class_744)this) {
         FreecamModule f = FreecamModule.get();
         if (f != null && f.isActive()) {
            FreecamModule.reapplyBodyInput(mc);
         }
      }
   }
}

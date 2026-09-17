package dev.neko.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.neko.client.NekoClient;
import dev.neko.client.module.ModuleManager;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1309.class)
public class LivingEntitySwingMixin {
   @ModifyReturnValue(method = "getHandSwingDuration", at = @At("RETURN"))
   private int nekoclient$swingSpeed(int original) {
      class_1309 self = (class_1309)this;
      if (class_310.method_1551().field_1724 != self) {
         return original;
      } else {
         ModuleManager modules = NekoClient.modules();
         if (modules != null && modules.swingSpeed != null && modules.swingSpeed.isEnabled()) {
            float multiplier = modules.swingSpeed.multiplier();
            return multiplier <= 0.01F ? original : Math.max(1, Math.round(original / multiplier));
         } else {
            return original;
         }
      }
   }
}

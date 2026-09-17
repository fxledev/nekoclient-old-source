package dev.neko.client.mixin;

import dev.neko.client.module.impl.FreecamModule;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4184.class)
public abstract class CameraFreecamMixin {
   @Shadow
   protected abstract void method_19322(class_243 var1);

   @Shadow
   protected abstract void method_19325(float var1, float var2);

   @Inject(method = "update", at = @At("TAIL"))
   private void nekoclient$freecam(class_1937 level, class_1297 entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
      FreecamModule freecam = FreecamModule.get();
      if (freecam != null && freecam.isActive()) {
         freecam.updatePerFrame(class_310.method_1551());
         freecam.updateRenderState(partialTick);
         this.method_19325(freecam.getRenderYaw(), freecam.getRenderPitch());
         this.method_19322(freecam.getRenderPos());
      }
   }
}

package dev.neko.client.mixin;

import dev.neko.client.module.impl.FreeLookModule;
import dev.neko.client.module.impl.FreecamModule;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(class_4184.class)
public abstract class CameraFreeLookMixin {
   @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
   private void nekoclient$freeLookRotation(Args args) {
      FreeLookModule freeLook = FreeLookModule.get();
      if (freeLook != null && freeLook.isActive()) {
         FreecamModule freecam = FreecamModule.get();
         if (freecam == null || !freecam.isActive()) {
            args.set(0, freeLook.getCameraYaw());
            args.set(1, freeLook.getCameraPitch());
         }
      }
   }

   @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
   private void nekoclient$freeLookThroughWalls(float distance, CallbackInfoReturnable<Float> cir) {
      FreeLookModule freeLook = FreeLookModule.get();
      if (freeLook != null && freeLook.seeThroughWalls()) {
         FreecamModule freecam = FreecamModule.get();
         if (freecam == null || !freecam.isActive()) {
            cir.setReturnValue(distance);
         }
      }
   }

   @Inject(method = "update", at = @At("TAIL"))
   private void nekoclient$freeLookChunkLoading(
      class_1937 world, class_1297 entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci
   ) {
      FreeLookModule freeLook = FreeLookModule.get();
      if (freeLook != null) {
         freeLook.syncChunkLoading((class_4184)this);
      }
   }
}

package dev.neko.client.mixin;

import dev.neko.client.suschunk.ServerLightCache;
import net.minecraft.class_2672;
import net.minecraft.class_2676;
import net.minecraft.class_310;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_634.class)
public class LightPacketMixin {
   @Inject(method = "onLightUpdate", at = @At("TAIL"))
   private void nekoclient$captureLightUpdate(class_2676 packet, CallbackInfo ci) {
      ServerLightCache.get().ingest(packet.method_11558(), packet.method_11554(), packet.method_38600(), class_310.method_1551().field_1687);
   }

   @Inject(method = "onChunkData", at = @At("TAIL"))
   private void nekoclient$captureChunkLight(class_2672 packet, CallbackInfo ci) {
      ServerLightCache.get().ingest(packet.method_11523(), packet.method_11524(), packet.method_38599(), class_310.method_1551().field_1687);
   }
}

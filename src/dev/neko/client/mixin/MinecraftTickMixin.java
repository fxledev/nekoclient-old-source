package dev.neko.client.mixin;

import dev.neko.agent.NekoAgent;
import dev.neko.agent.NekoEvents;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_310.class)
public class MinecraftTickMixin {
   @Inject(method = "tick", at = @At("HEAD"))
   private void neko$tickHead(CallbackInfo ci) {
      class_310 mc = (class_310)this;
      NekoAgent.ensureInit(mc);
      NekoEvents.fireStartTick(mc);
   }

   @Inject(method = "tick", at = @At("RETURN"))
   private void neko$tickTail(CallbackInfo ci) {
      NekoEvents.fireEndTick((class_310)this);
   }
}

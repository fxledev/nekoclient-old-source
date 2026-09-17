package dev.neko.client.mixin;

import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_437.class)
public class ScreenInitMixin {
   private static boolean logged;

   @Inject(method = "init()V", at = @At("HEAD"))
   private void neko$markAlive(CallbackInfo ci) {
      if (!logged) {
         logged = true;
         System.out.println("[NekoAgent] mixin-alive: " + ((class_437)this).getClass().getName());
      }
   }
}

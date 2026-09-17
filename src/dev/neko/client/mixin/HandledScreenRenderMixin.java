package dev.neko.client.mixin;

import dev.neko.agent.NekoScreens;
import net.minecraft.class_332;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_465.class)
public class HandledScreenRenderMixin {
   @Inject(method = "renderMain", at = @At("TAIL"))
   private void neko$gamblePanel(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      NekoScreens.renderCurrent(context, mouseX, mouseY);
   }
}

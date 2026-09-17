package dev.neko.client.mixin;

import dev.neko.client.render.WorldNametagRenderer;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_329.class)
public class GuiNametagEquipmentMixin {
   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("TAIL"))
   private void nekoclient$nametagEquipment(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo ci) {
      WorldNametagRenderer.renderEquipment(guiGraphics);
   }
}

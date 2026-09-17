package dev.neko.client.mixin;

import dev.neko.agent.NekoScreens;
import dev.neko.client.NekoClient;
import dev.neko.client.hud.HudDragController;
import dev.neko.client.util.CpsTracker;
import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_312.class)
public class MouseHandlerMixin {
   @Inject(method = "onMouseButton(JLnet/minecraft/client/input/MouseInput;I)V", at = @At("HEAD"), cancellable = true)
   private void nekoclient$onButton(long window, class_11910 buttonInfo, int action, CallbackInfo ci) {
      class_310 minecraft = class_310.method_1551();
      if (window == minecraft.method_22683().method_4490()) {
         if (action == 1 && minecraft.field_1755 == null) {
            CpsTracker.onClick(buttonInfo.comp_4801());
         }

         if (action == 1 && minecraft.field_1755 != null) {
            try {
               if (NekoScreens.click(minecraft.field_1729.method_1603(), minecraft.field_1729.method_1604(), buttonInfo.comp_4801())) {
                  ci.cancel();
                  return;
               }
            } catch (Throwable var8) {
            }
         }

         if (minecraft.field_1755 instanceof class_408 && buttonInfo.comp_4801() == 0 && NekoClient.hud() != null) {
            if (action == 1) {
               if (HudDragController.tryStartDrag(NekoClient.hud())) {
                  ci.cancel();
               }
            } else if (action == 0 && HudDragController.isDragging()) {
               HudDragController.stopDrag();
               NekoClient.config().save();
               ci.cancel();
            }
         }
      }
   }

   @Inject(method = "onMouseScroll(JDD)V", at = @At("HEAD"), cancellable = true)
   private void nekoclient$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
      class_310 minecraft = class_310.method_1551();
      if (window == minecraft.method_22683().method_4490()) {
         if (minecraft.field_1755 == null && NekoClient.modules() != null && NekoClient.modules().freecam != null && NekoClient.modules().freecam.isActive()) {
            NekoClient.modules().freecam.adjustSpeed(yOffset);
            ci.cancel();
         } else if (minecraft.field_1755 instanceof class_408 && NekoClient.hud() != null && HudDragController.tryResize(NekoClient.hud(), yOffset)) {
            ci.cancel();
         }
      }
   }
}

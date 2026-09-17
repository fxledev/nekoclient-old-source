package dev.neko.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.neko.client.module.impl.FreecamModule;
import dev.neko.client.render.BlurHook;
import dev.neko.client.render.OverlayRenderer;
import dev.neko.client.render.WorldProjection;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_757.class)
public class GameRendererMixin {
   @Inject(
      method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrame()V", shift = Shift.AFTER)
   )
   private void nekoclient$renderOverlay(class_9779 deltaTracker, boolean bl, CallbackInfo ci) {
      OverlayRenderer.render();
   }

   @Inject(
      method = "renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V",
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/systems/ProjectionType;)V"
      )
   )
   private void nekoclient$captureProjection(class_9779 deltaTracker, CallbackInfo ci, @Local(ordinal = 0) Matrix4f matrix4f) {
      WorldProjection.capture(matrix4f, deltaTracker.method_60637(false));
   }

   @ModifyArg(
      method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gl/GlobalSettings;set(IIDJLnet/minecraft/client/render/RenderTickCounter;ILnet/minecraft/client/render/Camera;Z)V"
      ),
      index = 5
   )
   private int nekoclient$overrideBlurRadius(int blurriness) {
      return BlurHook.apply(blurriness);
   }

   @ModifyExpressionValue(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"))
   private boolean nekoclient$freecamRenderFirstPersonHands(boolean isFirstPerson) {
      FreecamModule freecam = FreecamModule.get();
      return freecam != null && freecam.isActive() && freecam.renderHands() ? true : isFirstPerson;
   }
}

package dev.neko.client.mixin;

import dev.neko.client.NekoClient;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.render.AccessoryRenderer;
import dev.neko.client.render.BlockEntityEspRenderer;
import dev.neko.client.render.BlockEspRenderer;
import dev.neko.client.render.ClusterEspRenderer;
import dev.neko.client.render.EntityEspRenderer;
import dev.neko.client.render.HitParticleRenderer;
import dev.neko.client.render.LightDebugRenderer;
import dev.neko.client.render.PrimeChunkRenderer;
import dev.neko.client.render.StorageEspRenderer;
import dev.neko.client.render.SusChunkRenderer;
import net.minecraft.class_11658;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import net.minecraft.class_4597.class_4598;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_761.class)
public class LevelRendererMixin {
   @Inject(
      method = "renderTargetBlockOutline(Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/util/math/MatrixStack;ZLnet/minecraft/client/render/state/WorldRenderState;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void nekoclient$renderWorldOverlays(
      class_4598 bufferSource, class_4587 poseStack, boolean translucentPass, class_11658 levelRenderState, CallbackInfo ci
   ) {
      ModuleManager modules = NekoClient.modules();
      if (modules != null) {
         if (translucentPass && modules.susChunkFinder.isEnabled()) {
            SusChunkRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.susChunkFinder);
         }

         if (translucentPass && modules.primeChunkFinder.isEnabled()) {
            PrimeChunkRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.primeChunkFinder);
         }

         if (translucentPass && modules.clusterEsp.isEnabled()) {
            ClusterEspRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.clusterEsp);
         }

         if (translucentPass && modules.lightDebug.isEnabled()) {
            LightDebugRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.lightDebug);
         }

         if (translucentPass && modules.storageEsp != null && modules.storageEsp.isEnabled()) {
            StorageEspRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.storageEsp);
         }

         if (translucentPass && modules.blockEsp != null && modules.blockEsp.isEnabled()) {
            BlockEspRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.blockEsp);
         }

         if (translucentPass && modules.blockEntityEsp != null && modules.blockEntityEsp.isEnabled()) {
            BlockEntityEspRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.blockEntityEsp);
         }

         if (translucentPass && modules.playerEsp != null && modules.playerEsp.isEnabled()) {
            EntityEspRenderer.renderPlayers(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.playerEsp);
         }

         if (translucentPass && modules.hitParticles != null && modules.hitParticles.isEnabled()) {
            HitParticleRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.hitParticles);
         }

         if (translucentPass && modules.customAccessories != null && modules.customAccessories.isEnabled()) {
            AccessoryRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.customAccessories);
         }
      }
   }
}

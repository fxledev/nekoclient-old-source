package dev.neko.client.render;

import dev.neko.client.module.impl.PlayerEspModule;
import dev.neko.client.util.Colors;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_4597.class_4598;
import org.joml.Vector3fc;

public final class EntityEspRenderer {
   private static final float TRACER_WIDTH = 1.2F;

   private EntityEspRenderer() {
   }

   public static void renderPlayers(class_4598 bufferSource, class_4587 poseStack, class_243 cam, PlayerEspModule module) {
      class_310 mc = class_310.method_1551();
      class_638 level = mc.field_1687;
      if (level != null && mc.field_1724 != null) {
         int color = module.color.get();
         boolean tracers = module.tracers.get();
         Vector3fc forward = tracers ? mc.field_1773.method_19418().method_19335() : null;

         for (class_742 player : level.method_18456()) {
            if (player != mc.field_1724 && player.method_5805() && !player.method_7325()) {
               class_238 playerBox = interpolatedBox(player);
               box(bufferSource, poseStack, cam, playerBox, color);
               if (tracers) {
                  tracer(bufferSource, poseStack, cam, forward, playerBox, color);
               }
            }
         }

         EspBoxRenderer.flush(bufferSource);
      }
   }

   private static void box(class_4598 bufferSource, class_4587 poseStack, class_243 cam, class_238 b, int color) {
      EspBoxRenderer.outline(bufferSource, poseStack, cam, b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324, color, 2.0F);
      EspBoxRenderer.fill(
         bufferSource, poseStack, cam, b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324, Colors.withAlpha(color, 0.18F)
      );
   }

   private static class_238 interpolatedBox(class_1297 entity) {
      class_238 box = entity.method_5829();
      class_243 currentPos = new class_243(entity.method_23317(), entity.method_23318(), entity.method_23321());
      class_243 renderPos = entity.method_30950(WorldProjection.partialTick());
      return box.method_989(
         renderPos.field_1352 - currentPos.field_1352, renderPos.field_1351 - currentPos.field_1351, renderPos.field_1350 - currentPos.field_1350
      );
   }

   private static void tracer(class_4598 bufferSource, class_4587 poseStack, class_243 cam, Vector3fc forward, class_238 b, int color) {
      EspBoxRenderer.tracer(
         bufferSource,
         poseStack,
         cam,
         forward,
         (b.field_1323 + b.field_1320) / 2.0,
         (b.field_1322 + b.field_1325) / 2.0,
         (b.field_1321 + b.field_1324) / 2.0,
         Colors.withAlpha(color, 0.72F),
         1.2F
      );
   }
}

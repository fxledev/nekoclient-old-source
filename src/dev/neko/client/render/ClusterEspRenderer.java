package dev.neko.client.render;

import dev.neko.client.module.Modules;
import dev.neko.client.util.Colors;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597.class_4598;
import org.joml.Vector3fc;

public final class ClusterEspRenderer {
   private static final int COLOR = -6596097;
   private static final int MAX_BOXES = 400;

   private ClusterEspRenderer() {
   }

   public static void render(class_4598 bufferSource, class_4587 poseStack, class_243 camera, Modules.ClusterEspModule module) {
      int rendered = 0;
      Vector3fc forward = class_310.method_1551().field_1773.method_19418().method_19335();

      for (class_2338 pos : module.scanner.amethystCells()) {
         if (rendered++ >= 400) {
            break;
         }

         EspBoxRenderer.fill(
            bufferSource,
            poseStack,
            camera,
            pos.method_10263(),
            pos.method_10264(),
            pos.method_10260(),
            pos.method_10263() + 1.0,
            pos.method_10264() + 1.0,
            pos.method_10260() + 1.0,
            Colors.withAlpha(-6596097, 0.22F)
         );
         if (module.tracers.get()) {
            EspBoxRenderer.tracer(
               bufferSource,
               poseStack,
               camera,
               forward,
               pos.method_10263() + 0.5,
               pos.method_10264() + 0.5,
               pos.method_10260() + 0.5,
               Colors.withAlpha(-6596097, 0.8F),
               1.2F
            );
         }
      }

      EspBoxRenderer.flush(bufferSource);
   }
}

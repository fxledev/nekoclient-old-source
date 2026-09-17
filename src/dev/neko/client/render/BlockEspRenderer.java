package dev.neko.client.render;

import dev.neko.client.module.impl.BlockEspModule;
import dev.neko.client.settings.BlockListSetting;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2248;
import net.minecraft.class_243;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597.class_4598;
import org.joml.Vector3fc;

public final class BlockEspRenderer {
   private static final int MAX_CHUNK_RADIUS = 12;
   private static final int MAX_RESULTS = 8000;
   private static final double INSET = 0.002;
   private static final IncrementalScan<BlockEspRenderer.Hit> SCAN = new IncrementalScan<>(48, 80000, 20);
   private static Set<class_2248> lastWanted = Set.of();

   private BlockEspRenderer() {
   }

   public static void clear() {
      SCAN.clear();
      lastWanted = Set.of();
   }

   public static int cachedCount() {
      return SCAN.get().size();
   }

   public static void scan(BlockEspModule module) {
      Set<class_2248> wanted = new HashSet<>();

      for (BlockListSetting.Target target : module.targets.targets()) {
         if (target.enabled.get() && target.block() != null) {
            wanted.add(target.block());
         }
      }

      if (wanted.isEmpty()) {
         SCAN.clear();
         lastWanted = Set.of();
      } else {
         if (!wanted.equals(lastWanted)) {
            lastWanted = wanted;
            SCAN.markDirty();
         }

         int chunkRadius = Math.min(12, (Integer)class_310.method_1551().field_1690.method_42503().method_41753());
         SCAN.tick(chunkRadius, (chunk, out) -> scanChunk(chunk, wanted, out));
      }
   }

   private static int scanChunk(class_2818 chunk, Set<class_2248> wanted, List<BlockEspRenderer.Hit> out) {
      class_2826[] sections = chunk.method_12006();
      int minSectionY = chunk.method_32891();
      int baseX = chunk.method_12004().method_8326();
      int baseZ = chunk.method_12004().method_8328();
      int blocks = 0;
      if (out.size() >= 8000) {
         return 0;
      } else {
         for (int s = 0; s < sections.length; s++) {
            class_2826 section = sections[s];
            if (!section.method_38292() && section.method_19523(st -> wanted.contains(st.method_26204()))) {
               int baseY = minSectionY + s << 4;
               blocks += 4096;

               for (int y = 0; y < 16; y++) {
                  for (int z = 0; z < 16; z++) {
                     for (int x = 0; x < 16; x++) {
                        class_2248 block = section.method_12254(x, y, z).method_26204();
                        if (wanted.contains(block)) {
                           out.add(new BlockEspRenderer.Hit(baseX + x, baseY + y, baseZ + z, block));
                           if (out.size() >= 8000) {
                              return blocks;
                           }
                        }
                     }
                  }
               }
            }
         }

         return blocks;
      }
   }

   public static void render(class_4598 bufferSource, class_4587 poseStack, class_243 cam, BlockEspModule module) {
      List<BlockEspRenderer.Hit> snapshot = SCAN.get();
      if (!snapshot.isEmpty()) {
         Map<class_2248, Integer> colorByBlock = new HashMap<>();

         for (BlockListSetting.Target target : module.targets.targets()) {
            if (target.block() != null) {
               colorByBlock.put(target.block(), target.color.get());
            }
         }

         int fallback = module.lineColor.get();
         int alpha = Math.clamp((long)module.highlightAlpha.getInt(), 0, 255);
         boolean drawLines = module.shapeMode.is("Both") || module.shapeMode.is("Lines");
         boolean drawFill = module.shapeMode.is("Both") || module.shapeMode.is("Sides");
         boolean drawTracers = module.tracers.get() && module.tracer.get();

         for (BlockEspRenderer.Hit hit : snapshot) {
            int rgb = colorByBlock.getOrDefault(hit.block(), fallback) & 16777215;
            int argb = rgb | alpha << 24;
            if (drawFill) {
               EspBoxRenderer.fill(
                  bufferSource,
                  poseStack,
                  cam,
                  hit.x() + 0.002,
                  hit.y() + 0.002,
                  hit.z() + 0.002,
                  hit.x() + 1 - 0.002,
                  hit.y() + 1 - 0.002,
                  hit.z() + 1 - 0.002,
                  argb
               );
            }

            if (drawLines) {
               EspBoxRenderer.outline(
                  bufferSource,
                  poseStack,
                  cam,
                  hit.x() + 0.002,
                  hit.y() + 0.002,
                  hit.z() + 0.002,
                  hit.x() + 1 - 0.002,
                  hit.y() + 1 - 0.002,
                  hit.z() + 1 - 0.002,
                  argb,
                  1.6F
               );
            }
         }

         if (drawTracers) {
            int tracerAlpha = module.tracerColor.get() >>> 24 & 0xFF;
            if (tracerAlpha == 0) {
               tracerAlpha = 200;
            }

            Vector3fc forward = class_310.method_1551().field_1773.method_19418().method_19335();

            for (BlockEspRenderer.Hit hit : snapshot) {
               int rgbx = colorByBlock.getOrDefault(hit.block(), fallback) & 16777215;
               int col = rgbx | tracerAlpha << 24;
               EspBoxRenderer.tracer(bufferSource, poseStack, cam, forward, hit.x() + 0.5, hit.y() + 0.5, hit.z() + 0.5, col, 1.2F);
            }
         }

         EspBoxRenderer.flush(bufferSource);
      }
   }

   private record Hit(int x, int y, int z, class_2248 block) {
   }
}

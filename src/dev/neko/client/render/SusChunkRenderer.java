package dev.neko.client.render;

import dev.neko.client.module.Modules;
import dev.neko.client.suschunk.SusChunkScanner;
import dev.neko.client.util.Colors;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4597.class_4598;

public final class SusChunkRenderer {
   private static final int DEXTER_RED = -4965316;
   private static final double FIXED_CHUNK_Y = 64.0;
   private static final int[][] SPREAD_PATTERNS = new int[][]{{1, 4}, {2, 4}, {1, 2}, {3, 1}, {5, 4}, {5, 6}};

   private SusChunkRenderer() {
   }

   public static void reset() {
   }

   public static String debugState() {
      StringBuilder sb = new StringBuilder();
      return sb.toString();
   }

   public static void render(class_4598 bufferSource, class_4587 poseStack, class_243 camera, Modules.SusChunkFinderModule module) {
      SusChunkScanner scanner = module.scanner;

      for (long key : spreadChunks(scanner)) {
         double x0 = class_1923.method_8325(key) * 16.0;
         double z0 = class_1923.method_8332(key) * 16.0;
         drawQuad(bufferSource, poseStack, camera, x0, z0, x0 + 16.0, z0 + 16.0, 64.0, 0.39215687F);
      }

      FlatOverlay.flush(bufferSource);
   }

   private static void drawQuad(
      class_4598 bufferSource, class_4587 poseStack, class_243 camera, double x0, double z0, double x1, double z1, double y, float fill
   ) {
      FlatOverlay.fillQuad(bufferSource, poseStack, camera, x0, z0, x1, z1, y, Colors.withAlpha(-4965316, fill));
   }

   private static Set<Long> spreadChunks(SusChunkScanner scanner) {
      Set<Long> spread = new HashSet<>();

      for (SusChunkScanner.Flag flag : scanner.flags()) {
         long key = flag.chunkKey();
         int[] pattern = SPREAD_PATTERNS[Math.floorMod(Long.hashCode(key), SPREAD_PATTERNS.length)];
         int startX = -(pattern[0] / 2);
         int startZ = -(pattern[1] / 2);
         int chunkX = class_1923.method_8325(key);
         int chunkZ = class_1923.method_8332(key);

         for (int dx = 0; dx < pattern[0]; dx++) {
            for (int dz = 0; dz < pattern[1]; dz++) {
               spread.add(class_1923.method_8331(chunkX + startX + dx, chunkZ + startZ + dz));
            }
         }
      }

      return spread;
   }
}

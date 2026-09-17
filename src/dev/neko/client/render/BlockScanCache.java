package dev.neko.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_746;

public final class BlockScanCache {
   private final Predicate<class_2680> predicate;
   private final int intervalTicks;
   private final int maxChunkRadius;
   private final int maxResults;
   private final double rangeSq;
   private volatile List<class_2338> cache = List.of();
   private int tickCounter;

   public BlockScanCache(Predicate<class_2680> predicate, int intervalTicks, int maxChunkRadius, int maxResults, double range) {
      this.predicate = predicate;
      this.intervalTicks = intervalTicks;
      this.maxChunkRadius = maxChunkRadius;
      this.maxResults = maxResults;
      this.rangeSq = range * range;
   }

   public List<class_2338> get() {
      return this.cache;
   }

   public void clear() {
      this.cache = List.of();
      this.tickCounter = 0;
   }

   public void scan() {
      if (this.tickCounter++ % this.intervalTicks == 0) {
         class_310 mc = class_310.method_1551();
         class_638 level = mc.field_1687;
         class_746 player = mc.field_1724;
         if (level != null && player != null) {
            int radius = Math.min(this.maxChunkRadius, (Integer)mc.field_1690.method_42503().method_41753());
            int pcx = player.method_31476().field_9181;
            int pcz = player.method_31476().field_9180;
            List<class_2338> found = new ArrayList<>();

            for (int cx = pcx - radius; cx <= pcx + radius && found.size() < this.maxResults; cx++) {
               for (int cz = pcz - radius; cz <= pcz + radius && found.size() < this.maxResults; cz++) {
                  class_2818 chunk = level.method_8497(cx, cz);
                  class_2826[] sections = chunk.method_12006();
                  int minSectionY = chunk.method_32891();
                  int baseX = chunk.method_12004().method_8326();
                  int baseZ = chunk.method_12004().method_8328();

                  for (int s = 0; s < sections.length; s++) {
                     class_2826 section = sections[s];
                     if (!section.method_38292() && section.method_19523(this.predicate)) {
                        int baseY = minSectionY + s << 4;

                        for (int y = 0; y < 16; y++) {
                           for (int z = 0; z < 16; z++) {
                              for (int x = 0; x < 16; x++) {
                                 if (this.predicate.test(section.method_12254(x, y, z))) {
                                    int wx = baseX + x;
                                    int wy = baseY + y;
                                    int wz = baseZ + z;
                                    if (!(player.method_5649(wx + 0.5, wy + 0.5, wz + 0.5) > this.rangeSq)) {
                                       found.add(new class_2338(wx, wy, wz));
                                       if (found.size() >= this.maxResults) {
                                          break;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.cache = found;
         }
      }
   }
}

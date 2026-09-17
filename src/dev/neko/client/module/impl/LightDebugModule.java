package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_310;

public final class LightDebugModule extends Module {
   public static final int MIN_LIGHT = 1;
   public static final int MAX_LIGHT = 15;
   private static final int MIN_Y = -51;
   private static final int MAX_Y = 20;
   private final Map<class_1923, List<LightDebugModule.Cell>> cells = new ConcurrentHashMap<>();
   private final AtomicBoolean scanning = new AtomicBoolean();
   private ExecutorService executor;
   private class_1923 lastChunk;
   private int ticks;

   public LightDebugModule() {
      super("LightDebug", "Shows block light as black (dark) and white (lit)", Category.RENDER);
   }

   @Override
   protected void onEnable() {
      this.cells.clear();
      this.lastChunk = null;
      this.ticks = 0;
      this.scanning.set(false);
   }

   @Override
   protected void onDisable() {
      this.cells.clear();
      if (this.executor != null) {
         this.executor.shutdownNow();
      }
   }

   @Override
   public void onTick() {
      class_310 client = class_310.method_1551();
      if (client.field_1687 != null && client.field_1724 != null) {
         class_1923 center = client.field_1724.method_31476();
         boolean moved = !center.equals(this.lastChunk);
         this.lastChunk = center;
         if ((++this.ticks % 10 == 0 || moved) && this.scanning.compareAndSet(false, true)) {
            if (this.executor == null || this.executor.isShutdown()) {
               this.executor = Executors.newSingleThreadExecutor(task -> {
                  Thread thread = new Thread(task, "lightdebug-scan");
                  thread.setDaemon(true);
                  return thread;
               });
            }

            List<class_1923> positions = new ArrayList<>();

            for (int x = -3; x <= 3; x++) {
               for (int z = -3; z <= 3; z++) {
                  class_1923 position = new class_1923(center.field_9181 + x, center.field_9180 + z);
                  if (client.field_1687.method_2935().method_12126(position.field_9181, position.field_9180, false) != null) {
                     positions.add(position);
                  }
               }
            }

            this.cells
               .keySet()
               .removeIf(positionx -> Math.abs(positionx.field_9181 - center.field_9181) > 4 || Math.abs(positionx.field_9180 - center.field_9180) > 4);
            this.executor.submit(() -> {
               try {
                  for (int index = 0; index < positions.size(); index++) {
                     class_1923 positionx = positions.get(index);
                     List<LightDebugModule.Cell> found = this.scan(positionx);
                     if (found.isEmpty()) {
                        this.cells.remove(positionx);
                     } else {
                        this.cells.put(positionx, found);
                     }
                  }
               } finally {
                  this.scanning.set(false);
               }
            });
         }
      }
   }

   private List<LightDebugModule.Cell> scan(class_1923 position) {
      List<LightDebugModule.Cell> found = new ArrayList<>();
      class_310 client = class_310.method_1551();
      int startX = position.field_9181 << 4;
      int startZ = position.field_9180 << 4;

      for (int y = -51; y <= 20; y++) {
         for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
               class_2338 block = new class_2338(startX + x, y, startZ + z);
               int light = client.field_1687.method_8314(class_1944.field_9282, block);
               if (light >= 1 && light <= 15) {
                  found.add(new LightDebugModule.Cell(block, light));
               }
            }
         }
      }

      return found;
   }

   public Map<class_1923, List<LightDebugModule.Cell>> cells() {
      return this.cells;
   }

   public static final class Cell {
      private final class_2338 pos;
      private final int light;

      private Cell(class_2338 pos, int light) {
         this.pos = pos;
         this.light = light;
      }

      public class_2338 pos() {
         return this.pos;
      }

      public int light() {
         return this.light;
      }
   }
}

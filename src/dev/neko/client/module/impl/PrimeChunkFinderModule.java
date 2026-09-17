package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2462;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_4482;

public class PrimeChunkFinderModule extends Module {
   public final ConcurrentHashMap<class_1923, String> markedChunks = new ConcurrentHashMap<>();
   public final AtomicBoolean scanning = new AtomicBoolean(false);
   private ExecutorService executor;
   private long lastScanTime = 0L;
   private long lastMarkTime = 0L;
   private long lastRepScanTime = 0L;
   private long scanStartTime = 0L;
   private boolean scanActive = false;
   private static final long SCAN_INTERVAL_MS = 20000L;
   private static final long REPEATER_SCAN_INTERVAL_MS = 200L;
   private static final long COOLDOWN_MS = 50000L;
   private static final float SCAN_DURATION_MS = 800.0F;

   public PrimeChunkFinderModule() {
      super("Prime Chunk Finder", "Detects repeater chunks and base indicators", Category.RENDER);
   }

   @Override
   public void onEnable() {
      this.markedChunks.clear();
      this.lastScanTime = 0L;
      this.lastMarkTime = 0L;
      this.lastRepScanTime = 0L;
      this.scanActive = false;
   }

   @Override
   public void onDisable() {
      this.markedChunks.clear();
      if (this.executor != null) {
         this.executor.shutdownNow();
      }

      this.scanning.set(false);
      this.scanActive = false;
   }

   @Override
   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (this.scanActive && (float)(System.currentTimeMillis() - this.scanStartTime) > 800.0F) {
            this.scanActive = false;
         }

         long now = System.currentTimeMillis();
         if (now - this.lastRepScanTime >= 200L && !this.scanning.get()) {
            this.lastRepScanTime = now;
            class_1923 chunkPos = mc.field_1724.method_31476();
            int radius = Math.min(mc.field_1690.method_38521(), 8);
            ArrayList<class_1923> chunkPositions = new ArrayList<>();
            ArrayList<class_2818> chunks = new ArrayList<>();

            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
               for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                  class_1923 chunkPos2 = new class_1923(chunkPos.field_9181 + offsetX, chunkPos.field_9180 + offsetZ);
                  class_2818 chunk = mc.field_1687.method_2935().method_12126(chunkPos2.field_9181, chunkPos2.field_9180, false);
                  if (chunk != null && !chunk.method_12223()) {
                     chunkPositions.add(chunkPos2);
                     chunks.add(chunk);
                  }
               }
            }

            if (this.executor == null || this.executor.isShutdown()) {
               this.executor = Executors.newSingleThreadExecutor(r -> {
                  Thread thread = new Thread(r, "prime-repeater-scan");
                  thread.setDaemon(true);
                  return thread;
               });
            }

            this.executor.submit(() -> {
               for (int i = 0; i < chunkPositions.size(); i++) {
                  class_2818 chunk5 = chunks.get(i);
                  int count = 0;

                  for (class_2826 section : chunk5.method_12006()) {
                     if (section != null && !section.method_38292() && section.method_19523(statex -> statex.method_27852(class_2246.field_10450))) {
                        for (int x = 0; x < 16; x++) {
                           for (int y = 0; y < 16; y++) {
                              for (int z = 0; z < 16; z++) {
                                 class_2680 state = section.method_12254(x, y, z);
                                 if (state.method_27852(class_2246.field_10450) && (Boolean)state.method_11654(class_2462.field_10911)) {
                                    if (++count >= 3) {
                                       this.markedChunks.clear();
                                       this.markedChunks.put(chunkPositions.get(i), "repeater");
                                       return;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            });
         }

         if (now - this.lastScanTime >= 20000L && this.scanning.compareAndSet(false, true)) {
            if (this.executor == null || this.executor.isShutdown()) {
               this.executor = Executors.newSingleThreadExecutor(r -> {
                  Thread thread = new Thread(r, "prime-chunk-scan");
                  thread.setDaemon(true);
                  return thread;
               });
            }

            this.lastScanTime = now;
            this.scanActive = true;
            this.scanStartTime = now;
            class_1923 center = mc.field_1724.method_31476();
            int radius2 = Math.min(mc.field_1690.method_38521(), 8);
            ArrayList<class_1923> chunkPositions = new ArrayList<>();
            ArrayList<class_2818> chunks = new ArrayList<>();

            for (int offsetX = -radius2; offsetX <= radius2; offsetX++) {
               for (int offsetZx = -radius2; offsetZx <= radius2; offsetZx++) {
                  class_1923 chunkPos4 = new class_1923(center.field_9181 + offsetX, center.field_9180 + offsetZx);
                  class_2818 chunk6 = mc.field_1687.method_2935().method_12126(chunkPos4.field_9181, chunkPos4.field_9180, false);
                  if (chunk6 != null && !chunk6.method_12223()) {
                     chunkPositions.add(chunkPos4);
                     chunks.add(chunk6);
                  }
               }
            }

            this.executor
               .submit(
                  () -> {
                     try {
                        class_1923 foundRepeater = null;

                        label624:
                        for (int i = 0; i < chunkPositions.size(); i++) {
                           class_2818 chunk2 = chunks.get(i);
                           int count = 0;

                           for (class_2826 section : chunk2.method_12006()) {
                              if (section != null && !section.method_38292() && section.method_19523(statexx -> statexx.method_27852(class_2246.field_10450))) {
                                 for (int x = 0; x < 16; x++) {
                                    for (int y = 0; y < 16; y++) {
                                       for (int z = 0; z < 16; z++) {
                                          class_2680 state = section.method_12254(x, y, z);
                                          if (state.method_27852(class_2246.field_10450) && (Boolean)state.method_11654(class_2462.field_10911)) {
                                             if (++count >= 3) {
                                                foundRepeater = chunkPositions.get(i);
                                                break label624;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        if (foundRepeater != null) {
                           this.lastMarkTime = System.currentTimeMillis();
                           this.markedChunks.clear();
                           this.markedChunks.put(foundRepeater, "repeater");
                           return;
                        }

                        class_1923 foundNormal = null;

                        for (int i = 0; i < chunkPositions.size(); i++) {
                           class_1923 pos = chunkPositions.get(i);
                           class_2818 chunk7 = chunks.get(i);
                           boolean active = false;

                           for (class_2586 be : chunk7.method_12214().values()) {
                              class_2680 state3 = chunk7.method_8320(be.method_11016());
                              if ((state3.method_27852(class_2246.field_20422) || state3.method_27852(class_2246.field_20421))
                                 && be instanceof class_4482 beehive
                                 && beehive.method_23903() > 0) {
                                 active = true;
                                 break;
                              }
                           }

                           boolean active2 = false;
                           if (!active) {
                              int count = 0;
                              int bottomY = chunk7.method_31607();
                              class_2826[] sections = chunk7.method_12006();

                              label570:
                              for (int s = 0; s < sections.length; s++) {
                                 int sectionY = bottomY + s * 16;
                                 if (sectionY > 20) {
                                    break;
                                 }

                                 if (sectionY + 16 >= 0) {
                                    class_2826 sectionx = sections[s];
                                    if (sectionx != null
                                       && !sectionx.method_38292()
                                       && sectionx.method_19523(statexx -> statexx.method_27852(class_2246.field_29031))) {
                                       for (int x = 0; x < 16; x++) {
                                          for (int zx = 0; zx < 16; zx++) {
                                             for (int y = 0; y < 16; y++) {
                                                int worldY = sectionY + y;
                                                if (worldY >= 0 && worldY <= 20 && sectionx.method_12254(x, y, zx).method_27852(class_2246.field_29031)) {
                                                   if (++count >= 50) {
                                                      active2 = true;
                                                      break label570;
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           boolean active3 = false;
                           if (!active) {
                              int count = 0;

                              for (class_2826 sectionx : chunk7.method_12006()) {
                                 if (sectionx != null
                                    && !sectionx.method_38292()
                                    && sectionx.method_19523(statexx -> statexx.method_27852(class_2246.field_10597))) {
                                    for (int x = 0; x < 16; x++) {
                                       for (int yx = 0; yx < 16; yx++) {
                                          for (int zx = 0; zx < 16; zx++) {
                                             if (sectionx.method_12254(x, yx, zx).method_27852(class_2246.field_10597)) {
                                                if (++count >= 150) {
                                                   active3 = true;
                                                   break;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           boolean active4 = false;
                           if (!active && !active3) {
                              int count = 0;
                              class_2826[] sections2 = chunk7.method_12006();
                              int bottomY = chunk7.method_31607();

                              for (int s = 0; s < sections2.length && bottomY + s * 16 <= 70; s++) {
                                 class_2826 sectionxx = sections2[s];
                                 if (sectionxx != null
                                    && !sectionxx.method_38292()
                                    && sectionxx.method_19523(
                                       statexx -> statexx.method_27852(class_2246.field_10376) || statexx.method_27852(class_2246.field_10238)
                                    )) {
                                    for (int x = 0; x < 16; x++) {
                                       for (int yx = 0; yx < 16; yx++) {
                                          for (int zxx = 0; zxx < 16; zxx++) {
                                             class_2680 state = sectionxx.method_12254(x, yx, zxx);
                                             if (state.method_27852(class_2246.field_10376) || state.method_27852(class_2246.field_10238)) {
                                                if (++count >= 30) {
                                                   active4 = true;
                                                   break;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           boolean active9 = false;
                           if (!active && !active3 && !active4 && !active2) {
                              int count = 0;

                              label452:
                              for (class_2826 sectionxx : chunk7.method_12006()) {
                                 if (sectionxx != null
                                    && !sectionxx.method_38292()
                                    && sectionxx.method_19523(statexx -> statexx.method_27852(class_2246.field_10450))) {
                                    for (int x = 0; x < 16; x++) {
                                       for (int yx = 0; yx < 16; yx++) {
                                          for (int zxxx = 0; zxxx < 16; zxxx++) {
                                             if (sectionxx.method_12254(x, yx, zxxx).method_27852(class_2246.field_10450)) {
                                                if (++count >= 3) {
                                                   active9 = true;
                                                   break label452;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           if (active || active3 || active4 || active2 || active9) {
                              foundNormal = pos;
                              break;
                           }
                        }

                        if (foundNormal != null) {
                           boolean flag = System.currentTimeMillis() - this.lastMarkTime >= 50000L;
                           if (flag) {
                              this.lastMarkTime = System.currentTimeMillis();
                              this.markedChunks.clear();
                              this.markedChunks.put(foundNormal, "normal");
                           }

                           return;
                        }
                     } finally {
                        this.scanning.set(false);
                     }
                  }
               );
         }
      }
   }
}

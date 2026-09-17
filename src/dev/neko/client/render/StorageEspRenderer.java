package dev.neko.client.render;

import dev.neko.client.module.impl.FreecamModule;
import dev.neko.client.module.impl.StorageEspModule;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2248;
import net.minecraft.class_2260;
import net.minecraft.class_2281;
import net.minecraft.class_2315;
import net.minecraft.class_2325;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2363;
import net.minecraft.class_2377;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2496;
import net.minecraft.class_2531;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2745;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_3708;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_4597.class_4598;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class StorageEspRenderer {
   private static final int MAX_CHUNK_RADIUS = 16;
   private static final int MAX_RESULTS = 1500;
   private static final int MAX_RENDERED = 32;
   private static final float BOX_INFLATE = 0.002F;
   private static final double CHEST_INSET = 0.0625;
   private static final int INTERACTED_RGB = 6579300;
   private static final float LINE_WIDTH = 1.0F;
   private static final float TRACER_WIDTH = 1.15F;
   private static final int TRACER_ALPHA = 180;
   private static final IncrementalScan<StorageEspRenderer.Hit> SCAN = new IncrementalScan<>(20, 20000, 10);

   private StorageEspRenderer() {
   }

   public static int cachedCount() {
      return SCAN.get().size();
   }

   public static long cachedShulkerCount() {
      return SCAN.get().stream().filter(h -> h.type() == StorageEspModule.StorageType.SHULKER).count();
   }

   public static void resetScan() {
      SCAN.clear();
   }

   public static void scan(StorageEspModule module) {
      class_310 client = class_310.method_1551();
      class_243 camOrigin = client.field_1773.method_19418().method_71156();
      FreecamModule freecam = FreecamModule.get();
      class_243 origin;
      if (freecam != null && freecam.isActive()) {
         origin = camOrigin;
      } else {
         origin = client.field_1724 != null ? client.field_1724.method_73189() : camOrigin;
      }

      double range = module.range.get();
      double rangeSq = range * range;
      int wanted = (int)Math.ceil(range / 16.0) + 1;
      int chunkRadius = Math.min(16, wanted);
      int centerChunkX = (int)Math.floor(origin.field_1352) >> 4;
      int centerChunkZ = (int)Math.floor(origin.field_1350) >> 4;
      SCAN.tick(chunkRadius, centerChunkX, centerChunkZ, (chunk, out) -> scanChunk(chunk, origin, rangeSq, out));
   }

   private static int scanChunk(class_2818 chunk, class_243 origin, double rangeSq, List<StorageEspRenderer.Hit> out) {
      if (class_310.method_1551().field_1724 == null) {
         return 0;
      } else {
         Map<class_2338, class_2586> blockEntities = chunk.method_12214();
         int added = 0;

         for (Entry<class_2338, class_2586> entry : blockEntities.entrySet()) {
            if (out.size() >= 1500) {
               return added;
            }

            class_2338 pos = entry.getKey();
            StorageEspModule.StorageType type = classify(chunk.method_8320(pos).method_26204());
            if (type != null) {
               double dx = origin.field_1352 - (pos.method_10263() + 0.5);
               double dz = origin.field_1350 - (pos.method_10260() + 0.5);
               if (!(dx * dx + dz * dz > rangeSq)) {
                  out.add(new StorageEspRenderer.Hit(pos.method_10263(), pos.method_10264(), pos.method_10260(), type));
                  added++;
               }
            }
         }

         class_2826[] sections = chunk.method_12006();
         int bottomY = chunk.method_31607();

         for (int si = 0; si < sections.length; si++) {
            class_2826 section = sections[si];
            if (section != null && !section.method_38292() && out.size() < 1500) {
               int sectionY = bottomY + si * 16;

               for (int lx = 0; lx < 16; lx++) {
                  for (int lz = 0; lz < 16; lz++) {
                     for (int ly = 0; ly < 16; ly++) {
                        if (out.size() >= 1500) {
                           return added;
                        }

                        class_2338 pos = new class_2338(chunk.method_12004().method_8326() + lx, sectionY + ly, chunk.method_12004().method_8328() + lz);
                        class_2680 state = section.method_12254(lx, ly, lz);
                        if (!state.method_31709()) {
                           StorageEspModule.StorageType type = classify(state.method_26204());
                           if (type != null) {
                              double dx = origin.field_1352 - (pos.method_10263() + 0.5);
                              double dz = origin.field_1350 - (pos.method_10260() + 0.5);
                              if (!(dx * dx + dz * dz > rangeSq)) {
                                 out.add(new StorageEspRenderer.Hit(pos.method_10263(), pos.method_10264(), pos.method_10260(), type));
                                 added++;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return added;
      }
   }

   public static void render(class_4598 bufferSource, class_4587 poseStack, class_243 cam, StorageEspModule module) {
      List<StorageEspRenderer.Hit> snapshot = SCAN.get();
      if (!snapshot.isEmpty()) {
         class_310 mc = class_310.method_1551();
         class_638 level = mc.field_1687;
         if (level != null) {
            boolean fill = true;
            int alphaBits = module.highlightAlpha.get().intValue() << 24;
            int rendered = 0;
            boolean tracers = module.tracers.get();
            Vector3fc forward = new Vector3f(0.0F, 0.0F, 0.0F);
            if (tracers && mc.field_1773 != null && mc.field_1773.method_19418() != null) {
               forward = mc.field_1773.method_19418().method_19335();
            }

            class_2339 renderPos = new class_2339();
            double maxDistSq = module.range.get().doubleValue();
            maxDistSq *= maxDistSq;

            for (StorageEspRenderer.Hit hit : snapshot) {
               if (rendered >= 32) {
                  break;
               }

               double dx = hit.x() + 0.5 - cam.field_1352;
               double dz = hit.z() + 0.5 - cam.field_1350;
               if (!(dx * dx + dz * dz > maxDistSq)) {
                  StorageEspModule.StorageType type = hit.type();
                  if (module.isTypeEnabled(type)) {
                     renderPos.method_10103(hit.x(), hit.y(), hit.z());
                     boolean interacted = module.isInteracted(renderPos);
                     int rgb = interacted ? 6579300 : module.colorFor(type) & 16777215;
                     int color = alphaBits | rgb;
                     double x1 = hit.x();
                     double y1 = hit.y();
                     double z1 = hit.z();
                     double x2 = x1 + 1.0;
                     double y2 = y1 + 1.0;
                     double z2 = z1 + 1.0;
                     if (type == StorageEspModule.StorageType.CHEST
                        || type == StorageEspModule.StorageType.TRAPPED
                        || type == StorageEspModule.StorageType.ENDER) {
                        x1 += 0.0625;
                        z1 += 0.0625;
                        x2 -= 0.0625;
                        y2 -= 0.125;
                        z2 -= 0.0625;
                        if (type == StorageEspModule.StorageType.CHEST || type == StorageEspModule.StorageType.TRAPPED) {
                           class_2680 st = level.method_8320(renderPos);
                           if (st.method_26204() instanceof class_2281 && st.method_11654(class_2281.field_10770) != class_2745.field_12569) {
                              class_2350 facing = (class_2350)st.method_11654(class_2281.field_10768);
                              class_2745 ct = (class_2745)st.method_11654(class_2281.field_10770);
                              class_2350 nb = ct == class_2745.field_12574 ? facing.method_10170() : facing.method_10160();
                              if (nb == class_2350.field_11039) {
                                 x1 = hit.x();
                              } else if (nb == class_2350.field_11034) {
                                 x2 = hit.x() + 2.0 - 0.0625;
                              } else if (nb == class_2350.field_11043) {
                                 z1 = hit.z();
                              } else if (nb == class_2350.field_11035) {
                                 z2 = hit.z() + 2.0 - 0.0625;
                              }
                           }
                        }
                     }

                     if (fill) {
                        EspBoxRenderer.fill(bufferSource, poseStack, cam, x1 - 0.002F, y1 - 0.002F, z1 - 0.002F, x2 + 0.002F, y2 + 0.002F, z2 + 0.002F, color);
                     } else {
                        EspBoxRenderer.outline(bufferSource, poseStack, cam, x1, y1, z1, x2, y2, z2, color, 1.0F);
                     }

                     if (tracers) {
                        int tracerColor = -1275068416 | rgb;
                        EspBoxRenderer.tracer(bufferSource, poseStack, cam, forward, (x1 + x2) * 0.5, (y1 + y2) * 0.5, (z1 + z2) * 0.5, tracerColor, 1.15F);
                     }

                     rendered++;
                  }
               }
            }

            EspBoxRenderer.flush(bufferSource);
         }
      }
   }

   private static boolean isStorage(class_2680 state) {
      return classify(state.method_26204()) != null;
   }

   private static StorageEspModule.StorageType classify(class_2248 b) {
      if (b instanceof class_2531) {
         return StorageEspModule.StorageType.TRAPPED;
      } else if (b instanceof class_2281) {
         return StorageEspModule.StorageType.CHEST;
      } else if (b instanceof class_2336) {
         return StorageEspModule.StorageType.ENDER;
      } else if (b instanceof class_2480) {
         return StorageEspModule.StorageType.SHULKER;
      } else if (b instanceof class_3708) {
         return StorageEspModule.StorageType.BARREL;
      } else if (b instanceof class_2496) {
         return StorageEspModule.StorageType.SPAWNER;
      } else if (b instanceof class_2377) {
         return StorageEspModule.StorageType.HOPPER;
      } else if (b instanceof class_2363) {
         return StorageEspModule.StorageType.FURNACE;
      } else if (b instanceof class_2260) {
         return StorageEspModule.StorageType.BREWING_STAND;
      } else if (b instanceof class_2315) {
         return StorageEspModule.StorageType.DISPENSER;
      } else {
         return b instanceof class_2325 ? StorageEspModule.StorageType.DROPPER : null;
      }
   }

   public record Hit(int x, int y, int z, StorageEspModule.StorageType type) {
   }
}

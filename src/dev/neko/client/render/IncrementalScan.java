package dev.neko.client.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_746;

public final class IncrementalScan<H> {
   private final int chunksPerTick;
   private final int blockBudgetPerTick;
   private final int idleTicks;
   private volatile List<H> published = List.of();
   private List<H> building = new ArrayList<>();
   private int cursor;
   private int[] order = new int[0];
   private int orderRadius = -1;
   private int sweepPcx = Integer.MIN_VALUE;
   private int sweepPcz = Integer.MIN_VALUE;
   private int cooldown;
   private boolean dirty;

   public IncrementalScan(int chunksPerTick, int blockBudgetPerTick, int idleTicks) {
      this.chunksPerTick = chunksPerTick;
      this.blockBudgetPerTick = blockBudgetPerTick;
      this.idleTicks = idleTicks;
   }

   public List<H> get() {
      return this.published;
   }

   public void markDirty() {
      this.dirty = true;
   }

   public void clear() {
      this.published = List.of();
      this.building = new ArrayList<>();
      this.cursor = 0;
      this.cooldown = 0;
      this.dirty = false;
      this.sweepPcx = this.sweepPcz = Integer.MIN_VALUE;
   }

   public void publishForInstant(List<H> results) {
      this.published = new ArrayList<>(results);
   }

   public void tick(int radius, IncrementalScan.ChunkScanner<H> scanner) {
      class_310 mc = class_310.method_1551();
      class_638 level = mc.field_1687;
      class_746 player = mc.field_1724;
      if (level != null && player != null) {
         this.tick(radius, player.method_31476().field_9181, player.method_31476().field_9180, scanner);
      }
   }

   public void tick(int radius, int centerChunkX, int centerChunkZ, IncrementalScan.ChunkScanner<H> scanner) {
      class_310 mc = class_310.method_1551();
      class_638 level = mc.field_1687;
      if (level != null && mc.field_1724 != null) {
         if (this.orderRadius != radius) {
            this.ensureOrder(radius);
            this.dirty = true;
         }

         if (this.cursor == 0) {
            if (!this.dirty && this.cooldown > 0) {
               this.cooldown--;
               return;
            }

            this.sweepPcx = centerChunkX;
            this.sweepPcz = centerChunkZ;
            this.building = new ArrayList<>();
            this.dirty = false;
         } else if (this.dirty) {
            this.sweepPcx = centerChunkX;
            this.sweepPcz = centerChunkZ;
            this.cursor = 0;
            this.building = new ArrayList<>();
            this.dirty = false;
         }

         int total = this.order.length;
         int chunks = 0;

         for (int blocks = 0; this.cursor < total && chunks < this.chunksPerTick && blocks < this.blockBudgetPerTick; chunks++) {
            int packed = this.order[this.cursor];
            int dx = (short)(packed >> 16);
            int dz = (short)(packed & 65535);
            blocks += scanner.scan(level.method_8497(this.sweepPcx + dx, this.sweepPcz + dz), this.building);
            this.cursor++;
         }

         if (this.cursor >= total) {
            this.published = this.building;
            this.building = new ArrayList<>();
            this.cursor = 0;
            this.cooldown = this.idleTicks;
         }
      }
   }

   private void ensureOrder(int radius) {
      if (this.orderRadius != radius) {
         int side = 2 * radius + 1;
         Integer[] offs = new Integer[side * side];
         int i = 0;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               offs[i++] = (dx & 65535) << 16 | dz & 65535;
            }
         }

         Arrays.sort(offs, (a, b) -> {
            int adx = (short)(a >> 16);
            int adz = (short)(a & 65535);
            int bdx = (short)(b >> 16);
            int bdz = (short)(b & 65535);
            return Integer.compare(adx * adx + adz * adz, bdx * bdx + bdz * bdz);
         });
         int[] out = new int[offs.length];

         for (int k = 0; k < offs.length; k++) {
            out[k] = offs[k];
         }

         this.order = out;
         this.orderRadius = radius;
      }
   }

   public interface ChunkScanner<H> {
      int scan(class_2818 var1, List<H> var2);
   }
}

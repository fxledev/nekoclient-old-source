package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.SliderSetting;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1716;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class GambleRiggerModule extends Module {
   private static final int GRID = 9;
   private static final int INV_START = 9;
   private static final int INV_END = 45;
   private static final int MOVE = 0;
   private static final int QMOVE = 1;
   public final SliderSetting clickDelay = this.addSetting(
      new SliderSetting(
            "Click Delay",
            "Ticks between each moved stack. 0 = instant (whole grid in one tick); raise it only if a server flags rapid container clicks.",
            0.0,
            0.0,
            10.0,
            1.0
         )
         .withLabel(v -> (int)v <= 0 ? "Instant" : (int)v + " tick")
   );
   public final BooleanSetting keepStays = this.addSetting(
      new BooleanSetting(
         "Keep Slot Stays",
         "Leave the picked slot untouched in the container (someone else refills it). Off: pull it out too and restore leaves it empty.",
         true
      )
   );
   public final BooleanSetting chatFeedback = this.addSetting(new BooleanSetting("Chat Feedback", "Print what the rigger is doing to chat.", true));
   private final class_310 mc = class_310.method_1551();
   private GambleRiggerModule.Phase phase = GambleRiggerModule.Phase.IDLE;
   private int keepSlot = -1;
   private int containerId = -1;
   private final class_1799[] snapshot = new class_1799[9];
   private final int[] parked = new int[9];
   private final Deque<int[]> queue = new ArrayDeque<>();
   private int delay = 0;

   public GambleRiggerModule() {
      super(
         "GambleRigger",
         "Rig a dispenser/dropper gamble — a fake slot-panel keypad that pulls every slot but the one you keep, then restores the exact layout.",
         Category.MISC
      );
   }

   public GambleRiggerModule.Phase phase() {
      return this.phase;
   }

   public int keepSlot() {
      return this.keepSlot;
   }

   public int queued() {
      return this.queue.size();
   }

   public boolean busy() {
      return this.phase == GambleRiggerModule.Phase.EXTRACTING || this.phase == GambleRiggerModule.Phase.RESTORING;
   }

   public boolean canExtract() {
      return this.phase == GambleRiggerModule.Phase.IDLE && this.dispenserOpen();
   }

   public boolean canRestore() {
      return this.phase == GambleRiggerModule.Phase.EXTRACTED && this.dispenserOpen();
   }

   public void requestKeep(int slot) {
      if (this.canExtract() && slot >= 0 && slot < 9) {
         class_1657 p = this.mc.field_1724;
         if (p != null && p.field_7512 instanceof class_1716 menu) {
            this.containerId = menu.field_7763;
            this.keepSlot = slot;
            this.queue.clear();
            this.delay = 0;
            boolean[] var8 = new boolean[45];
            int fallback = 0;

            for (int j = 0; j < 9; j++) {
               this.snapshot[j] = menu.method_7611(j).method_7677().method_7972();
               this.parked[j] = -1;
            }

            for (int j = 0; j < 9; j++) {
               if ((!this.keepStays.get() || j != this.keepSlot) && !this.snapshot[j].method_7960()) {
                  int target = this.firstEmptyInv(menu, var8);
                  if (target >= 0) {
                     var8[target] = true;
                     this.parked[j] = target;
                     this.queue.add(new int[]{0, j, target});
                  } else {
                     fallback++;
                     this.queue.add(new int[]{1, j, 0});
                  }
               }
            }

            if (this.queue.isEmpty()) {
               this.phase = GambleRiggerModule.Phase.EXTRACTED;
               this.feedback("§dGambleRigger §7» grid empty — nothing to take, click §fRestore §7when done");
               return;
            }

            this.phase = GambleRiggerModule.Phase.EXTRACTING;
            if (fallback > 0) {
               this.feedback(
                  "§eGambleRigger §7» your inventory is nearly full — " + fallback + " stack(s) fall back to shift-click and may not restore exactly"
               );
            }

            return;
         }
      }
   }

   public void requestRestore() {
      if (this.canRestore()) {
         class_1657 p = this.mc.field_1724;
         if (p != null && p.field_7512 instanceof class_1716 menu && menu.field_7763 == this.containerId) {
            this.queue.clear();
            this.delay = 0;
            boolean[] used = new boolean[45];
            int missing = 0;

            for (int j = 0; j < 9; j++) {
               if (j != this.keepSlot) {
                  class_1799 want = this.snapshot[j];
                  if (want != null && !want.method_7960()) {
                     int src = this.findSource(menu, want, j, used);
                     if (src < 0) {
                        missing++;
                     } else {
                        used[src] = true;
                        this.queue.add(new int[]{0, src, j});
                     }
                  }
               }
            }

            this.phase = GambleRiggerModule.Phase.RESTORING;
            if (missing > 0) {
               this.feedback("§eGambleRigger §7» restoring, but §c" + missing + " §7stack(s) were not found in your inventory");
            }

            return;
         }

         this.abort(true);
      }
   }

   public void reset() {
      boolean had = this.phase != GambleRiggerModule.Phase.IDLE || this.keepSlot >= 0;
      this.resetState();
      if (had) {
         this.feedback("§7GambleRigger » reset");
      }
   }

   @Override
   public void onTick() {
      this.tickRigQueue();
   }

   private void tickRigQueue() {
      if (this.queue.isEmpty()) {
         this.finishIfDrained();
      } else {
         class_1657 p = this.mc.field_1724;
         if (p != null && this.mc.field_1761 != null && p.field_7512 instanceof class_1716 && p.field_7512.field_7763 == this.containerId) {
            boolean instant = this.clickDelay.getInt() <= 0;

            while (!this.queue.isEmpty()) {
               if (this.delay > 0) {
                  this.delay--;
                  return;
               }

               this.executeStep(p, this.queue.poll());
               this.delay = this.clickDelay.getInt();
               if (!instant) {
                  return;
               }
            }

            this.finishIfDrained();
         } else {
            this.abort(true);
         }
      }
   }

   private void finishIfDrained() {
      if (this.queue.isEmpty()) {
         if (this.phase == GambleRiggerModule.Phase.EXTRACTING) {
            this.phase = GambleRiggerModule.Phase.EXTRACTED;
            this.feedback("§aGambleRigger §7» pulled the grid (kept §f#" + (this.keepSlot + 1) + "§7) — click §fRestore §7when ready");
         } else if (this.phase == GambleRiggerModule.Phase.RESTORING) {
            this.feedback("§aGambleRigger §7» layout restored — slot §f#" + (this.keepSlot + 1) + " §7left open");
            this.resetState();
         }
      }
   }

   private void executeStep(class_1657 p, int[] step) {
      class_1703 menu = p.field_7512;
      if (step[0] == 1) {
         this.click(p, step[1], 0, class_1713.field_7794);
      } else {
         this.click(p, step[1], 0, class_1713.field_7790);
         if (!menu.method_34255().method_7960()) {
            this.click(p, step[2], 0, class_1713.field_7790);
         }

         if (!menu.method_34255().method_7960()) {
            this.click(p, step[1], 0, class_1713.field_7790);
         }
      }
   }

   @Override
   protected void onDisable() {
      this.abort(false);
   }

   private boolean dispenserOpen() {
      class_1657 p = this.mc.field_1724;
      return p != null && p.field_7512 instanceof class_1716;
   }

   private void click(class_1657 p, int slot, int button, class_1713 type) {
      this.mc.field_1761.method_2906(this.containerId, slot, button, type, p);
   }

   private int firstEmptyInv(class_1703 menu, boolean[] reserved) {
      for (int i = 9; i < 45; i++) {
         if (!reserved[i] && menu.method_7611(i).method_7677().method_7960()) {
            return i;
         }
      }

      return -1;
   }

   private int findSource(class_1703 menu, class_1799 want, int j, boolean[] used) {
      int pk = this.parked[j];
      if (pk >= 9 && pk < 45 && !used[pk] && class_1799.method_7973(menu.method_7611(pk).method_7677(), want)) {
         return pk;
      } else {
         for (int i = 9; i < 45; i++) {
            if (!used[i] && class_1799.method_7973(menu.method_7611(i).method_7677(), want)) {
               return i;
            }
         }

         for (int ix = 9; ix < 45; ix++) {
            if (!used[ix]) {
               class_1799 s = menu.method_7611(ix).method_7677();
               if (!s.method_7960() && class_1799.method_31577(s, want)) {
                  return ix;
               }
            }
         }

         return -1;
      }
   }

   private void abort(boolean warn) {
      boolean wasActive = this.phase != GambleRiggerModule.Phase.IDLE;
      this.resetState();
      if (warn && wasActive) {
         this.feedback("§cGambleRigger §7» aborted (the container closed or changed)");
      }
   }

   private void resetState() {
      this.phase = GambleRiggerModule.Phase.IDLE;
      this.keepSlot = -1;
      this.containerId = -1;
      this.queue.clear();
      this.delay = 0;

      for (int j = 0; j < 9; j++) {
         this.snapshot[j] = null;
         this.parked[j] = -1;
      }
   }

   private void feedback(String msg) {
      if (this.chatFeedback.get()) {
         class_1657 p = this.mc.field_1724;
         if (p != null) {
            p.method_7353(class_2561.method_43470(msg), false);
         }
      }
   }

   public static enum Phase {
      IDLE,
      EXTRACTING,
      EXTRACTED,
      RESTORING;

      private static GambleRiggerModule.Phase[] $values() {
         return new GambleRiggerModule.Phase[]{IDLE, EXTRACTING, EXTRACTED, RESTORING};
      }

      private static GambleRiggerModule.Phase[] $values$() {
         return new GambleRiggerModule.Phase[]{IDLE, EXTRACTING, EXTRACTED, RESTORING};
      }
   }
}

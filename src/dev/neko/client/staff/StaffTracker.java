package dev.neko.client.staff;

import dev.neko.client.module.impl.StaffListModule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1934;
import net.minecraft.class_268;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_640;

public final class StaffTracker {
   private static final int SCAN_INTERVAL = 10;
   private final StaffListModule module;
   private volatile List<StaffEntry> current = List.of();
   private Set<String> lastNames = new HashSet<>();
   private boolean primed;
   private int ticks;
   private static volatile List<StaffEntry> debugInject;

   public StaffTracker(StaffListModule module) {
      this.module = module;
   }

   public List<StaffEntry> current() {
      List<StaffEntry> inj = debugInject;
      return inj != null ? inj : this.current;
   }

   public void reset() {
      this.lastNames = new HashSet<>();
      this.primed = false;
      this.current = List.of();
      this.ticks = 0;
   }

   public void clear() {
      this.reset();
   }

   public void tick() {
      if (debugInject == null && ++this.ticks % 10 == 0) {
         this.scan();
      }
   }

   private void scan() {
      class_310 mc = class_310.method_1551();
      class_634 conn = mc.method_1562();
      if (conn != null && mc.field_1724 != null) {
         StaffDetector.DetectConfig cfg = this.module.detectConfig();
         UUID self = mc.field_1724.method_5667();
         Set<UUID> listed = new HashSet<>();

         for (class_640 pi : conn.method_45732()) {
            listed.add(pi.method_2966().id());
         }

         List<StaffEntry> found = new ArrayList<>();

         for (class_640 info : conn.method_2880()) {
            UUID id = info.method_2966().id();
            if (!id.equals(self)) {
               String name = info.method_2966().name();
               if (name != null && !name.isEmpty()) {
                  boolean vanished = info.method_2958() == class_1934.field_9219 || !listed.contains(id);
                  if (!vanished || cfg.showVanished()) {
                     class_268 team = info.method_2955();
                     StaffEntry entry = StaffDetector.classify(
                        name,
                        info.method_2971(),
                        team == null ? null : team.method_1144(),
                        team == null ? null : team.method_1136(),
                        team == null ? null : team.method_1197(),
                        vanished,
                        info.method_2959(),
                        cfg
                     );
                     if (entry != null) {
                        found.add(entry);
                     }
                  }
               }
            }
         }

         found.sort(
            Comparator.comparing(StaffEntry::vanished)
               .thenComparing(Comparator.comparingInt(StaffEntry::priority).reversed())
               .thenComparing(ex -> ex.name().toLowerCase(Locale.ROOT))
         );
         Set<String> names = new HashSet<>();

         for (StaffEntry e : found) {
            names.add(e.name());
         }

         if (this.primed) {
            for (StaffEntry e : found) {
               if (!this.lastNames.contains(e.name())) {
                  this.module.onStaffAppear(e);
               }
            }
         }

         this.lastNames = names;
         this.primed = true;
         this.current = List.copyOf(found);
      } else {
         this.current = List.of();
         this.lastNames = new HashSet<>();
         this.primed = false;
      }
   }

   public static void injectForTest(List<StaffEntry> entries) {
      debugInject = entries == null ? null : List.copyOf(entries);
   }

   public static void clearInject() {
      debugInject = null;
   }
}

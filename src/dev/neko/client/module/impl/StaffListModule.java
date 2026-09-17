package dev.neko.client.module.impl;

import dev.neko.client.NekoClient;
import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.staff.StaffDetector;
import dev.neko.client.staff.StaffEntry;
import dev.neko.client.staff.StaffTracker;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public class StaffListModule extends Module {
   public final BooleanSetting showOnHud = this.addSetting(new BooleanSetting("Show on HUD", "Display the fixed StaffList panel", true));
   public final StaffTracker tracker = new StaffTracker(this);

   public StaffListModule() {
      super("StaffList", "Lists online staff using the normal preset", Category.MISC);
   }

   @Override
   protected void onEnable() {
      this.tracker.reset();
   }

   @Override
   protected void onDisable() {
      this.tracker.clear();
   }

   @Override
   public void onTick() {
      this.tracker.tick();
   }

   public List<StaffEntry> staff() {
      return this.tracker.current();
   }

   public boolean isHudVisible() {
      return this.isEnabled() && this.showOnHud.get();
   }

   public StaffDetector.DetectConfig detectConfig() {
      return new StaffDetector.DetectConfig("Marker + Rank", Set.of(), StaffDetector.DEFAULT_RANK_KEYWORDS, "★☆✦✧✪✩✫✬✭✮✯⭐✰❂⚝✴✵✶✷✸✹⍟", true, true);
   }

   public boolean showRank() {
      return true;
   }

   public boolean showPing() {
      return false;
   }

   public int maxRows() {
      return 6;
   }

   public void onStaffAppear(StaffEntry entry) {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null) {
         String rank = entry.rankLabel().isEmpty() ? "Staff" : entry.rankLabel();
         String tail = entry.vanished() ? " (vanished)" : "";
         if (NekoClient.notifications() != null) {
            NekoClient.notifications().pushInfo(rank + " " + entry.name() + " online" + tail);
         }

         client.method_1483().method_4873(class_1109.method_4757((class_3414)class_3417.field_14793.comp_349(), 1.5F, 0.6F));
      }
   }
}

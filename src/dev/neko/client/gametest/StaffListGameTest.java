package dev.neko.client.gametest;

import dev.neko.client.NekoClient;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.staff.StaffDetector;
import dev.neko.client.staff.StaffEntry;
import dev.neko.client.staff.StaffTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_2561;

public class StaffListGameTest implements FabricClientGameTest {
   public void runTest(ClientGameTestContext context) {
      verifyDetection();
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1600, 900);
         context.waitTicks(2);
         context.runOnClient(mc -> {
            ModuleManager m = NekoClient.modules();
            require(m.staffList != null, "StaffList registered");
            m.staffList.setEnabled(true);
            StaffTracker.injectForTest(roster());
         });
         context.waitTicks(10);
         context.takeScreenshot("stafflist-roster");
         context.runOnClient(mc -> StaffTracker.injectForTest(bigRoster()));
         context.waitTicks(10);
         context.takeScreenshot("stafflist-overflow");
         context.runOnClient(mc -> StaffTracker.injectForTest(List.of()));
         context.waitTicks(10);
         context.takeScreenshot("stafflist-empty");
         context.runOnClient(mc -> StaffTracker.clearInject());
      } catch (Throwable var6) {
         if (world != null) {
            try {
               world.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (world != null) {
         world.close();
      }
   }

   private static void verifyDetection() {
      StaffDetector.DetectConfig cfg = new StaffDetector.DetectConfig(
         "Star + Rank", Set.of("knownstaff"), StaffDetector.DEFAULT_RANK_KEYWORDS, "★☆✦✧✪✩✫✬✭✮✯⭐✰❂⚝✴✵✶✷✸✹⍟", true, true
      );
      class_2561 starName = class_2561.method_43473()
         .method_10852(class_2561.method_43470("★ ").method_54663(16733525))
         .method_10852(class_2561.method_43470("StaffGuy"));
      StaffEntry star = StaffDetector.classify("StaffGuy", starName, (class_2561)null, (class_2561)null, (String)null, false, 30, cfg);
      require(star != null, "star-marked player detected");
      boolean var10000 = star.color() == -43691;
      String var10001 = Integer.toHexString(star == null ? 0 : star.color());
      require(var10000, "star colour captured (got " + var10001 + ")");
      require(star.rankLabel().isEmpty(), "star-only entry has no rank label");
      StaffEntry admin = StaffDetector.classify(
         "AdminDude", class_2561.method_43470("Admin | AdminDude"), (class_2561)null, (class_2561)null, (String)null, false, 40, cfg
      );
      require(admin != null, "text-rank player detected");
      var10000 = "Admin".equals(admin.rankLabel());
      var10001 = admin == null ? "null" : admin.rankLabel();
      require(var10000, "rank label resolves to Admin (got " + var10001 + ")");
      StaffEntry known = StaffDetector.classify(
         "KnownStaff", class_2561.method_43470("KnownStaff"), (class_2561)null, (class_2561)null, (String)null, false, 50, cfg
      );
      require(known != null, "allowlisted player detected");
      StaffEntry pua = StaffDetector.classify(
         "PuaMod", class_2561.method_43470("\ue001 PuaMod"), (class_2561)null, (class_2561)null, (String)null, false, 60, cfg
      );
      require(pua != null, "PUA font-icon staff detected");
      StaffEntry none = StaffDetector.classify(
         "RandomKid", class_2561.method_43470("RandomKid"), (class_2561)null, (class_2561)null, (String)null, false, 70, cfg
      );
      require(none == null, "ordinary player is not flagged");
      StaffEntry tricky = StaffDetector.classify(
         "xX_admin_Xx", class_2561.method_43470("xX_admin_Xx"), (class_2561)null, (class_2561)null, (String)null, false, 70, cfg
      );
      require(tricky == null, "username containing 'admin' is not flagged as staff");
      StaffDetector.DetectConfig starOnly = new StaffDetector.DetectConfig(
         "Star Only", Set.of(), StaffDetector.DEFAULT_RANK_KEYWORDS, "★☆✦✧✪✩✫✬✭✮✯⭐✰❂⚝✴✵✶✷✸✹⍟", true, true
      );
      require(
         StaffDetector.classify(
               "AdminDude", class_2561.method_43470("Admin | AdminDude"), (class_2561)null, (class_2561)null, (String)null, false, 40, starOnly
            )
            == null,
         "Star Only ignores text ranks"
      );
      require(
         StaffDetector.classify("StaffGuy", starName, (class_2561)null, (class_2561)null, (String)null, false, 30, starOnly) != null,
         "Star Only still detects the star"
      );
   }

   private static List<StaffEntry> roster() {
      return List.of(
         new StaffEntry("Owner_Jeff", "Owner", -45747, false, 22, 19),
         new StaffEntry("Admin_Kate", "Admin", -26368, false, 48, 17),
         new StaffEntry("ModSquad", "Mod", -11141291, false, 130, 11),
         new StaffEntry("HelperHank", "Helper", -11141121, false, 250, 8),
         new StaffEntry("StealthWatch", "Mod", -11141291, true, 90, 11),
         new StaffEntry("Twinkles", "", 0, false, 70, 1)
      );
   }

   private static List<StaffEntry> bigRoster() {
      List<StaffEntry> l = new ArrayList<>(roster());
      l.add(new StaffEntry("BuilderBob", "Builder", -11143, false, 40, 4));
      l.add(new StaffEntry("SupportSue", "Support", -6584321, false, 55, 3));
      l.add(new StaffEntry("TrialTom", "Trial", -5197648, false, 300, 2));
      return l;
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}

package dev.neko.client.gametest;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.widget.KeybindWidget;
import dev.neko.client.module.impl.AutoCrystalModule;
import dev.neko.client.settings.KeybindSetting;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_1511;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3675;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;

public class AutoCrystalGameTest implements FabricClientGameTest {
   private static final int TRIGGER_KEY = 71;
   private static final class_2338 BASE = new class_2338(0, 1, 0);
   private static final class_238 CRYSTAL_BOX = new class_238(-0.5, 1.5, -0.5, 1.5, 5.5, 2.5);

   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1280, 720);
         context.waitTicks(2);
         TestServerContext server = world.getServer();
         context.runOnClient(mc -> require(NekoClient.modules().autoCrystal != null, "AutoCrystal registered"));
         this.buildArena(context, server, "obsidian");
         context.runOnClient(mc -> {
            AutoCrystalModule ac = NekoClient.modules().autoCrystal;
            ac.activateKey.set(71);
            ac.placeDelay.set(0.0);
            ac.breakDelay.set(0.0);
            ac.range.set(1.0);
            ac.setEnabled(true);
         });
         context.takeScreenshot("autocrystal-aim");
         context.runOnClient(mc -> require(isAimingAtBase(mc.field_1765), "aim lands on the base, got " + describeHit(mc.field_1765)));
         context.getInput().holdKey(71);
         context.waitTicks(6);
         int[] capturedId = new int[1];
         context.runOnClient(
            mc -> {
               List<class_1511> crystals = mc.field_1687.method_18467(class_1511.class, CRYSTAL_BOX);
               if (crystals.size() != 1) {
                  String var10000 = String.valueOf(mc.field_1724.method_6047().method_7909());
                  String diag = "hand="
                     + var10000
                     + " off="
                     + mc.field_1724.method_6079().method_7909()
                     + " sel="
                     + mc.field_1724.method_31548().method_67532()
                     + " base="
                     + mc.field_1687.method_8320(new class_2338(0, 1, 0))
                     + " above="
                     + mc.field_1687.method_8320(new class_2338(0, 2, 0))
                     + " trigHeld="
                     + class_3675.method_15987(mc.method_22683(), 71)
                     + " enabled="
                     + NekoClient.modules().autoCrystal.isEnabled()
                     + " hit="
                     + describeHit(mc.field_1765);
                  int var10001 = crystals.size();
                  require(false, "one crystal placed on the obsidian base, got " + var10001 + " | " + diag);
               }

               capturedId[0] = crystals.get(0).method_5628();
            }
         );
         context.takeScreenshot("autocrystal-placed");
         context.runOnClient(mc -> NekoClient.modules().autoCrystal.range.set(3.0));
         context.waitTicks(6);
         context.runOnClient(
            mc -> {
               List<class_1511> survivors = mc.field_1687
                  .method_18467(class_1511.class, CRYSTAL_BOX)
                  .stream()
                  .filter(c -> c.method_5628() == capturedId[0])
                  .toList();
               if (!survivors.isEmpty()) {
                  double dist = mc.field_1724.method_33571().method_1022(survivors.get(0).method_73189());
                  require(
                     false,
                     "crystal id "
                        + capturedId[0]
                        + " should have been broken | dist="
                        + String.format(Locale.ROOT, "%.2f", dist)
                        + " hit="
                        + describeHit(mc.field_1765)
                        + " hand="
                        + mc.field_1724.method_6047().method_7909()
                  );
               }
            }
         );
         context.takeScreenshot("autocrystal-broken");
         context.getInput().releaseKey(71);
         context.waitTicks(2);
         this.buildArena(context, server, "stone");
         context.runOnClient(mc -> NekoClient.modules().autoCrystal.range.set(1.0));
         context.runOnClient(mc -> require(isAimingAtBase(mc.field_1765), "aim lands on the stone block, got " + describeHit(mc.field_1765)));
         context.getInput().holdKey(71);
         context.waitTicks(8);
         context.runOnClient(mc -> {
            boolean obiTop = mc.field_1687.method_8320(new class_2338(0, 2, 0)).method_27852(class_2246.field_10540);
            boolean obiSide = mc.field_1687.method_8320(new class_2338(0, 1, 1)).method_27852(class_2246.field_10540);
            require(obiTop || obiSide, "auto-obsidian laid a base (expected at 0,2,0 or 0,1,1)");
            int crystals = mc.field_1687.method_18467(class_1511.class, new class_238(-0.5, 1.5, -0.5, 1.5, 6.5, 2.5)).size();
            require(crystals >= 1, "a crystal was placed on the auto-laid obsidian, got " + crystals);
         });
         context.takeScreenshot("autocrystal-obsidian");
         context.getInput().releaseKey(71);
         context.runOnClient(mc -> NekoClient.modules().autoCrystal.setEnabled(false));
         context.waitTicks(2);
         context.runOnClient(mc -> {
            KeybindSetting kb = new KeybindSetting("Test", "", -1);
            KeybindWidget widget = new KeybindWidget(NekoClient.themes(), kb);
            widget.setBounds(0.0F, 0.0F, 100.0F);
            require(widget.mouseClicked(10.0F, 10.0F, 0), "arming left-click is consumed");
            require(widget.isListening(), "widget listens after the arming click");
            require(widget.mouseClicked(10.0F, 10.0F, 1), "the bind right-click is consumed");
            require(!widget.isListening(), "widget stops listening once bound");
            boolean var10000 = kb.get() == 1;
            String var10001 = String.valueOf(kb.get());
            require(var10000, "right-click bound RMB, got " + var10001);
            var10000 = kb.keyName().equals("RMB");
            var10001 = kb.keyName();
            require(var10000, "the bind shows as 'RMB', got " + var10001);
            widget.mouseClicked(10.0F, 10.0F, 0);
            require(widget.keyPressed(86), "a keyboard key still binds");
            var10000 = kb.get() == 86;
            var10001 = String.valueOf(kb.get());
            require(var10000, "V bound, got " + var10001);
            widget.mouseClicked(10.0F, 10.0F, 0);
            widget.keyPressed(256);
            require(kb.get() == -1, "ESC clears the bind");
         });
      } catch (Throwable var61) {
         if (world != null) {
            try {
               world.close();
            } catch (Throwable var51) {
               var61.addSuppressed(var51);
            }
         }

         throw var61;
      }

      if (world != null) {
         world.close();
      }
   }

   private void buildArena(ClientGameTestContext context, TestServerContext server, String baseBlock) {
      server.runCommand("kill @e[type=minecraft:end_crystal]");
      server.runCommand("fill -20 0 -20 20 0 20 minecraft:stone");
      server.runCommand("fill -20 1 -20 20 8 20 minecraft:air");
      server.runCommand("setblock 0 1 0 minecraft:" + baseBlock);
      server.runCommand("gamemode creative @a");
      server.runCommand("clear @a");
      server.runCommand("give @a minecraft:end_crystal 64");
      server.runCommand("give @a minecraft:obsidian 64");
      server.runCommand("tp @a 0.5 1 2.5 180 15");
      context.waitTicks(5);
      context.runOnClient(mc -> aimAt(mc.field_1724, 0.5, 2.0, 0.5));
      context.waitTicks(3);
   }

   private static void aimAt(class_746 player, double tx, double ty, double tz) {
      class_243 eye = player.method_33571();
      double dx = tx - eye.field_1352;
      double dy = ty - eye.field_1351;
      double dz = tz - eye.field_1350;
      double horiz = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz)));
      player.method_36456(yaw);
      player.method_36457(pitch);
      player.method_5847(yaw);
   }

   private static boolean isAimingAtBase(class_239 hit) {
      return hit instanceof class_3965 bhr && bhr.method_17783() == class_240.field_1332 && bhr.method_17777().equals(BASE);
   }

   private static String describeHit(class_239 hit) {
      if (hit instanceof class_3965 bhr && bhr.method_17783() == class_240.field_1332) {
         String var10000 = String.valueOf(bhr.method_17777());
         return "block " + var10000 + " face " + bhr.method_17780();
      } else {
         return hit == null ? "null" : hit.method_17783().toString();
      }
   }

   private static void require(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAILED: " + what);
      }
   }
}

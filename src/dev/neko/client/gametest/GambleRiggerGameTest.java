package dev.neko.client.gametest;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.module.impl.GambleRiggerModule;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_1268;
import net.minecraft.class_1703;
import net.minecraft.class_1716;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import net.minecraft.class_480;

public class GambleRiggerGameTest implements FabricClientGameTest {
   private static final class_2338 DISPENSER = new class_2338(2, -59, 4);
   private static final class_1792[] ITEM = new class_1792[]{
      class_1802.field_8477,
      class_1802.field_8687,
      class_1802.field_8695,
      class_1802.field_8620,
      class_1802.field_8725,
      class_1802.field_8759,
      class_1802.field_8713,
      class_1802.field_8155,
      class_1802.field_8601
   };
   private static final String[] ID = new String[]{
      "minecraft:diamond",
      "minecraft:emerald",
      "minecraft:gold_ingot",
      "minecraft:iron_ingot",
      "minecraft:redstone",
      "minecraft:lapis_lazuli",
      "minecraft:coal",
      "minecraft:quartz",
      "minecraft:glowstone_dust"
   };
   private static final int[] COUNT = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};

   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1600, 900);
         context.waitTicks(2);
         TestServerContext server = world.getServer();
         server.runCommand("gamemode survival @a");
         server.runCommand("clear @a");
         server.runCommand("setblock 2 -59 4 minecraft:dispenser");

         for (int i = 0; i < 9; i++) {
            server.runCommand("item replace block 2 -59 4 container." + i + " with " + ID[i] + " " + COUNT[i]);
         }

         server.runCommand("tp @a 2 -58 2 0 0");
         context.waitTicks(20);
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> {
            GambleRiggerModule mod = NekoClient.modules().gambleRigger;
            require(mod != null, "GambleRigger registered");
            require(!mod.isEnabled(), "starts disabled");
            mod.clickDelay.set(0.0);
            mod.chatFeedback.set(false);
            mod.keepStays.set(true);
            mod.setEnabled(true);
         });
         openDispenser(context);
         context.waitTicks(3);
         context.runOnClient(
            mc -> {
               class_1703 menu = mc.field_1724.field_7512;
               require(menu instanceof class_1716, "dispenser menu open, got " + menu.getClass().getSimpleName());

               for (int i = 0; i < 9; i++) {
                  class_1799 s = menu.method_7611(i).method_7677();
                  require(
                     s.method_31574(ITEM[i]) && s.method_7947() == COUNT[i],
                     "grid slot " + i + " should be " + ITEM[i] + " x" + COUNT[i] + ", got " + describe(s)
                  );
               }
            }
         );
         context.takeScreenshot("gamblerigger-01-panel");
         context.runOnClient(mc -> NekoClient.modules().gambleRigger.requestKeep(5));
         context.waitTicks(40);
         context.runOnClient(
            mc -> {
               GambleRiggerModule mod = NekoClient.modules().gambleRigger;
               require(mod.phase() == GambleRiggerModule.Phase.EXTRACTED, "after taking, phase should be EXTRACTED, got " + mod.phase());
               class_1703 menu = mc.field_1724.field_7512;
               require(
                  menu.method_7611(5).method_7677().method_31574(ITEM[5]) && menu.method_7611(5).method_7677().method_7947() == COUNT[5],
                  "kept slot #6 must stay in place, got " + describe(menu.method_7611(5).method_7677())
               );

               for (int i = 0; i < 9; i++) {
                  if (i != 5) {
                     require(
                        menu.method_7611(i).method_7677().method_7960(),
                        "grid slot " + i + " should be empty after taking, got " + describe(menu.method_7611(i).method_7677())
                     );
                     boolean var10000 = invHas(menu, ITEM[i], COUNT[i]);
                     String var10001 = String.valueOf(ITEM[i]);
                     require(var10000, "inventory must now hold " + var10001 + " x" + COUNT[i] + " pulled from slot " + i);
                  }
               }
            }
         );
         context.takeScreenshot("gamblerigger-02-taken-keep6");
         context.runOnClient(mc -> NekoClient.modules().gambleRigger.requestRestore());
         context.waitTicks(40);
         context.runOnClient(
            mc -> {
               GambleRiggerModule mod = NekoClient.modules().gambleRigger;
               require(mod.phase() == GambleRiggerModule.Phase.IDLE, "after restore, phase should be IDLE, got " + mod.phase());
               class_1703 menu = mc.field_1724.field_7512;

               for (int i = 0; i < 9; i++) {
                  class_1799 s = menu.method_7611(i).method_7677();
                  require(
                     s.method_31574(ITEM[i]) && s.method_7947() == COUNT[i],
                     "restored grid slot " + i + " should be " + ITEM[i] + " x" + COUNT[i] + ", got " + describe(s)
                  );
               }
            }
         );
         context.takeScreenshot("gamblerigger-03-restored");
         context.runOnClient(mc -> {
            GambleRiggerModule mod = NekoClient.modules().gambleRigger;
            mod.keepStays.set(false);
            mod.requestKeep(2);
         });
         context.waitTicks(40);
         context.runOnClient(
            mc -> {
               class_1703 menu = mc.field_1724.field_7512;

               for (int i = 0; i < 9; i++) {
                  require(
                     menu.method_7611(i).method_7677().method_7960(),
                     "keep-stays OFF: slot " + i + " should be empty after taking, got " + describe(menu.method_7611(i).method_7677())
                  );
               }
            }
         );
         context.runOnClient(mc -> NekoClient.modules().gambleRigger.requestRestore());
         context.waitTicks(40);
         context.runOnClient(
            mc -> {
               class_1703 menu = mc.field_1724.field_7512;
               require(
                  menu.method_7611(2).method_7677().method_7960(),
                  "keep-stays OFF: picked slot #3 must be left empty, got " + describe(menu.method_7611(2).method_7677())
               );
               boolean var10000 = invHas(menu, ITEM[2], COUNT[2]);
               String var10001 = String.valueOf(ITEM[2]);
               require(var10000, "keep-stays OFF: you must still hold the picked stack " + var10001 + " x" + COUNT[2]);

               for (int i = 0; i < 9; i++) {
                  if (i != 2) {
                     class_1799 s = menu.method_7611(i).method_7677();
                     require(
                        s.method_31574(ITEM[i]) && s.method_7947() == COUNT[i],
                        "keep-stays OFF: slot " + i + " should be restored to " + ITEM[i] + " x" + COUNT[i] + ", got " + describe(s)
                     );
                  }
               }
            }
         );
         context.takeScreenshot("gamblerigger-04-keepoff-slot3-empty");
         context.runOnClient(mc -> {
            if (mc.field_1724 != null) {
               mc.field_1724.method_7346();
            }

            NekoClient.modules().gambleRigger.keepStays.set(true);
            NekoClient.modules().gambleRigger.reset();
         });
         context.waitForScreen((Class)null);
         context.runOnClient(mc -> ClickGuiScreen.state().setExpanded("GambleRigger@MISC", true));
         context.getInput().pressKey(344);
         context.waitForScreen(ClickGuiScreen.class);
         context.waitTicks(8);
         context.takeScreenshot("gamblerigger-05-settings");
         context.runOnClient(mc -> {
            NekoClient.modules().gambleRigger.setEnabled(false);
            ClickGuiScreen.state().setExpanded("GambleRigger@MISC", false);
         });
         context.getInput().pressKey(344);
         context.waitForScreen((Class)null);
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

   private static void openDispenser(ClientGameTestContext context) {
      context.runOnClient(
         mc -> {
            if (mc.field_1724 != null && mc.field_1761 != null) {
               mc.field_1761
                  .method_2896(
                     mc.field_1724, class_1268.field_5808, new class_3965(class_243.method_24953(DISPENSER), class_2350.field_11036, DISPENSER, false)
                  );
            }
         }
      );
      context.waitForScreen(class_480.class);
   }

   private static boolean invHas(class_1703 menu, class_1792 item, int count) {
      for (int i = 9; i < 45; i++) {
         class_1799 s = menu.method_7611(i).method_7677();
         if (s.method_31574(item) && s.method_7947() == count) {
            return true;
         }
      }

      return false;
   }

   private static String describe(class_1799 s) {
      return s.method_7960() ? "empty" : s.method_7909() + " x" + s.method_7947();
   }

   private static void require(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAILED: " + what);
      }
   }
}

package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.ColorSetting;
import dev.neko.client.settings.ModeSetting;
import dev.neko.client.settings.SliderSetting;
import dev.neko.client.util.Colors;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_310;
import net.minecraft.class_746;

public class CustomAccessoriesModule extends Module {
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Base tint for every accessory", -49508));
   public final BooleanSetting rainbow = this.addSetting(new BooleanSetting("Rainbow", "Cycle every accessory through the rainbow", false));
   public final SliderSetting glow = this.addSetting(new SliderSetting("Glow", "Soft outer-halo intensity", 70.0, 0.0, 100.0, 5.0, "%"));
   public final BooleanSetting firstPerson = this.addSetting(new BooleanSetting("First Person", "Also show your accessories in first-person view", false));
   public final BooleanSetting cape = this.addSetting(new BooleanSetting("Cape", "A flowing cloth cape down your back", true));
   public final ModeSetting capeStyle = this.addSetting(new ModeSetting("Cape Style", "Cape look", "Neko", "Neko", "Wave", "Grid", "Solid"));
   public final BooleanSetting capePhysics = this.addSetting(new BooleanSetting("Cape Physics", "Sway & billow with your movement", true));
   public final BooleanSetting trail = this.addSetting(new BooleanSetting("Trail", "A glowing trail left behind as you move", true));
   public final ModeSetting trailStyle = this.addSetting(new ModeSetting("Trail Style", "Trail look", "Ribbon", "Ribbon", "Sparkle", "Echo"));
   public final SliderSetting trailLength = this.addSetting(new SliderSetting("Trail Length", "How long the trail lingers", 1.2, 0.2, 4.0, 0.1, "s"));
   public final BooleanSetting aura = this.addSetting(new BooleanSetting("Aura", "An orbiting aura around your feet", false));
   public final ModeSetting auraStyle = this.addSetting(new ModeSetting("Aura Style", "Aura look", "Orbit", "Orbit", "Ring"));
   public final BooleanSetting crown = this.addSetting(new BooleanSetting("Crown", "A floating, spinning 67 above your head", false));
   private final Deque<CustomAccessoriesModule.TrailNode> trailNodes = new ArrayDeque<>();
   private static final int MAX_TRAIL = 256;

   public CustomAccessoriesModule() {
      super("CustomAccessories", "Client-side cosmetics — cape, trail, aura & crown", Category.VISUALS);
      ModeSetting var10000 = this.capeStyle;
      BooleanSetting var10001 = this.cape;
      var10000.visibleWhen(var10001::get);
      BooleanSetting var1 = this.capePhysics;
      var10001 = this.cape;
      var1.visibleWhen(var10001::get);
      ModeSetting var2 = this.trailStyle;
      var10001 = this.trail;
      var2.visibleWhen(var10001::get);
      SliderSetting var3 = this.trailLength;
      var10001 = this.trail;
      var3.visibleWhen(var10001::get);
      ModeSetting var4 = this.auraStyle;
      var10001 = this.aura;
      var4.visibleWhen(var10001::get);
   }

   public Deque<CustomAccessoriesModule.TrailNode> trailNodes() {
      return this.trailNodes;
   }

   @Override
   public void onTick() {
      if (!this.trail.get()) {
         if (!this.trailNodes.isEmpty()) {
            this.trailNodes.clear();
         }
      } else {
         class_746 player = class_310.method_1551().field_1724;
         if (player != null) {
            double x = player.method_23317();
            double y = player.method_23318() + player.method_17682() * 0.5;
            double z = player.method_23321();
            this.trailNodes.addLast(new CustomAccessoriesModule.TrailNode(x, y, z, System.nanoTime()));
            long cutoff = System.nanoTime() - (long)(Math.max(0.2F, this.trailLength.getFloat()) * 1.4E9);

            while (!this.trailNodes.isEmpty() && this.trailNodes.peekFirst().nanos < cutoff) {
               this.trailNodes.removeFirst();
            }

            while (this.trailNodes.size() > 256) {
               this.trailNodes.removeFirst();
            }
         }
      }
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   public void clear() {
      this.trailNodes.clear();
   }

   public int currentRgb() {
      if (this.rainbow.get()) {
         float hue = (float)(System.currentTimeMillis() % 4000L) / 4000.0F * 360.0F;
         return Colors.hsvToRgb(hue, 0.8F, 1.0F) & 16777215;
      } else {
         return this.color.get() & 16777215;
      }
   }

   public float glowStrength() {
      return this.glow.getFloat() / 100.0F;
   }

   public static final class TrailNode {
      public final double x;
      public final double y;
      public final double z;
      public final long nanos;

      TrailNode(double x, double y, double z, long nanos) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.nanos = nanos;
      }

      public float ageSeconds(long now) {
         return (float)(now - this.nanos) / 1.0E9F;
      }
   }
}

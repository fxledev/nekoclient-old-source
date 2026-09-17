package dev.neko.client.module;

import dev.neko.client.render.SusChunkRenderer;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.Setting;
import dev.neko.client.settings.SliderSetting;
import dev.neko.client.suschunk.SusChunkScanner;

public final class Modules {
   private Modules() {
   }

   public static class ClickGuiModule extends Module {
      public final BooleanSetting blur = this.addSetting(new BooleanSetting("Blur", "Gaussian-blur the world behind the GUI", true));
      public final SliderSetting blurStrength = this.addSetting(new SliderSetting("Blur Strength", "How strong the background blur is", 4.0, 1.0, 10.0, 1.0));

      public ClickGuiModule() {
         super("ClickGUI", "The NekoClient menu. F12 opens it.", Category.CLIENT);
         this.getKeybind().set(301);
      }
   }

   public static class ClusterEspModule extends Module {
      public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Tracers", "Draw lines from the camera to detected cluster cells", false));
      public final SusChunkScanner scanner = new SusChunkScanner(15);

      public ClusterEspModule() {
         super("ClusterESP", "Highlights light-0 amethyst cluster cells", Category.RENDER);
      }

      @Override
      public void onTick() {
         this.scanner.tick();
      }

      @Override
      protected void onDisable() {
         this.scanner.clear();
      }
   }

   public static class HudModule extends Module {
      public final BooleanSetting watermark = this.addSetting(new BooleanSetting("Watermark", "Show the NekoClient badge", true));
      public final BooleanSetting arrayList = this.addSetting(new BooleanSetting("ArrayList", "Enabled modules list", true));
      public final BooleanSetting fps = this.addSetting(new BooleanSetting("FPS", "Framerate readout", true));
      public final BooleanSetting ping = this.addSetting(new BooleanSetting("Ping", "Latency readout", false));
      public final BooleanSetting coordinates = this.addSetting(new BooleanSetting("Coordinates", "Block position readout", true));
      public final BooleanSetting direction = this.addSetting(new BooleanSetting("Direction", "Facing readout", true));
      public final BooleanSetting cps = this.addSetting(new BooleanSetting("CPS", "Clicks per second", false));
      public final BooleanSetting armor = this.addSetting(new BooleanSetting("Armor", "Equipped armor + durability", false));
      public final BooleanSetting potions = this.addSetting(new BooleanSetting("Potions", "Active effects with timers", false));
      public final BooleanSetting keystrokes = this.addSetting(new BooleanSetting("Keystrokes", "WASD + mouse + space display", false));
      public final BooleanSetting radar = this.addSetting(new BooleanSetting("Radar", "Circular player radar", true));

      public HudModule() {
         super("HUD", "All HUD elements — move & resize via chat (T)", Category.CLIENT);
         this.setEnabled(true);
      }
   }

   public static class Placeholder extends Module {
      public Placeholder(String name, String description, Category category, Setting<?>... settings) {
         super(name, description, category);

         for (Setting<?> setting : settings) {
            this.addSetting(setting);
         }
      }
   }

   public static class SpotifyModule extends Module {
      public SpotifyModule() {
         super("SpotifyHUD", "Now playing — skip and seek from the HUD", Category.CLIENT);
         this.setEnabled(true);
      }
   }

   public static class SusChunkFinderModule extends Module {
      public final SliderSetting sensitivity = this.addSetting(
         new SliderSetting("Sensitivity", "Higher = stricter: more weighted evidence before a chunk flags", 3.0, 1.0, 10.0, 1.0)
            .withLabel(v -> Integer.toString((int)v))
      );
      public final SusChunkScanner scanner = new SusChunkScanner(this.sensitivity);

      public SusChunkFinderModule() {
         super("SusChunkFinder", "Finds suspicious chunks using amethyst light signatures", Category.RENDER);
      }

      @Override
      public void onTick() {
         this.scanner.tick();
      }

      @Override
      protected void onDisable() {
         this.scanner.clear();
         SusChunkRenderer.reset();
      }
   }
}

package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.render.BlockEspRenderer;
import dev.neko.client.settings.BlockListSetting;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.ColorSetting;
import dev.neko.client.settings.ModeSetting;
import dev.neko.client.settings.SliderSetting;

public class BlockEspModule extends Module {
   public final BlockListSetting targets = this.addSetting(new BlockListSetting("Target Blocks", "Pick which blocks to highlight"));
   public final ModeSetting shapeMode = this.addSetting(new ModeSetting("Shape Mode", "How highlights are drawn", "Both", "Both", "Lines", "Sides"));
   public final ColorSetting lineColor = this.addSetting(new ColorSetting("Default Outline Color", "Color used for newly added blocks", -16711736));
   public final ColorSetting sideColor = this.addSetting(new ColorSetting("Fill Overlay", "Default fill tint", 419495880));
   public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each block", false));
   public final BooleanSetting tracer = this.addSetting(new BooleanSetting("Tracer", "Secondary tracer enable", true));
   public final ColorSetting tracerColor = this.addSetting(new ColorSetting("Default Tracer Tint", "Tracer line color / alpha", 2097217480));
   public final SliderSetting highlightAlpha = this.addSetting(new SliderSetting("Highlight Alpha", "Box opacity", 255.0, 0.0, 255.0, 5.0));

   public BlockEspModule() {
      super("BlockESP", "Highlights chosen blocks through walls", Category.RENDER);
      this.targets.seedDefaults();
      BooleanSetting var10000 = this.tracer;
      BooleanSetting var10001 = this.tracers;
      var10000.visibleWhen(var10001::get);
      ColorSetting var1 = this.tracerColor;
      var10001 = this.tracers;
      var1.visibleWhen(var10001::get);
   }

   @Override
   public void onTick() {
      BlockEspRenderer.scan(this);
   }

   @Override
   protected void onDisable() {
      BlockEspRenderer.clear();
   }
}

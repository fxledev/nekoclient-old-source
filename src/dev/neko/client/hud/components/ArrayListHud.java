package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.module.Modules;
import dev.neko.client.render.anim.Animation;
import dev.neko.client.render.anim.Easing;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class ArrayListHud extends HudComponent {
   private static final float ENTRY_HEIGHT = 24.0F;
   private static final float FONT_SIZE = 13.5F;
   private static final float PAD_X = 8.0F;
   private static final float STRIP_W = 2.5F;
   private final ModuleManager modules;
   private final Modules.HudModule hudModule;
   private final ThemeManager themes;
   private final Map<Module, Animation> slide = new HashMap<>();

   public ArrayListHud(ModuleManager modules, Modules.HudModule hudModule, ThemeManager themes, BooleanSupplier visible) {
      super("arraylist", 0.93F, 0.06F, visible);
      this.modules = modules;
      this.hudModule = hudModule;
      this.themes = themes;
      this.setScale(1.22F);
   }

   private List<Module> animatedEntries(NVGRenderer vg) {
      List<Module> list = new ArrayList<>();

      for (Module module : this.modules.all()) {
         if (module.getCategory() != Category.CLIENT) {
            Animation anim = this.slide.computeIfAbsent(module, m -> new Animation(240.0F, m.isEnabled() ? 1.0F : 0.0F, Easing.EASE_OUT_CUBIC));
            anim.setTarget(module.isEnabled() ? 1.0F : 0.0F);
            if (module.isEnabled() || anim.value() > 0.01F) {
               list.add(module);
            }
         }
      }

      list.sort(Comparator.<Module>comparingDouble(m -> vg.textWidth(m.getName(), 13.5F)).reversed());
      return list;
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      float max = 40.0F;

      for (Module m : this.animatedEntries(vg)) {
         max = Math.max(max, vg.textWidth(m.getName().toUpperCase(), 13.5F) + 18.0F);
      }

      return max;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      float h = 0.0F;

      for (Module m : this.animatedEntries(vg)) {
         h += 24.0F * this.slide.get(m).value();
      }

      return Math.max(20.0F, h);
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      boolean right = this.rightAnchored();
      float rowY = y;

      for (Module m : this.animatedEntries(vg)) {
         float t = this.slide.get(m).value();
         if (!(t <= 0.01F)) {
            float textW = vg.textWidth(m.getName().toUpperCase(), 13.5F);
            float rowW = textW + 24.0F;
            float slideOff = (1.0F - t) * (rowW + 12.0F) * (right ? 1 : -1);
            float rowX = Math.round((right ? x + w - rowW : x) + slideOff);
            float rowTop = Math.round(rowY);
            vg.save();
            vg.alpha(t);
            if (right) {
               vg.rectVarying(rowX, rowTop, rowW, 24.0F, 10.0F, 0.0F, 0.0F, 10.0F, -16579837);
               vg.rectVaryingOutline(rowX, rowTop, rowW, 24.0F, 10.0F, 0.0F, 0.0F, 10.0F, 1.5F, -1);
            } else {
               vg.rectVarying(rowX, rowTop, rowW, 24.0F, 0.0F, 10.0F, 10.0F, 0.0F, -16579837);
               vg.rectVaryingOutline(rowX, rowTop, rowW, 24.0F, 0.0F, 10.0F, 10.0F, 0.0F, 1.5F, -1);
            }

            vg.text(m.getName().toUpperCase(), rowX + 12.0F, rowTop + 12.0F, 13.5F, theme.textPrimary());
            vg.restore();
            rowY += 24.0F * t;
         }
      }
   }
}

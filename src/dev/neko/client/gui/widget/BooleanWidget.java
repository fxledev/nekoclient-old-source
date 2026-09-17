package dev.neko.client.gui.widget;

import dev.neko.client.render.anim.Animation;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.UiSounds;

public class BooleanWidget extends SettingWidget {
   public static final float HEIGHT = 22.0F;
   private static final float BOX = 14.0F;
   private final BooleanSetting setting;
   private final Animation check = new Animation(150.0F, 0.0F);

   public BooleanWidget(ThemeManager themes, BooleanSetting setting) {
      super(themes, setting);
      this.setting = setting;
      this.check.snapTo(setting.get() ? 1.0F : 0.0F);
   }

   @Override
   public float height(NVGRenderer vg) {
      return 22.0F;
   }

   @Override
   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      float cy = this.y + 11.0F;
      vg.text(this.setting.getName(), this.x, cy, 12.5F, theme.textMuted());
      this.check.setTarget(this.setting.get() ? 1.0F : 0.0F);
      float t = this.check.value();
      float bx = this.x + this.width - 14.0F;
      float by = cy - 7.0F;
      if (t > 0.02F) {
         vg.rect(bx, by, 14.0F, 14.0F, 4.0F, theme.accent());
      } else {
         vg.rectOutline(bx, by, 14.0F, 14.0F, 4.0F, 1.0F, theme.borderBright());
      }

      if (t > 0.02F) {
         vg.save();
         vg.alpha(t);
         vg.checkmark(bx, by, 14.0F, 2.0F, -1);
         vg.restore();
      }
   }

   @Override
   public boolean mouseClicked(float mx, float my, int button) {
      if (button == 0 && this.contains(mx, my)) {
         this.setting.toggle();
         UiSounds.checkbox(this.setting.get());
         return true;
      } else {
         return false;
      }
   }
}

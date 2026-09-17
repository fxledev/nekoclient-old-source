package dev.neko.client.gui.widget;

import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.settings.BlockListSetting;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import dev.neko.client.util.UiSounds;
import net.minecraft.class_310;

public class BlockListWidget extends SettingWidget {
   private static final float ROW = 26.0F;
   private final BlockListSetting setting;

   public BlockListWidget(ThemeManager themes, BlockListSetting setting) {
      super(themes, setting);
      this.setting = setting;
   }

   @Override
   public float height(NVGRenderer vg) {
      return 26.0F;
   }

   @Override
   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      float cy = this.y + 13.0F;
      vg.text(this.setting.getName(), this.x, cy, 12.5F, theme.textMuted());
      float bw = 88.0F;
      float bh = 18.0F;
      float bx = this.x + this.width - bw;
      float by = cy - bh / 2.0F;
      boolean hovered = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
      vg.rect(bx, by, bw, bh, 4.0F, theme.headerTop());
      vg.rectOutline(bx, by, bw, bh, bh / 2.0F, 1.1F, Colors.withAlpha(hovered ? theme.accentBright() : theme.accent(), hovered ? 0.9F : 0.4F));
      String label = "Pick Block";
      float tw = vg.textWidth(label, 12.0F);
      vg.textGradient(label, bx + (bw - tw) / 2.0F, by + bh / 2.0F, 12.0F, theme.accentBright(), theme.accent());
      long var10000 = this.setting.enabledCount();
      String count = var10000 + "/" + this.setting.size();
      vg.text(count, bx - 8.0F - vg.textWidth(count, 11.5F), cy, 11.5F, theme.textDisabled());
   }

   @Override
   public boolean mouseClicked(float mx, float my, int button) {
      if (button == 0 && this.contains(mx, my)) {
         if (class_310.method_1551().field_1755 instanceof ClickGuiScreen gui) {
            gui.openBlockPicker(this.setting);
            UiSounds.select();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}

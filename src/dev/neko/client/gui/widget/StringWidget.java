package dev.neko.client.gui.widget;

import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.settings.StringSetting;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import dev.neko.client.util.UiSounds;

public class StringWidget extends SettingWidget {
   private static final float HEIGHT = 34.0F;
   private static final float BOX_H = 18.0F;
   private static final float FONT = 12.0F;
   private final StringSetting setting;
   private boolean focused;

   public StringWidget(ThemeManager themes, StringSetting setting) {
      super(themes, setting);
      this.setting = setting;
   }

   @Override
   public float height(NVGRenderer vg) {
      return 34.0F;
   }

   @Override
   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      vg.text(this.setting.getName(), this.x, this.y + 8.0F, 12.5F, theme.textMuted());
      float bx = this.x;
      float by = this.y + 14.0F;
      float bw = this.width;
      int fill = Colors.withAlpha(-16777216, 0.45F);
      vg.rect(bx, by, bw, 18.0F, 9.0F, fill);
      vg.rectOutline(bx, by, bw, 18.0F, 9.0F, 1.1F, Colors.withAlpha(this.focused ? theme.accentBright() : theme.accent(), this.focused ? 0.9F : 0.35F));
      float textX = bx + 8.0F;
      float textY = by + 9.0F;
      String value = this.setting.get();
      if (value.isEmpty() && !this.focused) {
         vg.text(this.setting.getPlaceholder(), textX, textY, 12.0F, theme.textDisabled());
      } else {
         float w = vg.text(value, textX, textY, 12.0F, theme.textPrimary());
         if (this.focused && System.nanoTime() / 400000000L % 2L == 0L) {
            vg.rect(textX + w + 1.5F, textY - 6.0F, 1.4F, 12.0F, 0.7F, theme.accentBright());
         }
      }
   }

   @Override
   public boolean mouseClicked(float mx, float my, int button) {
      if (button == 0 && this.contains(mx, my)) {
         this.focused = true;
         UiSounds.select();
         return true;
      } else {
         this.focused = false;
         return false;
      }
   }

   @Override
   public boolean keyPressed(int keyCode) {
      if (!this.focused) {
         return false;
      } else {
         switch (keyCode) {
            case 256:
            case 257:
            case 335:
               this.focused = false;
               break;
            case 259:
               String v = this.setting.get();
               if (!v.isEmpty()) {
                  this.setting.set(v.substring(0, v.length() - 1));
               }
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(int codepoint) {
      if (!this.focused) {
         return false;
      } else if (this.setting.get().length() >= this.setting.getMaxLength()) {
         return true;
      } else if (Character.isValidCodePoint(codepoint) && !Character.isISOControl(codepoint)) {
         StringSetting var10000 = this.setting;
         String var10001 = this.setting.get();
         var10000.set(var10001 + new String(Character.toChars(codepoint)));
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean isListening() {
      return this.focused;
   }
}

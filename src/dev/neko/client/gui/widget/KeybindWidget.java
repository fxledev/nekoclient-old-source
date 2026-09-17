package dev.neko.client.gui.widget;

import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.settings.KeybindSetting;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import dev.neko.client.util.UiSounds;

public class KeybindWidget extends SettingWidget {
   public static final float HEIGHT = 22.0F;
   private final KeybindSetting setting;
   private boolean listening;

   public KeybindWidget(ThemeManager themes, KeybindSetting setting) {
      super(themes, setting);
      this.setting = setting;
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
      String label = this.listening ? "..." : this.setting.keyName();
      float chipW = Math.max(30.0F, vg.textWidth(label, 11.5F) + 12.0F);
      float chipX = this.x + this.width - chipW;
      float chipH = 16.0F;
      float chipY = cy - chipH / 2.0F;
      int fill = this.listening ? Colors.withAlpha(theme.accent(), 0.3F) : Colors.withAlpha(-16777216, 0.45F);
      vg.rect(chipX, chipY, chipW, chipH, chipH / 2.0F, fill);
      if (this.listening) {
         float pulse = (float)(0.5 + 0.5 * Math.sin(System.nanoTime() / 2.2E8));
         vg.rectOutline(chipX, chipY, chipW, chipH, chipH / 2.0F, 1.2F, Colors.withAlpha(theme.accentBright(), 0.4F + 0.6F * pulse));
      }

      vg.text(label, chipX + (chipW - vg.textWidth(label, 11.5F)) / 2.0F, cy, 11.5F, this.listening ? theme.accentBright() : theme.textPrimary());
   }

   @Override
   public boolean mouseClicked(float mx, float my, int button) {
      if (this.listening) {
         if (button == 0) {
            this.listening = false;
            return this.contains(mx, my);
         } else {
            this.setting.set(button);
            this.listening = false;
            UiSounds.keybindSet();
            return true;
         }
      } else if (button == 0 && this.contains(mx, my)) {
         this.listening = true;
         UiSounds.keybindListen();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(int keyCode) {
      if (!this.listening) {
         return false;
      } else {
         if (keyCode == 256) {
            this.setting.set(-1);
         } else {
            this.setting.set(keyCode);
         }

         this.listening = false;
         UiSounds.keybindSet();
         return true;
      }
   }

   @Override
   public boolean isListening() {
      return this.listening;
   }

   public void listen() {
      if (!this.listening) {
         this.listening = true;
         UiSounds.keybindListen();
      }
   }
}

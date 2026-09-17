package dev.neko.client.gui.panel;

import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.gui.ClickGuiState;
import dev.neko.client.gui.widget.BlockListWidget;
import dev.neko.client.gui.widget.BooleanWidget;
import dev.neko.client.gui.widget.ColorWidget;
import dev.neko.client.gui.widget.IconListWidget;
import dev.neko.client.gui.widget.KeybindWidget;
import dev.neko.client.gui.widget.ModeWidget;
import dev.neko.client.gui.widget.SettingWidget;
import dev.neko.client.gui.widget.SliderWidget;
import dev.neko.client.gui.widget.StringWidget;
import dev.neko.client.module.Module;
import dev.neko.client.render.anim.Animation;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.settings.BlockListSetting;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.ColorSetting;
import dev.neko.client.settings.IconListSetting;
import dev.neko.client.settings.KeybindSetting;
import dev.neko.client.settings.ModeSetting;
import dev.neko.client.settings.Setting;
import dev.neko.client.settings.SliderSetting;
import dev.neko.client.settings.StringSetting;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import dev.neko.client.util.UiSounds;
import java.util.ArrayList;
import java.util.List;

public class ModuleEntry {
   public static final float ROW_H = 54.0F;
   private static final float RADIUS = 16.0F;
   private static final float SETTING_INDENT = 12.0F;
   private final Module module;
   private final ThemeManager themes;
   private final ClickGuiState state;
   private final String key;
   private final List<SettingWidget> widgets = new ArrayList<>();
   private final Animation hover = new Animation(140.0F, 0.0F);
   private final Animation enable = new Animation(160.0F, 0.0F);
   private final Animation expand;
   private final KeybindWidget keybindChip;
   private float x;
   private float y;
   private float width;
   private float hitX;
   private float hitW;

   public ModuleEntry(Module module, ThemeManager themes, ClickGuiState state) {
      this.module = module;
      this.themes = themes;
      this.state = state;
      String var10001 = module.getName();
      this.key = var10001 + "@" + module.getCategory().name();
      this.expand = new Animation(190.0F, state.isExpanded(this.key) ? 1.0F : 0.0F);
      this.enable.snapTo(module.isEnabled() ? 1.0F : 0.0F);

      for (Setting<?> setting : module.getSettings()) {
         if (setting instanceof BooleanSetting b) {
            this.widgets.add(new BooleanWidget(themes, b));
         } else if (setting instanceof SliderSetting s) {
            this.widgets.add(new SliderWidget(themes, s));
         } else if (setting instanceof ModeSetting m) {
            this.widgets.add(new ModeWidget(themes, m));
         } else if (setting instanceof ColorSetting c) {
            this.widgets.add(new ColorWidget(themes, c));
         } else if (setting instanceof BlockListSetting bl) {
            this.widgets.add(new BlockListWidget(themes, bl));
         } else if (setting instanceof IconListSetting il) {
            this.widgets.add(new IconListWidget(themes, il));
         } else if (setting instanceof StringSetting str) {
            this.widgets.add(new StringWidget(themes, str));
         } else if (setting instanceof KeybindSetting k) {
            this.widgets.add(new KeybindWidget(themes, k));
         }
      }

      this.keybindChip = new KeybindWidget(themes, module.getKeybind());
      this.widgets.add(this.keybindChip);
   }

   public Module getModule() {
      return this.module;
   }

   public void setBounds(float x, float y, float width) {
      this.x = x;
      this.y = y;
      this.width = width;
   }

   private float settingsHeight(NVGRenderer vg) {
      float h = 6.0F;

      for (SettingWidget widget : this.widgets) {
         if (widget.isVisible()) {
            h += widget.height(vg) + 3.0F;
         }
      }

      return h + 3.0F;
   }

   public float height(NVGRenderer vg) {
      float t = this.expand.value();
      return 54.0F + (t <= 0.005F ? 0.0F : t * this.settingsHeight(vg));
   }

   public void render(NVGRenderer vg, float mx, float my, float fadeAlpha) {
      Theme theme = this.theme();
      boolean hovered = mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + 54.0F;
      if (hovered && this.hover.getTarget() < 0.5F) {
      }

      this.hover.setTarget(hovered ? 1.0F : 0.0F);
      this.enable.setTarget(this.module.isEnabled() ? 1.0F : 0.0F);
      float hoverT = this.hover.value();
      float enableT = this.enable.value();
      float expandT = this.expand.value();
      vg.save();
      vg.alpha(fadeAlpha);
      boolean enabled = this.module.isEnabled();
      vg.glass(this.x, this.y, this.width, 54.0F, 16.0F);
      if (enabled) {
         vg.rectOutline(this.x, this.y, this.width, 54.0F, 16.0F, 1.2F, Colors.withAlpha(theme.accent(), 0.45F));
      }

      boolean fav = ClickGuiScreen.guiFavs.contains(ClickGuiScreen.favKey(this.module));
      float contentDx = fav ? 12.0F : 0.0F;
      if (fav) {
         float dx = this.x + 14.0F;
         float dy = this.y + 27.0F;
         vg.save();
         vg.translate(dx, dy);
         vg.rotate((float) (Math.PI / 4));
         vg.rect(-3.5F, -3.5F, 7.0F, 7.0F, 1.5F, theme.accent());
         vg.restore();
      }

      float chx = this.x + 15.0F + contentDx;
      float chy = this.y + 27.0F;
      if (expandT > 0.5F) {
         vg.triangle(chx - 4.0F, chy - 2.5F, chx + 4.0F, chy - 2.5F, chx, chy + 3.5F, theme.textMuted());
      } else {
         vg.triangle(chx - 2.5F, chy - 4.0F, chx - 2.5F, chy + 4.0F, chx + 3.5F, chy, theme.textMuted());
      }

      float textY = this.y + 27.0F - 6.0F;
      float subY = this.y + 27.0F + 9.0F;
      float nameX = this.x + 28.0F + contentDx;
      if (enableT > 0.01F) {
         vg.save();
         vg.alpha(enableT);
         vg.text(this.module.getName(), nameX, textY, 13.5F, theme.accentBright());
         vg.restore();
      }

      if (enableT < 0.99F) {
         vg.save();
         vg.alpha(1.0F - enableT);
         int idle = Colors.lerp(theme.textMuted(), theme.textPrimary(), hoverT);
         vg.text(this.module.getName(), nameX, textY, 13.5F, idle);
         vg.restore();
      }

      vg.textTruncated(this.module.getDescription(), nameX, subY, 11.0F, theme.textMuted(), this.width - 190.0F);
      String chipLabel = this.keybindChip.isListening() ? "..." : this.module.getKeybind().keyName();
      float chipW = Math.max(36.0F, vg.textWidth(chipLabel, 11.0F) + 14.0F);
      float chipX = this.x + this.width - 58.0F - 8.0F - chipW;
      float chipY = this.y + 18.0F;
      this.hitX = chipX;
      this.hitW = chipW;
      vg.glass(chipX, chipY, chipW, 18.0F, 9.0F);
      if (this.keybindChip.isListening()) {
         vg.rectOutline(chipX, chipY, chipW, 18.0F, 9.0F, 1.2F, Colors.withAlpha(theme.accentBright(), 0.8F));
      }

      vg.text(
         chipLabel,
         chipX + (chipW - vg.textWidth(chipLabel, 11.0F)) / 2.0F,
         chipY + 9.0F,
         11.0F,
         this.keybindChip.isListening() ? theme.accentBright() : theme.textMuted()
      );
      vg.iosToggle(this.x + this.width - 58.0F, this.y + 15.0F, 46.0F, 24.0F, enabled, theme.accent());
      if (expandT > 0.005F) {
         float settingsH = this.settingsHeight(vg) * expandT;
         vg.save();
         vg.scissor(this.x, this.y + 54.0F, this.width, settingsH);
         vg.alpha(expandT);
         vg.rect(this.x + 4.0F, this.y + 54.0F - 4.0F, this.width - 8.0F, settingsH + 0.0F, 10.0F, Colors.withAlpha(-16777216, 0.35F));
         float wy = this.y + 54.0F + 6.0F;

         for (SettingWidget widget : this.widgets) {
            if (widget.isVisible()) {
               widget.setBounds(this.x + 12.0F, wy, this.width - 24.0F);
               widget.render(vg, mx, my);
               wy += widget.height(vg) + 3.0F;
            }
         }

         vg.restore();
      }

      vg.restore();
   }

   private Theme theme() {
      return this.themes.current();
   }

   public float chipX(NVGRenderer vg) {
      return this.hitX;
   }

   public float chipW(NVGRenderer vg) {
      return this.hitW;
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + 54.0F) {
         float cx = this.hitX;
         float cw = this.hitW;
         float cy = this.y + 27.0F - 9.0F;
         if (button == 0 && mx >= cx && mx <= cx + cw && my >= cy && my <= cy + 18.0F) {
            this.keybindChip.listen();
            return true;
         } else {
            if (button == 0) {
               this.module.toggle();
               UiSounds.toggle(this.module.isEnabled());
            } else if (button == 1) {
               boolean expanded = !(this.expand.getTarget() > 0.5F);
               this.expand.setTarget(expanded ? 1.0F : 0.0F);
               this.state.setExpanded(this.key, expanded);
               UiSounds.select();
            } else if (button == 2) {
               String k = ClickGuiScreen.favKey(this.module);
               if (!ClickGuiScreen.guiFavs.remove(k)) {
                  ClickGuiScreen.guiFavs.add(k);
               }

               ClickGuiScreen.saveGuiPrefs();
               UiSounds.select();
            }

            return true;
         }
      } else if (this.expand.getTarget() > 0.5F && my >= this.y + 54.0F && my <= this.y + this.height((NVGRenderer)null)) {
         for (SettingWidget widget : this.widgets) {
            if (widget.isVisible() && widget.mouseClicked(mx, my, button)) {
               return true;
            }
         }

         return mx >= this.x && mx <= this.x + this.width;
      } else {
         return false;
      }
   }

   public void mouseDragged(float mx, float my) {
      for (SettingWidget widget : this.widgets) {
         widget.mouseDragged(mx, my);
      }
   }

   public void mouseReleased() {
      for (SettingWidget widget : this.widgets) {
         widget.mouseReleased();
      }
   }

   public boolean keyPressed(int keyCode) {
      for (SettingWidget widget : this.widgets) {
         if (widget.keyPressed(keyCode)) {
            return true;
         }
      }

      return false;
   }

   public boolean charTyped(int codepoint) {
      for (SettingWidget widget : this.widgets) {
         if (widget.charTyped(codepoint)) {
            return true;
         }
      }

      return false;
   }

   public boolean isListening() {
      for (SettingWidget widget : this.widgets) {
         if (widget.isListening()) {
            return true;
         }
      }

      return false;
   }
}

package dev.neko.client.gui.widget;

import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.settings.Setting;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;

public abstract class SettingWidget {
   protected final ThemeManager themes;
   private final Setting<?> boundSetting;
   protected float x;
   protected float y;
   protected float width;

   protected SettingWidget(ThemeManager themes, Setting<?> boundSetting) {
      this.themes = themes;
      this.boundSetting = boundSetting;
   }

   public boolean isVisible() {
      return this.boundSetting == null || this.boundSetting.isVisible();
   }

   protected Theme theme() {
      return this.themes.current();
   }

   public void setBounds(float x, float y, float width) {
      this.x = x;
      this.y = y;
      this.width = width;
   }

   public boolean contains(float mx, float my) {
      return mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height((NVGRenderer)null);
   }

   public abstract float height(NVGRenderer var1);

   public abstract void render(NVGRenderer var1, float var2, float var3);

   public boolean mouseClicked(float mx, float my, int button) {
      return false;
   }

   public void mouseDragged(float mx, float my) {
   }

   public void mouseReleased() {
   }

   public boolean keyPressed(int keyCode) {
      return false;
   }

   public boolean charTyped(int codepoint) {
      return false;
   }

   public boolean isListening() {
      return false;
   }
}

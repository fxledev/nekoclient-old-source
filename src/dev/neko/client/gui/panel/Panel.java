package dev.neko.client.gui.panel;

import dev.neko.client.gui.ClickGuiState;
import dev.neko.client.render.anim.Animation;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;

public abstract class Panel {
   public static final float WIDTH = 638.0F;
   public static final float HEADER_H = 38.0F;
   public static final float RADIUS = 4.0F;
   protected static final float CONTENT_PAD = 6.0F;
   private static final float FADE_ZONE = 16.0F;
   protected final ThemeManager themes;
   protected final ClickGuiState.PanelState ps;
   private final Animation open;
   private final Animation scroll = new Animation(200.0F, 0.0F);
   private float maxScroll;
   private boolean headerHovered;

   protected Panel(ThemeManager themes, ClickGuiState.PanelState ps) {
      this.themes = themes;
      this.ps = ps;
      this.open = new Animation(200.0F, ps.collapsed ? 0.0F : 1.0F);
   }

   protected Theme theme() {
      return this.themes.current();
   }

   public float x() {
      return this.ps.x;
   }

   public float y() {
      return this.ps.y;
   }

   public void moveTo(float x, float y) {
      this.ps.x = x;
      this.ps.y = y;
   }

   public void toggleCollapsed() {
      this.ps.collapsed = !this.ps.collapsed;
      this.open.setTarget(this.ps.collapsed ? 0.0F : 1.0F);
   }

   protected abstract String title();

   protected boolean showHeader() {
      return true;
   }

   protected int icon() {
      return -1;
   }

   protected abstract float contentHeight(NVGRenderer var1);

   protected abstract void renderContent(NVGRenderer var1, float var2, float var3, float var4, float var5, float var6);

   protected float maxViewHeight(float uiHeight) {
      float headerHeight = this.showHeader() ? 38.0F : 0.0F;
      return Math.max(60.0F, Math.min(468.0F, uiHeight - this.ps.y - headerHeight - 24.0F));
   }

   protected float viewHeight(NVGRenderer vg, float uiHeight) {
      return Math.min(this.contentHeight(vg), this.maxViewHeight(uiHeight)) * this.open.value();
   }

   public float totalHeight(NVGRenderer vg, float uiHeight) {
      return 38.0F + this.viewHeight(vg, uiHeight);
   }

   protected float edgeFade(float rowTop, float rowBottom, float viewTop, float viewBottom) {
      float fadeTop = Math.clamp((rowBottom - viewTop) / 16.0F, 0.0F, 1.0F);
      float fadeBottom = Math.clamp((viewBottom - rowTop) / 16.0F, 0.0F, 1.0F);
      return Math.min(fadeTop, fadeBottom);
   }

   public void render(NVGRenderer vg, float mx, float my, float uiWidth, float uiHeight) {
      this.ps.x = Math.clamp(this.ps.x, 0.0F, Math.max(0.0F, uiWidth - 638.0F - 12.0F));
      this.ps.y = Math.clamp(this.ps.y, 0.0F, uiHeight - 38.0F);
      Theme theme = this.theme();
      float viewH = this.viewHeight(vg, uiHeight);
      boolean hasBody = viewH > 0.5F;
      boolean hoveredNow = this.showHeader() && this.headerHit(mx, my);
      if (hoveredNow && !this.headerHovered) {
      }

      this.headerHovered = hoveredNow;
      float headerHeight = this.showHeader() ? 38.0F : 0.0F;
      if (hasBody) {
         vg.rect(this.ps.x, this.ps.y + headerHeight, 638.0F, viewH, 14.0F, Colors.withAlpha(-16777216, 0.18F));
      }

      if (this.showHeader()) {
         vg.rect(this.ps.x, this.ps.y, 638.0F, 38.0F, 14.0F, -16053493);
         float textX = this.ps.x + 12.0F;
         int iconHandle = this.icon();
         if (iconHandle > 0) {
            vg.image(iconHandle, this.ps.x + 11.0F, this.ps.y + 10.0F, 18.0F, 18.0F, theme.accentBright());
            textX = this.ps.x + 36.0F;
         }

         vg.text(this.title(), textX, this.ps.y + 19.0F, 16.5F, theme.textPrimary());
         float chevX = this.ps.x + 638.0F - 16.0F;
         float chevY = this.ps.y + 19.0F;
         vg.save();
         vg.translate(chevX, chevY);
         vg.rotate((float)(this.open.value() * Math.PI / 2.0));
         vg.chevron(0.0F, 0.0F, 4.5F, 1.8F, theme.textMuted(), false);
         vg.restore();
      }

      if (hasBody) {
         float viewTop = this.ps.y + headerHeight;
         float viewBottom = viewTop + viewH;
         this.maxScroll = Math.max(0.0F, this.contentHeight(vg) - this.maxViewHeight(uiHeight));
         this.scroll.setTarget(Math.clamp(this.scroll.getTarget(), 0.0F, this.maxScroll));
         vg.save();
         vg.scissor(this.ps.x, viewTop, 638.0F, viewH);
         this.renderContent(vg, viewTop + 6.0F - this.scroll.value(), mx, my, viewTop, viewBottom);
         vg.restore();
      }
   }

   public boolean headerHit(float mx, float my) {
      return this.showHeader() && mx >= this.ps.x && mx <= this.ps.x + 638.0F && my >= this.ps.y && my <= this.ps.y + 38.0F;
   }

   public boolean bodyHit(NVGRenderer vg, float mx, float my, float uiHeight) {
      float viewH = this.viewHeight(vg, uiHeight);
      float headerHeight = this.showHeader() ? 38.0F : 0.0F;
      return mx >= this.ps.x && mx <= this.ps.x + 638.0F && my >= this.ps.y + headerHeight && my <= this.ps.y + headerHeight + viewH;
   }

   public void onScroll(double amount) {
      if (!(this.maxScroll <= 0.0F)) {
         this.scroll.setTarget(Math.clamp(this.scroll.getTarget() - (float)amount * 38.0F, 0.0F, this.maxScroll));
      }
   }

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

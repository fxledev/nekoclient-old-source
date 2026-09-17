package dev.neko.client.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.neko.client.NekoClient;
import dev.neko.client.gui.config.ConfigPanel;
import dev.neko.client.gui.panel.CategoryPanel;
import dev.neko.client.gui.panel.Panel;
import dev.neko.client.gui.picker.BlockGridModel;
import dev.neko.client.gui.picker.IconListGridModel;
import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.module.Modules;
import dev.neko.client.module.impl.BlockEspModule;
import dev.neko.client.render.BlurHook;
import dev.neko.client.render.NvgDrawable;
import dev.neko.client.render.OverlayRenderer;
import dev.neko.client.render.anim.Animation;
import dev.neko.client.render.nanovg.NVGImages;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.settings.BlockListSetting;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.IconListSetting;
import dev.neko.client.spotify.SpotifyState;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import dev.neko.client.util.CpsTracker;
import dev.neko.client.util.UiSounds;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_640;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;

public class ClickGuiScreen extends class_437 implements NvgDrawable {
   private static final ClickGuiState STATE = new ClickGuiState();
   public static int guiSort = 0;
   public static final Set<String> guiFavs = new LinkedHashSet<>();
   private final List<Panel> panels;
   private final Animation openAnim;
   private boolean closing;
   private final ConfigPanel configPanel;
   private static final float FIDGET_W = 118.0F;
   private static final float FIDGET_H = 28.0F;
   private boolean fidgetHovered;
   private static final float SEARCH_H = 32.0F;
   private static final float FRAME_W = 860.0F;
   private static final float FRAME_H = 550.0F;
   private static final float RAIL_W = 64.0F;
   private static final float CONTENT_W = 760.0F;
   private static final float SEARCH_W = 260.0F;
   private static final int CONFIG_TAB = 4;
   private final StringBuilder search;
   private int activeCategory;
   private boolean searchFocused;
   private Panel dragging;
   private float dragOffsetX;
   private float dragOffsetY;
   private float pressX;
   private float pressY;
   private boolean dragMoved;
   private Panel pressedContentPanel;
   private final class_437 parent;
   private boolean uiOpen;
   private float uiScroll;
   private final List<ClickGuiScreen.UiHit> uiHits = new ArrayList<>();
   private final List<ClickGuiScreen.UiToggle> hudToggles = new ArrayList<>();
   private boolean galleryOpen;
   private float galleryScroll;
   private final List<ClickGuiScreen.UiHit> galleryHits = new ArrayList<>();
   private final List<ClickGuiScreen.UiHit> musicHits = new ArrayList<>();
   private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("HH:mm");
   private static final float SIDEBAR_W = 190.0F;
   private static final float CONTENT_PAD = 16.0F;
   private static final class_2960 MEOW_ID = class_2960.method_60655("nekoclient", "meow-white.png");
   private static final float MEOW_ASPECT = 1.3364055F;
   private static final float GALLERY_W = 638.0F;
   private static final float GALLERY_H = 434.0F;

   public static String favKey(Module m) {
      return m.getName() + "@" + m.getCategory().name();
   }

   public static JsonObject guiPrefsToJson() {
      JsonObject json = new JsonObject();
      json.addProperty("sort", guiSort);
      JsonArray favs = new JsonArray();

      for (String k : guiFavs) {
         favs.add(k);
      }

      json.add("favs", favs);
      return json;
   }

   public static void guiPrefsFromJson(JsonObject json) {
      if (json != null) {
         if (json.has("sort")) {
            try {
               guiSort = Math.clamp((long)json.get("sort").getAsInt(), 0, 2);
            } catch (Exception var5) {
            }
         }

         guiFavs.clear();
         if (json.has("favs") && json.get("favs").isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray("favs")) {
               try {
                  guiFavs.add(e.getAsString());
               } catch (Exception var4) {
               }
            }
         }
      }
   }

   public static void saveGuiPrefs() {
      try {
         if (NekoClient.config() != null) {
            NekoClient.config().save();
         }
      } catch (Exception var1) {
      }
   }

   public ClickGuiScreen() {
      this((class_437)null);
   }

   public ClickGuiScreen(class_437 parent) {
      super(class_2561.method_43470("NekoClient"));
      this.panels = new ArrayList<>();
      this.openAnim = new Animation(180.0F, 0.0F);
      this.configPanel = new ConfigPanel();
      this.search = new StringBuilder();
      this.parent = parent;
      STATE.ensureDefaultLayout(OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight());
      ModuleManager modules = NekoClient.modules();
      ThemeManager themes = NekoClient.themes();

      for (Category category : Category.values()) {
         if (category != Category.VISUALS && category != Category.CLIENT) {
            this.panels.add(new CategoryPanel(category, modules, themes, STATE));
         }
      }

      this.activeCategory = Category.COMBAT.ordinal();
      this.openAnim.setTarget(1.0F);
      Modules.HudModule hud = modules.hud;
      this.hudToggles.add(new ClickGuiScreen.UiToggle("Watermark", hud.watermark));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("ArrayList", hud.arrayList));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("FPS", hud.fps));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("Ping", hud.ping));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("Coordinates", hud.coordinates));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("Direction", hud.direction));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("CPS", hud.cps));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("Armor", hud.armor));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("Potions", hud.potions));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("Keystrokes", hud.keystrokes));
      this.hudToggles.add(new ClickGuiScreen.UiToggle("Radar", hud.radar));
   }

   public void method_49589() {
      super.method_49589();
   }

   public static ClickGuiState state() {
      return STATE;
   }

   private Modules.ClickGuiModule guiModule() {
      return NekoClient.modules().clickGui;
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25419() {
      if (!this.closing) {
         this.closing = true;
         this.openAnim.setTarget(0.0F);
      }
   }

   public void method_25432() {
      BlurHook.clear();
      NekoClient.config().save();
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      int alpha = (int)(48.0F * this.openAnim.value());
      context.method_25294(0, 0, this.field_22789, this.field_22790, alpha << 24 | 394506);
      this.finishCloseIfDone();
   }

   public void method_25393() {
      this.finishCloseIfDone();
   }

   private void finishCloseIfDone() {
      if (this.closing && this.openAnim.isDone()) {
         this.field_22787.method_1507(this.parent);
      }
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      if (this.field_22787.field_1687 == null) {
         this.method_57728(context, deltaTicks);
      }

      if (this.guiModule().blur.get()) {
         BlurHook.set(this.guiModule().blurStrength.getFloat() * this.openAnim.value());
         context.method_71278();
      } else {
         BlurHook.clear();
      }
   }

   @Override
   public void renderNvg(NVGRenderer vg, float mouseX, float mouseY, float uiWidth, float uiHeight) {
      if (vg.hasFont()) {
         float t = this.openAnim.value();
         if (!(t <= 0.002F) || !this.closing) {
            vg.save();
            vg.alpha(t);
            float scale = 0.93F + 0.07F * t;
            vg.translate(uiWidth / 2.0F, uiHeight / 2.0F);
            vg.scale(scale);
            vg.translate(-uiWidth / 2.0F, -uiHeight / 2.0F);
            String query = this.search.toString();

            for (Panel panel : this.panels) {
               if (panel instanceof CategoryPanel categoryPanel) {
                  categoryPanel.setFilter(query);
               }
            }

            this.renderFrame(vg, uiWidth, uiHeight, mouseX, mouseY);
            float cx0 = this.contentX(uiWidth);
            float listY = this.listY(uiHeight);
            float listH = this.listH();
            if (this.configPanel.isOpen()) {
               this.configPanel.setEmbeddedBounds(cx0, listY, this.contentW(), listH);
               this.configPanel.render(vg, mouseX, mouseY, uiWidth, uiHeight);
            } else if (!this.galleryOpen && !this.uiOpen) {
               CategoryPanel active = this.activePanel();
               active.moveTo(cx0, listY);
               vg.save();
               vg.scissor(cx0, listY, this.contentW() + 18.0F, listH);
               active.render(vg, mouseX, mouseY, uiWidth, uiHeight);
               vg.restore();
            }

            if (this.galleryOpen) {
               this.renderGallery(vg, uiWidth, uiHeight, mouseX, mouseY);
            }

            if (this.uiOpen) {
               this.renderUiPopup(vg, uiWidth, uiHeight, mouseX, mouseY);
            }

            vg.restore();
         }
      }
   }

   private CategoryPanel activePanel() {
      return (CategoryPanel)this.panels.get(Math.clamp((long)this.activeCategory, 0, this.panels.size() - 1));
   }

   private float frameX(float uiWidth) {
      return (uiWidth - 860.0F) / 2.0F;
   }

   private float frameY(float uiHeight) {
      return (uiHeight - 550.0F) / 2.0F;
   }

   private float contentX(float uiWidth) {
      return this.frameX(uiWidth) + 190.0F + 16.0F;
   }

   private float contentW() {
      return 638.0F;
   }

   private float listY(float uiHeight) {
      return this.frameY(uiHeight) + 100.0F;
   }

   private float listH() {
      return 434.0F;
   }

   private void renderFrame(NVGRenderer vg, float uiWidth, float uiHeight, float mx, float my) {
      Theme theme = NekoClient.themes().current();
      float x = this.frameX(uiWidth);
      float y = this.frameY(uiHeight);
      int bg = -1;

      try {
         bg = NVGImages.fromResource(class_2960.method_60655("nekoclient", "background.jpg"));
      } catch (Exception var37) {
      }

      if (bg > 0) {
         vg.imageCover(bg, x, y, 860.0F, 550.0F, 20.0F);
      } else {
         vg.rect(x, y, 860.0F, 550.0F, 20.0F, theme.background());
      }

      vg.rect(x, y, 860.0F, 550.0F, 20.0F, Colors.withAlpha(-16777216, 0.58F));
      vg.rectOutline(x, y, 860.0F, 550.0F, 20.0F, 1.0F, Colors.withAlpha(-1, 0.14F));
      vg.rectGradient(x, y, 190.0F, 550.0F, 20.0F, Colors.withAlpha(-16777216, 0.38F), Colors.withAlpha(-16777216, 0.0F), false);
      this.drawMeow(vg, x + 14.0F, y + 10.0F, 26.0F, theme.accent());
      vg.text("Neko", x + 14.0F + 34.746544F + 8.0F, y + 25.0F, 18.0F, theme.textPrimary());
      vg.text("1.0.0-neko.1", x + 14.0F, y + 44.0F, 10.0F, theme.textDisabled());
      float sbx = x + 12.0F;
      float sby = y + 56.0F;
      float sbw = 166.0F;
      vg.glass(sbx, sby, sbw, 30.0F, 10.0F);
      vg.circleOutline(sbx + 15.0F, sby + 14.5F, 4.5F, 1.6F, theme.textMuted());
      vg.line(sbx + 18.4F, sby + 18.2F, sbx + 21.5F, sby + 21.4F, 1.6F, theme.textMuted());
      float stx = sbx + 28.0F;
      if (this.search.isEmpty() && !this.searchFocused) {
         vg.text("Search...", stx, sby + 15.0F, 12.5F, theme.textDisabled());
      } else {
         float sw = vg.text(this.search.toString(), stx, sby + 15.0F, 12.5F, theme.textPrimary());
         if (this.searchFocused && System.nanoTime() / 400000000L % 2L == 0L) {
            vg.rect(stx + sw + 2.0F, sby + 8.0F, 1.5F, 14.0F, 0.75F, theme.accentBright());
         }
      }

      if (!this.search.isEmpty()) {
         vg.cross(sbx + sbw - 22.0F, sby + 10.0F, 12.0F, 1.6F, theme.textMuted());
      }

      CategoryPanel ap = this.activePanel();
      String cnt2 = ap.visibleCount() + "/" + ap.totalCount();
      vg.text(cnt2, sbx + sbw - 34.0F - vg.textWidth(cnt2, 10.0F), sby + 15.0F, 10.0F, theme.textDisabled());
      Category[] cats = new Category[]{Category.COMBAT, Category.MISC, Category.RENDER};
      float navY = y + 100.0F;

      for (int i = 0; i < 3; i++) {
         boolean selected = !this.configPanel.isOpen() && !this.uiOpen && !this.galleryOpen && i == this.activeCategory;
         this.sideRow(vg, theme, x + 10.0F, navY + i * 44.0F, 170.0F, selected, mx, my);
         this.drawCatIcon(vg, cats[i], x + 10.0F + 20.0F, navY + i * 44.0F + 20.0F, 18.0F, selected ? theme.accentBright() : theme.textMuted());
         int ncol = selected ? theme.textPrimary() : theme.textMuted();
         vg.text(cats[i].getDisplayName(), x + 10.0F + 36.0F, navY + i * 44.0F + 20.0F, 13.0F, ncol);
         int total = NekoClient.modules().inCategory(cats[i]).size();
         int en = 0;

         for (Module m : NekoClient.modules().inCategory(cats[i])) {
            if (m.isEnabled()) {
               en++;
            }
         }

         String cnt = en + "/" + total;
         vg.text(cnt, x + 190.0F - 10.0F - vg.textWidth(cnt, 10.0F), navY + i * 44.0F + 20.0F, 10.0F, theme.textDisabled());
      }

      float divY = navY + 132.0F + 8.0F;
      vg.line(x + 16.0F, divY, x + 190.0F - 16.0F, divY, 1.0F, Colors.withAlpha(theme.textDisabled(), 0.4F));
      float cfgY = divY + 12.0F;
      boolean cfgSel = this.configPanel.isOpen();
      this.sideRow(vg, theme, x + 10.0F, cfgY, 170.0F, cfgSel, mx, my);
      int cfgCol = cfgSel ? theme.accentBright() : theme.textMuted();
      vg.rect(x + 10.0F + 13.0F, cfgY + 13.0F, 14.0F, 10.0F, 2.5F, Colors.withAlpha(cfgCol, 0.4F));
      vg.rect(x + 10.0F + 8.0F, cfgY + 17.0F, 14.0F, 10.0F, 2.5F, cfgCol);
      vg.text("Configs", x + 10.0F + 36.0F, cfgY + 20.0F, 13.0F, cfgSel ? theme.textPrimary() : theme.textMuted());
      float ifcY = cfgY + 44.0F;
      this.sideRow(vg, theme, x + 10.0F, ifcY, 170.0F, this.uiOpen, mx, my);
      this.drawGearGlyph(vg, x + 10.0F + 20.0F, ifcY + 20.0F, 10.0F, this.uiOpen ? theme.accentBright() : theme.textMuted());
      vg.text("Interface", x + 10.0F + 36.0F, ifcY + 20.0F, 13.0F, this.uiOpen ? theme.textPrimary() : theme.textMuted());
      this.renderSideProfile(vg);
      this.renderSideSpotify(vg, uiWidth, uiHeight, mx, my);
      this.renderSideStats(vg, uiWidth, uiHeight);
      float cx0 = this.contentX(uiWidth);
      float cw = this.contentW();
      String title = this.categoryTitle();
      vg.text(title, cx0, y + 30.0F, 22.0F, theme.textPrimary());
      String sub = this.headerSubtitle();
      vg.text(sub, cx0, y + 52.0F, 11.5F, theme.textMuted());
      float right = cx0 + cw;
      if (!this.configPanel.isOpen() && !this.galleryOpen) {
         float noneX = right - 72.0F;
         float allX = noneX - 8.0F - 56.0F;
         float palCX = allX - 8.0F - 15.0F;
         float hcy = y + 34.0F;
         float sortX = palCX - 15.0F - 8.0F - 52.0F;
         String sortLabel = guiSort == 1 ? "A-Z" : (guiSort == 2 ? "ON" : "Sort");
         boolean sortHover = mx >= sortX && mx <= sortX + 52.0F && my >= hcy - 14.0F && my <= hcy + 14.0F;
         this.pillBtn(vg, theme, sortX, hcy - 14.0F, 52.0F, sortLabel, mx, my);
         if (guiSort != 0 && !sortHover) {
            vg.rectOutline(sortX, hcy - 14.0F, 52.0F, 28.0F, 14.0F, 1.0F, Colors.withAlpha(theme.accent(), 0.55F));
         }

         boolean palHover = mx >= palCX - 15.0F && mx <= palCX + 15.0F && my >= hcy - 15.0F && my <= hcy + 15.0F;
         int palCol = !this.galleryOpen && !palHover ? theme.textMuted() : theme.accentBright();
         if (this.galleryOpen) {
            vg.glow(palCX - 15.0F, hcy - 15.0F, 30.0F, 30.0F, 15.0F, 6.0F, Colors.withAlpha(theme.accent(), 0.4F));
         }

         vg.circleOutline(palCX, hcy, 9.0F, 1.6F, palCol);
         vg.circle(palCX - 3.5F, hcy - 1.0F, 2.4F, theme.accent());
         vg.circle(palCX + 3.5F, hcy - 2.0F, 2.4F, Colors.withAlpha(-11698946, 0.95F));
         vg.circle(palCX, hcy + 4.0F, 2.4F, Colors.withAlpha(-13315175, 0.95F));
         this.pillBtn(vg, theme, allX, hcy - 14.0F, 56.0F, "All", mx, my);
         this.pillBtn(vg, theme, noneX, hcy - 14.0F, 72.0F, "None", mx, my);
      }
   }

   private void sideRow(NVGRenderer vg, Theme theme, float rx, float ry, float rw, boolean selected, float mx, float my) {
      boolean hover = mx >= rx && mx <= rx + rw && my >= ry && my <= ry + 40.0F;
      if (selected) {
         vg.glassPressed(rx, ry, rw, 40.0F, 12.0F, theme.accent());
      } else if (hover) {
         vg.glass(rx, ry, rw, 40.0F, 12.0F);
      }
   }

   private void pillBtn(NVGRenderer vg, Theme theme, float bx, float by, float bw, String label, float mx, float my) {
      boolean hover = mx >= bx && mx <= bx + bw && my >= by && my <= by + 28.0F;
      vg.glass(bx, by, bw, 28.0F, 14.0F);
      if (hover) {
         vg.rectOutline(bx, by, bw, 28.0F, 14.0F, 1.0F, Colors.withAlpha(theme.accentBright(), 0.6F));
      }

      vg.text(label, bx + (bw - vg.textWidth(label, 12.0F)) / 2.0F, by + 14.0F, 12.0F, hover ? theme.textPrimary() : theme.textMuted());
   }

   private String headerSubtitle() {
      if (this.configPanel.isOpen()) {
         return "15 slots · click to load, save, rename";
      } else if (this.galleryOpen) {
         return NekoClient.themes().getThemes().size() + " presets · click to apply";
      } else {
         Category[] cats = new Category[]{Category.COMBAT, Category.MISC, Category.RENDER};
         int i = Math.clamp((long)this.activeCategory, 0, 2);
         int total = NekoClient.modules().inCategory(cats[i]).size();
         int en = 0;

         for (Module m : NekoClient.modules().inCategory(cats[i])) {
            if (m.isEnabled()) {
               en++;
            }
         }

         return total + " modules · " + en + " enabled";
      }
   }

   private void renderSideProfile(NVGRenderer vg) {
      Theme theme = NekoClient.themes().current();
      float x = this.frameX(OverlayRenderer.uiWidth());
      float y = this.frameY(OverlayRenderer.uiHeight());
      float py = y + 338.0F;
      String name = "—";
      int ms = -1;
      int skin = -1;

      try {
         if (this.field_22787 != null && this.field_22787.field_1724 != null) {
            name = this.field_22787.field_1724.method_7334().name();
            if (this.field_22787.method_1562() != null) {
               class_640 e = this.field_22787.method_1562().method_2871(this.field_22787.field_1724.method_5667());
               if (e != null) {
                  ms = e.method_2959();
               }
            }

            skin = NVGImages.wrapGlTexture(this.field_22787.field_1724.method_52814().comp_1626().comp_3627(), 64, 64);
         }
      } catch (Exception var10) {
         skin = -1;
      }

      if (skin > 0) {
         NVGImages.drawSubImage(vg, skin, 64.0F, 64.0F, 8.0F, 8.0F, 16.0F, 16.0F, x + 16.0F, py, 24.0F, 24.0F, 1.0F);
         NVGImages.drawSubImage(vg, skin, 64.0F, 64.0F, 40.0F, 8.0F, 48.0F, 16.0F, x + 16.0F, py, 24.0F, 24.0F, 1.0F);
         vg.rectOutline(x + 16.0F, py, 24.0F, 24.0F, 6.0F, 1.0F, Colors.withAlpha(theme.textPrimary(), 0.35F));
      } else {
         vg.circle(x + 28.0F, py + 12.0F, 11.0F, Colors.withAlpha(theme.textDisabled(), 0.4F));
         vg.text("?", x + 28.0F - vg.textWidth("?", 12.0F) / 2.0F, py + 12.0F, 12.0F, theme.textMuted());
      }

      String line = name + " · " + (ms >= 0 ? ms + "ms" : "--");
      vg.textTruncated(line, x + 48.0F, py + 12.0F, 12.0F, theme.textPrimary(), 118.0F);
   }

   private void renderSideStats(NVGRenderer vg, float uiWidth, float uiHeight) {
      Theme theme = NekoClient.themes().current();
      float x = this.frameX(uiWidth);
      float y = this.frameY(uiHeight);
      float cx = x + 95.0F;
      String l1 = this.field_22787.method_47599() + " FPS · " + pingStr();

      String clock;
      try {
         clock = LocalTime.now().format(CLOCK_FMT);
      } catch (Exception var11) {
         clock = "--:--";
      }

      String l2 = CpsTracker.get(0) + "/" + CpsTracker.get(1) + " CPS · " + clock + " · " + sessionStr();
      vg.text(l1, cx - vg.textWidth(l1, 11.0F) / 2.0F, y + 550.0F - 34.0F, 11.0F, theme.textPrimary());
      vg.text(l2, cx - vg.textWidth(l2, 10.0F) / 2.0F, y + 550.0F - 14.0F, 10.0F, theme.textMuted());
   }

   private void renderSideSpotify(NVGRenderer vg, float uiWidth, float uiHeight, float mx, float my) {
      if (this.musicVisible()) {
         Theme theme = NekoClient.themes().current();
         float x = this.frameX(uiWidth);
         float y = this.frameY(uiHeight);
         float cardX = x + 10.0F;
         float cardW = 170.0F;
         float cardH = 118.0F;
         float cardY = y + 550.0F - 16.0F - 44.0F - 8.0F - cardH;
         vg.glass(cardX, cardY, cardW, cardH, 14.0F);
         this.musicHits.clear();
         SpotifyState state = SpotifyState.INACTIVE;

         try {
            if (NekoClient.spotify() != null) {
               state = NekoClient.spotify().state();
            }
         } catch (Exception var23) {
         }

         float ax = cardX + 12.0F;
         float ay = cardY + 10.0F;
         int art = -1;

         try {
            if (state.active()) {
               art = NVGImages.fromFile(NekoClient.spotify().artPath(), state.artVersion());
            }
         } catch (Exception var22) {
         }

         if (art > 0) {
            this.drawRoundImage(vg, art, ax, ay, 44.0F, 9.0F);
         } else {
            vg.rect(ax, ay, 44.0F, 44.0F, 9.0F, Colors.withAlpha(-16777216, 0.4F));
            vg.circle(ax + 22.0F, ay + 22.0F, 12.0F, Colors.withAlpha(theme.accent(), 0.5F));
            vg.circle(ax + 22.0F, ay + 22.0F, 4.0F, theme.accentBright());
         }

         String title = state.active() ? state.title() : "Nothing playing";
         vg.textTruncated(title, ax + 52.0F, ay + 15.0F, 12.0F, state.active() ? theme.textPrimary() : theme.textDisabled(), cardW - 70.0F);
         if (state.active()) {
            vg.textTruncated(state.artist(), ax + 52.0F, ay + 31.0F, 10.5F, theme.textMuted(), cardW - 70.0F);
         }

         float ccy = cardY + 78.0F;
         float ccx = cardX + cardW / 2.0F;
         int btn = theme.textMuted();
         vg.rect(ccx - 34.0F, ccy - 5.0F, 2.2F, 10.0F, 1.0F, btn);
         vg.triangle(ccx - 31.0F, ccy - 5.0F, ccx - 31.0F, ccy + 5.0F, ccx - 37.0F, ccy, btn);
         this.musicHits.add(new ClickGuiScreen.UiHit(10, 0, ccx - 40.0F, ccy - 10.0F, 20.0F, 20.0F));
         if (state.playing()) {
            vg.rect(ccx - 4.0F, ccy - 5.0F, 2.6F, 10.0F, 1.2F, theme.accentBright());
            vg.rect(ccx + 0.4F, ccy - 5.0F, 2.6F, 10.0F, 1.2F, theme.accentBright());
         } else {
            vg.triangle(ccx - 5.0F, ccy - 5.5F, ccx - 5.0F, ccy + 5.5F, ccx + 4.0F, ccy, theme.accentBright());
         }

         this.musicHits.add(new ClickGuiScreen.UiHit(11, 0, ccx - 11.0F, ccy - 10.0F, 22.0F, 20.0F));
         vg.triangle(ccx + 21.0F, ccy - 5.0F, ccx + 21.0F, ccy + 5.0F, ccx + 27.0F, ccy, btn);
         vg.rect(ccx + 27.8F, ccy - 5.0F, 2.2F, 10.0F, 1.0F, btn);
         this.musicHits.add(new ClickGuiScreen.UiHit(12, 0, ccx + 18.0F, ccy - 10.0F, 22.0F, 20.0F));
         if (state.active() && state.durMs() > 0L) {
            float frac = Math.clamp((float)state.livePosMs() / (float)state.durMs(), 0.0F, 1.0F);
            vg.rect(cardX + 12.0F, cardY + cardH - 10.0F, cardW - 24.0F, 2.0F, 1.0F, Colors.withAlpha(-16777216, 0.5F));
            vg.rect(cardX + 12.0F, cardY + cardH - 10.0F, Math.max(4.0F, (cardW - 24.0F) * frac), 2.0F, 1.0F, theme.accent());
         }
      }
   }

   private void drawRoundImage(NVGRenderer vg, int image, float ix, float iy, float size, float radius) {
      long ctx = vg.ctx();
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgImagePattern(ctx, ix, iy, size, size, 0.0F, image, 1.0F, paint);
         NanoVG.nvgBeginPath(ctx);
         NanoVG.nvgRoundedRect(ctx, ix, iy, size, size, radius);
         NanoVG.nvgFillPaint(ctx, paint);
         NanoVG.nvgFill(ctx);
      } catch (Throwable var14) {
         throw new RuntimeException(var14);
      } finally {
         stack.close();
      }
   }

   private void drawMeow(NVGRenderer vg, float x, float y, float h, int tint) {
      int handle = -1;

      try {
         handle = NVGImages.fromResource(MEOW_ID);
      } catch (Exception var8) {
      }

      if (handle > 0) {
         vg.imageTinted(handle, x, y, h * 1.3364055F, h, tint);
      }
   }

   private static String pingStr() {
      try {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 != null && mc.method_1562() != null) {
            class_640 e = mc.method_1562().method_2871(mc.field_1724.method_5667());
            return e == null ? "--" : e.method_2959() + "ms";
         } else {
            return "--";
         }
      } catch (Exception var2) {
         return "--";
      }
   }

   private static String sessionStr() {
      long s = NekoClient.sessionStartMs;
      if (s < 0L) {
         return "--:--";
      } else {
         long sec = (System.currentTimeMillis() - s) / 1000L;
         long h = sec / 3600L;
         long m = sec % 3600L / 60L;
         long r = sec % 60L;
         return h > 0L ? h + ":" + String.format("%02d:%02d", m, r) : m + ":" + String.format("%02d", r);
      }
   }

   private float sideNavY(float uiHeight) {
      return this.frameY(uiHeight) + 100.0F;
   }

   private float sideRowY(float uiHeight, int row) {
      float navY = this.sideNavY(uiHeight);
      return row < 3 ? navY + row * 44.0F : navY + 132.0F + 20.0F + (row - 3) * 44.0F;
   }

   private int sideHit(float mx, float my) {
      float x = this.frameX(OverlayRenderer.uiWidth());
      float uiH = OverlayRenderer.uiHeight();
      if (!(mx < x + 10.0F) && !(mx > x + 190.0F - 10.0F)) {
         for (int i = 0; i < 5; i++) {
            float ry = this.sideRowY(uiH, i);
            if (my >= ry && my <= ry + 40.0F) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private boolean configHit(float mx, float my) {
      return this.sideHit(mx, my) == 3;
   }

   private void drawCatIcon(NVGRenderer vg, Category category, float cx, float cy, float s, int color) {
      switch (category) {
         case COMBAT:
            vg.circleOutline(cx, cy, s * 0.36F, 1.6F, color);
            vg.line(cx - s * 0.5F, cy, cx - s * 0.22F, cy, 1.6F, color);
            vg.line(cx + s * 0.22F, cy, cx + s * 0.5F, cy, 1.6F, color);
            vg.line(cx, cy - s * 0.5F, cx, cy - s * 0.22F, 1.6F, color);
            vg.line(cx, cy + s * 0.22F, cx, cy + s * 0.5F, 1.6F, color);
            vg.circle(cx, cy, 1.8F, color);
            break;
         case MISC:
            float[] offs = new float[]{-s * 0.26F, 0.0F, s * 0.26F};
            float[] knobs = new float[]{s * 0.14F, -s * 0.12F, s * 0.05F};

            for (int i = 0; i < 3; i++) {
               float ly = cy + offs[i];
               vg.line(cx - s * 0.34F, ly, cx + s * 0.34F, ly, 1.5F, color);
               vg.circle(cx + knobs[i] + 0.8F, ly + 1.0F, s * 0.1F, Colors.withAlpha(-16777216, 0.45F));
               vg.circle(cx + knobs[i], ly, s * 0.1F, color);
            }
            break;
         case RENDER:
            vg.circleOutline(cx, cy, s * 0.36F, 1.6F, color);
            vg.circle(cx, cy, s * 0.15F, color);
            vg.circle(cx - s * 0.05F, cy - s * 0.05F, s * 0.05F, Colors.withAlpha(-1, 0.85F));
            break;
         default:
            vg.rectOutline(cx - s * 0.34F, cy - s * 0.26F, s * 0.68F, s * 0.52F, 3.0F, 1.5F, color);
            vg.line(cx - s * 0.34F, cy - s * 0.08F, cx + s * 0.34F, cy - s * 0.08F, 1.2F, color);
            vg.circle(cx - s * 0.16F, cy + s * 0.12F, 1.7F, color);
            vg.circle(cx + s * 0.02F, cy + s * 0.12F, 1.7F, color);
            vg.circle(cx + s * 0.2F, cy + s * 0.12F, 1.7F, color);
      }
   }

   private void drawGearGlyph(NVGRenderer vg, float cx, float cy, float r, int color) {
      vg.circleOutline(cx, cy, r * 0.52F, 1.7F, color);

      for (int i = 0; i < 8; i++) {
         double a = Math.PI * i / 4.0;
         float x1 = cx + (float)Math.cos(a) * r * 0.62F;
         float y1 = cy + (float)Math.sin(a) * r * 0.62F;
         float x2 = cx + (float)Math.cos(a) * r * 0.85F;
         float y2 = cy + (float)Math.sin(a) * r * 0.85F;
         vg.line(x1, y1, x2, y2, 1.8F, color);
      }

      vg.circle(cx, cy, r * 0.16F, color);
   }

   private void renderFidget(NVGRenderer vg, float mouseX, float mouseY, float uiWidth, float uiHeight) {
      Theme theme = NekoClient.themes().current();
      float fx = fidgetX(uiWidth);
      float fy = fidgetY(uiHeight);
      boolean hover = mouseX >= fx && mouseX <= fx + 760.0F && mouseY >= fy && mouseY <= fy + 28.0F;
      boolean lit = hover || this.configPanel.isOpen();
      if (lit != this.fidgetHovered) {
         this.fidgetHovered = lit;
         if (lit) {
         }
      }

      float width = 760.0F;
      vg.rect(fx, fy, width, 28.0F, 5.0F, lit ? Colors.withAlpha(theme.accent(), 0.18F) : theme.headerTop());
      vg.rectOutline(fx, fy, width, 28.0F, 5.0F, 1.0F, Colors.withAlpha(lit ? theme.accentBright() : theme.border(), 0.75F));
      vg.text("Configs", fx + 14.0F, fy + 14.0F, 12.5F, lit ? theme.textPrimary() : theme.textMuted());
      vg.text("Manage profiles", fx + width - 92.0F, fy + 14.0F, 10.5F, theme.textDisabled());
   }

   private static float fidgetX(float uiWidth) {
      return (uiWidth - 860.0F) / 2.0F + 64.0F + 18.0F;
   }

   private static float fidgetY(float uiHeight) {
      return (uiHeight - 550.0F) / 2.0F + 550.0F - 42.0F;
   }

   private boolean fidgetHit(float mx, float my) {
      float fx = fidgetX(OverlayRenderer.uiWidth());
      float fy = fidgetY(OverlayRenderer.uiHeight());
      return mx >= fx && mx <= fx + 860.0F - 64.0F - 32.0F && my >= fy && my <= fy + 28.0F;
   }

   private boolean uiGearHit(float mx, float my) {
      return this.sideHit(mx, my) == 4;
   }

   private float uiPopupX(float uiWidth) {
      return this.contentX(uiWidth);
   }

   private float uiPopupY(float uiHeight) {
      return this.frameY(uiHeight) + 550.0F - 16.0F - 400.0F;
   }

   private float uiContentHeight() {
      return 152.0F + this.hudToggles.size() * 30.0F + 14.0F;
   }

   private void renderUiPopup(NVGRenderer vg, float uiWidth, float uiHeight, float mx, float my) {
      Theme theme = NekoClient.themes().current();
      float px = this.uiPopupX(uiWidth);
      float py = this.uiPopupY(uiHeight);
      float pw = 330.0F;
      float ph = 400.0F;
      vg.glow(px, py, pw, ph, 16.0F, 14.0F, Colors.withAlpha(-16777216, 0.6F));
      vg.rect(px, py, pw, ph, 16.0F, Colors.withAlpha(-16777216, 0.82F));
      vg.rectOutline(px, py, pw, ph, 16.0F, 1.0F, Colors.withAlpha(theme.accent(), 0.35F));
      this.uiHits.clear();
      float total = this.uiContentHeight();
      float viewH = ph - 58.0F;
      this.uiScroll = Math.clamp(this.uiScroll, 0.0F, Math.max(0.0F, total - viewH));
      vg.text("Interface", px + 18.0F, py + 26.0F, 16.0F, theme.textPrimary());
      this.drawMeow(vg, px + pw - 16.0F - 26.728111F, py + 27.0F - 10.0F, 20.0F, Colors.withAlpha(theme.accent(), 0.9F));
      float top = py + 48.0F;
      float bottom = py + ph - 10.0F;
      vg.save();
      vg.scissor(px, top, pw, bottom - top);
      float ry = top + 6.0F - this.uiScroll;
      ry = this.renderUiSection(vg, theme, px, ry, pw, mx, my, "BACKGROUND BLUR", -1);
      ry = this.renderUiBlurRow(vg, theme, px, ry, pw, mx, my);
      ry = this.renderUiBlurStrengthRow(vg, theme, px, ry, pw, mx, my);
      ry = this.renderUiSection(vg, theme, px, ry, pw, mx, my, "HUD ELEMENTS", -1);

      for (int i = 0; i < this.hudToggles.size(); i++) {
         ry = this.renderUiToggleRow(vg, theme, this.hudToggles.get(i), px, ry, pw, mx, my, i);
      }

      vg.restore();
      if (total > viewH) {
         float tx = px + pw - 10.0F;
         vg.rect(tx, top + 4.0F, 4.0F, viewH - 8.0F, 2.0F, Colors.withAlpha(-16777216, 0.5F));
         float th = Math.max(28.0F, viewH * (viewH / total));
         float ty = top + 4.0F + (viewH - 8.0F - th) * (this.uiScroll / (total - viewH));
         vg.rect(tx, ty, 4.0F, th, 2.0F, Colors.withAlpha(theme.accent(), 0.7F));
      }
   }

   private float renderUiSection(NVGRenderer vg, Theme theme, float px, float ry, float pw, float mx, float my, String label, int unused) {
      vg.text(label, px + 18.0F, ry + 8.0F, 10.5F, theme.textDisabled());
      return ry + 22.0F;
   }

   private float renderUiThemeRow(NVGRenderer vg, Theme theme, Theme t, float px, float ry, float pw, float mx, float my, int index) {
      float x = px + 12.0F;
      float w = pw - 24.0F;
      boolean selected = t == theme;
      if (selected) {
         vg.neuPressed(x, ry, w, 28.0F, 9.0F, theme.card(), t.accent());
      } else {
         vg.neuRaised(x, ry, w, 28.0F, 9.0F, theme.card());
      }

      vg.circle(x + 20.0F, ry + 14.0F, 6.0F, t.accent());
      vg.text(t.getName(), x + 34.0F, ry + 14.0F, 13.0F, selected ? theme.textPrimary() : theme.textMuted());
      if (selected) {
         vg.text("ON", x + w - 26.0F, ry + 14.0F, 11.0F, t.accentBright());
      }

      this.uiHits.add(new ClickGuiScreen.UiHit(0, index, x, ry, w, 28.0F));
      return ry + 32.0F;
   }

   private float renderUiBlurRow(NVGRenderer vg, Theme theme, float px, float ry, float pw, float mx, float my) {
      float x = px + 12.0F;
      float w = pw - 24.0F;
      boolean on = this.guiModule().blur.get();
      vg.glass(x, ry, w, 28.0F, 9.0F);
      vg.text("Blur", x + 14.0F, ry + 14.0F, 13.0F, theme.textMuted());
      float sx = x + w - 46.0F;
      if (on) {
         vg.glassPressed(sx, ry + 6.0F, 32.0F, 16.0F, 8.0F, theme.accent());
         vg.circle(sx + 24.0F, ry + 14.0F, 6.0F, theme.accent());
      } else {
         vg.glass(sx, ry + 6.0F, 32.0F, 16.0F, 8.0F);
         vg.circle(sx + 8.0F, ry + 14.0F, 6.0F, -1);
      }

      this.uiHits.add(new ClickGuiScreen.UiHit(1, 0, x, ry, w, 28.0F));
      return ry + 32.0F;
   }

   private float renderUiBlurStrengthRow(NVGRenderer vg, Theme theme, float px, float ry, float pw, float mx, float my) {
      float x = px + 12.0F;
      float w = pw - 24.0F;
      vg.glass(x, ry, w, 28.0F, 9.0F);
      vg.text("Strength", x + 14.0F, ry + 14.0F, 13.0F, theme.textMuted());
      String val = Integer.toString(this.guiModule().blurStrength.getInt());
      float vx = x + w - 118.0F;
      vg.glassPressed(vx, ry + 5.0F, 24.0F, 18.0F, 6.0F, theme.accent());
      vg.text("-", vx + 12.0F, ry + 14.0F, 14.0F, theme.textPrimary());
      vg.text(val, vx + 40.0F, ry + 14.0F, 13.0F, theme.textPrimary());
      vg.glassPressed(vx + 62.0F, ry + 5.0F, 24.0F, 18.0F, 6.0F, theme.accent());
      vg.text("+", vx + 74.0F, ry + 14.0F, 14.0F, theme.textPrimary());
      this.uiHits.add(new ClickGuiScreen.UiHit(2, 0, vx, ry + 5.0F, 24.0F, 18.0F));
      this.uiHits.add(new ClickGuiScreen.UiHit(3, 0, vx + 62.0F, ry + 5.0F, 24.0F, 18.0F));
      return ry + 32.0F;
   }

   private float renderUiToggleRow(NVGRenderer vg, Theme theme, ClickGuiScreen.UiToggle t, float px, float ry, float pw, float mx, float my, int index) {
      float x = px + 12.0F;
      float w = pw - 24.0F;
      boolean on = t.setting().get();
      vg.glass(x, ry, w, 26.0F, 9.0F);
      vg.text(t.label(), x + 14.0F, ry + 13.0F, 12.5F, on ? theme.textPrimary() : theme.textMuted());
      float dx = x + w - 14.0F - 8.0F;
      vg.circle(dx + 1.0F, ry + 14.0F, 5.0F, Colors.withAlpha(-16777216, 0.5F));
      vg.circle(dx, ry + 13.0F, 5.0F, on ? theme.accent() : -12960184);
      this.uiHits.add(new ClickGuiScreen.UiHit(4, index, x, ry, w, 26.0F));
      return ry + 30.0F;
   }

   private boolean dispatchUiClick(float mx, float my) {
      for (ClickGuiScreen.UiHit h : this.uiHits) {
         if (h.contains(mx, my)) {
            if (h.kind == 0) {
               List<Theme> themes = NekoClient.themes().getThemes();
               if (h.index >= 0 && h.index < themes.size()) {
                  NekoClient.themes().select(themes.get(h.index));
                  UiSounds.select();
               }

               return true;
            }

            if (h.kind == 1) {
               Modules.ClickGuiModule g = this.guiModule();
               g.blur.set(!g.blur.get());
               UiSounds.toggle(g.blur.get());
               return true;
            }

            if (h.kind == 2) {
               Modules.ClickGuiModule g = this.guiModule();
               int v = Math.max(1, g.blurStrength.getInt() - 1);
               g.blurStrength.set((double)v);
               UiSounds.select();
               return true;
            }

            if (h.kind == 3) {
               Modules.ClickGuiModule g = this.guiModule();
               int v = Math.min(10, g.blurStrength.getInt() + 1);
               g.blurStrength.set((double)v);
               UiSounds.select();
               return true;
            }

            if (h.kind == 4) {
               if (h.index >= 0 && h.index < this.hudToggles.size()) {
                  BooleanSetting s = this.hudToggles.get(h.index).setting();
                  s.set(!s.get());
                  UiSounds.toggle(s.get());
               }

               return true;
            }
         }
      }

      return false;
   }

   private boolean musicVisible() {
      ModuleManager mm = NekoClient.modules();
      return mm != null && mm.spotify != null && mm.spotify.isEnabled();
   }

   private float[] headerPills(float uiWidth, float uiHeight) {
      float right = this.contentX(uiWidth) + this.contentW();
      float hcy = this.frameY(uiHeight) + 34.0F;
      float noneX = right - 72.0F;
      float allX = noneX - 8.0F - 56.0F;
      float palCX = allX - 8.0F - 15.0F;
      return new float[]{palCX, hcy, allX, noneX};
   }

   private boolean musicHit(float mx, float my) {
      float x = this.frameX(OverlayRenderer.uiWidth());
      float y = this.frameY(OverlayRenderer.uiHeight());
      float cardX = x + 10.0F;
      float cardY = y + 550.0F - 16.0F - 44.0F - 8.0F - 118.0F;
      return this.musicVisible() && mx >= cardX && mx <= cardX + 190.0F - 20.0F && my >= cardY && my <= cardY + 118.0F;
   }

   private void dispatchMusicClick(float mx, float my) {
      for (ClickGuiScreen.UiHit h : this.musicHits) {
         if (h.contains(mx, my)) {
            try {
               if (h.kind == 10) {
                  NekoClient.spotify().previous();
               } else if (h.kind == 11) {
                  NekoClient.spotify().togglePlay();
               } else if (h.kind == 12) {
                  NekoClient.spotify().next();
               }

               UiSounds.select();
            } catch (Exception var6) {
            }

            return;
         }
      }
   }

   private boolean uiPopupHit(float mx, float my) {
      float px = this.uiPopupX(OverlayRenderer.uiWidth());
      float py = this.uiPopupY(OverlayRenderer.uiHeight());
      return mx >= px && mx <= px + 330.0F && my >= py && my <= py + 400.0F;
   }

   private int headerBtnHit(float mx, float my) {
      float[] p = this.headerPills(OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight());
      if (mx >= p[0] - 15.0F && mx <= p[0] + 15.0F && my >= p[1] - 15.0F && my <= p[1] + 15.0F) {
         return 0;
      } else {
         float allX = p[2];
         if (mx >= allX && mx <= allX + 56.0F && my >= p[1] - 14.0F && my <= p[1] + 14.0F) {
            return 1;
         } else {
            float noneX = p[3];
            if (mx >= noneX && mx <= noneX + 72.0F && my >= p[1] - 14.0F && my <= p[1] + 14.0F) {
               return 2;
            } else {
               float sortX = p[0] - 15.0F - 8.0F - 52.0F;
               return mx >= sortX && mx <= sortX + 52.0F && my >= p[1] - 14.0F && my <= p[1] + 14.0F ? 3 : -1;
            }
         }
      }
   }

   private boolean themeBtnHit(float mx, float my) {
      return this.headerBtnHit(mx, my) == 0;
   }

   private float galleryX(float uiWidth) {
      return this.contentX(uiWidth);
   }

   private float galleryY(float uiHeight) {
      return this.listY(uiHeight);
   }

   private boolean galleryHit(float mx, float my) {
      float px = this.galleryX(OverlayRenderer.uiWidth());
      float py = this.galleryY(OverlayRenderer.uiHeight());
      return mx >= px && mx <= px + 638.0F && my >= py && my <= py + 434.0F;
   }

   private float galleryTotal() {
      int n = NekoClient.themes().getThemes().size();
      int rows = (n + 3) / 4;
      return rows * 100.0F + Math.max(0, rows - 1) * 10.0F;
   }

   private void renderGallery(NVGRenderer vg, float uiWidth, float uiHeight, float mx, float my) {
      Theme theme = NekoClient.themes().current();
      float px = this.galleryX(uiWidth);
      float py = this.galleryY(uiHeight);
      vg.glow(px, py, 638.0F, 434.0F, 16.0F, 14.0F, Colors.withAlpha(-16777216, 0.6F));
      vg.rect(px, py, 638.0F, 434.0F, 16.0F, Colors.withAlpha(-16777216, 0.45F));
      vg.rectOutline(px, py, 638.0F, 434.0F, 16.0F, 1.0F, Colors.withAlpha(-1, 0.14F));
      this.galleryHits.clear();
      float total = this.galleryTotal();
      float top = py + 56.0F;
      float viewH = 368.0F;
      this.galleryScroll = Math.clamp(this.galleryScroll, 0.0F, Math.max(0.0F, total - viewH));
      int count = NekoClient.themes().getThemes().size();
      vg.text("Themes", px + 20.0F, py + 26.0F, 17.0F, theme.textPrimary());
      vg.text(count + " presets · click to apply", px + 20.0F + vg.textWidth("Themes", 17.0F) + 10.0F, py + 27.0F, 11.5F, theme.textDisabled());
      this.drawMeow(vg, px + 638.0F - 16.0F - 29.40092F, py + 28.0F - 11.0F, 22.0F, Colors.withAlpha(theme.accent(), 0.9F));
      vg.save();
      vg.scissor(px, top, 638.0F, viewH);
      float cw = 142.0F;
      float ch = 100.0F;
      float gap = 10.0F;
      float startX = px + 20.0F;
      float ry = top + 4.0F - this.galleryScroll;
      List<Theme> themes = NekoClient.themes().getThemes();

      for (int i = 0; i < themes.size(); i++) {
         int col = i % 4;
         int row = i / 4;
         float cx = startX + col * (cw + gap);
         float cy = ry + row * (ch + gap);
         if (cy + ch >= top && cy <= top + viewH) {
            this.renderGalleryCard(vg, theme, themes.get(i), cx, cy, cw, ch, i);
         }
      }

      vg.restore();
      if (total > viewH) {
         float tx = px + 638.0F - 10.0F;
         vg.rect(tx, top + 4.0F, 4.0F, viewH - 8.0F, 2.0F, Colors.withAlpha(-16777216, 0.5F));
         float th = Math.max(28.0F, viewH * (viewH / total));
         float ty = top + 4.0F + (viewH - 8.0F - th) * (this.galleryScroll / (total - viewH));
         vg.rect(tx, ty, 4.0F, th, 2.0F, Colors.withAlpha(theme.accent(), 0.7F));
      }
   }

   private void renderGalleryCard(NVGRenderer vg, Theme theme, Theme t, float x, float y, float w, float h, int index) {
      boolean selected = t == theme;
      if (selected) {
         vg.glow(x, y, w, h, 12.0F, 6.0F, Colors.withAlpha(t.accent(), 0.35F));
         vg.glassPressed(x, y, w, h, 12.0F, t.accent());
      } else {
         vg.glass(x, y, w, h, 12.0F);
      }

      vg.textTruncated(t.getName(), x + 12.0F, y + 20.0F, 13.5F, selected ? theme.textPrimary() : theme.textMuted(), w - 24.0F);
      vg.text(t.accentHex(), x + 12.0F, y + 38.0F, 10.5F, theme.textDisabled());
      if (selected) {
         String tag = "ACTIVE";
         float tw = vg.textWidth(tag, 9.0F);
         vg.rect(x + w - tw - 20.0F, y + 10.0F, tw + 12.0F, 15.0F, 7.0F, Colors.withAlpha(t.accent(), 0.22F));
         vg.text(tag, x + w - tw - 14.0F, y + 17.5F, 9.0F, t.accentBright());
      }

      int[] dots = new int[]{t.accent(), t.background(), t.card(), t.textPrimary()};
      float dx = x + 20.0F;

      for (int d : dots) {
         vg.circle(dx + 0.8F, y + h - 20.0F, 7.5F, Colors.withAlpha(-16777216, 0.5F));
         vg.circle(dx, y + h - 21.0F, 7.5F, d);
         vg.circleOutline(dx, y + h - 21.0F, 7.5F, 1.0F, Colors.withAlpha(-1, 0.14F));
         dx += 24.0F;
      }

      vg.rect(x + 12.0F, y + 48.0F, w - 24.0F, 5.0F, 2.5F, Colors.withAlpha(-16777216, 0.45F));
      vg.rect(x + 12.0F, y + 48.0F, (w - 24.0F) * 0.62F, 5.0F, 2.5F, t.accent());
      this.galleryHits.add(new ClickGuiScreen.UiHit(5, index, x, y, w, h));
   }

   private boolean dispatchGalleryClick(float mx, float my) {
      for (ClickGuiScreen.UiHit h : this.galleryHits) {
         if (h.kind == 5 && h.contains(mx, my)) {
            List<Theme> themes = NekoClient.themes().getThemes();
            if (h.index >= 0 && h.index < themes.size()) {
               NekoClient.themes().select(themes.get(h.index));
               NekoClient.config().save();
               UiSounds.select();
            }

            return true;
         }
      }

      return false;
   }

   public void openSearch(String query) {
      this.search.setLength(0);
      this.search.append(query);
      this.searchFocused = true;
   }

   public ConfigPanel configPanel() {
      return this.configPanel;
   }

   private boolean searchBarHit(float mx, float my) {
      float sx = this.frameX(OverlayRenderer.uiWidth()) + 12.0F;
      float sy = this.frameY(OverlayRenderer.uiHeight()) + 56.0F;
      return mx >= sx && mx <= sx + 190.0F - 24.0F && my >= sy && my <= sy + 30.0F;
   }

   private boolean tabHit(float mx, float my) {
      int row = this.sideHit(mx, my);
      return row >= 0 && row <= 2;
   }

   private int tabAt(float my) {
      float mx = this.frameX(OverlayRenderer.uiWidth()) + 95.0F;
      return Math.clamp((long)this.sideHit(mx, my), 0, 2);
   }

   private String categoryTitle() {
      if (this.configPanel.isOpen()) {
         return "Configs";
      } else if (this.galleryOpen) {
         return "Themes";
      } else {
         int index = Math.clamp((long)this.activeCategory, 0, this.panels.size() - 1);
         int current = 0;

         for (Category category : Category.values()) {
            if (category != Category.VISUALS && category != Category.CLIENT && current++ == index) {
               return category.getDisplayName();
            }
         }

         return "Modules";
      }
   }

   private float uiX(double guiX) {
      return OverlayRenderer.guiToUi(guiX);
   }

   private float uiY(double guiY) {
      return OverlayRenderer.guiToUi(guiY);
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      float mx = this.uiX(click.comp_4798());
      float my = this.uiY(click.comp_4799());
      NVGRenderer vg = NVGRenderer.get();
      float uiHeight = OverlayRenderer.uiHeight();
      if (this.configPanel.isOpen()) {
         this.configPanel.mouseClicked(mx, my, click.method_74245());
         return true;
      } else if (this.galleryOpen) {
         if (this.galleryHit(mx, my)) {
            this.dispatchGalleryClick(mx, my);
         } else {
            this.galleryOpen = false;
         }

         return true;
      } else if (this.uiGearHit(mx, my)) {
         this.uiOpen = !this.uiOpen;
         if (this.uiOpen) {
            this.configPanel.close();
         }

         UiSounds.select();
         return true;
      } else if (this.uiOpen) {
         if (this.uiPopupHit(mx, my)) {
            this.dispatchUiClick(mx, my);
         } else {
            this.uiOpen = false;
         }

         return true;
      } else if (this.configHit(mx, my)) {
         this.configPanel.open();
         this.galleryOpen = false;
         this.uiOpen = false;
         UiSounds.select();
         return true;
      } else if (this.musicVisible() && this.musicHit(mx, my)) {
         this.dispatchMusicClick(mx, my);
         return true;
      } else if (this.headerBtnHit(mx, my) >= 0) {
         int which = this.headerBtnHit(mx, my);
         if (which == 0) {
            this.galleryOpen = !this.galleryOpen;
            if (this.galleryOpen) {
               this.uiOpen = false;
            }

            UiSounds.select();
         } else if (which == 3) {
            guiSort = (guiSort + 1) % 3;
            saveGuiPrefs();
            UiSounds.select();
         } else {
            Category[] cats = new Category[]{Category.COMBAT, Category.MISC, Category.RENDER};
            int i = Math.clamp((long)this.activeCategory, 0, 2);
            NekoClient.modules().setAll(cats[i], which == 1);
            NekoClient.config().save();
            UiSounds.toggle(which == 1);
         }

         return true;
      } else if (this.tabHit(mx, my)) {
         int category = this.tabAt(my);
         if (category != this.activeCategory) {
            this.activeCategory = category;
            this.search.setLength(0);
            UiSounds.select();
         }

         return true;
      } else if (!this.searchBarHit(mx, my)) {
         this.searchFocused = false;
         Panel panel = this.activePanel();
         if (panel.headerHit(mx, my)) {
            if (click.method_74245() == 0) {
               this.dragging = panel;
               this.dragOffsetX = mx - panel.x();
               this.dragOffsetY = my - panel.y();
               this.pressX = mx;
               this.pressY = my;
               this.dragMoved = false;
            } else if (click.method_74245() == 1) {
               panel.toggleCollapsed();
            }

            return true;
         } else if (panel.bodyHit(vg, mx, my, uiHeight)) {
            this.pressedContentPanel = panel;
            panel.mouseClicked(mx, my, click.method_74245());
            return true;
         } else {
            return true;
         }
      } else {
         float clearX = this.frameX(OverlayRenderer.uiWidth()) + 12.0F + 190.0F - 24.0F - 22.0F;
         if (!this.search.isEmpty() && mx >= clearX - 4.0F && mx <= clearX + 16.0F) {
            this.search.setLength(0);
         } else {
            this.searchFocused = true;
         }

         UiSounds.select();
         return true;
      }
   }

   public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
      if (!this.configPanel.isOpen() && !this.uiOpen) {
         float mx = this.uiX(click.comp_4798());
         float my = this.uiY(click.comp_4799());
         if (this.dragging != null) {
            if (Math.abs(mx - this.pressX) + Math.abs(my - this.pressY) > 3.0F) {
               this.dragMoved = true;
            }

            if (this.dragMoved) {
               this.dragging.moveTo(mx - this.dragOffsetX, my - this.dragOffsetY);
            }

            return true;
         } else if (this.pressedContentPanel != null) {
            this.pressedContentPanel.mouseDragged(mx, my);
            return true;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public boolean method_25406(class_11909 click) {
      if (this.configPanel.isOpen() || this.uiOpen) {
         return true;
      } else if (this.dragging == null) {
         if (this.pressedContentPanel != null) {
            this.pressedContentPanel.mouseReleased();
            this.pressedContentPanel = null;
         }

         return true;
      } else {
         if (!this.dragMoved && click.method_74245() == 0) {
            this.dragging.toggleCollapsed();
         } else if (this.dragMoved) {
            STATE.markCustomized();
         }

         this.dragging = null;
         return true;
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.configPanel.isOpen()) {
         this.configPanel.onScroll(verticalAmount);
         return true;
      } else if (this.galleryOpen && this.galleryHit(this.uiX(mouseX), this.uiY(mouseY))) {
         float total = this.galleryTotal();
         float viewH = 368.0F;
         this.galleryScroll = Math.clamp(this.galleryScroll - (float)verticalAmount * 40.0F, 0.0F, Math.max(0.0F, total - viewH));
         return true;
      } else if (this.uiOpen && this.uiPopupHit(this.uiX(mouseX), this.uiY(mouseY))) {
         float total = this.uiContentHeight();
         float viewH = 342.0F;
         this.uiScroll = Math.clamp(this.uiScroll - (float)verticalAmount * 30.0F, 0.0F, Math.max(0.0F, total - viewH));
         return true;
      } else {
         float mx = this.uiX(mouseX);
         float my = this.uiY(mouseY);
         NVGRenderer vg = NVGRenderer.get();
         float uiHeight = OverlayRenderer.uiHeight();
         Panel panel = this.activePanel();
         if (!panel.bodyHit(vg, mx, my, uiHeight) && !panel.headerHit(mx, my)) {
            return true;
         } else {
            panel.onScroll(verticalAmount);
            return true;
         }
      }
   }

   public boolean method_25404(class_11908 input) {
      if (this.configPanel.isOpen()) {
         this.configPanel.keyPressed(input.comp_4795());
         return true;
      } else if (this.galleryOpen && input.method_74231()) {
         this.galleryOpen = false;
         return true;
      } else if (this.uiOpen && input.method_74231()) {
         this.uiOpen = false;
         return true;
      } else if (this.activePanel().isListening()) {
         this.activePanel().keyPressed(input.comp_4795());
         return true;
      } else if (this.searchFocused) {
         switch (input.comp_4795()) {
            case 256:
               this.search.setLength(0);
               this.searchFocused = false;
               break;
            case 257:
            case 335:
               this.searchFocused = false;
               break;
            case 259:
               if (!this.search.isEmpty()) {
                  this.search.deleteCharAt(this.search.length() - 1);
               }
         }

         return true;
      } else if (input.method_74231()
         || this.guiModule().getKeybind().matches(input.comp_4795())
         || !this.guiModule().getKeybind().isBound() && input.comp_4795() == 261) {
         this.method_25419();
         return true;
      } else {
         return super.method_25404(input);
      }
   }

   public boolean method_25400(class_11905 input) {
      if (this.configPanel.isOpen()) {
         this.configPanel.charTyped(input.comp_4793());
         return true;
      } else if (this.activePanel().isListening()) {
         this.activePanel().charTyped(input.comp_4793());
         return true;
      } else if (this.searchFocused && input.method_74227()) {
         if (this.search.length() < 32) {
            this.search.append(input.method_74226());
         }

         return true;
      } else {
         return super.method_25400(input);
      }
   }

   private void bringToFront(Panel panel) {
      if (this.panels.remove(panel)) {
         this.panels.addFirst(panel);
      }
   }

   public void openBlockPicker(BlockListSetting setting) {
      BlockGridModel model = new BlockGridModel(setting, () -> {
         BlockEspModule blockEsp = NekoClient.modules().blockEsp;
         return blockEsp != null ? blockEsp.lineColor.get() : -16711736;
      }, "Pick Block");
      this.field_22787.method_1507(new IconPickerScreen(this, model, NekoClient.themes()));
   }

   public void openIconPicker(IconListSetting setting) {
      IconListGridModel model = new IconListGridModel(setting);
      this.field_22787.method_1507(new IconPickerScreen(this, model, NekoClient.themes()));
   }

   private static final class UiHit {
      final int kind;
      final int index;
      final float x;
      final float y;
      final float w;
      final float h;

      UiHit(int kind, int index, float x, float y, float w, float h) {
         this.kind = kind;
         this.index = index;
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
      }

      boolean contains(float mx, float my) {
         return mx >= this.x && mx <= this.x + this.w && my >= this.y && my <= this.y + this.h;
      }
   }

   private record UiToggle(String label, BooleanSetting setting) {
   }
}

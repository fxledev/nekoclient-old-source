package dev.neko.client.gui;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.picker.BlockGridModel;
import dev.neko.client.gui.picker.IconListGridModel;
import dev.neko.client.gui.picker.PickerGrid;
import dev.neko.client.gui.widget.ColorWidget;
import dev.neko.client.render.NvgDrawable;
import dev.neko.client.render.OverlayRenderer;
import dev.neko.client.render.nanovg.NVGImages;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.settings.ColorSetting;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import dev.neko.client.util.UiSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;

public class IconPickerScreen extends class_437 implements NvgDrawable {
   private static final float HEADER_H = 42.0F;
   private static final float SEARCH_H = 34.0F;
   private static final float PAD = 12.0F;
   private static final float CELL = 34.0F;
   private static final float SEL_CHIP_W = 116.0F;
   private static final float PANEL_RADIUS = 14.0F;
   private final class_437 parent;
   private final PickerGrid model;
   private final ThemeManager themes;
   private final StringBuilder search = new StringBuilder();
   private boolean searchFocused;
   private List<PickerGrid.Cell> filtered = List.of();
   private String lastQuery = null;
   private boolean filterDirty = true;
   private float scroll;
   private float maxScroll;
   private ColorSetting colorSetting;
   private String colorTitle;
   private ColorWidget colorWidget;
   private final float[] popupRect = new float[4];
   private final float[] closeRect = new float[4];
   private final float[] searchRect = new float[4];

   public IconPickerScreen(class_437 parent, PickerGrid model, ThemeManager themes) {
      super(class_2561.method_43470(model.title()));
      this.parent = parent;
      this.model = model;
      this.themes = themes;
   }

   private void refreshFilter() {
      String q = this.search.toString().trim().toLowerCase(Locale.ROOT);
      if (this.filterDirty || !q.equals(this.lastQuery)) {
         this.lastQuery = q;
         this.filterDirty = false;
         List<PickerGrid.Cell> out = new ArrayList<>();

         for (PickerGrid.Cell cell : this.model.cells()) {
            if (q.isEmpty() || cell.matches(q)) {
               out.add(cell);
            }
         }

         this.filtered = out;
         this.scroll = 0.0F;
      }
   }

   private IconPickerScreen.Layout layout() {
      float uiW = OverlayRenderer.uiWidth();
      float uiH = OverlayRenderer.uiHeight();
      boolean compactPicker = this.model instanceof BlockGridModel || this.model instanceof IconListGridModel;
      float panelW = Math.min(uiW * (compactPicker ? 0.62F : 0.82F), compactPicker ? 660.0F : 940.0F);
      float panelH = Math.min(uiH * (compactPicker ? 0.64F : 0.84F), compactPicker ? 460.0F : 660.0F);
      float px = (uiW - panelW) / 2.0F;
      float py = (uiH - panelH) / 2.0F;
      float gridX = px + 12.0F;
      float gridY = py + 42.0F + 34.0F;
      float gridW = panelW - 24.0F;
      float gridH = panelH - 42.0F - 34.0F - 12.0F;
      float cellTarget = compactPicker ? 30.0F : 34.0F;
      int cols = Math.max(1, (int)(gridW / cellTarget));
      float cell = gridW / cols;
      return new IconPickerScreen.Layout(px, py, panelW, panelH, gridX, gridY, gridW, gridH, cols, cell, cell - (compactPicker ? 10.0F : 11.0F));
   }

   private float contentHeight(IconPickerScreen.Layout l) {
      int rows = (this.filtered.size() + l.cols() - 1) / l.cols();
      return rows * l.cell();
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      this.refreshFilter();
      IconPickerScreen.Layout l = this.layout();
      this.maxScroll = Math.max(0.0F, this.contentHeight(l) - l.gridH());
      this.scroll = Math.clamp(this.scroll, 0.0F, this.maxScroll);
      float uiScale = OverlayRenderer.uiScale();
      double guiScale = this.field_22787.method_22683().method_4495();
      float k = (float)(uiScale / guiScale);
      boolean blockPicker = this.model instanceof BlockGridModel;
      boolean nativePicker = blockPicker || this.model instanceof IconListGridModel;
      context.method_25294(0, 0, this.field_22789, this.field_22790, nativePicker ? -16777216 : -670694391);
      fillRoundedRectV(
         context,
         l.px() * k,
         l.py() * k,
         l.panelW() * k,
         l.panelH() * k,
         (nativePicker ? 10.0F : 14.0F) * k,
         nativePicker ? -16777216 : -132771297,
         nativePicker ? -16777216 : -133560304
      );
      context.method_44379(gi(l.gridX() * k), gi(l.gridY() * k), gi((l.gridX() + l.gridW()) * k), gi((l.gridY() + l.gridH()) * k));
      if (nativePicker) {
         int first = Math.max(0, (int)(this.scroll / l.cell()) * l.cols());
         int perView = l.cols() * ((int)(l.gridH() / l.cell()) + 3);
         int last = Math.min(this.filtered.size(), first + perView);
         Matrix3x2fStack pose = context.method_51448();

         for (int i = first; i < last; i++) {
            PickerGrid.Cell cell = this.filtered.get(i);
            int col = i % l.cols();
            int row = i / l.cols();
            float cx = l.gridX() + col * l.cell() + (l.cell() - l.icon()) / 2.0F;
            float cy = l.gridY() - this.scroll + row * l.cell() + (l.cell() - l.icon()) / 2.0F;
            float iconScale = l.icon() * k / 16.0F;
            pose.pushMatrix();
            pose.translate(cx * k, cy * k);
            pose.scale(iconScale, iconScale);
            if (!blockPicker || !cell.icon().method_31574(class_1802.field_8077)) {
               context.method_51427(cell.icon(), 0, 0);
            }

            pose.popMatrix();
         }
      }

      context.method_44380();
   }

   private static int gi(float v) {
      return Math.round(v);
   }

   private static void fillRoundedRectV(class_332 gg, float x, float y, float w, float h, float radius, int top, int bottom) {
      int x0 = Math.round(x);
      int y0 = Math.round(y);
      int x1 = Math.round(x + w);
      int y1 = Math.round(y + h);
      int height = y1 - y0;
      int width = x1 - x0;
      if (height > 0 && width > 0) {
         int r = Math.min(Math.round(radius), Math.min(width, height) / 2);

         for (int row = 0; row < height; row++) {
            int inset = 0;
            if (r > 0) {
               int dy = row < r ? row : (row >= height - r ? height - 1 - row : -1);
               if (dy >= 0) {
                  double off = r - dy - 0.5;
                  inset = (int)Math.round(r - Math.sqrt(Math.max(0.0, (double)r * r - off * off)));
               }
            }

            int color = Colors.lerp(top, bottom, height <= 1 ? 0.0F : (float)row / (height - 1));
            gg.method_25294(x0 + inset, y0 + row, x1 - inset, y0 + row + 1, color);
         }
      }
   }

   @Override
   public void renderNvg(NVGRenderer vg, float mouseX, float mouseY, float uiWidth, float uiHeight) {
      if (vg.hasFont()) {
         Theme theme = this.themes.current();
         IconPickerScreen.Layout l = this.layout();
         boolean blockPicker = this.model instanceof BlockGridModel;
         boolean nativePicker = blockPicker || this.model instanceof IconListGridModel;
         boolean blink = System.nanoTime() / 400000000L % 2L == 0L;
         int panelInk = Colors.withAlpha(-16777216, nativePicker ? 0.98F : 0.9F);
         if (!nativePicker) {
            vg.glow(
               l.px(),
               l.py(),
               l.panelW(),
               l.panelH(),
               nativePicker ? 10.0F : 14.0F,
               nativePicker ? 8.0F : 17.0F,
               Colors.withAlpha(-16777216, nativePicker ? 0.65F : 0.45F)
            );
            vg.rect(l.px(), l.py(), l.panelW(), l.panelH(), nativePicker ? 10.0F : 14.0F, panelInk);
         }

         vg.rectOutline(
            l.px(), l.py(), l.panelW(), l.panelH(), nativePicker ? 10.0F : 14.0F, 1.4F, Colors.withAlpha(theme.accentBright(), nativePicker ? 0.5F : 0.75F)
         );
         vg.rectOutline(
            l.px() + 2.2F,
            l.py() + 2.2F,
            l.panelW() - 4.4F,
            l.panelH() - 4.4F,
            nativePicker ? 7.8F : 11.8F,
            1.0F,
            Colors.withAlpha(theme.accentBright(), 0.13F)
         );
         vg.textGradient(this.model.title(), l.px() + 12.0F, l.py() + 16.0F, nativePicker ? 14.0F : 16.0F, theme.accentBright(), theme.accent());
         float closeCx = l.px() + l.panelW() - 12.0F - 3.0F;
         float closeCy = l.py() + 18.0F;
         boolean closeHover = Math.abs(mouseX - closeCx) < 10.0F && Math.abs(mouseY - closeCy) < 10.0F;
         vg.cross(closeCx - 6.0F, closeCy - 6.0F, 12.0F, 1.8F, closeHover ? theme.accentBright() : theme.textMuted());
         this.closeRect[0] = closeCx - 10.0F;
         this.closeRect[1] = closeCy - 10.0F;
         this.closeRect[2] = closeCx + 10.0F;
         this.closeRect[3] = closeCy + 10.0F;
         float sx = l.px() + 12.0F;
         float sy = l.py() + 42.0F + 4.0F;
         float sh = 22.0F;
         float sw = l.panelW() - 24.0F;
         this.searchRect[0] = sx;
         this.searchRect[1] = sy;
         this.searchRect[2] = sx + sw;
         this.searchRect[3] = sy + sh;
         vg.rect(sx, sy, sw, sh, sh / 2.0F, this.searchFocused ? Colors.withAlpha(theme.accent(), 0.16F) : Colors.withAlpha(-16777216, 0.4F));
         vg.rectOutline(
            sx, sy, sw, sh, sh / 2.0F, 1.1F, Colors.withAlpha(this.searchFocused ? theme.accentBright() : theme.accent(), this.searchFocused ? 0.9F : 0.35F)
         );
         float ix = sx + 12.0F;
         float iy = sy + sh / 2.0F;
         vg.circleOutline(ix, iy - 1.0F, 4.0F, 1.4F, theme.textMuted());
         vg.line(ix + 3.0F, iy + 2.0F, ix + 6.0F, iy + 5.0F, 1.4F, theme.textMuted());
         if (this.search.length() == 0 && !this.searchFocused) {
            vg.text("Search…", sx + 24.0F, iy, 12.5F, theme.textDisabled());
         } else {
            float w = vg.text(this.search.toString(), sx + 24.0F, iy, 12.5F, theme.textPrimary());
            if (this.searchFocused && blink) {
               vg.rect(sx + 24.0F + w + 1.5F, iy - 6.0F, 1.4F, 12.0F, 0.7F, theme.accentBright());
            }
         }

         vg.save();
         vg.scissor(l.gridX(), l.gridY(), l.gridW(), l.gridH());
         int first = Math.max(0, (int)(this.scroll / l.cell()) * l.cols());
         int perView = l.cols() * ((int)(l.gridH() / l.cell()) + 3);
         int last = Math.min(this.filtered.size(), first + perView);
         int hovered = this.cellAt(mouseX, mouseY, l);

         for (int i = first; i < last; i++) {
            PickerGrid.Cell cell = this.filtered.get(i);
            int col = i % l.cols();
            int row = i / l.cols();
            float cellX = l.gridX() + col * l.cell();
            float cellY = l.gridY() - this.scroll + row * l.cell();
            boolean tracked = cell.tracked();
            boolean active = cell.enabled();
            if (blockPicker) {
               vg.rect(cellX + 2.0F, cellY + 2.0F, l.cell() - 4.0F, l.cell() - 4.0F, 5.0F, Colors.withAlpha(-1, 0.035F));
               vg.rectOutline(cellX + 2.0F, cellY + 2.0F, l.cell() - 4.0F, l.cell() - 4.0F, 5.0F, 0.7F, Colors.withAlpha(-1, 0.08F));
               if (blockPicker && cell.icon().method_31574(class_1802.field_8077)) {
                  int image = -1;

                  for (class_2960 texture : cell.fallbackTextures()) {
                     image = NVGImages.fromResource(texture);
                     if (image > 0) {
                        break;
                     }
                  }

                  if (image > 0) {
                     float iconSize = Math.min(l.icon(), l.cell() - 8.0F);
                     float iconX = cellX + (l.cell() - iconSize) / 2.0F;
                     float iconY = cellY + (l.cell() - iconSize) / 2.0F;
                     vg.imagePattern(image, iconX, iconY, iconSize, iconSize, iconX, iconY, iconSize, iconSize, 1.0F);
                  }
               }
            }

            if (i == hovered) {
               vg.rect(cellX + 1.0F, cellY + 1.0F, l.cell() - 2.0F, l.cell() - 2.0F, 6.0F, Colors.withAlpha(theme.accent(), 0.12F));
            }

            if (tracked) {
               int base = cell.color();
               int ring = active ? base : Colors.withAlpha(base, 0.35F);
               vg.rectOutline(cellX + 1.5F, cellY + 1.5F, l.cell() - 3.0F, l.cell() - 3.0F, 6.0F, active ? 1.8F : 1.0F, ring | (active ? -16777216 : 0));
               vg.rect(cellX + 4.0F, cellY + l.cell() - 5.0F, l.cell() - 8.0F, 2.5F, 1.0F, base | 0xFF000000);
               if (active) {
                  vg.circle(cellX + l.cell() - 6.0F, cellY + 6.0F, 2.6F, theme.statusEnabled());
               }
            }
         }

         vg.restore();
         if (this.maxScroll > 0.0F) {
            float trackX = l.px() + l.panelW() - 6.0F;
            float thumbH = Math.max(24.0F, l.gridH() * (l.gridH() / this.contentHeight(l)));
            float thumbY = l.gridY() + (l.gridH() - thumbH) * (this.scroll / this.maxScroll);
            vg.rect(trackX, thumbY, 3.0F, thumbH, 1.5F, Colors.withAlpha(theme.accent(), 0.55F));
         }

         if (this.colorSetting != null && this.colorWidget != null) {
            this.renderColorPopup(vg, theme);
         } else {
            this.popupRect[0] = this.popupRect[1] = this.popupRect[2] = this.popupRect[3] = 0.0F;
         }
      }
   }

   private void renderColorPopup(NVGRenderer vg, Theme theme) {
      this.colorWidget.setExpanded(true);
      float cardW = 220.0F;
      float titleH = 26.0F;
      float uiW = OverlayRenderer.uiWidth();
      float uiH = OverlayRenderer.uiHeight();
      float wx = (uiW - cardW) / 2.0F + 12.0F;
      float bodyH = this.colorWidget.height(vg);
      float cardH = titleH + bodyH + 12.0F;
      float cardX = (uiW - cardW) / 2.0F;
      float cardY = (uiH - cardH) / 2.0F;
      this.popupRect[0] = cardX;
      this.popupRect[1] = cardY;
      this.popupRect[2] = cardX + cardW;
      this.popupRect[3] = cardY + cardH;
      vg.glow(cardX, cardY, cardW, cardH, 12.0F, 14.0F, Colors.withAlpha(theme.accent(), 0.3F));
      vg.rect(cardX, cardY, cardW, cardH, 8.0F, theme.background());
      vg.rectOutline(cardX, cardY, cardW, cardH, 12.0F, 1.4F, Colors.withAlpha(theme.accentBright(), 0.7F));
      vg.textGradient(this.colorTitle, cardX + 12.0F, cardY + 14.0F, 12.5F, theme.accentBright(), theme.accent());
      this.colorWidget.setBounds(wx, cardY + titleH, cardW - 24.0F);
      this.colorWidget.render(vg, OverlayRenderer.uiMouseX(), OverlayRenderer.uiMouseY());
   }

   private int cellAt(float mx, float my, IconPickerScreen.Layout l) {
      if (!(mx < l.gridX()) && !(mx > l.gridX() + l.gridW()) && !(my < l.gridY()) && !(my > l.gridY() + l.gridH())) {
         int col = (int)((mx - l.gridX()) / l.cell());
         int row = (int)((my - (l.gridY() - this.scroll)) / l.cell());
         if (col >= 0 && col < l.cols() && row >= 0) {
            int index = row * l.cols() + col;
            return index >= 0 && index < this.filtered.size() ? index : -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private float ux(double guiX) {
      return OverlayRenderer.guiToUi(guiX);
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      float mx = this.ux(click.comp_4798());
      float my = this.ux(click.comp_4799());
      IconPickerScreen.Layout l = this.layout();
      if (this.colorSetting != null) {
         if (inRect(mx, my, this.popupRect)) {
            this.colorWidget.mouseClicked(mx, my, click.method_74245());
         } else {
            this.closeColor();
            UiSounds.select();
         }

         return true;
      } else if (inRect(mx, my, this.closeRect)) {
         this.method_25419();
         return true;
      } else if (inRect(mx, my, this.searchRect)) {
         this.searchFocused = true;
         UiSounds.select();
         return true;
      } else {
         this.searchFocused = false;
         if (!(mx < l.px()) && !(mx > l.px() + l.panelW()) && !(my < l.py()) && !(my > l.py() + l.panelH())) {
            int index = this.cellAt(mx, my, l);
            if (index >= 0) {
               PickerGrid.Cell cell = this.filtered.get(index);
               if (click.method_74245() == 1) {
                  this.openColor(cell.colorTarget(), cell.label());
               } else if (click.method_74245() == 0) {
                  cell.toggle();
                  UiSounds.toggle(cell.enabled());
               }

               return true;
            } else {
               return true;
            }
         } else {
            this.method_25419();
            return true;
         }
      }
   }

   private void openColor(ColorSetting target, String title) {
      if (target != null) {
         this.colorSetting = target;
         this.colorTitle = title;
         this.colorWidget = new ColorWidget(this.themes, target);
         this.colorWidget.setExpanded(true);
         this.searchFocused = false;
         UiSounds.select();
      }
   }

   private void closeColor() {
      this.colorSetting = null;
      this.colorTitle = null;
      this.colorWidget = null;
   }

   public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
      if (this.colorSetting != null && this.colorWidget != null) {
         this.colorWidget.mouseDragged(this.ux(click.comp_4798()), this.ux(click.comp_4799()));
      }

      return true;
   }

   public boolean method_25406(class_11909 click) {
      if (this.colorWidget != null) {
         this.colorWidget.mouseReleased();
      }

      return true;
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.colorSetting == null) {
         this.scroll = Math.clamp(this.scroll - (float)(verticalAmount * this.layout().cell()), 0.0F, this.maxScroll);
      }

      return true;
   }

   public boolean method_25404(class_11908 input) {
      int key = input.comp_4795();
      if (this.colorSetting != null) {
         if (this.colorWidget != null && this.colorWidget.isListening()) {
            this.colorWidget.keyPressed(key);
            return true;
         } else if (key == 256) {
            this.closeColor();
            return true;
         } else {
            return true;
         }
      } else if (this.searchFocused) {
         switch (key) {
            case 256:
            case 257:
            case 335:
               this.searchFocused = false;
               break;
            case 259:
               if (this.search.length() > 0) {
                  this.search.deleteCharAt(this.search.length() - 1);
                  this.filterDirty = true;
               }
         }

         return true;
      } else if (key == 256) {
         this.method_25419();
         return true;
      } else {
         return super.method_25404(input);
      }
   }

   public boolean method_25400(class_11905 input) {
      if (this.colorSetting != null) {
         return true;
      } else if (!this.searchFocused) {
         return true;
      } else if (this.search.length() >= 48) {
         return true;
      } else {
         char c = (char)input.comp_4793();
         if (c == ' ' || c == '_' || c == ':' || c == '/' || c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
            this.search.append(Character.toLowerCase(c));
            this.filterDirty = true;
         }

         return true;
      }
   }

   public void method_25419() {
      NekoClient.config().save();
      this.field_22787.method_1507(this.parent);
   }

   public boolean method_25421() {
      return false;
   }

   public void debugSetSearch(String query) {
      this.search.setLength(0);
      this.search.append(query);
      this.searchFocused = true;
      this.filterDirty = true;
      this.refreshFilter();
   }

   public void debugOpenColor(int filteredIndex) {
      if (filteredIndex >= 0 && filteredIndex < this.filtered.size()) {
         PickerGrid.Cell cell = this.filtered.get(filteredIndex);
         this.openColor(cell.colorTarget(), cell.label());
      }
   }

   public int debugFilteredCount() {
      return this.filtered.size();
   }

   private static boolean inRect(float mx, float my, float[] r) {
      return mx >= r[0] && mx <= r[2] && my >= r[1] && my <= r[3];
   }

   private record Layout(float px, float py, float panelW, float panelH, float gridX, float gridY, float gridW, float gridH, int cols, float cell, float icon) {
   }
}

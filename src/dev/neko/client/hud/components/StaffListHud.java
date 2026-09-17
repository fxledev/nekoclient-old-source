package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.module.impl.StaffListModule;
import dev.neko.client.render.anim.Animation;
import dev.neko.client.render.anim.Easing;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.staff.StaffEntry;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StaffListHud extends HudComponent {
   private static final float PAD = 8.0F;
   private static final float HEADER_H = 15.0F;
   private static final float GAP_HEADER = 5.0F;
   private static final float ROW_H = 18.0F;
   private static final float EMPTY_H = 16.0F;
   private static final float OVERFLOW_H = 13.0F;
   private static final float MARK_W = 15.0F;
   private static final float PING_W = 11.0F;
   private static final float FONT_TITLE = 9.0F;
   private static final float FONT_NAME = 12.5F;
   private static final float FONT_RANK = 9.0F;
   private static final float FONT_EMPTY = 11.0F;
   private static final float MIN_CONTENT_W = 92.0F;
   private static final float MAX_NAME_W = 128.0F;
   private static final int GREEN = -11870592;
   private static final int YELLOW = -340971;
   private static final int RED = -495247;
   private final StaffListModule module;
   private final ThemeManager themes;
   private final Map<String, StaffListHud.RowAnim> rows = new LinkedHashMap<>();

   public StaffListHud(StaffListModule module, ThemeManager themes) {
      super("staffList", 0.08F, 0.22F, module::isHudVisible);
      this.module = module;
      this.themes = themes;
   }

   private int shownCount(List<StaffEntry> staff) {
      return Math.min(staff.size(), Math.max(1, this.module.maxRows()));
   }

   private List<StaffListHud.RowAnim> layoutRows(List<StaffEntry> staff) {
      int shown = this.shownCount(staff);
      Set<String> visible = new HashSet<>();

      for (int i = 0; i < shown && i < staff.size(); i++) {
         StaffEntry e = staff.get(i);
         visible.add(e.name());
         StaffListHud.RowAnim r = this.rows.get(e.name());
         if (r == null) {
            r = new StaffListHud.RowAnim(e);
            this.rows.put(e.name(), r);
         } else {
            r.entry = e;
         }

         r.anim.setTarget(1.0F);
      }

      List<StaffListHud.RowAnim> out = new ArrayList<>();

      for (int i = 0; i < shown && i < staff.size(); i++) {
         out.add(this.rows.get(staff.get(i).name()));
      }

      Iterator<StaffListHud.RowAnim> it = this.rows.values().iterator();

      while (it.hasNext()) {
         StaffListHud.RowAnim r = it.next();
         if (!visible.contains(r.entry.name())) {
            r.anim.setTarget(0.0F);
            if (r.anim.value() <= 0.01F) {
               it.remove();
            } else {
               out.add(r);
            }
         }
      }

      return out;
   }

   private float rowWidth(NVGRenderer vg, StaffEntry e) {
      float w = 15.0F + Math.min(vg.textWidth(e.name(), 12.5F), 128.0F);
      if (this.module.showRank() && !e.rankLabel().isEmpty()) {
         w += 6.0F + vg.textWidth(e.rankLabel(), 9.0F);
      }

      if (this.module.showPing()) {
         w += 19.0F;
      }

      return w;
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      List<StaffEntry> staff = this.module.staff();
      float content = 92.0F;
      String count = Integer.toString(staff.size());
      content = Math.max(content, vg.textWidth("STAFF", 9.0F) + 10.0F + vg.textWidth(count, 8.5F) + 9.0F);

      for (StaffListHud.RowAnim r : this.layoutRows(staff)) {
         content = Math.max(content, this.rowWidth(vg, r.entry));
      }

      if (staff.isEmpty()) {
         content = Math.max(content, 14.0F + vg.textWidth("No staff online", 11.0F));
      }

      return 16.0F + content;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      List<StaffEntry> staff = this.module.staff();
      float listH = 0.0F;

      for (StaffListHud.RowAnim r : this.layoutRows(staff)) {
         listH += 18.0F * r.anim.value();
      }

      if (staff.isEmpty() && listH < 0.5F) {
         listH = 16.0F;
      }

      float h = 28.0F + listH + 8.0F;
      if (staff.size() > this.shownCount(staff)) {
         h += 13.0F;
      }

      return h;
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      List<StaffEntry> staff = this.module.staff();
      float drift = (float)(0.5 + 0.5 * Math.sin(System.nanoTime() / 4.8E8));
      int gradTop = Colors.lerp(theme.accentBright(), theme.accent(), drift);
      int gradBottom = Colors.lerp(theme.accent(), theme.accentBright(), drift);
      vg.glow(x, y, w, h, 13.0F, 8.0F, Colors.withAlpha(theme.accent(), 0.1F));
      vg.rectGradient(x, y, w, h, 10.0F, theme.background(), theme.backgroundTo(), true);
      vg.rectOutline(x, y, w, h, 10.0F, 1.5F, -1);
      this.drawHeader(vg, theme, x, y, w, staff.size());
      float rowY = y + 8.0F + 15.0F + 5.0F;
      if (staff.isEmpty()) {
         float cy = rowY + 8.0F;
         vg.text("No staff online", x + 8.0F, cy, 11.0F, theme.textMuted());
      } else {
         for (StaffListHud.RowAnim r : this.layoutRows(staff)) {
            float t = Math.clamp(r.anim.value(), 0.0F, 1.0F);
            if (!(t <= 0.01F)) {
               this.drawRow(vg, theme, r.entry, x, rowY, w, t);
               rowY += 18.0F * t;
            }
         }
      }
   }

   private void drawHeader(NVGRenderer vg, Theme theme, float x, float y, float w, int count) {
      float cy = y + 8.0F + 7.5F;
      float drift = (float)(0.5 + 0.5 * Math.sin(System.nanoTime() / 4.8E8));
      int gradTop = Colors.lerp(theme.accentBright(), theme.accent(), drift);
      int gradBottom = Colors.lerp(theme.accent(), theme.accentBright(), drift);
      vg.textGradient("STAFF", x + 8.0F, cy, 9.0F, gradTop, gradBottom);
   }

   private void drawRow(NVGRenderer vg, Theme theme, StaffEntry e, float x, float rowY, float w, float t) {
      float cy = rowY + 9.0F;
      int color = e.hasColor() ? e.color() : theme.accent();
      vg.save();
      vg.alpha(t);
      vg.translate((1.0F - t) * -10.0F, 0.0F);
      this.drawDot(vg, x + 8.0F + 7.5F - 2.0F, cy, 4.2F, color, e.vanished());
      float nameX = x + 8.0F + 15.0F;
      int nameColor = e.vanished() ? theme.textMuted() : theme.textPrimary();
      float adv = vg.textTruncated(e.name(), nameX, cy, 12.5F, nameColor, 128.0F);
      if (this.module.showRank() && !e.rankLabel().isEmpty()) {
         int rankColor = e.hasColor() ? Colors.lighten(color, 0.25F) : theme.accent();
         vg.text(e.rankLabel(), nameX + adv + 6.0F, cy + 0.5F, 9.0F, Colors.withAlpha(rankColor, e.vanished() ? 0.6F : 0.95F));
      }

      if (this.module.showPing()) {
         this.drawPingBars(vg, x + w - 8.0F - 11.0F, cy, e.latency(), theme);
      }

      vg.restore();
   }

   private void drawDot(NVGRenderer vg, float cx, float cy, float r, int color, boolean vanished) {
      if (vanished) {
         vg.circleOutline(cx, cy, r * 0.55F, 1.2F, Colors.withAlpha(color, 0.55F));
      } else {
         vg.circleGlow(cx, cy, r * 0.5F, r * 1.3F, Colors.withAlpha(color, 0.5F));
         vg.circle(cx, cy, r * 0.5F, color);
      }
   }

   private void drawPingBars(NVGRenderer vg, float x, float cy, int latency, Theme theme) {
      int bars = latency < 0 ? 0 : (latency <= 80 ? 4 : (latency <= 150 ? 3 : (latency <= 300 ? 2 : (latency <= 600 ? 1 : 0))));
      int col = bars >= 3 ? -11870592 : (bars == 2 ? -340971 : -495247);

      for (int i = 0; i < 4; i++) {
         float bh = 2.0F + i * 2.0F;
         float bx = x + i * 3.0F;
         float by = cy + 4.0F - bh;
         int c = i < bars ? col : Colors.withAlpha(theme.textDisabled(), 0.45F);
         vg.rect(bx, by, 2.0F, bh, 0.5F, c);
      }
   }

   private static final class RowAnim {
      StaffEntry entry;
      final Animation anim = new Animation(220.0F, 0.0F, Easing.EASE_OUT_CUBIC);

      RowAnim(StaffEntry entry) {
         this.entry = entry;
      }
   }
}

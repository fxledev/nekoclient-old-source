package dev.neko.client.gui.config;

import dev.neko.client.NekoClient;
import dev.neko.client.config.ConfigStore;
import dev.neko.client.render.anim.Animation;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.util.Colors;
import dev.neko.client.util.UiSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;

public class ConfigPanel {
   private static final float CARD_W = 760.0F;
   private static final float HEADER_H = 54.0F;
   private static final float FOOTER_H = 38.0F;
   private static final float ROW_H = 50.0F;
   private static final float ROW_GAP = 5.0F;
   private static final float PAD = 18.0F;
   private static final float BTN_INSET = 16.0F;
   private static final int DANGER = -45730;
   private static final int DANGER_BRIGHT = -37252;
   private final Animation openAnim = new Animation(160.0F, 0.0F);
   private boolean open;
   private float cardX;
   private float cardY;
   private float cardH;
   private float scroll;
   private boolean embedded;
   private final List<ConfigPanel.Hit> hits = new ArrayList<>();
   private int renamingSlot = -1;
   private final StringBuilder renameBuffer = new StringBuilder();
   private ConfigPanel.Confirm pendingConfirm;
   private float confirmOkX;
   private float confirmOkY;
   private float confirmOkW;
   private float confirmOkH;
   private float confirmCancelX;
   private float confirmCancelY;
   private float confirmCancelW;
   private float confirmCancelH;
   private float confirmCardX;
   private float confirmCardY;
   private float confirmCardW;
   private float confirmCardH;

   public boolean isOpen() {
      return this.open;
   }

   public void open() {
      this.open = true;
      this.openAnim.setTarget(1.0F);
   }

   public void close() {
      this.open = false;
      this.openAnim.setTarget(0.0F);
      this.cancelRename();
      this.pendingConfirm = null;
   }

   public boolean isListening() {
      return this.open && this.renamingSlot >= 0;
   }

   public void setEmbeddedBounds(float x, float y, float width, float height) {
      this.embedded = true;
      this.cardX = x;
      this.cardY = y;
      this.cardH = height;
   }

   private ConfigStore store() {
      return NekoClient.configStore();
   }

   public void render(NVGRenderer vg, float mx, float my, float uiWidth, float uiHeight) {
      float t = this.openAnim.value();
      if (!(t <= 0.002F) || this.open) {
         Theme theme = NekoClient.themes().current();
         if (!this.embedded) {
            vg.rect(0.0F, 0.0F, uiWidth, uiHeight, 0.0F, Colors.withAlpha(-16316918, 0.55F * t));
         }

         vg.save();
         vg.alpha(t);
         if (!this.embedded) {
            float scale = 0.97F + 0.03F * t;
            vg.translate(uiWidth / 2.0F, uiHeight / 2.0F);
            vg.scale(scale);
            vg.translate(-uiWidth / 2.0F, -uiHeight / 2.0F);
         }

         ConfigStore store = this.store();
         if (!this.embedded) {
            this.cardH = 490.0F;
            this.cardX = (uiWidth - 760.0F) / 2.0F;
            this.cardY = (uiHeight - this.cardH) / 2.0F;
         }

         vg.rect(this.cardX, this.cardY, 760.0F, this.cardH, 12.0F, Colors.withAlpha(-16777216, 0.28F));
         this.renderHeader(vg, theme, mx, my);
         this.hits.clear();
         float viewTop = this.cardY + 54.0F;
         float viewH = this.cardH - 54.0F;
         float total = 825.0F;
         this.scroll = Math.clamp(this.scroll, 0.0F, Math.max(0.0F, total - viewH));
         vg.save();
         vg.scissor(this.cardX, viewTop, 760.0F, viewH);

         for (int i = 0; i < 15; i++) {
            float rowY = viewTop - this.scroll + i * 55.0F;
            if (rowY + 50.0F >= viewTop && rowY <= viewTop + viewH) {
               this.renderSlot(vg, theme, store.slot(i), this.cardX + 18.0F, rowY, 724.0F, mx, my, store.activeIndex() == i);
            }
         }

         vg.restore();
         if (total > viewH) {
            float tx = this.cardX + 760.0F - 10.0F;
            vg.rect(tx, viewTop + 4.0F, 4.0F, viewH - 8.0F, 2.0F, Colors.withAlpha(-16777216, 0.5F));
            float th = Math.max(28.0F, viewH * (viewH / total));
            float ty = viewTop + 4.0F + (viewH - 8.0F - th) * (this.scroll / (total - viewH));
            vg.rect(tx, ty, 4.0F, th, 2.0F, Colors.withAlpha(theme.accent(), 0.7F));
         }

         this.renderFooter(vg, theme);
         if (this.pendingConfirm != null) {
            this.renderConfirm(vg, theme, mx, my, uiWidth, uiHeight);
         }

         vg.restore();
      }
   }

   private void renderHeader(NVGRenderer vg, Theme theme, float mx, float my) {
   }

   private void renderSlot(NVGRenderer vg, Theme theme, ConfigStore.Slot slot, float x, float y, float w, float mx, float my, boolean active) {
      if (mx >= x && mx <= x + w && my >= y && my <= y + 50.0F) {
         boolean var14 = true;
      } else {
         boolean var10000 = false;
      }

      if (active) {
         vg.glow(x, y, w, 50.0F, 12.0F, 6.0F, Colors.withAlpha(theme.accent(), 0.3F));
         vg.glassPressed(x, y, w, 50.0F, 12.0F, theme.accent());
      } else {
         vg.glass(x, y, w, 50.0F, 12.0F);
      }

      float nameX = x + 16.0F;
      if (this.renamingSlot == slot.index()) {
         this.renderRenameField(vg, theme, nameX, y + 12.0F, 190.0F);
      } else {
         vg.textTruncated(slot.name(), nameX, y + 18.0F, 15.0F, theme.textPrimary(), 200.0F);
         if (active) {
            float nameW = vg.textWidth(slot.name(), 15.0F);
            this.drawTag(vg, theme, nameX + Math.min(nameW, 200.0F) + 8.0F, y + 18.0F, "ACTIVE");
         }
      }

      String status = this.renamingSlot == slot.index()
         ? "Enter to confirm · Esc to cancel"
         : (slot.filled() ? "Saved · " + relativeTime(slot.savedAt()) : "Empty slot");
      vg.text(status, nameX, y + 35.0F, 11.5F, theme.textMuted());
      this.layoutButtons(vg, theme, slot, x + w - 16.0F, y, mx, my);
   }

   private void renderRenameField(NVGRenderer vg, Theme theme, float x, float y, float w) {
      float h = 20.0F;
      vg.rect(x, y, w, h, h / 2.0F, Colors.withAlpha(-16777216, 0.5F));
      vg.rectOutline(x, y, w, h, h / 2.0F, 1.2F, Colors.withAlpha(theme.accentBright(), 0.9F));
      float tx = x + 8.0F;
      float ty = y + h / 2.0F;
      float tw = vg.text(this.renameBuffer.toString(), tx, ty, 12.5F, theme.textPrimary());
      if (System.nanoTime() / 400000000L % 2L == 0L) {
         vg.rect(tx + tw + 1.5F, ty - 6.0F, 1.4F, 12.0F, 0.7F, theme.accentBright());
      }
   }

   private void drawTag(NVGRenderer vg, Theme theme, float x, float y, String label) {
      float tw = vg.textWidth(label, 9.5F);
      vg.rect(x, y - 7.0F, tw + 12.0F, 14.0F, 7.0F, Colors.withAlpha(theme.accent(), 0.22F));
      vg.text(label, x + 6.0F, y, 9.5F, theme.accentBright());
   }

   private void layoutButtons(NVGRenderer vg, Theme theme, ConfigStore.Slot slot, float rowRight, float rowY, float mx, float my) {
      record Spec(ConfigPanel.Action action, String label, boolean primary, boolean danger) {
      }

      List<Spec> specs = new ArrayList<>();
      if (slot.filled()) {
         specs.add(new Spec(ConfigPanel.Action.ACTIVATE, "Load", true, false));
         specs.add(new Spec(ConfigPanel.Action.SAVE, "Save", false, false));
         specs.add(new Spec(ConfigPanel.Action.RENAME, "Rename", false, false));
         specs.add(new Spec(ConfigPanel.Action.DELETE, "Delete", false, true));
      } else {
         specs.add(new Spec(ConfigPanel.Action.SAVE, "Save", true, false));
      }

      float h = 26.0F;
      float padX = 11.0F;
      float gap = 6.0F;
      float font = 12.0F;
      float total = 0.0F;
      float[] widths = new float[specs.size()];

      for (int i = 0; i < specs.size(); i++) {
         widths[i] = vg.textWidth(specs.get(i).label(), font) + padX * 2.0F;
         total += widths[i] + (i > 0 ? gap : 0.0F);
      }

      float bx = rowRight - total;
      float by = rowY + (50.0F - h) / 2.0F;

      for (int i = 0; i < specs.size(); i++) {
         Spec spec = specs.get(i);
         float bw = widths[i];
         boolean hover = mx >= bx && mx <= bx + bw && my >= by && my <= by + h;
         this.drawButton(vg, theme, bx, by, bw, h, spec.label(), font, spec.primary(), hover, spec.danger());
         this.hits.add(new ConfigPanel.Hit(spec.action(), slot.index(), bx, by, bw, h, spec.primary()));
         bx += bw + gap;
      }
   }

   private void drawButton(
      NVGRenderer vg, Theme theme, float x, float y, float w, float h, String label, float font, boolean primary, boolean hover, boolean danger
   ) {
      int accent = danger ? -45730 : theme.accent();
      int accentBright = danger ? -37252 : theme.accentBright();
      if (primary) {
         int top = hover ? Colors.lighten(accentBright, 0.1F) : accentBright;
         vg.rect(x, y, w, h, 5.0F, top);
         vg.rectOutline(x, y, w, h, 5.0F, 1.0F, Colors.withAlpha(accent, 0.65F));
         if (hover) {
            vg.glow(x, y, w, h, h / 2.0F, 5.0F, Colors.withAlpha(accent, 0.35F));
         }

         vg.text(label, x + (w - vg.textWidth(label, font)) / 2.0F, y + h / 2.0F, font, -15593706);
      } else {
         vg.rect(x, y, w, h, h / 2.0F, Colors.withAlpha(-1, hover ? 0.12F : 0.06F));
         vg.rectOutline(x, y, w, h, h / 2.0F, 1.0F, Colors.withAlpha(hover ? accentBright : accent, hover ? 0.7F : 0.28F));
         int rest = danger ? Colors.withAlpha(accent, 0.85F) : theme.textMuted();
         vg.text(label, x + (w - vg.textWidth(label, font)) / 2.0F, y + h / 2.0F, font, hover ? (danger ? accentBright : theme.textPrimary()) : rest);
      }
   }

   private void renderFooter(NVGRenderer vg, Theme theme) {
   }

   private void renderConfirm(NVGRenderer vg, Theme theme, float mx, float my, float uiWidth, float uiHeight) {
      vg.rect(this.cardX, this.cardY, 760.0F, this.cardH, 12.0F, Colors.withAlpha(-16316918, 0.35F));
      this.confirmCardW = 380.0F;
      this.confirmCardH = 148.0F;
      this.confirmCardX = (uiWidth - this.confirmCardW) / 2.0F;
      this.confirmCardY = (uiHeight - this.confirmCardH) / 2.0F;
      boolean danger = this.pendingConfirm.action() == ConfigPanel.Action.DELETE;
      vg.glow(this.confirmCardX, this.confirmCardY, this.confirmCardW, this.confirmCardH, 16.0F, 20.0F, Colors.withAlpha(-16777216, 0.5F));
      vg.rect(this.confirmCardX, this.confirmCardY, this.confirmCardW, this.confirmCardH, 8.0F, theme.background());
      vg.rectOutline(
         this.confirmCardX, this.confirmCardY, this.confirmCardW, this.confirmCardH, 8.0F, 1.0F, Colors.withAlpha(danger ? -45730 : theme.accent(), 0.55F)
      );
      vg.text(this.pendingConfirm.title(), this.confirmCardX + 22.0F, this.confirmCardY + 34.0F, 16.0F, theme.textPrimary());
      vg.text(this.pendingConfirm.body(), this.confirmCardX + 22.0F, this.confirmCardY + 58.0F, 12.0F, theme.textMuted());
      float h = 30.0F;
      float gap = 10.0F;
      float by = this.confirmCardY + this.confirmCardH - h - 20.0F;
      this.confirmOkW = 118.0F;
      this.confirmCancelW = 92.0F;
      this.confirmOkH = h;
      this.confirmCancelH = h;
      this.confirmOkX = this.confirmCardX + this.confirmCardW - 22.0F - this.confirmOkW;
      this.confirmOkY = by;
      this.confirmCancelX = this.confirmOkX - gap - this.confirmCancelW;
      this.confirmCancelY = by;
      boolean okHover = mx >= this.confirmOkX && mx <= this.confirmOkX + this.confirmOkW && my >= by && my <= by + h;
      boolean cancelHover = mx >= this.confirmCancelX && mx <= this.confirmCancelX + this.confirmCancelW && my >= by && my <= by + h;
      this.drawButton(vg, theme, this.confirmCancelX, this.confirmCancelY, this.confirmCancelW, this.confirmCancelH, "Cancel", 12.5F, false, cancelHover, false);
      this.drawButton(vg, theme, this.confirmOkX, this.confirmOkY, this.confirmOkW, this.confirmOkH, this.confirmLabel(), 12.5F, true, okHover, danger);
   }

   private String confirmLabel() {
      return switch (this.pendingConfirm.action()) {
         case IMPORT -> "Import";
         case DELETE -> "Delete";
         default -> "Overwrite";
      };
   }

   public void onScroll(double amount) {
      float viewH = this.cardH - 54.0F;
      float total = 825.0F;
      this.scroll = Math.clamp(this.scroll - (float)amount * 44.0F, 0.0F, Math.max(0.0F, total - viewH));
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (!this.open) {
         return false;
      } else {
         if (this.renamingSlot >= 0) {
            this.commitRename();
         }

         if (this.pendingConfirm != null) {
            if (this.hit(mx, my, this.confirmOkX, this.confirmOkY, this.confirmOkW, this.confirmOkH)) {
               this.runConfirm();
            } else if (this.hit(mx, my, this.confirmCancelX, this.confirmCancelY, this.confirmCancelW, this.confirmCancelH)
               || !this.hit(mx, my, this.confirmCardX, this.confirmCardY, this.confirmCardW, this.confirmCardH)) {
               this.pendingConfirm = null;
               UiSounds.select();
            }

            return true;
         } else {
            for (ConfigPanel.Hit h : this.hits) {
               if (h.contains(mx, my)) {
                  this.dispatch(h.action, h.slot);
                  return true;
               }
            }

            if (mx < this.cardX || mx > this.cardX + 760.0F || my < this.cardY || my > this.cardY + this.cardH) {
               this.close();
            }

            return true;
         }
      }
   }

   public boolean keyPressed(int keyCode) {
      if (!this.open) {
         return false;
      } else if (this.renamingSlot >= 0) {
         switch (keyCode) {
            case 256:
               this.cancelRename();
               break;
            case 257:
            case 335:
               this.commitRename();
               break;
            case 259:
               if (this.renameBuffer.length() > 0) {
                  this.renameBuffer.deleteCharAt(this.renameBuffer.length() - 1);
               }
         }

         return true;
      } else if (this.pendingConfirm != null) {
         switch (keyCode) {
            case 256:
               this.pendingConfirm = null;
               UiSounds.select();
               break;
            case 257:
            case 335:
               this.runConfirm();
         }

         return true;
      } else if (keyCode == 256) {
         this.close();
         return true;
      } else {
         return true;
      }
   }

   public boolean charTyped(int codepoint) {
      if (this.open && this.renamingSlot >= 0) {
         if (this.renameBuffer.length() >= 24) {
            return true;
         } else {
            char c = (char)codepoint;
            if (c >= ' ' && c < 127) {
               this.renameBuffer.append(c);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private void dispatch(ConfigPanel.Action action, int slot) {
      ConfigStore store = this.store();
      switch (action) {
         case ACTIVATE:
            this.doActivate(slot);
            break;
         case SAVE:
            if (store.slot(slot).filled()) {
               this.pendingConfirm = new ConfigPanel.Confirm(
                  ConfigPanel.Action.SAVE,
                  slot,
                  "Overwrite \"" + store.slot(slot).name() + "\"?",
                  "This replaces the config saved in slot " + (slot + 1) + ".",
                  (String)null
               );
               UiSounds.select();
            } else {
               this.doSave(slot);
            }
            break;
         case RENAME:
            this.beginRename(slot);
         case EXPORT:
         case IMPORT:
         default:
            break;
         case DELETE:
            this.pendingConfirm = new ConfigPanel.Confirm(
               ConfigPanel.Action.DELETE,
               slot,
               "Delete \"" + store.slot(slot).name() + "\"?",
               "This permanently removes slot " + (slot + 1) + ".",
               (String)null
            );
            UiSounds.select();
      }
   }

   private void runConfirm() {
      ConfigPanel.Confirm c = this.pendingConfirm;
      this.pendingConfirm = null;
      if (c != null) {
         if (c.action() == ConfigPanel.Action.SAVE) {
            this.doSave(c.slot());
         } else if (c.action() == ConfigPanel.Action.IMPORT) {
            this.doImport(c.slot(), c.payload());
         } else if (c.action() == ConfigPanel.Action.DELETE) {
            this.doDelete(c.slot());
         }
      }
   }

   private void doDelete(int slot) {
      String name = this.store().slot(slot).name();
      if (this.store().delete(slot)) {
         this.toast("Deleted \"" + name + "\"");
         UiSounds.select();
      } else {
         this.toast("Couldn't delete the config");
      }
   }

   private void doSave(int slot) {
      if (this.store().save(slot)) {
         String var10001 = this.store().slot(slot).name();
         this.toast("Saved to \"" + var10001 + "\"");
         UiSounds.toggle(true);
      } else {
         this.toast("Couldn't save the config");
      }
   }

   private void doActivate(int slot) {
      if (this.store().activate(slot)) {
         String var10001 = this.store().slot(slot).name();
         this.toast("Activated \"" + var10001 + "\"");
         UiSounds.toggle(true);
      } else {
         this.toast("That slot is empty");
      }
   }

   private void doExport(int slot) {
      String json = this.store().export(slot);
      if (json == null) {
         this.toast("That slot is empty");
      } else {
         this.setClipboard(json);
         String var10001 = this.store().slot(slot).name();
         this.toast("Copied \"" + var10001 + "\" to clipboard");
         UiSounds.select();
      }
   }

   private void doImport(int slot, String payload) {
      ConfigStore.ImportResult result = this.store().importInto(slot, payload);
      this.toast(result.message());
      UiSounds.toggle(true);
   }

   private void beginRename(int slot) {
      this.renamingSlot = slot;
      this.renameBuffer.setLength(0);
      this.renameBuffer.append(this.store().slot(slot).name());
      UiSounds.select();
   }

   private void commitRename() {
      if (this.renamingSlot >= 0) {
         this.store().rename(this.renamingSlot, this.renameBuffer.toString());
         this.renamingSlot = -1;
      }
   }

   private void cancelRename() {
      this.renamingSlot = -1;
   }

   private boolean hit(float mx, float my, float x, float y, float w, float h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   private void toast(String message) {
      if (NekoClient.notifications() != null) {
         NekoClient.notifications().pushInfo(message);
      }
   }

   private String readClipboard() {
      try {
         return class_310.method_1551().field_1774.method_1460();
      } catch (Exception var2) {
         return null;
      }
   }

   private void setClipboard(String text) {
      try {
         class_310.method_1551().field_1774.method_1455(text);
      } catch (Exception var3) {
      }
   }

   private static String relativeTime(long savedAt) {
      if (savedAt <= 0L) {
         return "just now";
      } else {
         long diff = System.currentTimeMillis() - savedAt;
         if (diff < 60000L) {
            return "just now";
         } else {
            long minutes = diff / 60000L;
            if (minutes < 60L) {
               return minutes + "m ago";
            } else {
               long hours = minutes / 60L;
               return hours < 24L ? hours + "h ago" : hours / 24L + "d ago";
            }
         }
      }
   }

   public void debugBeginRename(int slot) {
      this.open();
      this.beginRename(slot);
   }

   public void debugConfirmOverwrite(int slot) {
      this.open();
      this.pendingConfirm = new ConfigPanel.Confirm(
         ConfigPanel.Action.SAVE,
         slot,
         "Overwrite \"" + this.store().slot(slot).name() + "\"?",
         "This replaces the config saved in slot " + (slot + 1) + ".",
         (String)null
      );
   }

   public void debugConfirmDelete(int slot) {
      this.open();
      this.pendingConfirm = new ConfigPanel.Confirm(
         ConfigPanel.Action.DELETE,
         slot,
         "Delete \"" + this.store().slot(slot).name() + "\"?",
         "This permanently removes slot " + (slot + 1) + ".",
         (String)null
      );
   }

   private static enum Action {
      ACTIVATE,
      SAVE,
      RENAME,
      EXPORT,
      IMPORT,
      DELETE;

      private static ConfigPanel.Action[] $values() {
         return new ConfigPanel.Action[]{ACTIVATE, SAVE, RENAME, EXPORT, IMPORT, DELETE};
      }

      private static ConfigPanel.Action[] $values$() {
         return new ConfigPanel.Action[]{ACTIVATE, SAVE, RENAME, EXPORT, IMPORT, DELETE};
      }
   }

   private record Confirm(ConfigPanel.Action action, int slot, String title, String body, String payload) {
   }

   private static final class Hit {
      final ConfigPanel.Action action;
      final int slot;
      final float x;
      final float y;
      final float w;
      final float h;
      final boolean primary;

      Hit(ConfigPanel.Action action, int slot, float x, float y, float w, float h, boolean primary) {
         this.action = action;
         this.slot = slot;
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
         this.primary = primary;
      }

      boolean contains(float mx, float my) {
         return mx >= this.x && mx <= this.x + this.w && my >= this.y && my <= this.y + this.h;
      }
   }
}

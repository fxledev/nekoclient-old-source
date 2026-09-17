package dev.neko.client.gui.panel;

import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.gui.ClickGuiState;
import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.render.nanovg.NVGIcons;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.ThemeManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CategoryPanel extends Panel {
   private static final float ENTRY_GAP = 5.0F;
   private static final float ENTRY_INSET = 6.0F;
   private final Category category;
   private final List<ModuleEntry> entries = new ArrayList<>();
   private String filter = "";

   public CategoryPanel(Category category, ModuleManager modules, ThemeManager themes, ClickGuiState state) {
      super(themes, state.panel(category.name()));
      this.category = category;

      for (Module module : modules.inCategory(category)) {
         this.entries.add(new ModuleEntry(module, themes, state));
      }
   }

   public void setFilter(String query) {
      this.filter = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
   }

   private List<ModuleEntry> visibleEntries() {
      List<ModuleEntry> out = new ArrayList<>();

      for (ModuleEntry e : this.entries) {
         if (this.filter.isEmpty() || e.getModule().getName().toLowerCase(Locale.ROOT).contains(this.filter)) {
            out.add(e);
         }
      }

      int sort = ClickGuiScreen.guiSort;
      Comparator<ModuleEntry> byMode = sort == 1
         ? Comparator.comparing(ex -> ex.getModule().getName().toLowerCase(Locale.ROOT))
         : (
            sort == 2
               ? Comparator.<ModuleEntry, Boolean>comparing(ex -> !ex.getModule().isEnabled())
                  .thenComparing(ex -> ex.getModule().getName().toLowerCase(Locale.ROOT))
               : null
         );
      List<ModuleEntry> favs = new ArrayList<>();
      List<ModuleEntry> rest = new ArrayList<>();

      for (ModuleEntry ex : out) {
         if (ClickGuiScreen.guiFavs.contains(ClickGuiScreen.favKey(ex.getModule()))) {
            favs.add(ex);
         } else {
            rest.add(ex);
         }
      }

      if (byMode != null) {
         favs.sort(byMode);
         rest.sort(byMode);
      }

      favs.addAll(rest);
      return favs;
   }

   public int visibleCount() {
      if (this.filter.isEmpty()) {
         return this.entries.size();
      } else {
         int n = 0;

         for (ModuleEntry e : this.entries) {
            if (e.getModule().getName().toLowerCase(Locale.ROOT).contains(this.filter)) {
               n++;
            }
         }

         return n;
      }
   }

   public int totalCount() {
      return this.entries.size();
   }

   @Override
   protected String title() {
      return this.category.getDisplayName();
   }

   @Override
   protected boolean showHeader() {
      return false;
   }

   @Override
   protected int icon() {
      return NVGIcons.get(this.category);
   }

   @Override
   protected float contentHeight(NVGRenderer vg) {
      float h = 12.0F;

      for (ModuleEntry entry : this.visibleEntries()) {
         h += entry.height(vg) + 5.0F;
      }

      return h - 5.0F + 26.0F;
   }

   @Override
   protected void renderContent(NVGRenderer vg, float startY, float mx, float my, float viewTop, float viewBottom) {
      float rowY = startY;
      float entryWidth = 626.0F;

      for (ModuleEntry entry : this.visibleEntries()) {
         float h = entry.height(vg);
         entry.setBounds(this.ps.x + 6.0F, rowY, entryWidth);
         if (rowY + h >= viewTop - 20.0F && rowY <= viewBottom + 20.0F) {
            float fade = this.edgeFade(rowY, rowY + h, viewTop, viewBottom);
            entry.render(vg, mx, my, fade);
         }

         rowY += h + 5.0F;
      }
   }

   @Override
   public boolean mouseClicked(float mx, float my, int button) {
      for (ModuleEntry entry : this.visibleEntries()) {
         if (entry.mouseClicked(mx, my, button)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void mouseDragged(float mx, float my) {
      for (ModuleEntry entry : this.visibleEntries()) {
         entry.mouseDragged(mx, my);
      }
   }

   @Override
   public void mouseReleased() {
      for (ModuleEntry entry : this.visibleEntries()) {
         entry.mouseReleased();
      }
   }

   @Override
   public boolean keyPressed(int keyCode) {
      for (ModuleEntry entry : this.visibleEntries()) {
         if (entry.keyPressed(keyCode)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean charTyped(int codepoint) {
      for (ModuleEntry entry : this.visibleEntries()) {
         if (entry.charTyped(codepoint)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isListening() {
      for (ModuleEntry entry : this.visibleEntries()) {
         if (entry.isListening()) {
            return true;
         }
      }

      return false;
   }
}

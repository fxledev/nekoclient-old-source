package dev.neko.client.gui;

import com.google.gson.JsonObject;
import dev.neko.client.module.Category;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ClickGuiState {
   private final Map<String, ClickGuiState.PanelState> panels = new LinkedHashMap<>();
   private final Set<String> expandedModules = new HashSet<>();
   private boolean laidOut;
   private boolean customized;
   private float lastLayoutWidth = -1.0F;
   private float lastLayoutHeight = -1.0F;
   private static final int LAYOUT_VERSION = 3;
   private static final float PANEL_W = 210.0F;
   private static final float SEARCH_W = 280.0F;

   public void markCustomized() {
      this.customized = true;
   }

   public ClickGuiState() {
      float x = 16.0F;

      for (Category category : Category.values()) {
         this.panels.put(category.name(), new ClickGuiState.PanelState(x, 16.0F));
         x += 222.0F;
      }
   }

   public void ensureDefaultLayout(float uiWidth, float uiHeight) {
      if (!this.customized && (!this.laidOut || uiWidth != this.lastLayoutWidth || uiHeight != this.lastLayoutHeight)) {
         this.laidOut = true;
         this.lastLayoutWidth = uiWidth;
         this.lastLayoutHeight = uiHeight;
         String[] order = new String[]{Category.COMBAT.name(), Category.MISC.name(), Category.RENDER.name(), Category.CLIENT.name()};
         float needed = 1572.0F;
         if (uiWidth >= needed) {
            float gap = (uiWidth - 1260.0F - 280.0F) / 8.0F;
            float x = gap;

            for (int i = 0; i < order.length; i++) {
               ClickGuiState.PanelState ps = this.panel(order[i]);
               ps.x = x;
               ps.y = 16.0F;
               ps.collapsed = false;
               x += 210.0F + gap;
               if (i == 2) {
                  x += 280.0F + gap;
               }
            }
         } else {
            float spacing = 222.0F;
            int perRow = Math.max(1, (int)((uiWidth - 24.0F) / spacing));

            for (int ix = 0; ix < order.length; ix++) {
               ClickGuiState.PanelState ps = this.panel(order[ix]);
               ps.x = 12.0F + ix % perRow * spacing;
               ps.y = 58.0F + ix / perRow * uiHeight * 0.44F;
               ps.collapsed = ix / perRow > 0;
            }
         }
      }
   }

   public ClickGuiState.PanelState panel(String key) {
      return this.panels.computeIfAbsent(key, k -> new ClickGuiState.PanelState(16.0F, 16.0F));
   }

   public boolean isExpanded(String moduleKey) {
      return this.expandedModules.contains(moduleKey);
   }

   public void setExpanded(String moduleKey, boolean expanded) {
      if (expanded) {
         this.expandedModules.add(moduleKey);
      } else {
         this.expandedModules.remove(moduleKey);
      }
   }

   public JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.addProperty("v", 3);
      json.addProperty("custom", this.customized);

      for (Entry<String, ClickGuiState.PanelState> entry : this.panels.entrySet()) {
         JsonObject p = new JsonObject();
         p.addProperty("x", entry.getValue().x);
         p.addProperty("y", entry.getValue().y);
         p.addProperty("collapsed", entry.getValue().collapsed);
         json.add(entry.getKey(), p);
      }

      return json;
   }

   public void fromJson(JsonObject json) {
      if (json.has("v") && json.get("v").getAsInt() >= 3 && json.has("custom") && json.get("custom").getAsBoolean()) {
         this.laidOut = true;
         this.customized = true;

         for (Entry<String, ClickGuiState.PanelState> entry : this.panels.entrySet()) {
            JsonObject p = json.getAsJsonObject(entry.getKey());
            if (p != null) {
               if (p.has("x")) {
                  entry.getValue().x = p.get("x").getAsFloat();
               }

               if (p.has("y")) {
                  entry.getValue().y = p.get("y").getAsFloat();
               }

               if (p.has("collapsed")) {
                  entry.getValue().collapsed = p.get("collapsed").getAsBoolean();
               }
            }
         }
      }
   }

   public static class PanelState {
      public float x;
      public float y;
      public boolean collapsed;

      PanelState(float x, float y) {
         this.x = x;
         this.y = y;
      }
   }
}

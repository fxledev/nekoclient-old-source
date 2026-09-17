package dev.neko.client.theme;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
   private final List<Theme> themes = new ArrayList<>();
   private Theme current;

   public ThemeManager() {
      this.themes.add(new Theme("Neko", -33098, false));
      this.themes.add(new Theme("Pink", -757066, false));
      this.themes.add(new Theme("Rose", -45715, false));
      this.themes.add(new Theme("Crimson", -1689274, false));
      this.themes.add(new Theme("Coral", -38053, false));
      this.themes.add(new Theme("Orange", -29630, false));
      this.themes.add(new Theme("Amber", -20448, false));
      this.themes.add(new Theme("Gold", -11930, false));
      this.themes.add(new Theme("Yellow", -729011, false));
      this.themes.add(new Theme("Lime", -4659666, false));
      this.themes.add(new Theme("Green", -11870592, false));
      this.themes.add(new Theme("Emerald", -15681151, false));
      this.themes.add(new Theme("Mint", -13315175, false));
      this.themes.add(new Theme("Teal", -13773633, false));
      this.themes.add(new Theme("Cyan", -14494738, false));
      this.themes.add(new Theme("Sky", -13058568, false));
      this.themes.add(new Theme("Blue", -11698946, false));
      this.themes.add(new Theme("Indigo", -10262799, false));
      this.themes.add(new Theme("Violet", -7643914, false));
      this.themes.add(new Theme("Purple", -5745161, false));
      this.themes.add(new Theme("Magenta", -1541639, false));
      this.themes.add(new Theme("Orchid", -2461482, false));
      this.themes.add(new Theme("Cream", -661816, false));
      this.themes.add(new Theme("Mono", -1512723, false));
      this.themes.add(new Theme("Ocean", -14716949, false));
      this.themes.add(new Theme("Blood", -4645860, false));
      this.themes.add(new RainbowTheme());
      this.current = this.themes.getFirst();
   }

   public Theme current() {
      return this.current;
   }

   public List<Theme> getThemes() {
      return this.themes;
   }

   public void select(Theme theme) {
      if (this.themes.contains(theme)) {
         this.current = theme;
      }
   }

   public Theme addCustom(int accent) {
      int n = 1;

      for (Theme t : this.themes) {
         if (t.isCustom()) {
            n++;
         }
      }

      Theme theme = new Theme("Custom " + n, accent, true);
      this.themes.add(theme);
      return theme;
   }

   public void removeCustom(Theme theme) {
      if (theme.isCustom() && this.themes.remove(theme) && this.current == theme) {
         this.current = this.themes.getFirst();
      }
   }

   public JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.addProperty("current", this.current.getName());
      JsonArray customs = new JsonArray();

      for (Theme t : this.themes) {
         if (t.isCustom()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", t.getName());
            entry.addProperty("accent", t.accent());
            customs.add(entry);
         }
      }

      json.add("custom", customs);
      return json;
   }

   public void fromJson(JsonObject json) {
      if (json != null) {
         this.themes.removeIf(Theme::isCustom);
         if (json.has("custom")) {
            for (JsonElement el : json.getAsJsonArray("custom")) {
               JsonObject entry = el.getAsJsonObject();
               this.themes.add(new Theme(entry.get("name").getAsString(), entry.get("accent").getAsInt(), true));
            }
         }

         if (json.has("current")) {
            String name = json.get("current").getAsString();

            for (Theme t : this.themes) {
               if (t.getName().equals(name)) {
                  this.current = t;
                  break;
               }
            }
         }
      }
   }
}

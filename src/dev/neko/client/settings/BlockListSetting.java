package dev.neko.client.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class BlockListSetting extends Setting<List> {
   public static final int DEFAULT_COLOR = -16711736;
   private final Set<class_2960> ids = new HashSet<>();

   public BlockListSetting(String name, String description) {
      super(name, description, new ArrayList());
   }

   public List<BlockListSetting.Target> targets() {
      return this.value;
   }

   public int size() {
      return this.value.size();
   }

   public long enabledCount() {
      return this.value.stream().filter(t -> t.enabled.get()).count();
   }

   public boolean contains(class_2960 id) {
      return this.ids.contains(id);
   }

   public BlockListSetting.Target find(class_2248 block) {
      if (block == null) {
         return null;
      } else {
         class_2960 id = class_7923.field_41175.method_10221(block);
         if (id != null && this.ids.contains(id)) {
            for (BlockListSetting.Target t : this.value) {
               if (t.id().equals(id)) {
                  return t;
               }
            }

            return null;
         } else {
            return null;
         }
      }
   }

   public boolean isActive(class_2248 block) {
      BlockListSetting.Target t = this.find(block);
      return t != null && t.enabled.get();
   }

   public BlockListSetting.Target add(class_2248 block, boolean enabled, int color) {
      if (block != null && block != class_2246.field_10124) {
         class_2960 id = class_7923.field_41175.method_10221(block);
         if (id != null && !this.ids.contains(id)) {
            BlockListSetting.Target target = new BlockListSetting.Target(id, block, enabled, color);
            this.value.add(target);
            this.ids.add(id);
            return target;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void remove(BlockListSetting.Target target) {
      if (this.value.remove(target)) {
         this.ids.remove(target.id());
      }
   }

   public void clear() {
      this.value.clear();
      this.ids.clear();
   }

   public List<class_2248> searchRegistry(String rawQuery, int limit) {
      String q = rawQuery == null ? "" : rawQuery.trim().toLowerCase(Locale.ROOT);
      List<class_2248> out = new ArrayList<>();
      if (!q.isEmpty() && limit > 0) {
         for (class_2248 block : class_7923.field_41175) {
            if (block != class_2246.field_10124 && block != class_2246.field_10543 && block != class_2246.field_10243) {
               class_2960 id = class_7923.field_41175.method_10221(block);
               if (id != null && !this.ids.contains(id)) {
                  String path = id.method_12832().toLowerCase(Locale.ROOT);
                  String ns = id.method_12836().toLowerCase(Locale.ROOT);
                  String name = displayName(block).toLowerCase(Locale.ROOT);
                  if (path.contains(q) || ns.contains(q) || name.contains(q)) {
                     out.add(block);
                     if (out.size() >= limit) {
                        break;
                     }
                  }
               }
            }
         }

         return out;
      } else {
         return out;
      }
   }

   public static String displayName(class_2248 block) {
      try {
         return block.method_9518().getString();
      } catch (Throwable var3) {
         class_2960 id = class_7923.field_41175.method_10221(block);
         return id != null ? id.method_12832() : "block";
      }
   }

   public void seedDefaults() {
      this.clear();
      this.add(class_2246.field_10442, true, -16711736);
      this.add(class_2246.field_29029, true, -16711736);
      this.add(class_2246.field_10013, false, -16711868);
      this.add(class_2246.field_29220, false, -16711868);
      this.add(class_2246.field_22109, true, -39356);
      this.add(class_2246.field_23077, false, -10496);
      this.add(class_2246.field_10571, false, -10496);
      this.add(class_2246.field_29026, false, -10496);
      this.add(class_2246.field_10212, false, -3618616);
      this.add(class_2246.field_29027, false, -3618616);
      this.add(class_2246.field_10418, false, -12303292);
      this.add(class_2246.field_29219, false, -12303292);
      this.add(class_2246.field_27120, false, -4689101);
      this.add(class_2246.field_29221, false, -4689101);
      this.add(class_2246.field_10090, false, -12490271);
      this.add(class_2246.field_29028, false, -12490271);
      this.add(class_2246.field_10080, false, -65536);
      this.add(class_2246.field_29030, false, -65536);
      this.add(class_2246.field_10260, false, -7846657);
      this.add(class_2246.field_10398, false, -12255250);
      this.add(class_2246.field_10034, false, -22016);
   }

   @Override
   public JsonElement toJson() {
      JsonArray arr = new JsonArray();

      for (BlockListSetting.Target t : this.value) {
         JsonObject o = new JsonObject();
         o.addProperty("id", t.id().toString());
         o.addProperty("enabled", t.enabled.get());
         o.addProperty("color", t.color.get());
         arr.add(o);
      }

      return arr;
   }

   @Override
   public void fromJson(JsonElement element) {
      if (element != null && element.isJsonArray()) {
         this.clear();
         Iterator var2 = element.getAsJsonArray().iterator();

         while (true) {
            JsonObject o;
            class_2960 id;
            while (true) {
               if (!var2.hasNext()) {
                  return;
               }

               JsonElement el = (JsonElement)var2.next();
               if (el.isJsonObject()) {
                  o = el.getAsJsonObject();
                  if (o.has("id")) {
                     try {
                        id = class_2960.method_60654(o.get("id").getAsString());
                        break;
                     } catch (Exception var8) {
                     }
                  }
               }
            }

            class_2248 block = (class_2248)class_7923.field_41175.method_63535(id);
            if (block != null && block != class_2246.field_10124) {
               boolean en = !o.has("enabled") || o.get("enabled").getAsBoolean();
               int color = o.has("color") ? o.get("color").getAsInt() : -16711736;
               this.add(block, en, color);
            }
         }
      }
   }

   public static final class Target {
      private final class_2960 id;
      private final class_2248 block;
      public final BooleanSetting enabled;
      public final ColorSetting color;

      Target(class_2960 id, class_2248 block, boolean enabled, int color) {
         this.id = id;
         this.block = block;
         this.enabled = new BooleanSetting("Enabled", "Highlight this block", enabled);
         this.color = new ColorSetting(BlockListSetting.displayName(block), "Highlight color", color);
      }

      public class_2960 id() {
         return this.id;
      }

      public class_2248 block() {
         return this.block;
      }

      public String label() {
         return this.color.getName();
      }
   }
}

package dev.neko.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.neko.agent.NekoPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigStore {
   public static final int SLOT_COUNT = 15;
   public static final int CONFIG_VERSION = 3;
   public static final String FORMAT = "neko-profile";
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   public static volatile boolean applying;
   private final ConfigManager config;
   private final Path dir;
   private final ConfigStore.Slot[] slots = new ConfigStore.Slot[15];
   private int active = -1;

   public ConfigStore(ConfigManager config) {
      this.config = config;
      this.dir = NekoPaths.configDir().resolve("nekoclient-configs");

      for (int i = 0; i < 15; i++) {
         this.slots[i] = new ConfigStore.Slot(i);
      }
   }

   public static String defaultName(int index) {
      return "Config " + (index + 1);
   }

   public ConfigStore.Slot slot(int index) {
      return this.slots[index];
   }

   public ConfigStore.Slot[] slots() {
      return this.slots;
   }

   public int activeIndex() {
      return this.active;
   }

   public Path directory() {
      return this.dir;
   }

   public void loadAll() {
      for (int i = 0; i < 15; i++) {
         this.refreshSlot(i);
      }

      JsonObject index = this.readJson(this.dir.resolve("index.json"));
      if (index != null && index.has("active")) {
         int a = index.get("active").getAsInt();
         if (a >= 0 && a < 15 && this.slots[a].filled) {
            this.active = a;
         }
      }
   }

   private void refreshSlot(int i) {
      ConfigStore.Slot slot = this.slots[i];
      JsonObject file = this.readJson(this.slotPath(i));
      JsonObject state = this.extractState(file);
      if (state == null) {
         slot.filled = false;
         slot.savedAt = 0L;
         slot.name = defaultName(i);
      } else {
         slot.filled = true;
         slot.name = file.has("name") && !file.get("name").getAsString().isBlank() ? file.get("name").getAsString() : defaultName(i);
         slot.savedAt = file.has("savedAt") ? file.get("savedAt").getAsLong() : 0L;
      }
   }

   public boolean save(int i) {
      ConfigStore.Slot slot = this.slots[i];
      JsonObject file = new JsonObject();
      file.addProperty("format", "neko-profile");
      file.addProperty("version", 3);
      file.addProperty("name", slot.name);
      file.addProperty("savedAt", System.currentTimeMillis());
      JsonObject identity = new JsonObject();
      identity.addProperty("name", "NekoClient");
      identity.addProperty("schema", "neko.profile.v3");
      file.add("neko", identity);
      file.add("payload", this.config.captureState());
      if (!this.write(this.slotPath(i), file)) {
         return false;
      } else {
         this.refreshSlot(i);
         return true;
      }
   }

   public boolean activate(int i) {
      JsonObject state = this.extractState(this.readJson(this.slotPath(i)));
      if (state == null) {
         return false;
      } else {
         applying = true;

         try {
            this.config.applyState(state);
         } finally {
            applying = false;
         }

         this.active = i;
         this.writeIndex();
         return true;
      }
   }

   public boolean delete(int i) {
      try {
         Files.deleteIfExists(this.slotPath(i));
      } catch (IOException var3) {
         return false;
      }

      if (this.active == i) {
         this.active = -1;
         this.writeIndex();
      }

      this.refreshSlot(i);
      return true;
   }

   public boolean rename(int i, String name) {
      ConfigStore.Slot slot = this.slots[i];
      String clean = this.sanitizeName(name);
      if (clean.isEmpty()) {
         clean = defaultName(i);
      }

      slot.name = clean;
      if (!slot.filled) {
         return true;
      } else {
         JsonObject file = this.readJson(this.slotPath(i));
         if (file == null) {
            return false;
         } else {
            file.addProperty("name", clean);
            return this.write(this.slotPath(i), file);
         }
      }
   }

   public String export(int i) {
      JsonObject file = this.readJson(this.slotPath(i));
      if (this.extractState(file) == null) {
         return null;
      } else {
         String json = GSON.toJson(file);
         String base = this.sanitizeFileName(this.slots[i].name);
         this.write(this.dir.resolve(base + ".json"), file);
         return json;
      }
   }

   public ConfigStore.ImportResult importInto(int i, String raw) {
      if (raw != null && !raw.isBlank()) {
         JsonObject parsed;
         try {
            parsed = JsonParser.parseString(raw).getAsJsonObject();
         } catch (Exception var9) {
            return new ConfigStore.ImportResult(false, "Not valid config JSON");
         }

         if (parsed.has("version") && parsed.get("version").isJsonPrimitive() && parsed.get("version").getAsInt() > 3) {
            return new ConfigStore.ImportResult(false, "Config is from a newer client");
         } else {
            JsonObject state = this.extractState(parsed);
            if (state == null) {
               return new ConfigStore.ImportResult(false, "No config data found");
            } else {
               ConfigStore.Slot slot = this.slots[i];
               String name = parsed.has("name") && !parsed.get("name").getAsString().isBlank()
                  ? this.sanitizeName(parsed.get("name").getAsString())
                  : slot.name;
               JsonObject file = new JsonObject();
               file.addProperty("format", "neko-profile");
               file.addProperty("version", 3);
               file.addProperty("name", name);
               file.addProperty("savedAt", parsed.has("savedAt") ? parsed.get("savedAt").getAsLong() : System.currentTimeMillis());
               JsonObject identity = new JsonObject();
               identity.addProperty("name", "NekoClient");
               identity.addProperty("schema", "neko.profile.v3");
               file.add("neko", identity);
               file.add("payload", state);
               if (!this.write(this.slotPath(i), file)) {
                  return new ConfigStore.ImportResult(false, "Couldn't write the slot file");
               } else {
                  this.refreshSlot(i);
                  return new ConfigStore.ImportResult(true, "Imported \"" + slot.name + "\"");
               }
            }
         }
      } else {
         return new ConfigStore.ImportResult(false, "Clipboard is empty");
      }
   }

   private JsonObject extractState(JsonObject file) {
      if (file == null) {
         return null;
      } else if (file.has("payload") && file.get("payload").isJsonObject()) {
         return file.getAsJsonObject("payload");
      } else if (file.has("state") && file.get("state").isJsonObject()) {
         return file.getAsJsonObject("state");
      } else {
         return file.has("modules") && file.get("modules").isJsonObject() ? file : null;
      }
   }

   private Path slotPath(int i) {
      return this.dir.resolve("slot" + (i + 1) + ".json");
   }

   private void writeIndex() {
      JsonObject index = new JsonObject();
      index.addProperty("active", this.active);
      this.write(this.dir.resolve("index.json"), index);
   }

   private JsonObject readJson(Path path) {
      if (!Files.exists(path)) {
         return null;
      } else {
         try {
            return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
         } catch (Exception var3) {
            return null;
         }
      }
   }

   private boolean write(Path path, JsonObject json) {
      try {
         Files.createDirectories(this.dir);
         Files.writeString(path, GSON.toJson(json));
         return true;
      } catch (IOException var4) {
         return false;
      }
   }

   private String sanitizeName(String name) {
      if (name == null) {
         return "";
      } else {
         String clean = name.replaceAll("[\\r\\n\\t]", " ").trim();
         return clean.length() > 24 ? clean.substring(0, 24) : clean;
      }
   }

   private String sanitizeFileName(String name) {
      String clean = name.replaceAll("[^a-zA-Z0-9-_ ]", "").trim().replace(' ', '_');
      return clean.isEmpty() ? "config" : clean;
   }

   public record ImportResult(boolean ok, String message) {
   }

   public static final class Slot {
      private final int index;
      private String name;
      private boolean filled;
      private long savedAt;

      Slot(int index) {
         this.index = index;
         this.name = ConfigStore.defaultName(index);
      }

      public int index() {
         return this.index;
      }

      public String name() {
         return this.name;
      }

      public boolean filled() {
         return this.filled;
      }

      public long savedAt() {
         return this.savedAt;
      }
   }
}

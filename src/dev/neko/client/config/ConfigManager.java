package dev.neko.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.neko.agent.NekoPaths;
import dev.neko.client.module.Module;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.settings.Setting;
import dev.neko.client.theme.ThemeManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigManager {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Path file = NekoPaths.configDir().resolve("nekoclient.json");
   private final ModuleManager modules;
   private final ThemeManager themes;
   private final Map<String, ConfigManager.Section> sections = new LinkedHashMap<>();

   public ConfigManager(ModuleManager modules, ThemeManager themes) {
      this.modules = modules;
      this.themes = themes;
   }

   public void addSection(String key, Supplier<JsonObject> save, Consumer<JsonObject> load) {
      this.sections.put(key, new ConfigManager.Section(save, load));
   }

   public JsonObject captureState() {
      JsonObject root = new JsonObject();
      root.add("appearance", this.themes.toJson());
      JsonObject moduleJson = new JsonObject();

      for (Module module : this.modules.all()) {
         JsonObject m = new JsonObject();
         m.addProperty("enabled", module.isEnabled());
         m.add("keybind", module.getKeybind().toJson());
         JsonObject settings = new JsonObject();

         for (Setting<?> setting : module.getSettings()) {
            settings.add(setting.getName(), setting.toJson());
         }

         m.add("settings", settings);
         moduleJson.add(module.getName() + "/" + module.getCategory().name(), m);
      }

      root.add("features", moduleJson);

      for (Entry<String, ConfigManager.Section> entry : this.sections.entrySet()) {
         root.add(entry.getKey(), (JsonElement)entry.getValue().save().get());
      }

      return root;
   }

   public void applyState(JsonObject root) {
      if (root != null) {
         JsonObject appearance = root.has("appearance") ? root.getAsJsonObject("appearance") : root.getAsJsonObject("theme");
         if (appearance != null) {
            this.themes.fromJson(appearance);
         }

         JsonObject moduleJson = root.has("features") ? root.getAsJsonObject("features") : root.getAsJsonObject("modules");
         if (moduleJson != null) {
            for (Module module : this.modules.all()) {
               String var10001 = module.getName();
               JsonObject m = moduleJson.has(var10001 + "/" + module.getCategory().name())
                  ? moduleJson.getAsJsonObject(var10001 + "/" + module.getCategory().name())
                  : moduleJson.getAsJsonObject(var10001 + "@" + module.getCategory().name());
               if (m != null) {
                  if (m.has("enabled") && m.get("enabled").getAsBoolean() != module.isEnabled()) {
                     module.setEnabled(m.get("enabled").getAsBoolean());
                  }

                  if (m.has("keybind")) {
                     module.getKeybind().fromJson(m.get("keybind"));
                  }

                  JsonObject settings = m.getAsJsonObject("settings");
                  if (settings != null) {
                     for (Setting<?> setting : module.getSettings()) {
                        if (settings.has(setting.getName())) {
                           setting.fromJson(settings.get(setting.getName()));
                        }
                     }
                  }
               }
            }
         }

         for (Entry<String, ConfigManager.Section> entry : this.sections.entrySet()) {
            if (root.has(entry.getKey()) && root.get(entry.getKey()).isJsonObject()) {
               entry.getValue().load().accept(root.getAsJsonObject(entry.getKey()));
            }
         }
      }
   }

   public synchronized void save() {
      try {
         Files.createDirectories(this.file.getParent());
         Files.writeString(this.file, GSON.toJson(this.captureState()));
      } catch (IOException var2) {
      }
   }

   public synchronized void load() {
      if (Files.exists(this.file)) {
         JsonObject root;
         try {
            root = JsonParser.parseString(Files.readString(this.file)).getAsJsonObject();
         } catch (Exception var3) {
            return;
         }

         this.applyState(root);
      }
   }

   public record Section(Supplier<JsonObject> save, Consumer<JsonObject> load) {
   }
}
